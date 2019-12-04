package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.math.XMath;
import one.microstream.memory.XMemory;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import one.microstream.persistence.types.PersistenceInstanceHandler;
import one.microstream.persistence.types.PersistenceLoader;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceObjectRetriever;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceSourceSupplier;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerLookup;
import one.microstream.reference._intReference;

public interface BinaryLoader extends PersistenceLoader<Binary>, PersistenceObjectIdResolver
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

	public static BinaryLoader.Default New(
		final PersistenceTypeHandlerLookup<Binary> typeLookup     ,
		final PersistenceObjectRegistry            registry       ,
		final PersistenceSourceSupplier<Binary>    source         ,
		final LoadItemsChain                       loadItems      ,
		final boolean                              switchByteOrder
	)
	{
		return new BinaryLoader.Default(
			notNull(typeLookup),
			notNull(registry),
			notNull(source),
			notNull(loadItems),
			switchByteOrder
		);
	}

	public final class Default implements BinaryLoader, BinaryEntityDataReader, PersistenceObjectIdAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int DEFAULT_HASH_SLOTS_LENGTH = 1024;


		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// may be a relay lookup that provides special handlers providing logic
		private final boolean                                        switchByteOrder          ;
		private final PersistenceTypeHandlerLookup<Binary>           typeHandlerLookup        ;
		private final PersistenceObjectRegistry                      registry                 ;
		private final BulkList<XGettingCollection<? extends Binary>> anchor = new BulkList<>();

		private final PersistenceInstanceHandler skipObjectRegisterer = (objectId, instance) ->
			this.putBuildItem(
				this.createSkipItem(objectId, instance)
			)
		;

		private final PersistenceSourceSupplier<Binary> sourceSupplier;
		private final LoadItemsChain                    loadItems     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeHandlerLookup<Binary> typeLookup     ,
			final PersistenceObjectRegistry            registry       ,
			final PersistenceSourceSupplier<Binary>    source         ,
			final LoadItemsChain                       loadItems      ,
			final boolean                              switchByteOrder
		)
		{
			super();
			this.typeHandlerLookup = typeLookup     ;
			this.registry          = registry       ;
			this.sourceSupplier    = source         ;
			this.loadItems         = loadItems      ;
			this.switchByteOrder   = switchByteOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void readBinaryEntities(final ByteBuffer entitiesData)
		{
			if(this.switchByteOrder)
			{
				this.internalReadBinaryEntitiesByteReversing(entitiesData);
			}
			else
			{
				this.internalReadBinaryEntities(entitiesData);
			}
		}
		
		private void internalReadBinaryEntities(final ByteBuffer entitiesData)
		{
			final long startAddress = XMemory.getDirectByteBufferAddress(entitiesData);
			final long boundAddress = startAddress + entitiesData.limit();
			
			// the start of an entity always contains its length. Loading chunks do not contain gaps (negative length)
			for(long address = startAddress; address < boundAddress; address += XMemory.get_long(address))
			{
				this.createBuildItem(new BinaryLoadItem(
					Binary.toEntityContentOffset(address)
				));
			}
		}
		
		private void internalReadBinaryEntitiesByteReversing(final ByteBuffer entitiesData)
		{
			final long startAddress = XMemory.getDirectByteBufferAddress(entitiesData);
			final long boundAddress = startAddress + entitiesData.limit();
			
			// the start of an entity always contains its length. Loading chunks do not contain gaps (negative length)
			for(long address = startAddress; address < boundAddress; address += Long.reverseBytes(XMemory.get_long(address)))
			{
				this.createBuildItem(new BinaryLoadItemByteReversing(
					Binary.toEntityContentOffset(address)
				));
			}
		}
		
		private void createBuildItem(final BinaryLoadItem loadItem)
		{
			// at some point, a nasty cast from ? to Object is necessary. Safety guaranteed by logic.
			@SuppressWarnings("unchecked")
			final PersistenceTypeHandler<Binary, Object> typeHandler = (PersistenceTypeHandler<Binary, Object>)
				this.typeHandlerLookup.lookupTypeHandler(
					loadItem.getBuildItemTypeId()
				)
			;
			
			// proper build items must have a typeHandler
			if(typeHandler == null)
			{
				throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(
					loadItem.getBuildItemTypeId()
				);
			}
			
			loadItem.handler = typeHandler;
			if((loadItem.existingInstance = this.registry.lookupObject(loadItem.getBuildItemObjectId())) == null)
			{
				loadItem.createdInstance = loadItem.handler.create(loadItem, this);
			}
			
			// register build item
			this.putBuildItem(loadItem);
		}
		
		// CHECKSTYLE.OFF: FinalParameters: this method is just an outsourced scroll-helper
		protected void handleAllReferences(BinaryLoadItem item)
		{
			// iterate over all new build items (relocated loaded and created unrequested items alike)
			while((item = item.next) != null)
			{
				if(!item.hasData())
				{
					continue;
				}
				this.loadReferences(item); // register references of each new item
			}
		}
		// CHECKSTYLE.ON: FinalParameters

		private Object getEffectiveInstance(final BinaryLoadItem entry)
		{
			/* tricky concurrent part:
			 * if a proper existing instance already was registered, simply use that.
			 *
			 * Otherwise:
			 * try to register the thread-locally created instance as the context instance for the OID at this point and in
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
			
			/* (03.09.2019 TM)TODO: priv#141: existingInstance and createdInstance redundant?
			 * Aren't these two unnecessary?
			 * ExistingInstance is a potentially already existing one.
			 * CreatedInstance is a preliminarily created instance that might be discarded in preference of a
			 * meanwhile created instance (which is kind of a race condition, isn't it?).
			 * 
			 * This could be consolidated to a single "instance" field with the following logic:
			 * - initially null
			 * - in here, a "this.registry.ensureInstance(entry, this)" is called that either returns the existing
			 *   instance or creates a new instance under the protection of its internal lock.
			 * - the entry would have to implement a proper interface of course. The implementation of its method
			 *   does what currently is "buildItem.handler.create(buildItem, this);"
			 *   Something like "this.handler.create(this, loader);"
			 * 
			 * This way, there would be only one instance, no discarded preliminary instance and precisely one point
			 * in time where the proper instance is (selected) or (created and registered).
			 * 
			 * (11.11.2019 TM)NOTE:
			 * Not a good idea. The concept of the two instances was:
			 * If a new instance has to be created, it is not in a consistent state until after #update
			 * oder even after #complete. Until then, it may not be publicly available via the object registry.
			 * Funnily, this code does exactely that, nonetheless:
			 * - new instance instance is created locally (safe)
			 * - new instance gets registered in the object Registry (not safe)
			 * - THEN it gets updated
			 * - And completed even later.
			 * 
			 * Hm.
			 * So either the concept can/must be changed to
			 * "a single and early registered instance is okay because it is the application logic's concurrency responsibility"
			 * (a little shady)
			 * OR the registration process must be shifted to behind complete or at least to behind update.
			 * 
			 * Ah yes. The point below did already handle that.
			 */
			
			/* (03.09.2019 TM)TODO: priv#141: loading race conditions?
			 * (Addon to the point above)
			 * Albeit: what about globally registering an instance before it is completely built?
			 * Couldn't that cause race conditions and inconcistencies?
			 * And if not: Why not determine (select or created&register) the instance right away when creating
			 * the build item? Maybe the whole process of created all required build items should happen under
			 * one big lock of the objectRegistry?
			 * 
			 * This would all have to be thought through, researched and tested with the appropriate time and care
			 * which, currently, is not available.
			 */

			// (26.08.2019 TM)NOTE: paradigm change: #create may return null. Required for handling deleted enums.
			if(entry.existingInstance != null)
			{
				return entry.existingInstance;
			}
			if(entry.createdInstance == null)
			{
				return null;
			}
			
			// this makes the locally created instance the "officially existing" instance for the registry's context.
			return entry.existingInstance = this.registry.optionalRegisterObject(
				entry.getBuildItemObjectId(),
				entry.createdInstance
			);
		}

		protected PersistenceTypeHandler<Binary, ?> lookupTypeHandler(final long tid)
		{
			final PersistenceTypeHandler<Binary, ?> handler;
			
			if((handler = this.typeHandlerLookup.lookupTypeHandler(tid)) == null)
			{
				throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(tid);
			}
			
			return handler;
		}

		protected void loadReferences(final BinaryLoadItem entry)
		{
			/*
			 * Custom handler implementation can decide whether references of a particular field shall be loaded.
			 * Note that the handler has been provided by the context instance, so it can already be a
			 * context-specific handler implementation.
			 */

//			XDebug.debugln("refs of " + entry.handler.typeName() + " " + entry.handler.typeId() + " " + entry.oid);

			entry.handler.iterateLoadableReferences(entry, this);
		}
				
		@Override
		public final Object lookupObject(final long objectId)
		{
			return this.getBuildInstance(objectId);
		}

		private void build()
		{
			this.buildInstances();
			this.completeInstances();
		}

		private void buildInstances()
		{
			for(BinaryLoadItem entry = this.buildItemsHead.next; entry != null; entry = entry.next)
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
//				XDebug.println("Updating " + entry);
				
				// (26.08.2019 TM)NOTE: paradigm change: #create may return null. Required for handling deleted enums.
				final Object effectiveInstance = this.getEffectiveInstance(entry);
				if(effectiveInstance != null)
				{
					entry.handler.update(entry, effectiveInstance, this);
				}
				
				// (26.08.2019 TM)NOTE: old version
//				entry.handler.update(
//					entry,
//					this.getEffectiveInstance(entry),
//					this
//				);
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
			for(BinaryLoadItem entry = this.buildItemsHead.next; entry != null; entry = entry.next)
			{
				// dummy-buildItems for skipping (filtering) OIDs don't have data and can and may not update anything.
				if(!entry.hasData())
				{
					continue;
				}
				entry.handler.complete(entry, entry.existingInstance, this);
			}
		}

		@Override
		public final void acceptObjectId(final long objectId)
		{
			if(this.isUnrequiredReference(objectId))
			{
				return;
			}
			// oid is required to have data loaded even if instance is already in global registry
			this.requireReference(objectId);
		}


		// (17.10.2013 TM)XXX: refactor to builditems instance similar to ... idk storer or so.

		///////////////////////////////////////////////////////////////////////////
		// build items map //
		////////////////////

		private final BinaryLoadItem   buildItemsHead      = this.createLoadItemDummy();
		private       BinaryLoadItem   buildItemsTail      = this.buildItemsHead       ;
		private       int              buildItemsSize                                  ;
		private       BinaryLoadItem[] buildItemsHashSlots = new BinaryLoadItem[DEFAULT_HASH_SLOTS_LENGTH];
		private       int              buildItemsHashRange = this.buildItemsHashSlots.length - 1;



		private Object internalGetFirst()
		{
			return this.buildItemsHead.next == null ? null : this.buildItemsHead.next.existingInstance;
		}

		private void rebuildBuildItems()
		{
			// moreless academic check for more than 1 billion entries
			if(XMath.isGreaterThanOrEqualHighestPowerOf2(this.buildItemsHashSlots.length))
			{
				return; // note that aborting rebuild does not ruin anything, only performance degrades
			}

			final int newRange; // potential int overflow ignored deliberately
			final BinaryLoadItem[] newSlots = new BinaryLoadItem[(newRange = (this.buildItemsHashSlots.length << 1) - 1) + 1];
			for(BinaryLoadItem entry : this.buildItemsHashSlots)
			{
				for(BinaryLoadItem next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[(int)entry.getBuildItemObjectId() & newRange];
					newSlots[(int)entry.getBuildItemObjectId() & newRange] = entry;
				}
			}
			this.buildItemsHashSlots = newSlots;
			this.buildItemsHashRange = newRange;
		}

		private void putBuildItem(final BinaryLoadItem entry)
		{
			entry.link = this.buildItemsHashSlots[(int)entry.getBuildItemObjectId() & this.buildItemsHashRange];
			this.buildItemsHashSlots[(int)entry.getBuildItemObjectId() & this.buildItemsHashRange] =
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
		private boolean isUnrequiredReference(final long objectId)
		{
			// spare pointless null reference roundtrips
			if(objectId == 0L)
			{
				return true;
			}

			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(BinaryLoadItem e = this.buildItemsHashSlots[(int)(objectId & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.getBuildItemObjectId() == objectId)
				{
					return true;
				}
			}

			// if a reference is unrequired (e.g. constant), simply register it as a build item right away
			if(this.handleKnownObject(objectId, this.skipObjectRegisterer))
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

		private Object getBuildInstance(final long objectId)
		{
			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(BinaryLoadItem e = this.buildItemsHashSlots[(int)(objectId & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.getBuildItemObjectId() == objectId)
				{
					return this.getEffectiveInstance(e);
				}
			}
			return null;
		}

		private void registerSkipOid(final long objectId)
		{
			for(BinaryLoadItem e = this.buildItemsHashSlots[(int)(objectId & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.getBuildItemObjectId() == objectId)
				{
					return;
				}
			}
			
			this.putBuildItem(this.createSkipItem(objectId, null));
		}
		
		private BinaryLoadItem createSkipItem(final long objectId, final Object instance)
		{
			/*
			 * A little hacky, but worth it:
			 * Since BinaryLoadItem does not hold an oid value explicitely, but instead reads it from the entity header
			 * in the binary data, a skip item has to emulate/fake such data with the explicit skip oid written at a
			 * conforming offset. Skip items are hardly ever used, so the little detour and memory footprint overhead
			 * are well worth it if spares an additional explicit 8 byte long field for the millions and millions
			 * of common case entities.
			 */
			final ByteBuffer dbb = XMemory.allocateDirectNative(Binary.entityHeaderLength());
			
			// skip items do not require a type handler, only objectId, a fakeContentAddress and optional instance
			final BinaryLoadItem skipItem = new BinaryLoadItem(0);
			skipItem.modifyLoadItem(dbb, 0, 0, 0, objectId);
			skipItem.existingInstance = instance;
			
			// skip items will never use the helper instance for anything, since they are skip dummies.
			skipItem.registerHelper(dbb, dbb);
			
			return skipItem;
		}
		
		private BinaryLoadItem createLoadItemDummy()
		{
			// tricky: dummies must have a valid memory address or else calling toString in the debugger crashes the JVM.
			return new BinaryLoadItem(XMemory.allocate(Binary.entityHeaderLength()) + Binary.entityHeaderLength());
		}

		private void clearBuildItems()
		{
			(this.buildItemsTail = this.buildItemsHead).next = null;
			final BinaryLoadItem[] slots = this.buildItemsHashSlots;
			
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
			final BinaryLoadItem referenceHandlingBaseItem = this.buildItemsTail;

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
				// iterate over all entity data parts in the chunk, creating build items for each one.
				chunk.iterateEntityData(this);
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
		public final synchronized Object getObject(final long objectId)
		{
			this.requireReference(objectId);
			this.readLoadOidData();
			this.build();
			final Object instance = this.getBuildInstance(objectId);
			this.clearBuildItems();
			
			return instance;
		}

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized <C extends Consumer<Object>> C collect(final C collector, final long... objectIds)
		{
			for(int i = 0; i < objectIds.length; i++)
			{
				this.requireReference(objectIds[i]);
			}
			this.readLoadOidData();
			this.build();
			this.populate(collector, objectIds);
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
		public final void registerSkip(final long objectId)
		{
			this.registerSkipOid(objectId);
		}

		private final void requireReference(final long objectId)
		{
			// add-logic: only put if not contained yet (single-lookup)
			this.loadItems.addLoadItem(objectId);
		}

		@Override
		public PersistenceObjectRetriever getObjectRetriever()
		{
			return this.sourceSupplier;
		}
		
	}



	public final class CreatorSimple implements BinaryLoader.Creator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final boolean switchByteOrder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		CreatorSimple(final boolean switchByteOrder)
		{
			super();
			this.switchByteOrder = switchByteOrder;
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
			return new BinaryLoader.Default(
				typeLookup,
				registry,
				source,
				new LoadItemsChain.Simple(),
				this.switchByteOrder
			);
		}

	}



	public final class CreatorChannelHashing implements BinaryLoader.Creator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final boolean       switchByteOrder     ;
		private final _intReference channelCountProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public CreatorChannelHashing(
			final _intReference channelCountProvider,
			final boolean       switchByteOrder
		)
		{
			super();
			this.switchByteOrder      = switchByteOrder     ;
			this.channelCountProvider = channelCountProvider;
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
			return new BinaryLoader.Default(
				typeLookup,
				registry,
				source,
				new LoadItemsChain.ChannelHashing(this.channelCountProvider.get()),
				this.switchByteOrder
			);
		}

	}

}
