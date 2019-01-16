package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional._longProcedure;
import net.jadoth.math.XMath;
import net.jadoth.memory.RawValueHandler;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import net.jadoth.persistence.types.PersistenceInstanceHandler;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceLoader;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.persistence.types.PersistenceObjectRetriever;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceSourceSupplier;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerLookup;
import net.jadoth.reference._intReference;

public interface BinaryLoader extends PersistenceLoader<Binary>, PersistenceLoadHandler, _longProcedure
{
	public interface Creator extends PersistenceLoader.Creator<Binary>
	{
		@Override
		public BinaryLoader createLoader(
			final PersistenceTypeHandlerLookup<Binary> typeLookup,
			final PersistenceObjectRegistry            registry  ,
			PersistenceSourceSupplier<Binary>          source
		);
	}

	public static BinaryLoader.Implementation New(
		final RawValueHandler                      rawValueHandler,
		final PersistenceTypeHandlerLookup<Binary> typeLookup     ,
		final PersistenceObjectRegistry            registry       ,
		final PersistenceSourceSupplier<Binary>    source         ,
		final LoadItemsChain                       loadItems
	)
	{
		return new BinaryLoader.Implementation(
			notNull(rawValueHandler),
			notNull(typeLookup),
			notNull(registry),
			notNull(source),
			notNull(loadItems)
		);
	}

	public final class Implementation implements BinaryLoader
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int DEFAULT_HASH_SLOTS_LENGTH = 1024;


		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// may be a relay lookup that provides special handlers providing logic
		private final RawValueHandler                                rawValueHandler          ;
		private final PersistenceTypeHandlerLookup<Binary>           typeHandlerLookup        ;
		private final PersistenceObjectRegistry                      registry                 ;
		private final BulkList<XGettingCollection<? extends Binary>> anchor = new BulkList<>();

		private final PersistenceInstanceHandler skipObjectRegisterer = (oid, instance) ->
			this.putBuildItem(
				this.createSkipBuildItem(oid, instance)
			)
		;

		private final PersistenceSourceSupplier<Binary> sourceSupplier;
		private final LoadItemsChain                    loadItems     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final RawValueHandler                      rawValueHandler,
			final PersistenceTypeHandlerLookup<Binary> typeLookup     ,
			final PersistenceObjectRegistry            registry       ,
			final PersistenceSourceSupplier<Binary>    source         ,
			final LoadItemsChain                       loadItems
		)
		{
			super();
			this.rawValueHandler   = rawValueHandler;
			this.typeHandlerLookup = typeLookup     ;
			this.registry          = registry       ;
			this.sourceSupplier    = source         ;
			this.loadItems         = loadItems      ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		final void createInstanceBuildItems(final Binary bytes)
		{
			final long[] startOffsets = bytes.startOffsets();
			final long[] boundOffsets = bytes.boundOffsets();

			// iterate all chunks to collect instance data (and effectively existing/created instances)
			for(int i = 0; i < startOffsets.length; i++)
			{
				this.createInstanceBuildItems(startOffsets[i], boundOffsets[i]);
			}
		}

		// CHECKSTYLE.OFF: FinalParameters: this method is just an outsourced scroll-helper
		protected void handleAllReferences(Item item)
		{
			// iterate over all new build items (relocated loaded and created unrequested items alike)
			while((item = item.next) != null)
			{
				if(!item.hasData())
				{
					continue;
				}
				this.handleReferences(item); // register references of each new item
			}
		}
		// CHECKSTYLE.ON: FinalParameters

		private void createInstanceBuildItems(final long startAddress, final long boundAddress)
		{
			for(long address = startAddress; address < boundAddress; address += XMemory.get_long(address))
			{
				this.createInstanceBuildItem(address);
			}
		}

		private Object getEffectiveInstance(final Item entry)
		{
			/* tricky concurrent part:
			 * if a proper context instance already was registered, simply use that.
			 *
			 * Otherwise:
			 * try to register thread-locally created instance as the context instance for the OID at this point and in
			 * the process use a meanwhile created and registered context instance instead and ignore the own local one
			 *
			 * This way, every thread will use the same instance for a given OID even if multiple threads
			 * are loading and creating different instances for the same OID concurrently.
			 * The only requirement is that the registry's register method is thread-safe.
			 *
			 * Note that the context field is still a thread-local field, just one that points to an instance
			 * known by the context. The only true multithread-ly used part is the context itself.
			 *
			 * Note on instance type: both ways that set instances here (local and context),
			 * namely registry and type handler, are trusted to provide (instantiate or know) instances of the
			 * correct type.
			 */
			// (07.12.2018 TM)XXX: difference between contextInstance and localInstance? Relevant?
			return entry.contextInstance != null
				? entry.contextInstance
				: (entry.contextInstance = this.registry.optionalRegisterObject(
					entry.oid,
					entry.localInstance
				))
			;
		}

		protected PersistenceTypeHandler<Binary, ?> lookupTypeHandler(final long oid, final long tid)
		{
			final PersistenceTypeHandler<Binary, ?> handler;
			
			if((handler = this.typeHandlerLookup.lookupTypeHandler(oid, tid)) == null)
			{
				throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(tid);
			}
			
			return handler;
		}

		// note: this method is guaranteed to be called only once per instance data
		protected void createInstanceBuildItem(final long address) throws ClassCastException
		{
			// get the build item for the instance with the type handler and maybe an existing global instance
			final Item buildItem = this.createBuildItem(
				BinaryPersistence.getEntityObjectId(address),
				BinaryPersistence.getEntityTypeId(address)
			);
			
			// content of item begins after instance header
			buildItem.entityContentAddress = BinaryPersistence.entityContentAddress(address);

//			XDebug.debugln("Item @ " + address + ":\n" +
//				"LEN=" + BinaryPersistence.getEntityLength(address) + "\n" +
//				"TID=" + BinaryPersistence.getEntityTypeId(address) + "\n" +
//				"OID=" + BinaryPersistence.getEntityObjectId(address) + "\n" +
//				"contentAddress=" + buildItem.entityContentAddress
//			);

			/* (08.07.2015 TM)TODO: unnecessary local instance
			 * there was a case where an unnecessary local instance was created because the context instance could not
			 * have been found before. The instance was an enum constants array, so it should have been findable already.
			 * Check if this is always the case (would be a tremendous amount of unnecessary instances) or a buggy
			 * special case (constant registration or whatever)
			 */
			if(buildItem.contextInstance == null)
			{
				buildItem.localInstance = buildItem.handler.create(buildItem);
			}

			// register build item
			this.putBuildItem(buildItem);
		}

		protected void handleReferences(final Item entry)
		{
			/*
			 * Custom handler implementation can decide whether references of a particular field shall be loaded.
			 * Note that the handler has been provided by the context instance, so it can already be a
			 * context-specific handler implementation.
			 */

//			XDebug.debugln("refs of " + entry.handler.typeName() + " " + entry.handler.typeId() + " " + entry.oid);

			entry.handler.iteratePersistedReferences(entry, this);
		}

		private void handleReference(final long refOid)
		{
			if(this.isUnrequiredReference(refOid))
			{
				return;
			}
			// oid is required to have data loaded even if instance is already in global registry
			this.requireReference(refOid);
		}

		private Item createBuildItem(
			final long                                   oid        ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final Object                                 instance
		)
		{
			return new Item(oid, instance, typeHandler);
		}
		
		private Item createSkipBuildItem(final long oid, final Object instance)
		{
			// skip items do not require a type handler, only oid and optional instance
			return new Item(oid, instance, null);
		}
		
		private Item createBuildItem(final long objectId, final long typeId)
		{
			// at one point or another, a nasty ?->Object cast is necessary. Safety guaranteed by logic.
			@SuppressWarnings("unchecked")
			final PersistenceTypeHandler<Binary, Object> typeHandler = (PersistenceTypeHandler<Binary, Object>)
				this.typeHandlerLookup.lookupTypeHandler(objectId, typeId)
			;
			
			if(typeHandler == null)
			{
				// at this point, a handler must definitely be present
				throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
			}
			
			return this.createBuildItem(
				objectId,
				typeHandler,
				this.registry.lookupObject(objectId)
			);
		}

		@Override
		public final Object lookupObject(final long oid)
		{
			return this.getBuildInstance(oid);
		}

		private void build()
		{
			this.buildInstances();
			this.completeInstances();
		}

		private void buildInstances()
		{
			for(Item entry = this.buildItemsHead.next; entry != null; entry = entry.next)
			{
				// dummy-buildItems for skipping (filtering) OIDs don't have data and can and may not update anything.
				if(!entry.hasData())
				{
					continue;
				}

				// all buildItems that have a handler must be complete and valid to be updated.
				/* (10.09.2015 TM)TODO: already existing instance gets updated (error for a DB situation)
				 * Why does the global instance have to be updated?
				 * Isn't this a bug?
				 * If the data in memory is the most current and the DB is only a receiving storer of
				 * information, how can there ever be a situation where an already existing instance has to be
				 * (or even MAY be!) updated?
				 * The persistence layer is NOT a data modification reverting tool, it is a persistence layer.
				 * This means: only newly created instaces should have to be updated (filled with data), not already
				 * existing ones.
				 *
				 * This has to be thought through thoroughly.
				 * Maybe the reason behind it was a more generic use aside from a database.
				 * E.g. receiving data from a client and directly updating an existing object graph with it.
				 *
				 * Also: already existing instances are not registered as to-be-built items anyway, only as skip items.
				 * So they will never be updated in the first place.
				 *
				 * But: if the instance was not already present at oid requiring time but is meanwhile at the
				 * building time, the already present instance gets updated, which is wrong. So the code below must
				 * be changed
				 *
				 * However: constants MUST be updated on the initial load of a database if they contain mutable fields.
				 * So maybe a persistence layer must use two different concepts:
				 * - one for initially loading (updating existing instances)
				 * - one for normal loading after initialization (never updating existing instances)
				 * 
				 * (19.09.2018 TM)NOTE:
				 * Another use case where it is valid to update instances from the persisted data is to keep
				 * replicating server nodes ("shadow server" or "read-only node" or whatever) up to date.
				 * 
				 * It is also conceivable that a read from the database shall be used to reset modified instances
				 * to their latest persisted state. While this is generally a rather bad design (an application
				 * should be able to produce consistent states or store its resetting state on its own), this
				 * might be a valid approach for specific applications.
				 * 
				 * In any case, there should be a distinction between logic to initially restore persisted state
				 * and logic for regular runtime uses. The latter might be the same thing, but not always.
				 */
				entry.handler.update(
					entry,
					this.getEffectiveInstance(entry),
					this
				);
			}
		}

		private void completeInstances()
		{
			/* (29.07.2015 TM)TODO: binary builder: complex completion cases
			 * what if completing one instance properly depends on completing another instance first?
			 * E.g. hash collection of hash collections with the hash value depending on the inner
			 * hash collection's content?
			 * Stupid idea in the first place, but a general implementation pattern in JDK with its naive equals&hash
			 * concept.
			 * What if two such instances have a circular dependency between each other?
			 * Possible solutions:
			 * - complete entries in reverse order. Should cover more, although not all cases
			 * - ignore difficult cases and consider such weird cases to be tailored business logic that has to be
			 *   handled accordingly.
			 * - Maybe some kind of "already completed" registry? But how would that help?
			 *
			 * If the thought turns out to be not a problem at all, comment here accordingly.
			 */
			for(Item entry = this.buildItemsHead.next; entry != null; entry = entry.next)
			{
				// dummy-buildItems for skipping (filtering) OIDs don't have data and can and may not update anything.
				if(!entry.hasData())
				{
					continue;
				}
				entry.handler.complete(entry, entry.contextInstance, this);
			}
		}

		@Override
		public final void accept(final long value)
		{
			this.handleReference(value);
		}


		// (17.10.2013 TM)XXX: refactor to builditems instance similar to ... idk storer or so.

		///////////////////////////////////////////////////////////////////////////
		// build items map //
		////////////////////

		private final Item   buildItemsHead      = new Item()                         ;
		private       Item   buildItemsTail      = this.buildItemsHead                ;
		private       int    buildItemsSize                                           ;
		private       Item[] buildItemsHashSlots = new Item[DEFAULT_HASH_SLOTS_LENGTH];
		private       int    buildItemsHashRange = this.buildItemsHashSlots.length - 1;



		private Object internalGetFirst()
		{
			return this.buildItemsHead.next == null ? null : this.buildItemsHead.next.contextInstance;
		}

		private void rebuildBuildItems()
		{
			// moreless academic check for more than 1 billion entries
			if(XMath.isGreaterThanOrEqualHighestPowerOf2(this.buildItemsHashSlots.length))
			{
				return; // note that aborting rebuild does not ruin anything, only performance degrades
			}

			final int newRange; // potential int overflow ignored deliberately
			final Item[] newSlots = new Item[(newRange = (this.buildItemsHashSlots.length << 1) - 1) + 1];
			for(Item entry : this.buildItemsHashSlots)
			{
				for(Item next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[(int)(entry.oid & newRange)];
					newSlots[(int)(entry.oid & newRange)] = entry;
				}
			}
			this.buildItemsHashSlots = newSlots;
			this.buildItemsHashRange = newRange;
		}

		private void putBuildItem(final Item entry)
		{
			entry.link = this.buildItemsHashSlots[(int)(entry.oid & this.buildItemsHashRange)];
			this.buildItemsHashSlots[(int)(entry.oid & this.buildItemsHashRange)] =
				this.buildItemsTail = this.buildItemsTail.next = entry
			;
			if(++this.buildItemsSize >= this.buildItemsHashRange)
			{
				this.rebuildBuildItems();
			}
		}

		/* required reference is one that does not meet any of the following conditions:
		 * - null
		 * - already registered as complete build item
		 * - registered as to be skipped dummy build item
		 * - decided by the context to be already present (e.g. a class/constant/entity that shall not be updated)
		 */
		private boolean isUnrequiredReference(final long oid)
		{
			// spare pointless null reference roundtrips
			if(oid == 0L)
			{
				return true;
			}

			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(Item e = this.buildItemsHashSlots[(int)(oid & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.oid == oid)
				{
					return true;
				}
			}

			// if a reference is unrequired (e.g. constant), simply register it as a build item right away
			if(this.handleKnownObject(oid, this.skipObjectRegisterer))
			{
				return true;
			}
			
			// reaching here means the reference is really required to be resolved (loaded)
			return false;
		}
		
		private boolean handleKnownObject(final long objectId, final PersistenceInstanceHandler handler)
		{
			final Object instance = this.registry.lookupObject(objectId);
			if(instance == null)
			{
				return false;
			}
			
			handler.handle(objectId, instance);
			
			return true;
		}

		private Object getBuildInstance(final long oid)
		{
			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(Item e = this.buildItemsHashSlots[(int)(oid & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.oid == oid)
				{
					return this.getEffectiveInstance(e);
				}
			}
			return null;
		}

		private void registerSkipOid(final long oid)
		{
			for(Item e = this.buildItemsHashSlots[(int)(oid & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.oid == oid)
				{
					return;
				}
			}
			
			this.putBuildItem(this.createSkipBuildItem(oid, null));
		}

		private void clearBuildItems()
		{
			(this.buildItemsTail = this.buildItemsHead).next = null;
			final Item[] slots = this.buildItemsHashSlots;
			
			for(int i = 0; i < slots.length; i++)
			{
				slots[i] = null;
			}
			
			this.buildItemsSize = 0;
			this.anchor.clear(); // release helper anchor to allow the chunks to be collected
		}

		//////////////////////////
		// build items map end //
		//////////////////////////////////////////////////////////////////////////
		
		
	
		private void readLoadOnce()
		{
			this.addChunks(this.sourceSupplier.source().read());

			/* the processing of the initial read might have resulted in reference oids that have to be loaded
			 * (e.g. the initial read returns a root instance). So call the standard loading method at this point.
			 * If no additional load items have been added, the method will return very quickly.
			 */
			this.readLoadOidData();
		}

		private void readLoadOidData()
		{
			/* (09.09.2015 TM)TODO: persistence loading consistency
			 * Doesn't the incremental loading of references have to be an atomic process on the source side
			 * (e.g. storage engine) to avoid potential inconstencies by concurrent write requests?
			 * As BinaryLoading and Storage are decoupled, maybe both should be implemented:
			 * The storage task collects all required items via reference iteration by the handler and
			 * returns the completed result to the loader.
			 * This logic here will then never need to do more than 1 loop, but CAN do more loops if the source
			 * only provides exactely what is requested (e.g. a simple file source with no concurrency issues).
			 *
			 * On problem is, however: the storage task processing may not return instances that are already loaded
			 * and registered in the registry. Otherwise, every load request could potentially load "half the database",
			 * just to discard most of the data again while building.
			 * So lookup-access to the registry would have to be provided to the storage task as well.
			 *
			 * Concept:
			 * The Storage load request task gets passed a function of the form
			 * boolean requireOid(long)
			 * that is called for every OID to be loaded by the storage entity type handler while deep-loading
			 * references.
			 * The function is or delegates to this instance, which in turn asks the object registry, if the instance
			 * is already present. If yes, it is enqueued as a build item (to ensure a strong reference) and for later
			 * building. If no, then a new blank instance of the type and length is enqueued as a local build item.
			 * Return values are no/yes accordingly.
			 *
			 * Problems:
			 * 1.) The storage (entity type handler) needs a way to recognize the special nature of Lazy
			 * references. Otherwise, the lazy reference would become effectively eager, i.e. stop working.
			 * In the end, this means to enhance the type description syntax with a "do not deep load references"
			 * marker.
			 * Which is kind of ugly :(.
			 * The currently used logic-side BinaryHandler solution is much more elegant...
			 *
			 * 2.) maybe, once all the data is assembled, the loader has to acquire and keep a lock on the object
			 * registry for the whole building process. Otherwise, it could occur that concurrent modifications in the
			 * the object registry lead to a mixture of entity state (inconcistencies).
			 * Or maybe than can never happen as the memory is always more current than the database and building
			 * the graph automatically creates the right state. Tricky ... needs more thought.
			 *
			 *
			 * HM...
			 *
			 * OR is it maybe not required at all because:
			 * - the application memory always has the most current version of an entity
			 * - if an entity already exists in the registry, it will never be required to be loaded at all
			 * - therefore, a concurrent write to the DB CANNOT create an inconsistent state.
			 *   The situation "Thread A load part 1, Thread B write entity X, Thread A load part 2 includint entity X"
			 *   can never occur.
			 * Is that correct?
			 */

			final PersistenceSource<Binary> source = this.sourceSupplier.source();
			while(!this.loadItems.isEmpty())
			{
				this.addChunks(source.readByObjectIds(this.loadItems.getObjectIdSets()));
			}
		}

		protected void addChunks(final XGettingCollection<? extends Binary> chunks)
		{
			this.loadItems.clear();
			
			// remember last buildItem that already has its references registered for later iteration
			final Item referenceHandlingBaseItem = this.buildItemsTail;

			/*
			 * Create build items for ALL instances prior to handling references to ensure that already loaded
			 * instances are properly found when coming accross their reference id.
			 *
			 * Note on anchor:
			 * As only each chunk's memory address is stored in each entity entry it must be guaranteed that
			 * the chunks don't get garbage collected prematurely.
			 * To avoid adding a redundant reference to every entry, all chunks are registered here.
			 * As this is a little hacky, it must be ensured that the add really happens before the iterate,
			 * otherwise the premature GC could maybe still happen. This means the JIT must be prevented from
			 * reordering the code. This is done by making the iteration line depending on the collection reference
			 * (".last()" instead of "chunks").
			 * The collection gets cleared before finishing the whole loading call to make sure the chunks are
			 * present until the very end.
			 */
			this.anchor.add(chunks);
			for(final Binary chunk : this.anchor.last())
			{
				this.createInstanceBuildItems(chunk);
			}

			/*
			 * Handle all references of all newly created build items (loaded entities/instances).
			 * "base" means the item itself won't get its references handled inside.
			 */
			this.handleAllReferences(referenceHandlingBaseItem);
		}
		
		private void populate(final Consumer<Object> collector, final long... oids)
		{
			for(int i = 0; i < oids.length; i++)
			{
				collector.accept(this.getBuildInstance(oids[i]));
			}
		}

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized Object get()
		{
			this.readLoadOnce();
			this.build();
			final Object instance = this.internalGetFirst();
			this.clearBuildItems();

			return instance;
		}

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized Object getObject(final long oid)
		{
			this.requireReference(oid);
			this.readLoadOidData();
			this.build();
			final Object instance = this.getBuildInstance(oid);
			this.clearBuildItems();
			
			return instance;
		}

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized <C extends Consumer<Object>> C collect(final C collector, final long... oids)
		{
			for(int i = 0; i < oids.length; i++)
			{
				this.requireReference(oids[i]);
			}
			this.readLoadOidData();
			this.build();
			this.populate(collector, oids);
			this.clearBuildItems();
			return collector;
		}

		@Override
		public PersistenceRoots loadRoots()
		{
			final Object initial = this.get();

			if(initial == null)
			{
				return null; // might be null if there is no data at all
			}

			/*
			 * Not sure if this instanceof is a hack or clean. The intention / rationale is:
			 * A datasource that supports / contains persistence roots (e.g. graph database or a file writer
			 * using the concept) must return that instance on the initial get anyway.
			 * For a datasource that does not support it, this method would have to throw an exception anyway,
			 * one way or another.
			 * So design-wise, it should be viable to just relay to the initialGet() and make a runtime type check.
			 */
			if(!(initial instanceof PersistenceRoots))
			{
				// (21.10.2013 TM)EXCP: proper exception
				throw new RuntimeException("Initially read data is no roots instance");
			}
			return (PersistenceRoots)initial;
		}

		@Override
		public final void registerSkip(final long oid)
		{
			this.registerSkipOid(oid);
		}

		private final void requireReference(final long refOid)
		{
			// add-logic: only put if not contained yet (single-lookup)
			this.loadItems.addLoadItem(refOid);
		}

		@Override
		public PersistenceObjectRetriever getObjectRetriever()
		{
			return this.sourceSupplier;
		}

		
		
		static final class Item extends Binary
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			final long oid;
			PersistenceTypeHandler<Binary, Object> handler;
			Object contextInstance, localInstance;
			Item next, link;



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			Item()
			{
				super();
				this.oid = 0L;
			}

			Item(
				final long                                   oid            ,
				final Object                                 contextInstance,
				final PersistenceTypeHandler<Binary, Object> handler
			)
			{
				super();
				this.oid             = oid            ;
				this.handler         = handler        ;
				this.contextInstance = contextInstance;
			}



			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final long storeEntityHeader(
				final long entityContentLength,
				final long entityTypeId       ,
				final long entityObjectId
			)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public final ByteBuffer[] buffers()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected final void internalIterateCurrentData(final Consumer<byte[]> iterator)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected final long[] internalGetStartOffsets()
			{
				// (26.10.2013)NOTE: this is moreless only for debugging and should not be necessary for productive use
				if(this.entityContentAddress == 0)
				{
					throw new IllegalStateException();
				}
				return new long[]{this.entityContentAddress};
			}

			@Override
			protected final long[] internalGetBoundOffsets()
			{
				// (26.10.2013)NOTE: this is moreless only for debugging and should not be necessary for productive use
				if(this.entityContentAddress == 0)
				{
					throw new IllegalStateException();
				}
				return new long[]{this.entityContentAddress + XMemory.get_long(this.entityContentAddress)};
			}

			@Override
			public final long[] startOffsets()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public final long[] boundOffsets()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public final long buildItemAddress()
			{
				return this.entityContentAddress;
			}

			@Override
			public final void clear()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public final boolean isEmpty()
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public final long totalLength()
			{
				throw new UnsupportedOperationException();
			}

		}
		
	}



	public final class CreatorSimple implements BinaryLoader.Creator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final RawValueHandler rawValueHandler;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		CreatorSimple(final RawValueHandler rawValueHandler)
		{
			super();
			this.rawValueHandler = rawValueHandler;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public BinaryLoader createLoader(
			final PersistenceTypeHandlerLookup<Binary> typeLookup,
			final PersistenceObjectRegistry            registry  ,
			final PersistenceSourceSupplier<Binary>    source
		)
		{
			return new BinaryLoader.Implementation(
				this.rawValueHandler,
				typeLookup,
				registry,
				source,
				new LoadItemsChain.Simple()
			);
		}

	}



	public final class CreatorChannelHashing implements BinaryLoader.Creator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final RawValueHandler rawValueHandler ;
		private final _intReference   hashSizeProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public CreatorChannelHashing(
			final RawValueHandler rawValueHandler ,
			final _intReference   hashSizeProvider
		)
		{
			super();
			this.rawValueHandler  = rawValueHandler ;
			this.hashSizeProvider = hashSizeProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public BinaryLoader createLoader(
			final PersistenceTypeHandlerLookup<Binary> typeLookup,
			final PersistenceObjectRegistry            registry  ,
			final PersistenceSourceSupplier<Binary>    source
		)
		{
			return new BinaryLoader.Implementation(
				this.rawValueHandler,
				typeLookup,
				registry,
				source,
				new LoadItemsChain.ChannelHashing(this.hashSizeProvider.get())
			);
		}

	}

}
