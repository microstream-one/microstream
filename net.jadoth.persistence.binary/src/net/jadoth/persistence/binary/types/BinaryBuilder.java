package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.math.XMath;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import net.jadoth.persistence.types.PersistenceBuildItem;
import net.jadoth.persistence.types.PersistenceBuilder;
import net.jadoth.persistence.types.PersistenceDistrict;
import net.jadoth.persistence.types.PersistenceInstanceHandler;
import net.jadoth.persistence.types.PersistenceTypeHandler;

public interface BinaryBuilder extends PersistenceBuilder<Binary>, _longProcedure
{
	public interface Creator extends PersistenceBuilder.Creator<Binary>
	{
		@Override
		public BinaryBuilder createPersistenceBuilder();
	}



	/* (02.12.2012)TODO: BinaryBuilder consolidate
	 * consolidate namings, methods, structure in combination with BinaryLoader
	 */
	public abstract class AbstractImplementation implements BinaryBuilder, PersistenceBuildItem.Creator<Binary, Entry>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int DEFAULT_HASH_SLOTS_LENGTH = 1024;


		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceDistrict<Binary>                    district;
		private final BulkList<XGettingCollection<? extends Binary>> anchor = new BulkList<>();

		private final PersistenceInstanceHandler skipObjectRegisterer = (oid, instance) ->
			this.putBuildItem(
				this.createSkipBuildItem(oid, instance)
			)
		;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		protected AbstractImplementation(final PersistenceDistrict<Binary> district)
		{
			super();
			this.district = notNull(district);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

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
		protected void handleAllReferences(Entry item)
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
			for(long address = startAddress; address < boundAddress; address += XVM.get_long(address))
			{
				this.createInstanceBuildItem(address);
			}
		}

		private Object getEffectiveInstance(final Entry entry)
		{
			/* tricky concurrent part:
			 * if a proper district instance already was registered, simply use that.
			 *
			 * Otherwise:
			 * try to register thread-locally created instance as the district instance for the OID at this point and in
			 * the process use a meanwhile created and registered district instance instead and ignore the own local one
			 *
			 * This way, every thread will use the same instance for a given OID even if multiple threads
			 * are loading and creating different instances for the same OID concurrently.
			 * The only requirement is that the registry's register method is thread-safe.
			 *
			 * Note that the district field is still a thread-local field, just one that points to an instance
			 * known by the district. The only true multithread-ly used part is the district itself.
			 *
			 * Note on instance type: both ways that set instances here (local and district),
			 * namely registry and type handler, are trusted to provide (instantiate or know) instances of the
			 * correct type.
			 */
			return entry.districtInstance != null
				? entry.districtInstance
				: (entry.districtInstance = this.district.optionalRegisterObject(
					entry.oid,
					entry.localInstance
				))
			;
		}

		protected PersistenceTypeHandler<Binary, ?> lookupTypeHandler(final long oid, final long tid)
		{
			final PersistenceTypeHandler<Binary, ?> handler;
			
			if((handler = this.district.lookupTypeHandler(oid, tid)) == null)
			{
				throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(tid);
			}
			
			return handler;
		}

		protected Entry createBuildItem(final long objectId, final long typeId)
		{
			final Entry buildItem = this.district.createBuildItem(this, objectId, typeId);
			
			if(buildItem.handler == null)
			{
				// at this point, a handler must definitely be present
				throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
			}
			
			return buildItem;
		}

		// note: this method is guaranteed to be called only once per instance data
		protected void createInstanceBuildItem(final long address) throws ClassCastException
		{
			// get the build item for the instance with the type handler and maybe an existing global instance
			final Entry buildItem = this.createBuildItem(
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
			 * created an unnecessary local instance because the district instance could not have found before.
			 * The instance was an enum constants array, so it should have been findable already.
			 * Check if this is always the case (would be a tremendous amount of unnecessary instances) or a buggy
			 * special case (constant registration or whatever)
			 */
			if(buildItem.districtInstance == null)
			{
				buildItem.localInstance = buildItem.handler.create(buildItem);
			}

			// register build item
			this.putBuildItem(buildItem);
		}

		protected void handleReferences(final Entry entry)
		{
			/*
			 * Custom handler implementation can decide whether references of a particular field shall be loaded.
			 * Note that the handler has been provided by the district instance, so it can already be a
			 * district-specific handler implementation.
			 */

//			XDebug.debugln("refs of " + entry.handler.typeName() + " " + entry.handler.typeId() + " " + entry.oid);

			entry.handler.iteratePersistedReferences(entry, this);
		}

		protected final void handleReference(final long refOid)
		{
			if(this.isUnrequiredReference(refOid))
			{
				return;
			}
			// oid is required to have data loaded even if instance is already in global registry
			this.requireReference(refOid);
		}

		protected abstract void requireReference(long refOid);

		@Override
		public final Entry createBuildItem(final long oid)
		{
			return this.district.createBuildItem(this, oid);
		}

		protected void addChunks(final XGettingCollection<? extends Binary> chunks)
		{
			// remember last buildItem that already has its references registered for later iteration
			final Entry referenceHandlingBaseItem = this.buildItemsTail;

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

		@Override
		public final Object lookupObject(final long oid)
		{
			return this.getBuildInstance(oid);
		}

		protected final void build()
		{
			this.buildInstances();
			this.completeInstances();
		}

		protected final void buildInstances()
		{
			for(Entry entry = this.buildItemsHead.next; entry != null; entry = entry.next)
			{
				// dummy-buildItems for skipping (filtering) OIDs don't have data and can and may not update anything.
				if(!entry.hasData())
				{
					continue;
				}
				// MARKER: BinaryBuilder#buildInstance()
//				XDebug.debugln("building instance " + entry.oid + " of type " + entry.handler.typeName());
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

		protected final void completeInstances()
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
			for(Entry entry = this.buildItemsHead.next; entry != null; entry = entry.next)
			{
				// dummy-buildItems for skipping (filtering) OIDs don't have data and can and may not update anything.
				if(!entry.hasData())
				{
					continue;
				}
				entry.handler.complete(entry, entry.districtInstance, this);
			}
		}

		protected final <T> void internalCollectByType(final Consumer<? super T> collector, final Class<T> type)
		{
			for(Entry entry = this.buildItemsHead.next; entry != null; entry = entry.next)
			{
				if(type.isInstance(entry.districtInstance))
				{
					collector.accept(type.cast(entry.districtInstance));
				}
			}
		}

		protected final void commit()
		{
			this.district.commit();
		}

		@Override
		public final void accept(final long value)
		{
			this.handleReference(value);
		}

		@Override
		public final Entry createBuildItem(
			final long                                   oid        ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final Object                                 instance
		)
		{
			return new Entry(oid, instance, typeHandler);
		}

		@Override
		public final Entry createSkipBuildItem(final long oid, final Object instance)
		{
			// skip items do not require a type handler, only oid and optional instance
			return new Entry(oid, instance, null);
		}


		// (17.10.2013 TM)XXX: refactor to builditems instance similar to ... idk storer or so.

		///////////////////////////////////////////////////////////////////////////
		// build items map //
		////////////////////

		private final Entry   buildItemsHead      = new Entry()                         ;
		private       Entry   buildItemsTail      = this.buildItemsHead                 ;
		private       int     buildItemsSize                                            ;
		private       Entry[] buildItemsHashSlots = new Entry[DEFAULT_HASH_SLOTS_LENGTH];
		private       int     buildItemsHashRange = this.buildItemsHashSlots.length - 1 ;



		protected final Object internalGetFirst()
		{
			return this.buildItemsHead.next == null ? null : this.buildItemsHead.next.districtInstance;
		}

		private void rebuildBuildItems()
		{
			// moreless academic check for more than 1 billion entries
			if(XMath.isGreaterThanOrEqualHighestPowerOf2Integer(this.buildItemsHashSlots.length))
			{
				return; // note that aborting rebuild does not ruin anything, only performance degrades
			}

			final int newRange; // potential int overflow ignored deliberately
			final Entry[] newSlots = new Entry[(newRange = (this.buildItemsHashSlots.length << 1) - 1) + 1];
			for(Entry entry : this.buildItemsHashSlots)
			{
				for(Entry next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[(int)(entry.oid & newRange)];
					newSlots[(int)(entry.oid & newRange)] = entry;
				}
			}
			this.buildItemsHashSlots = newSlots;
			this.buildItemsHashRange = newRange;
		}

		protected final void putBuildItem(final Entry entry)
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
		 * - decided by the district to be already present (e.g. a class/constant/entity that shall not be updated)
		 */
		protected final boolean isUnrequiredReference(final long oid)
		{
			// spare pointless null reference roundtrips
			if(oid == 0L)
			{
				return true;
			}

			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(Entry e = this.buildItemsHashSlots[(int)(oid & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.oid == oid)
				{
					return true;
				}
			}

			// if district deems the reference to be unrequired, simply register it as a build item right away
			if(this.district.handleKnownObject(oid, this.skipObjectRegisterer))
			{
				return true;
			}
			// reaching here means the reference is really required to be resolved (loaded)
			return false;
		}

		protected final Object getBuildInstance(final long oid)
		{
			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(Entry e = this.buildItemsHashSlots[(int)(oid & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.oid == oid)
				{
					return this.getEffectiveInstance(e);
				}
			}
			return null;
		}

		protected final void registerSkipOid(final long oid)
		{
			for(Entry e = this.buildItemsHashSlots[(int)(oid & this.buildItemsHashRange)]; e != null; e = e.link)
			{
				if(e.oid == oid)
				{
					return;
				}
			}
			this.putBuildItem(this.createBuildItem(oid));
		}

		protected final void clearBuildItems()
		{
			(this.buildItemsTail = this.buildItemsHead).next = null;
			final Entry[] slots = this.buildItemsHashSlots;
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


	}



	static final class Entry extends Binary implements PersistenceBuildItem<Binary>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final long oid;
		Object districtInstance, localInstance;
		PersistenceTypeHandler<Binary, Object> handler;
		Entry next, link;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Entry()
		{
			super();
			this.oid              = 0L  ;
			this.districtInstance = null;
			this.localInstance    = null;
			this.handler          = null;
			this.link             = null;
			this.next             = null;
		}

		Entry(final long oid, final Object districtInstance, final PersistenceTypeHandler<Binary, Object> handler)
		{
			super();
			this.oid              = oid    ;
			this.districtInstance = districtInstance;
			this.localInstance    = null   ;
			this.handler          = handler;
			this.link             = null   ;
			this.next             = null   ;
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
			return new long[]{this.entityContentAddress + XVM.get_long(this.entityContentAddress)};
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
