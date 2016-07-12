package net.jadoth.storage.types;

import static net.jadoth.Jadoth.checkArrayRange;
import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;
import static net.jadoth.Jadoth.to_int;
import static net.jadoth.math.JadothMath.log2pow2;
import static net.jadoth.math.JadothMath.notNegative;

import java.nio.ByteBuffer;

import net.jadoth.Jadoth;
import net.jadoth.functional.ThrowingProcedure;
import net.jadoth.functional._longProcedure;
import net.jadoth.math.JadothMath;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.ChunksBuffer;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.util.branching.ThrowBreak;


public interface StorageEntityCache<I extends StorageEntityCacheItem<I>> extends StorageHashChannelPart
{
	public StorageTypeDictionary typeDictionary();

	public StorageEntityType<I> lookupType(long typeId);

	public void markGray(long objectId);

	public boolean incrementalLiveCheck(long timeBudgetBound);

	public boolean incrementalGarbageCollection(long timeBudgetBound, StorageChannel channel);

	public boolean issuedGarbageCollection(long nanoTimeBudget, StorageChannel channel);

	public boolean issuedCacheCheck(long nanoTimeBudget, StorageEntityCacheEvaluator entityEvaluator);

	public void copyRoots(ChunksBuffer dataCollector);

	public long cacheSize();

	public long getHighestRootInstanceObjectId();

	public long getLowestRootInstanceObjectId();



	public final class Implementation implements StorageEntityCache<StorageEntity.Implementation>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		static final _longProcedure BREAK_ON_NONNULL_REFERENCE_VALUE = new _longProcedure()
		{
			@Override
			public final void accept(final long value)
			{
				if(value != Swizzle.nullId())
				{
					throw Jadoth.BREAK; // found a non-null reference, break to signal
				}
			}
		};



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int                                 channelIndex         ;
		private final int                                 channelHashModulo    ;
		private final int                                 channelHashShift     ;
		private final long                                rootTypeId           ;
		        final StorageEntityCacheEvaluator         entityCacheEvaluator ;
		private final StorageTypeDictionary               typeDictionary       ;
		private final StorageEntityCache.Implementation[] colleagues           ;
		private final StorageEntityCache.GcPhaseMonitor   gcPhaseMonitor       ;
		private final GrayReferenceMarker                 grayReferenceMarker  ;
		private final StorageValidRootIdCalculator        validRootIdCalculator;


		/* (19.04.2016 TM)TODO: StorageFileManager in StorageEntityCache
		 * fix call-site init-loop for that
		 */
		// currently only used for entity iteration
//		private final StorageFileManager.Implementation   fileManager          ;

		// cache function pointers because this::method creates a new instance on every call (tested, at least in Java 8).
		private final GrayInitializer                     grayInitializer   = new GrayInitializer();
		private final MaxObjectId                         maxObjectId       = new MaxObjectId()    ;
		private final MinObjectId                         minObjectId       = new MinObjectId()    ;
		private final RootsDeleter                        rootsDeleter      = new RootsDeleter()   ;

		private final GraySegment                         graySegmentRoot   = new GraySegment()   ;
		private       GraySegment                         graySegmentHead   = this.graySegmentRoot;
		private       GraySegment                         graySegmentTail   = this.graySegmentRoot;

		private       StorageEntity.Implementation[]      oidHashTable     ;
		private       int                                 oidModulo        ; // long modulo makes not difference
		private       long                                oidSize          ;

		private       StorageEntityType.Implementation[]  tidHashTable     ;
		private       int                                 tidModulo        ;
		private       int                                 tidSize          ;

		private final StorageEntityType.Implementation    typeHead         ;
		private       StorageEntityType.Implementation    typeTail         ;
		private       StorageEntityType.Implementation    rootType         ;

//		private       StorageEntityType.Implementation    liveCursorType   ;
		private       StorageEntity.Implementation        liveCursor       ;

		private       StorageEntityType.Implementation    sweepCursorType  ;
		private       StorageEntity.Implementation        sweepCursor      ;

		private       long                                usedCacheSize    ;
		volatile      boolean                             completedSweeping;



		/*
		 * Helper flag to prevent premature phase switching after a store or import.
		 * Consider the following example:
		 * Alle channels are actually finished marking (no more gray entities to mark), but have not switched
		 * to sweep phase yet (e.g. timeout just before the phase switch check).
		 * Without the flag, one channel (without any entities to post-store update) might race directly to the
		 * phase check and switch phases, while another channel just enqueued some saved entities in the gray chain.
		 * The result would be an inconsistent GC state.
		 * Note that the post-store updating is performed asynchronous. The channel threads only wait for each other
		 * synchronizedly until the data has been written (guaranteed to be persisted) and then every channel performs
		 * the thread-local entity updating and post-task-processing housekeeping on its own.
		 * The flag also serves as a short cut for quicker checking of still being in mark phase
		 */
		private       boolean                             isMarking        ;

		/* Note on concurrency:
		 * There are two aspects to concurrency that have to be considered:
		 *
		 * 1.) GC-internal concurrency
		 * Meaning channels may not switch prematurely to sweep mode while others are still marking.
		 * There STILL seems to be a bug in it (07.04.2015) and it is not clear where or how.
		 *
		 * 2.) Interference from storing entities.
		 * A store can occur at any given time and stir up existing data (also see "doomed-kept-alive" and "slipped-through" cases)
		 * It also has concurrency aspect: Channels only wait for each other on completion of the write, not on
		 * updating all entities. This happens after the waiting point, followed by house keeping (GC) and advancing to the
		 * next task.
		 * Stores during the mark phase (~90% of the time) must set an additional isMarking flag BEFORE the waiting point
		 * to avoid race conditions afterwards and must (re-)enqueue changed entities.
		 * Stores during the sweep phase may NOT enqueue gray items as this would make the gray chain inconsistent
		 * (gray chain must be empty during sweep).
		 *
		 */

//		long DEBUG_grayCount = 0;
//		int DEBUG_marked = 0;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final int                                 channelIndex         ,
			final StorageEntityCacheEvaluator         cacheEvaluator       ,
			final StorageTypeDictionary               typeDictionary       ,
			final StorageEntityCache.Implementation[] colleagues           ,
			final StorageEntityCache.GcPhaseMonitor   gcPhaseMonitor       ,
			final long                                rootTypeId           ,
			final StorageValidRootIdCalculator        validRootIdCalculator,
			final StorageFileManager.Implementation   fileManager
		)
		{
			super();
			this.channelIndex          = notNegative(channelIndex)             ;
			this.rootTypeId            =             rootTypeId                ;
			this.entityCacheEvaluator  = notNull    (cacheEvaluator)           ;
			this.typeDictionary        = notNull    (typeDictionary)           ;
			this.colleagues            = notNull    (colleagues)               ;
			this.gcPhaseMonitor        = notNull    (gcPhaseMonitor)           ;
//			this.fileManager           =             fileManager               ;
			this.channelHashModulo     =             colleagues.length - 1     ;
			this.channelHashShift      = log2pow2   (colleagues.length)        ;
			this.validRootIdCalculator =             validRootIdCalculator     ;
			this.graySegmentRoot.next  =             this.graySegmentRoot      ; // grayRoot always marked as not used

			this.typeHead              = new StorageEntityType.Implementation(this.channelIndex);
			this.grayReferenceMarker   = new GrayReferenceMarker(colleagues, this.channelHashModulo);

			this.initializeState();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

//		final void DEBUG_gcState()
//		{
//			final VarString vs = VarString.New();
//
//			for(GraySegment s = this.graySegmentTail; s != this.graySegmentHead; s = s.next)
//			{
//				vs.add("(" + s.lowIndex + "/" + s.highIndex + ")-");
//			}
//			vs.add("(" + this.graySegmentHead.lowIndex + "/" + this.graySegmentHead.highIndex + ")");
//
//			GraySegment s = this.graySegmentRoot;
//			int elementCount1 = 0;
//			int elementCount2 = 0;
//			do
//			{
//				elementCount1 += s.highIndex - s.lowIndex;
//				for(int i = 0; i < GraySegment.MAX_SIZE; i++)
//				{
//					if(s.entities[i] != null)
//					{
//						elementCount2++;
//					}
//				}
//			}
//			while((s = s.next) != this.graySegmentRoot);
//
//			vs.add(" isMarking = " + this.isMarking);
//			vs.add(", gray element count = " + elementCount1 + "/" + elementCount2);
//			vs.add(", marked = " + this.DEBUG_marked);
//
//			DEBUGStorage.println(vs.toString());
//		}

		final void truncateGraySegments()
		{
			this.graySegmentRoot.next = this.graySegmentRoot;
			this.graySegmentRoot.clear();
		}

		final void resetGraySegments()
		{
//			DEBUGStorage.println(this.channelIndex() + " resetting gray segements.");

			if(this.gcPhaseMonitor.isComplete())
			{
//				DEBUGStorage.println(this.channelIndex() + " resetting gray segements to root.");

				// if no garbage collection has to be done at all, reduce gray segements to root.
				this.truncateGraySegments();
			}
			else
			{
//				int DEBUG_resetCount = 0, DEBUG_clearCount = 0;

				// reset all segments that were used for next run
				GraySegment s = this.graySegmentRoot, last;
				while((s = (last = s).next).used)
				{
					s.clear();
					s.used = false;
//					DEBUG_resetCount++;
				}

				// cut off the unused rest (idempotent for full circle, which is pretty cool)
				last.next = this.graySegmentRoot;
//				s.prev.next = this.graySegmentRoot;
//				this.graySegmentRoot.prev = s.prev;

				// only for debugging
//				while(s != this.graySegmentRoot)
//				{
//					DEBUG_clearCount++;
//					s = s.next;
//				}
//				DEBUGStorage.println(this.channelIndex() + " gray segments resetted: " + DEBUG_resetCount + ", cleared: " + DEBUG_clearCount);
			}

			// reset gray marking/processing iteration state for next run
			this.graySegmentHead = this.graySegmentTail = this.graySegmentRoot;
		}


		final void initializeState()
		{
			// reset state without ruining gcPhaseMonitor initial state
			this.resetState();
		}

		final synchronized void resetState()
		{
			this.oidHashTable   = new StorageEntity.Implementation[1];
			this.oidModulo      = this.oidHashTable.length - 1;
			this.oidSize        = 0;

			this.tidHashTable   = new StorageEntityType.Implementation[1];
			this.tidModulo      = this.tidHashTable.length - 1;
			this.tidSize        = 0;

			(this.typeTail      = this.typeHead).next = null;

//			this.liveCursorType = this.typeHead;
//			this.liveCursor     = this.typeHead.head;
			this.resetLiveCursor();

			this.usedCacheSize  = 0L;

			// create a new root type instance on every clear. Everything else is not worth the reset&register-hassle.
			this.rootType       = this.getType(this.rootTypeId);

			// gc stuff
			this.sweepCursorType   = this.typeHead;
			this.sweepCursor       = this.sweepCursorType.head;
			this.isMarking         = true ;
			this.completedSweeping = false;
		}

		final void clearState()
		{
			// must lock independently of gcPhaseMonitor to avoid deadlock!
			this.resetState();

			// Also resets gray segments at the end
			this.gcPhaseMonitor.reset(this.colleagues);
		}

		private void resetLiveCursor()
		{
			this.liveCursor = this.typeHead.head;
		}

		// must use lock to keep other channels from marking while rebuild is in progress
		private synchronized void enlargeOidHashTable()
		{
			final int newModulo;
			final StorageEntity.Implementation[] newSlots =
				JadothMath.isGreaterThanOrEqualHighestPowerOf2Integer(this.oidHashTable.length)
				? new StorageEntity.Implementation[newModulo = Integer.MAX_VALUE] // perfect hash range special case
				: new StorageEntity.Implementation[(newModulo = (this.oidModulo + 1 << 1) - 1) + 1] // 1111 :D
			;
			rebuildOidHashSlots(this.oidHashTable, newSlots, this.channelHashShift, newModulo);
			this.oidHashTable = newSlots;
			this.oidModulo    = newModulo;
		}

		private static void rebuildOidHashSlots(
			final StorageEntity.Implementation[] oldSlots     ,
			final StorageEntity.Implementation[] newSlots     ,
			final int                            bitShiftCount,
			final int                            newModulo
		)
		{
			for(StorageEntity.Implementation entry : oldSlots)
			{
				for(StorageEntity.Implementation next; entry != null; entry = next)
				{
					next = entry.hashNext;
					entry.hashNext = newSlots[oidHashIndex(entry.objectId(), bitShiftCount, newModulo)];
					newSlots[oidHashIndex(entry.objectId(), bitShiftCount, newModulo)] = entry;
				}
			}
		}

		private synchronized void checkOidHashTableConsolidation()
		{
			// if the hash table has suitable size, abort
			if(this.oidHashTable.length >>> 1 < this.oidSize)
			{
				return;
			}

			// if the hash table is unnecessary large, shrink it
			final int                            newModulo = JadothMath.pow2BoundMaxed((int)this.oidSize) - 1;
			final StorageEntity.Implementation[] newSlots  = new StorageEntity.Implementation[newModulo + 1];
			rebuildOidHashSlots(this.oidHashTable, newSlots, this.channelHashShift, newModulo);
			this.oidHashTable = newSlots;
			this.oidModulo    = newModulo;
		}

		private synchronized void rebuildTidHashTable()
		{
			final int newModulo;
			final StorageEntityType.Implementation[] newSlots =
				new StorageEntityType.Implementation[(newModulo = (this.tidModulo + 1 << 1) - 1) + 1]
			;

			for(StorageEntityType.Implementation entries : this.tidHashTable)
			{
				for(StorageEntityType.Implementation next; entries != null; entries = next)
				{
					next = entries.hashNext;
					entries.hashNext = newSlots[tidHashIndex(entries.typeId, newModulo)];
					newSlots[tidHashIndex(entries.typeId, newModulo)] = entries;
				}
			}
			this.tidHashTable = newSlots;
			this.tidModulo    = newModulo;
		}

		final StorageEntityType.Implementation getType(final long typeId)
		{
			final StorageEntityType.Implementation type;
			if((type = this.lookupType(typeId)) != null)
			{
				return type;
			}
			return this.addNewType(typeId);
		}

		private StorageEntityType.Implementation addNewType(final long typeId)
		{
			// the order is important: first rebuild hash table, THEN create and register the instance. DONT MESS UP.
			if(this.tidSize >= this.tidModulo)
			{
				this.rebuildTidHashTable();
			}

			// explicit hash index for debug purposes. Creating types is not performance critical.
			final int hashIndex = tidHashIndex(typeId, this.tidModulo);

			// create and register
			final StorageEntityType.Implementation type = new StorageEntityType.Implementation(
				this.channelIndex,
				this.typeDictionary.lookupTypeHandler(typeId),
				this.tidHashTable[hashIndex],
				this.typeHead
			);
			this.typeTail = this.typeTail.next = this.tidHashTable[hashIndex] = type;

			// increment type size at the end on definite success, not before.
			this.tidSize++;

			return type;
		}

		static final int hash(final long value, final int modulo)
		{
			return (int)(value & modulo);
		}

		static final int hashNormalized(final long value, final int bitShiftCount, final int modulo)
		{
			// (09.08.2015)NOTE: included channel hash mod bit shifting to properly distribute in hash table
			return (int)(value >>> bitShiftCount & modulo);
		}

		static final int tidHashIndex(final long tid, final int tidModulo)
		{
			return hash(tid, tidModulo);
		}

		static final int oidHashIndex(final long oid, final int bitShiftCount, final int oidModulo)
		{
			return hashNormalized(oid, bitShiftCount, oidModulo);
		}

		static final int oidChannelIndex(final long oid, final int channelHashModulo)
		{
			return hash(oid, channelHashModulo);
		}

		private int oidHashIndex(final long oid)
		{
			return oidHashIndex(oid, this.channelHashShift, this.oidModulo);
		}

		private int oidChannelIndex(final long oid)
		{
			return oidChannelIndex(oid, this.channelHashModulo);
		}

		private StorageEntity.Implementation getOidHashChainHead(final long oid)
		{
			return this.oidHashTable[this.oidHashIndex(oid)];
		}

		private void setOidHashChainHead(final long oid, final StorageEntity.Implementation head)
		{
			this.oidHashTable[this.oidHashIndex(oid)] = head;
		}


		/* Note on synchronization:
		 * This method does not need to be synchronized (locked), as it is exclusively always called by the
		 * channel's inherent thread which is also the same that rebuilds the hashTables, so it can never work
		 * on old cached instances.
		 */
		final void unregisterEntity(final StorageEntity.Implementation item)
		{
			StorageEntity.Implementation entry;
			if((entry = this.getOidHashChainHead(item.objectId())) == item)
			{
				this.setOidHashChainHead(item.objectId(), item.hashNext);
			}
			else
			{
				// subject is (must be) guaranteed to be contained in the hash chain, hence no null check
				while(entry.hashNext != item)
				{
					entry = entry.hashNext;
				}
				entry.hashNext = item.hashNext;
			}
		}

		/* Note on synchronization:
		 * This method does not need to be synchronized (locked), as it is exclusively always called by the
		 * channel's inherent thread which is also the same that rebuilds the hashTables, so it can never work
		 * on old outdated instances.
		 */
		public final StorageEntity.Implementation getEntry(final long objectId)
		{
			for(StorageEntity.Implementation e = this.getOidHashChainHead(objectId); e != null; e = e.hashNext)
			{
				if(e.objectId() == objectId)
				{
					return e;
				}
			}
			return null;
		}

		final boolean isGarbageCollectionComplete()
		{
			return this.gcPhaseMonitor.isComplete();
		}

		final void resetGarbageCollectionCompletionForEntityUpdate()
		{
			synchronized(this.gcPhaseMonitor)
			{
				if(!this.gcPhaseMonitor.isSweepMode())
				{
					// mark channel as beeing marking due to updated entities to be gray-enqueued. See flag comment.
					this.setIsMarking();
				}
				this.gcPhaseMonitor.resetCompletion();
			}
		}

		private final synchronized void setIsMarking()
		{
			this.isMarking = true;
		}

		private final void clearIsMarking()
		{
			this.isMarking = false;
		}

		@Override
		public final synchronized long getHighestRootInstanceObjectId()
		{
			return this.rootType.iterateEntities(this.maxObjectId.reset()).yield();
		}

		@Override
		public final synchronized long getLowestRootInstanceObjectId()
		{
			return this.rootType.iterateEntities(this.minObjectId.reset()).yield();
		}

		private void ensureNoCachedData(final StorageEntity.Implementation entry)
		{
			if(entry.isLive())
			{
				this.modifyUsedCacheSize(-entry.clearCache());
			}
		}

		private void validateObjectId(final long objectId)
		{
			// validate object Id in general
			Swizzle.validateObjectId(objectId);

			// validate channel for object Id
			if(this.oidChannelIndex(objectId) != this.channelIndex)
			{
				// (05.05.2014)EXCP: proper exception
				throw new RuntimeException("Invalid objectId " + objectId + " for hash channel " + this.channelIndex);
			}
		}

		final StorageIdRangeAnalysis validateEntities(final StorageTypeDictionary oldTypes)
		{
			long maxTid = 0, maxOid = 0, maxCid = 0;

			// validate all entities via iteration by type. Simplifies debugging and requires less type pointer chasing
			for(StorageEntityType.Implementation type : this.tidHashTable)
			{
				while(type != null)
				{
					final StorageIdRangeAnalysis maxTypeOid = type.validateEntities(oldTypes);
					type = type.hashNext;

					final Long typeMaxTid = maxTypeOid.highestIdsPerType().get(Swizzle.IdType.TID);
					if(typeMaxTid != null && typeMaxTid >= maxTid)
					{
						maxTid = typeMaxTid;
					}

					final Long typeMaxOid = maxTypeOid.highestIdsPerType().get(Swizzle.IdType.OID);
					if(typeMaxOid != null && typeMaxOid >= maxOid)
					{
						maxOid = typeMaxOid;
					}

					final Long typeMaxCid = maxTypeOid.highestIdsPerType().get(Swizzle.IdType.CID);
					if(typeMaxCid != null && typeMaxCid >= maxCid)
					{
						maxCid = typeMaxCid;
					}
				}
			}

			return StorageIdRangeAnalysis.New(maxTid, maxOid, maxCid);
		}

		final StorageEntityType.Implementation validateEntity(
			final long length,
			final long typeId,
			final long objcId
		)
		{
			final StorageEntityType.Implementation type;
			final StorageEntity.Implementation entry = this.getEntry(objcId);

			if(entry != null)
			{
				if((type = entry.typeInFile.type).typeId != typeId)
				{
					// (29.07.2014 TM)EXCP: proper exception
					throw new RuntimeException(
						"Object Id already assigned to an entity of another type. "
						+ "Existing: " + objcId + ", type " + type.typeId + ". "
						+ "Subject: " + objcId + ", type " + typeId + "."
					);
				}
			}
			else
			{
				this.validateObjectId(objcId);
				type = this.getType(typeId);
			}

			type.typeHandler().validateEntityGuaranteedType(length, objcId);

			return type;
		}

		final StorageEntity.Implementation putEntityValidated(
			final long                             objectId ,
			final StorageEntityType.Implementation type     ,
			final boolean                          isMarking
		)
		{
			/* This logic is a copy from #putEntity(long).
			 * This is intentionally done for performance reasons:
			 * The normal case (putEntity while storing) is faster if the type has to be looked up only if it is
			 * really required (creation of a new entry), while the validating case is faster if the anyway present
			 * type can be linked right away instead of being looked up again.
			 * This is one of the cases, where higher abstraction and redundant code prevents comes at the price of
			 * performance, hence to avoid that price, a little code redundancy is accepted.
			 */

			// ensure (lookup or create) complete entity item for storing
//			DEBUGStorage.println("looking for " + BinaryPersistence.getEntityObjectId(entityAddress));
			final StorageEntity.Implementation entry;
			if((entry = this.getEntry(objectId)) != null)
			{
//				DEBUGStorage.println("updating entry " + entry);
				this.updatePutEntity(entry, isMarking);
				return entry;
			}

//			DEBUGStorage.println("creating " + BinaryPersistence.getEntityObjectId(entityAddress) + ", " + BinaryPersistence.getEntityTypeId(entityAddress) + ", [" + BinaryPersistence.getEntityLength(entityAddress) + "]");
			return this.createEntity(objectId, type, isMarking);
		}

		final StorageEntity.Implementation updatePutEntity(final long entityAddress, final boolean isMarking)
		{
			// ensure (lookup or create) complete entity item for storing
//			DEBUGStorage.println("looking for " + BinaryPersistence.getEntityObjectId(entityAddress));
			final StorageEntity.Implementation entry;
			if((entry = this.getEntry(BinaryPersistence.getEntityObjectId(entityAddress))) != null)
			{
//				DEBUGStorage.println("updating entry " + entry);
				// same as updatePutEntity() but without the retro-reference-marking
				this.updatePutEntity(entry, isMarking);
				return entry;
			}

//			DEBUGStorage.println("creating " + BinaryPersistence.getEntityObjectId(entityAddress) + ", " + BinaryPersistence.getEntityTypeId(entityAddress) + ", [" + BinaryPersistence.getEntityLength(entityAddress) + "]");
			return this.createEntity(
				BinaryPersistence.getEntityObjectId(entityAddress),
				this.getType(BinaryPersistence.getEntityTypeId(entityAddress)),
				isMarking
			);
		}

		private void updatePutEntity(final StorageEntity.Implementation entry, final boolean isMarking)
		{
			// ensure the old data is not cached any longer
			this.ensureNoCachedData(entry);
			entry.detachFromFile();

			if(isMarking)
			{
				this.markPhaseEnsureGray(entry);
			}
			else
			{
				this.sweepPhaseMarkInitial(entry);
			}
		}

		public final long entityCount()
		{
			return this.oidSize;
		}

		/* Note on synchronization:
		 * This method does not need to be synchronized (locked), as it is exclusively always called by the
		 * channel's inherent thread which is also the same that rebuilds the hashTables, so it can never work
		 * on old cached instances.
		 */
		private StorageEntity.Implementation createEntity(
			final long                             objectId,
			final StorageEntityType.Implementation type    ,
			final boolean                          enqueueGray
		)
		{
			// increment size and check for necessary (and reasonable) rebuild
			if(this.oidSize >= this.oidModulo && this.oidModulo < Integer.MAX_VALUE)
			{
				this.enlargeOidHashTable();
			}

			// create and put entry
			final StorageEntity.Implementation entity;
			this.setOidHashChainHead(
				objectId,
				entity = StorageEntity.Implementation.New(
					objectId,
					type.dummy,
					this.getOidHashChainHead(objectId),
					type.hasReferences(),
					type.simpleReferenceDataCount()
				)
			);
			type.add(entity);
			this.oidSize++; // increment size not before creating and registering succeeded

			/* must enqueue referencing entities in case the new entity holds the last reference to an existing one.
			 * Example:
			 * A and B already exit. A references B.
			 * Then A and C get stored.
			 * A no longer references B. Instead, C now references B.
			 * If A was not already processed, then B has not been marked yet.
			 * If the new C does not get enqueued to have its references checked, B will not get marked in the
			 * current cycle and hence will be collected, desite being referenced by C.
			 * To avoid this, newly created entities with references must be enqueued gray right away.
			 */
			if(enqueueGray && entity.hasReferences())
			{
				// enqueuing is sufficient as new entities are initially gray anyway
				this.enqueueAndMarkGray(entity);
			}

			return entity;
		}

		/*
		 * Does not need to be synchronized as every concurrency-critical call of it has its own synchronized block
		 * and post-store entity creation sets an explicit marking flag.
		 */
		private void enqueueAndMarkGray(final StorageEntity.Implementation entry)
		{
//			if(!DEBUG_GC_ENABLED)
//			{
//				return;
//			}

//			this.DEBUG_grayCount++;

//			DEBUGStorage.println(this.channelIndex + " enqueuing " + entry);
			entry.markGray();
			if(this.graySegmentHead.put(entry))
			{
				return;
			}

			if(this.graySegmentHead.next != this.graySegmentTail && this.graySegmentHead.next.put(entry))
			{
				this.graySegmentHead = this.graySegmentHead.next;
			}
			else
			{
				(this.graySegmentHead = this.graySegmentHead.addSegment()).put(entry);
			}
		}


		private synchronized void initializeRootGray(final StorageEntity.Implementation entry)
		{
			// mark and enqueue as gray
			this.enqueueAndMarkGray(entry);
		}

		/* instantly promote white entities upon update (for 1 cycle) or demote already black entities for recheck
		 * Rationale:
		 * Consider the following scenario:
		 * A does not reference C, B references C.
		 * A's refs have already been marked, B's not yet.
		 * A and B get stored (updated), now A references C, B does not anymore.
		 * this results in C being still unmarked (=doomed) when entering sweep mode.
		 * Therefore, every entity with references must be checked again when being updated.
		 *
		 * This is, however, only viale during mark phase, as graying entities during sweep phase
		 * would cause inconcistencies (already swept entities begin falsely interpreted as aready marked in the next
		 * mark phase, not being reference-iterated, causing their exclusively referenced entities to be deleted.
		 */
		final synchronized void markPhaseEnsureGray(final StorageEntity.Implementation entry)
		{
			// non-referential entities have no references to be checked, hence mark black right away
			if(!entry.hasReferences())
			{
				entry.markBlack(); // mark non-reference entries black right away
				return;
			}

			// marked gray means being already gray-enqueued and not handled yet (would be black otherwise)
			if(entry.isGcGray())
			{
				return;
			}

			// set to gray, both white (promotion) and black (demotion for recheck)
			this.enqueueAndMarkGray(entry);
		}

		/*
		 * Used to mark an updated entity as initial (neither white nor gray/black, see states) during sweep mode.
		 * Rationale for the additional state:
		 * On updating entities after a store (or import etc) during sweep mode, it can't be determined if
		 * a white entity is an already visited black entity (resetted to white) or an actually not marked entity
		 * yet to be visited (i.e. normally doomed entity that got saved by the store's update).
		 * Simply setting white entities to black here could cause already visited entities to remain black in the
		 * next mark phase, being falsely considered as already handled, thus not having their references iterated
		 * and thus falsely dooming all their excklusively referenced entities. This MAY NOT happen.
		 * The solution:
		 * A special "initial" (or "light gray") state, that prevents the entity from beeing deleted in this sweep
		 * ("more than white)" but does not prevent the next marking from handling it ("less than gray").
		 * The light gray state will either be resetted to white in the current sweep (saved but whited) if the
		 * entity was not yet visited, or in the next mark phase (set to gray or black by marking) if the entity
		 * is reachable or at the latest in the next sweep (saved but whited).
		 * Note that this also influences how the state of "GC completeness" is determined.
		 *
		 */
		final void sweepPhaseMarkInitial(final StorageEntity.Implementation entry)
		{
			// if an entity is still marked (definitely yet to be visited during sweep)
			if(entry.isGcMarked())
			{
				return;
			}
			entry.markInitial();
		}

		private synchronized void promoteGray(final StorageEntity.Implementation entry)
		{
			/* (08.07.2016 TM)TODO: optimize gc already handled check?
			 * Shouldn't the already handled check come first?
			 * Does it matter if an entry is gray or black in the end?
			 * Would it be an error if an initially gray non-reference entity remains gray instead of black?
			 * Why do processed reference entities have to be marked black instead of just remain gray?
			 * (to check if they have already been processed? But the isBlack state is never queried...)
			 * Why not simply make an isBlack check here as a first check?
			 */

			if(!entry.hasReferences())
			{
				entry.markBlack(); // mark non-reference entries black right away
				return;
			}

			if(entry.isGcAlreadyHandled())
			{
				return; // if meanwhile already gray or black, do nothing
			}

			// only set to gray if entity was white before (neither gray or black)
			this.enqueueAndMarkGray(entry);
		}

		final synchronized boolean isMarking()
		{
			// the flag is not updated on every graychain advance, it's only a helper to handle stores.
			return this.isMarking
				|| this.graySegmentTail.hasElements() || this.graySegmentTail != this.graySegmentHead
			;
		}

		final boolean isInMarkPhase()
		{
			return !this.gcPhaseMonitor.isSweepMode();
		}

		final synchronized StorageEntity.Implementation getNextGray()
		{
			/* advancing the chain before graying current references
			 * could cause those gray items to slip through completion check.
			 * reference graying can't be done in here to avoid deadlocks
			 * (thread/channel 1 being locked in here,
			 * needing the lock on thread/channel 2 to gray a reference and vice versa)
			 * so the progression has to be made in three separate steps:
			 * 1.) (locked) read (NOT advance!) next gray item
			 * 2.) (no lock) iterate references, gray them in their respective channels (acquire locks as needed)
			 * 3.) (locked) advance gray chain
			 */
			if(this.graySegmentTail.hasElements())
			{
				return this.graySegmentTail.get();
			}

			// if tail has no elements (checked above) and caught up to head, then there are no more elements at all.
			if(this.graySegmentTail == this.graySegmentHead)
			{
				this.clearIsMarking();
				return null; // no more elements to process at all
			}

			// next (non-head) segment is guaranteed to have elements to be processed
			return (this.graySegmentTail = this.graySegmentTail.next).get();
		}

		private void advanceGrayChain()
		{
			// acquire lock on gcPhaseMonitor to avoid gray references slipping through mark phase finish check
			synchronized(this.gcPhaseMonitor)
			{
				// acquire lock on this to guarantee gray item enqueing safety
				synchronized(this)
				{
//					this.DEBUG_marked++;
					this.graySegmentTail.advanceProcessed();
				}
			}
		}

		final void deleteEntity(
			final StorageEntity.Implementation     entity        ,
			final StorageEntityType.Implementation type          ,
			final StorageEntity.Implementation     previousInType
		)
		{
			// (19.10.2015 TM)FIX-ME: /!\ DEBUG GC problem
//			if(entity.objectId <= 1000000000032112569L || type.typeId != 10000 || type.typeId != 1000163)
//			{
//				// (19.10.2015 TM)NOTE: alle neu angelegten Lazy und ZahlungManuelleVerrechnungen referenzen ignorieren (hoechstwahrscheinlich Dummies, gibt im Fehlerfall genug andere Typen)
//				DEBUGStorage.println(this.channelIndex + " deleting " + entity.objectId() + " " + type.typeHandler().typeName());
//			}

//			DEBUGStorage.println(this.channelIndex + " deleting " + entity.objectId() + " " + entity.type.type.typeHandler().typeName());

			// 1.) unregister entity from hash table (= unfindable by future requests)
			this.unregisterEntity(entity);

			// 2.) detach entity from file registry. Actual physical remains don't hurt, even on restart, as they will be unreachable again.
			entity.detachFromFile();

			// 3.) remove entity from its type registry, effectively removing it from iteration, count and export logic.
			type.remove(entity, previousInType);

			// 4.) unload cached data and update entity cache track accordingly
			this.ensureNoCachedData(entity);

			// 5.) mark entity as deleted
			entity.setDeleted();
		}

//		final StorageEntity.Implementation getNextSweepPreNonNullEntity()
//		{
//			StorageEntityType.Implementation type = this.liveCursorType;
//			while(type.head.typeNext == null)
//			{
//				type = type.next;
//			}
//			return (this.liveCursorType = type).head;
//		}

		private boolean incrementalSweep(final long timeBudgetBound, final StorageChannel channel)
		{
//			DEBUGStorage.println(this.channelIndex + " sweeping a little (" + (timeBudgetBound - System.nanoTime()) + ")");
//			int DEBUG_safed = 0, DEBUG_collected = 0;
//			final long DEBUG_starttime = System.nanoTime();

			StorageEntityType.Implementation sweepType  = this.sweepCursorType;
			// never null, neither initially nor incrementally
			StorageEntity.Implementation     item, last = this.sweepCursor;

			// sweep at least one item, even if there no time, to avoid starvation
			do
			{
				// get next item and check for end of type (switch to next type required)
				if((item = last.typeNext) == null)
				{
					// current type completed. Seek next type with entities.
					do
					{
						// advance to next type and check for full cycle (reach head type again)
						if((sweepType = sweepType.next) == this.typeHead)
						{
							this.completeSweepCycle(channel);
//							DEBUGStorage.println(this.channelIndex + " sweep COMPLETED, safed " + DEBUG_safed + ", collected " + DEBUG_collected + " (" + (System.nanoTime() - DEBUG_starttime) + "ns) gcComplete: " + this.gcPhaseMonitor.isPartialComplete());
							return true;
						}
					}
					while(sweepType.head.typeNext == null);
					last = (this.sweepCursorType = sweepType).head;
					continue;
				}

				// actual sweep: white entities are deleted, non-white entities are marked white but not deleted
				if(item.isGcMarked())
				{
//					DEBUGStorage.println("Saving " + item);
					(last = item).markWhite(); // reset to white and advance one item
//					DEBUG_safed++;
				}
				else
				{
					// otherwise white entity, so collect it
//					DEBUGStorage.println("Collecting " + item.objectId() + " (" + item.type.type.typeHandler().typeId() + " " + item.type.type.typeHandler().typeName() + ")");
					this.deleteEntity(item, sweepType, last);
					// decrement entity count (strictly only once per remove as guaranteed by check above)
//					DEBUG_collected++;
					/* even if another thread concurrently grays the item, it can only cause a single
					 * "pre-death" grace cycle as the item is no longer reachable from now on for future marking.
					 */
				}
			}
			while(System.nanoTime() < timeBudgetBound);
			/*
			 * Surprising but true:
			 * Checking time on every item is not slower than only checking it every 10th, 100th or 1000ths.
			 * The time per item was always around 16-32 ns on the same machine. Despite the normal case
			 * being only a tiny amount of work, the nanoTime() call still seems to be insignificant in comparison.
			 */

			// time ran out, update current sweep cursor position for next run
			this.sweepCursor = last;

//			DEBUGStorage.println(this.channelIndex + " sweep timed out, safed " + DEBUG_safed + ", collected " + DEBUG_collected);

			// report back as not completed yet
			return false;
		}

		private void completeSweepCycle(final StorageChannel channel)
		{
			// mark sweeping as done
			this.completedSweeping = true;

			// reset sweep cursors
			this.sweepCursor = (this.sweepCursorType = this.typeHead).head;

			// signal to channel
			channel.signalGarbageCollectionSweepCompleted();
		}


		private void checkForCacheClear(final StorageEntity.Implementation entry, final long evalTime)
		{
			if(this.entityCacheEvaluator.clearEntityCache(this.usedCacheSize, evalTime, entry))
			{
//				DEBUGStorage.println(this.channelIndex + " unloading GC data for " + current.objectId());
				// use ensure method for that for purpose of uniformity / simplicity
				this.ensureNoCachedData(entry);
			}
			else
			{
				// if the loaded entity data can stay in memory, touch the entity to mark now as its last use.
				entry.touch();
			}
		}

		private boolean incrementalMark(final long timeBudgetBound)
		{
			final StorageEntityCacheEvaluator entityCacheEvaluator = this.entityCacheEvaluator ;
			final GrayReferenceMarker         grayReferenceMarker  = this.grayReferenceMarker  ;
			final long                        evalTime             = System.currentTimeMillis();

			this.setIsMarking(); // will be cleared in #getNextGray() if apropriate

//			int DEBUG_marked = 0;
//			DEBUGStorage.println(this.channelIndex + " marking a little (" + (timeBudgetBound - evalTime) + ")");

			// mark at least one entity, even if there no time, to avoid starvation
			do
			{
				final StorageEntity.Implementation current;
				if((current = this.getNextGray()) == null)
				{
//					DEBUGStorage.println(this.channelIndex + " Gray chain processed.");
					// no more gray entities, nothing to do here any more, sweep is next
					return this.gcPhaseMonitor.isMarkPhaseComplete(this.colleagues);
				}

//				DEBUGStorage.println(this.channelIndex + " marking references of " + current.objectId() + " with cache size " + this.usedCacheSize);
				if(current.iterateReferenceIds(grayReferenceMarker))
				{
					// must check for clearing the cache again if marking required loading
					if(entityCacheEvaluator.clearEntityCache(this.usedCacheSize, evalTime, current))
					{
//						DEBUGStorage.println(this.channelIndex + " unloading GC data for " + current.objectId());
						// use ensure method for that for purpose of uniformity / simplicity
						this.ensureNoCachedData(current);
					}
					else
					{
						// if the loaded entity data can stay in memory, touch the entity to mark now as its last use.
						current.touch();
					}
				}
				else if(current.hasReferences())
				{
					// if data was already present from an referencing entity, touch the entity to keep the data alive.
					current.touch();
				}
				// never touch non-referencing entity via reference marking.

				current.markBlack();
//				DEBUGStorage.println(this.channelIndex + " marked " + current);
//				DEBUG_marked++;

				/* advance gray chain after graying current entity's reference
				 * to prevent those gray items from slipping through the completion check
				 */
				this.advanceGrayChain();
//				this.advanceGrayChain(current);
			}
			while(System.nanoTime() < timeBudgetBound);

//			DEBUGStorage.println(this.channelIndex + " incrementally marked " + DEBUG_marked);
			return false;
		}

		final void initializeGrayChain()
		{
//			DEBUGStorage.println(this.channelIndex + " initializing gray chain with " + this.rootType.typeId + " (" + this.rootType.entityCount() + ")");

			this.ensureSingletonRootInstance();

			// initialize gray chain with all root type instances, remove all empty root type instances in the process
			this.rootType.iterateEntities(this.grayInitializer);
		}

		private void ensureSingletonRootInstance()
		{
			/*
			 * On retrieving the root instance, effectively only the first root type instance of the lowest channel
			 * is considered. Everything else is is just dead weight, at least at the moment.
			 * Also, loading and building multiple roots instances causes OID conflicts in the swizzle registry
			 * for class constant instances (same instance, different OIDs).
			 * It is currently not foreseeable that that will/should/must change, as everything beyond a single
			 * root instance would be nothing more than a special-cased hashtable overhead.
			 * The only conceivable case would be multiple independent domain model object graphs, each with
			 * its own class loader and own constant instances etc. But that would also mean as a consequence:
			 * Why not make separate databases for them in the first place?
			 * Meaning a simple rule "one database per domain model graph / class loader".
			 *
			 * As a consequence, all but one root instance gets deleted here.
			 * The kept instance is the one with the highest OID (newest saved).
			 * This algorithm is a little clums and redundant, however channel count and root instances count
			 * are both very low (usually in the one-digits), so it still should run through in an instant.
			 */
			final long validRootsId = this.validRootIdCalculator.determineValidRootId(this.colleagues);
			this.rootType.removeAll(this.rootsDeleter.setValidRootId(validRootsId));
		}

		final class RootsDeleter implements StorageEntityType.Implementation.EntityDeleter
		{
			private long validRootId;

			final RootsDeleter setValidRootId(final long validRootId)
			{
				this.validRootId = validRootId;
				return this;
			}

			@Override
			public boolean test(final StorageEntity.Implementation t)
			{
				return t.objectId() != this.validRootId;
			}

			@Override
			public void delete(
				final StorageEntity.Implementation     entity        ,
				final StorageEntityType.Implementation type          ,
				final StorageEntity.Implementation     previousInType
			)
			{
				Implementation.this.deleteEntity(entity, type, previousInType);
			}
		}

		final void internalUpdateEntities(
			final ByteBuffer                     chunk               ,
			final long                           chunkStoragePosition,
			final StorageDataFile.Implementation file                ,
			final boolean                        isMarking
		)
		{
			final long chunkStartAddress   = Memory.directByteBufferAddress(chunk);
			final long chunkLength         = chunk.limit();

			// calculated offset difference, may even be negative, doesn't matter
			final long storageBackset    = chunkStoragePosition - chunkStartAddress;
			final long chunkBoundAddress = chunkStartAddress    + chunkLength      ;

			// chunk's entities are iterated, put into the cache and have their current storage positions set/updated
			for(long adr = chunkStartAddress; adr < chunkBoundAddress; adr += BinaryPersistence.getEntityLength(adr))
			{
				this.updatePutEntity(adr, isMarking)
				.updateStorageInformation(
					checkArrayRange(BinaryPersistence.getEntityLength(adr)),
					file,
					to_int(storageBackset + adr)
				);
			}
		}

		final void modifyUsedCacheSize(final long cacheChange)
		{
			this.usedCacheSize += cacheChange;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public int channelIndex()
		{
			return this.channelIndex;
		}

		@Override
		public final long cacheSize()
		{
			return this.usedCacheSize;
		}

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.typeDictionary;
		}

		public void postStoreUpdateEntities(
			final ByteBuffer[]                   chunks                ,
			final long[]                         chunksStoragePositions,
			final StorageDataFile.Implementation dataFile

		)
			throws InterruptedException
		{
			final boolean isMarking = this.isInMarkPhase();

			for(int i = 0; i < chunks.length; i++)
			{
				this.internalUpdateEntities(chunks[i], chunksStoragePositions[i], dataFile, isMarking);
			}

			// signal to gc phase monitor that this channel has no more pending update
//			this.gcPhaseMonitor.decrementPostStorePendingUpdateCount();
		}

		@Override
		public final StorageEntityType.Implementation lookupType(final long typeId)
		{
			for(StorageEntityType.Implementation typeEntry = this.tidHashTable[tidHashIndex(typeId, this.tidModulo)];
				typeEntry != null;
				typeEntry = typeEntry.hashNext
			)
			{
				if(typeEntry.typeId == typeId)
				{
					return typeEntry;
				}
			}
			return null;
		}

		@Override
		public void copyRoots(final ChunksBuffer dataCollector)
		{
//			this.DEBUG_PRINT_OID_HASH_VALUES();

			// better to a quick ensure here, see inside for detailed explanation
			this.ensureSingletonRootInstance();

			// iterate over all entities of all root types and copy their data
			this.rootType.iterateEntities(e -> e.copyCachedData(dataCollector));
		}

		@Override
		public final boolean incrementalLiveCheck(final long timeBudgetBound)
		{
			return this.internalLiveCheck(timeBudgetBound, this.entityCacheEvaluator);
		}

		private StorageEntity.Implementation completeFullCircleLiveCheck(
			final long                         timeBudgetBound,
			final StorageEntity.Implementation entity         ,
			final StorageEntity.Implementation terminator     ,
			final StorageEntityCacheEvaluator  evaluator      ,
			final long                         evalTime
		)
		{
			StorageEntity.Implementation e = entity;

			while(this.usedCacheSize > 0 && System.nanoTime() < timeBudgetBound && e != terminator)
			{
				if(e.isLive() && evaluator.clearEntityCache(this.usedCacheSize, evalTime, e))
				{
//					DEBUGStorage.println(this.channelIndex + " clearing entity " + live);
					// entity has cached data but was deemed as having to be cleared, so clear it
					this.ensureNoCachedData(e); // use ensure method for that for purpose of uniformity / simplicity
				}
				e = e.typeNext;
			}
			return e;
		}

//		private void DEBUG_trailingLiveCheck_NPE(
//			final StorageEntity.Implementation entity    ,
//			final StorageEntity.Implementation terminator
//		)
//		{
//			// (06.10.2015 TM)NOTE: null entity in trailingLiveCheck. +liveCursorType typeID = 1015220. passed entity = @1380463763 type = 1015220. terminator = @373245650 type = 1035230. this.liveCursorType head = @1380463763.
//
//			final NullPointerException e = new NullPointerException(
//			"null entity in trailingLiveCheck. "
//				+ " + liveCursorType typeID = " + this.liveCursorType.typeId + ". "
//				+ "passed entity = @" + System.identityHashCode(entity) + " type = " + entity.typeId() + ". "
//				+ "terminator = @" + System.identityHashCode(terminator) + " type = " + terminator.typeId() + ". "
//				+ "this.liveCursorType head = @" + System.identityHashCode(this.liveCursorType.head) + "."
//				+ "terminator type next type = " + terminator.type.type.next.typeId
//			);
//			e.printStackTrace();
//			throw e;
//		}

		private StorageEntity.Implementation getNonDeletedCursor()
		{
			// seek the first non-deleted entity in the same type starting at the current cursor
			for(StorageEntity.Implementation cursor = this.liveCursor; (cursor = cursor.typeNext) != null;)
			{
				if(!cursor.isDeleted())
				{
					return cursor;
				}
			}

			// all remaining entities in the type were deleted. So the whole type is advanced and its head returned.
			return this.liveCursor.typeInFile.type.next.head; // note that types are circularly linked
		}

		private boolean internalLiveCheck(final long timeBudgetBound, final StorageEntityCacheEvaluator evaluator)
		{
			// quick check before setting up the local stuff.
			if(this.usedCacheSize == 0)
			{
//				DEBUGStorage.println(this.channelIndex + " aborting live check (cache is empty)");
				return true;
			}

//			DEBUGStorage.println(this.channelIndex + " checking live entries, cache size " + this.usedCacheSize + ", budget " + (timeBudgetBound - System.nanoTime()));

//			debugPrintLiveChain(this.liveCursor);

			final long                             evalTime   = System.currentTimeMillis();


			// update if necessary and setup consistent cursors. Cursor is guaranteed to be re-reachable in the loop.
			final StorageEntity.Implementation     cursor     = this.getNonDeletedCursor();
			final StorageEntityType.Implementation cursorType = cursor.typeInFile.type;
			      StorageEntity.Implementation     entity     = cursor;
			      StorageEntityType.Implementation entityType = entity.typeInFile.type;
//			int DEBUG_checked = 0;

			/*
			 * Loop has three aborting conditions:
			 * 1.) Time is up
			 * 2.) The loop has done a full circle (cursor's type is encountered again) within one time budget / method call
			 * 3.) The cache has been cleared completely, hence there is nothing more to do.
			 */
			// check at least one entity, even if there no time, to avoid starvation
			do
			{
//				DEBUGStorage.println(this.channelIndex + " checking " + entity + " with cached size " + this.usedCacheSize);
				// actual live check
				if(entity.isLive() && evaluator.clearEntityCache(this.usedCacheSize, evalTime, entity))
				{
//					DEBUGStorage.println(this.channelIndex + " clearing entity " + live);
					// entity has cached data but was deemed as having to be cleared, so clear it
					this.ensureNoCachedData(entity); // use ensure method for that for purpose of uniformity / simplicity
				}
//				DEBUG_checked++;

				// proceed to next entity and do special case checking
				if((entity = entity.typeNext) == null)
				{
					/*
					 * if the cursor type when the method was called is encountered again, it means the
					 * live check has (almost) done a complete cycle. Only the entities from the beginning
					 * of the type until the cursor remain to be checked. This is done in a special
					 * simplified "trailing iteration".
					 * After that, the cycle is guaranteed to be complete (cursor reached again), so the
					 * live check can be aborted. The cursor does not have to be updated.
					 * Note:
					 * The live entity could be checked directly, however then the check would have to be
					 * done on every entity instead of just on every type change. This is a dramatic difference:
					 * millions of per-entity check compared to a few hundreds or thousands of per-type checks.
					 */
					if((entityType = entityType.next) != cursorType)
					{
						// simply proceed with head entity of the next type
						entity = entityType.head;
					}
					else
					{
//						DEBUGStorage.println(this.channelIndex + " almost completed live check. Checked " + DEBUG_checked);

						// check remaining trailing entities, account for meanwhile
						entity = this.completeFullCircleLiveCheck(timeBudgetBound, cursorType.head, cursor, evaluator, evalTime);

						// this break does NOT mean usedCacheSize is 0. It means only that there was a full circle
						break;
					}
				}
			}
			while(this.usedCacheSize > 0 && System.nanoTime() < timeBudgetBound);
//			DEBUGStorage.println(this.channelIndex + " checked " + DEBUG_checked + ", usedCacheSize = " + this.usedCacheSize);
//			DEBUGStorage.println(this.channelIndex + " done live checking (used cache = " + this.usedCacheSize + ") Live cursor = " + this.liveCursor + ", time left = " + (timeBudgetBound - System.nanoTime()));

			// loop aborted, update live cursor and return result
			if(this.usedCacheSize == 0)
			{
				this.resetLiveCursor();
//				DEBUGStorage.println(this.channelIndex + " completed live check. Timebudget left = " + (timeBudgetBound - System.nanoTime()));
				return true;
			}

			this.liveCursor = entity;
//			DEBUGStorage.println(this.channelIndex + " interrupts live heck. cache size = " + this.usedCacheSize + ". Timebudget left = " + (timeBudgetBound - System.nanoTime()));
			return false;
		}

		// CHECKSTYLE.OFF: FinalParameters: this method is just an outsourced scroll-helper
		static final StorageEntity.Implementation getNextLiveEntity(StorageEntity.Implementation entity)
		{
			while(entity != null && !entity.isLive())
			{
				entity = entity.typeNext;
			}
			return entity;
		}
		// CHECKSTYLE.ON: FinalParameters

		@Override
		public boolean issuedCacheCheck(final long nanoTimeBudget, final StorageEntityCacheEvaluator entityEvaluator)
		{
//			DEBUGStorage.println(this.channelIndex + " issuedCacheCheck until " + nanoTimeBudget + " at " + System.nanoTime());
			return this.internalLiveCheck(
				nanoTimeBudget,
				coalesce(entityEvaluator, this.entityCacheEvaluator)
			);
		}

		@Override
		public final boolean incrementalGarbageCollection(final long timeBudgetBound, final StorageChannel channel)
		{
//			if(!DEBUG_GC_ENABLED)
//			{
//				return true;
//			}

			final boolean doSweep;

			// lock the phase monitor for the complete process of state querying to avoid concurrent changes.
			synchronized(this.gcPhaseMonitor)
			{
				if(this.gcPhaseMonitor.isComplete())
				{
//					DEBUGStorage.println(this.channelIndex + " aborting GC (no new data since last sweep)");
					return true;
				}

//				DEBUGStorage.println(this.channelIndex + " GC with budget of " + (timeBudgetBound - System.nanoTime()));

				if(this.gcPhaseMonitor.isSweepMode())
				{
					if(this.completedSweeping)
					{
						// global sweep mode, but this channel is already done sweeping, so just check/wait for global completion
//						DEBUGStorage.println(this.channelIndex + " waiting for other channels to complete sweeping.");
						return this.gcPhaseMonitor.isSweepPhaseComplete(this.colleagues);
					}
					doSweep = true;
				}
				else
				{
					doSweep = false;
				}
			}

			// perform the actual operation without/outside the phase monitor lock
			return doSweep
				? this.doSweep(timeBudgetBound, channel)
				: this.doMark(timeBudgetBound, channel)
			;
		}


		final synchronized void resetAfterSweep()
		{
			this.checkOidHashTableConsolidation(); // check for shrink after sweep
			this.resetGraySegments(); // must reset BEFORE gray initializing!
			this.initializeGrayChain();
		}

		private boolean doSweep(final long timeBudgetBound, final StorageChannel channel)
		{
//			DEBUGStorage.println(this.channelIndex + " sweeping...");
//			final long tStart = System.nanoTime();
			if(this.incrementalSweep(timeBudgetBound, channel))
			{
//				final long tStop = System.nanoTime();
//				DEBUGStorage.println(this.channelIndex + " sweeping completed.");
//				System.out.println(new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
				return true;
			}

//			final long tStop = System.nanoTime();
//			DEBUGStorage.println(this.channelIndex + " sweeping adjourned.");
//			System.out.println(new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			return false;
		}

		private boolean doMark(final long timeBudgetBound, final StorageChannel channel)
		{
//			DEBUGStorage.println(this.channelIndex + " marking....");
			if(this.incrementalMark(timeBudgetBound))
			{
//				DEBUGStorage.println(this.channelIndex + " marking completed.");
				return true;
			}
//			DEBUGStorage.println(this.channelIndex + " marking adjourned.");
			return false;
		}

		@Override
		public final boolean issuedGarbageCollection(final long nanoTimeBudgetBound, final StorageChannel channel)
		{
//			if(!DEBUG_GC_ENABLED)
//			{
//				return true;
//			}

			while(true)
			{
				// call gc for the given time budget and evaluate result
//				DEBUGStorage.println(this.hashIndex() + " issued gc (" + this.debugGcState() + ")");
				if(this.incrementalGarbageCollection(nanoTimeBudgetBound, channel))
				{
					// check for completion. Also aborts unnecessary gc issuing very quickly.
					if(this.gcPhaseMonitor.isComplete())
					{
						// garbage collection complete, at least for this channel, others will complete their sweeping.
						return true;
					}
//					DEBUGStorage.println(this.hashIndex() + " continue gc (" + this.debugGcState() + ")");
				}

				// has to be checked BEFORE the sleep in case the gc call returned due to expired time budget
				if(System.nanoTime() >= nanoTimeBudgetBound)
				{
					// time ran out
					return false;
				}

				/*
				 * If there is still enough time, but gc call returned false, it means that this channel has
				 * currently no work but is waiting for others to complete their phase.
				 * So it would burn all CPU time in the loop with active waiting for its colleagues to catch up.
				 * So in this case, a small wait is necessary.
				 */
//				DEBUGStorage.println(this.channelIndex() + " waiting due to (" + this.debugGcState() + ")");
				try
				{
					/* (16.04.2016)FIXME: possible GC race condition reason.
					 * a proper wait-notify structure has to be done here, not some clumsy wait
					 */
					// CHECKSTYLE.OFF: MagicNumber: must be replaced anyway
					Thread.sleep(10);
					// CHECKSTYLE.ON: MagicNumber
				}
				catch(final InterruptedException e)
				{
					// thread got interrupted while waiting for other threads to proceed, so abort unfinished
					return false;
				}
			}
		}

		/* must be synchronized to avoid inconsistencies if one thread wants to mark while
		 * another is currently rebuilding the hashtable
		 */
		@Override
		public final synchronized void markGray(final long objectId)
		{
			/* Note:
			 * If an entry has not been created yet and thus is missed here, it is no big deal.
			 * Newly created entries are marked as gray initially, meaning they get to live
			 * until the next sweep in any case and may get collected in the next cycle.
			 */
			for(StorageEntity.Implementation e = this.getOidHashChainHead(objectId); e != null; e = e.hashNext)
			{
				if(e.objectId() == objectId)
				{
					this.promoteGray(e);
					return;
				}
			}
		}

		final void markGrayIfNonNullReferences(final StorageEntity.Implementation rootEntity)
		{
			// not sure if this is extremely elegant or hacky. Anyway, it's strictly local, so perfectly safe
			try
			{
				// iterate references and break on first non-null value (see #accept())
				rootEntity.iterateReferenceIds(BREAK_ON_NONNULL_REFERENCE_VALUE);
				// iterated all reference and found no non-null value, so return leaving the root entity doomed for GC.
			}
			catch(final ThrowBreak b)
			{
				// break signals found non-null value, so mark gray
				this.initializeRootGray(rootEntity);
			}
		}

		// this class only exists to shorten the type length in the class' field list. True story :).
		final class GrayInitializer implements ThrowingProcedure<StorageEntity.Implementation, RuntimeException>
		{
			@Override
			public void accept(final StorageEntity.Implementation e) throws RuntimeException
			{
				Implementation.this.markGrayIfNonNullReferences(e);
			}
		}

//		private void DEBUG_PRINT_OID_HASH_VALUES()
//		{
//			final VarString vs = VarString.New(
//				this.channelIndex + " oid size = " + this.oidSize + ", hash length = " + this.oidHashTable.length
//			);
//			int nonNullCount = 0, nullCount = 0;
//			final _intList nullIndices = new _intList();
//			for(int i = 0; i < this.oidHashTable.length; i++)
//			{
//				if(this.oidHashTable[i] == null)
//				{
//					nullCount++;
//					nullIndices.add(i);
//				}
//				else
//				{
//					nonNullCount++;
//				}
//			}
//			vs.add(", nonNull count = " + nonNullCount + ", null count = " + nullCount);
//			DEBUGStorage.println(vs.toString());
//		}

	}



	static final class MaxObjectId implements ThrowingProcedure<StorageEntity, RuntimeException>
	{
		private long maxObjectId;

		public final MaxObjectId reset()
		{
			this.maxObjectId = 0;
			return this;
		}

		@Override
		public final void accept(final StorageEntity e)
		{
			if(e.objectId() >= this.maxObjectId)
			{
				this.maxObjectId = e.objectId();
			}
		}

		public final long yield()
		{
			return this.maxObjectId;
		}

	}

	static final class MinObjectId implements ThrowingProcedure<StorageEntity, RuntimeException>
	{
		private long minObjectId;

		public final MinObjectId reset()
		{
			this.minObjectId = Long.MAX_VALUE;
			return this;
		}

		@Override
		public final void accept(final StorageEntity e)
		{
			if(e.objectId() < this.minObjectId)
			{
				this.minObjectId = e.objectId();
			}
		}

		public final long yield()
		{
			return this.minObjectId == Long.MAX_VALUE ? 0 : this.minObjectId;
		}

	}

	static final class GcPhaseMonitor implements _longProcedure
	{
		private boolean isSweepMode;

		/*
		 * Indicates that no new data (store) has been received since the last sweep.
		 * This basically means that no more gc marking or sweeping is necessary, however as stored entities
		 * (both newly created and updated) are forced gray, potentially any number of entities can be
		 * virtually doomed but still be kept alive. Those will only be found in a second mark and sweep since the
		 * last store.
		 * This flag can be seen as "no new data level 1".
		 */
		private boolean gcHotPhaseComplete; // sweep once after startup in any case

		/*
		 * Indicates that not only no new data has been received since the last sweep, but also that a second sweep
		 * has already been executed since then, removing all unreachable entities and effectively establishing
		 * a clean / optimized / stable state.
		 * This flag can be seen as "no new data level 2".
		 * It will shut off all GC activity until the next store resets the flags.
		 */
		private boolean gcColdPhaseComplete;

		private boolean gcComplete;


		// (07.07.2016 TM)NOTE: new for oidMarkQueue concept

		private final OidMarkQueue[] oidMarkQueues         = new OidMarkQueue[0];
		private final int            channelCount          = this.oidMarkQueues.length;
		private final int            channelHash           = this.channelCount - 1;
		private       long           pendingMarksCount    ;

		private final boolean[]      sweepBoard            = new boolean[this.channelCount];
		private       int            sweepingChannelsCount;

		final synchronized void enqueue_New(final long oid)
		{
			this.oidMarkQueues[(int)(oid & this.channelHash)].enqueue(oid);
			this.pendingMarksCount++;
		}

		final synchronized void enqueueBulk_New(final long[] oids)
		{
			final OidMarkQueue[] oidMarkQueues = this.oidMarkQueues;
			final int            channelHash   = this.channelHash  ;

			for(final long oid : oids)
			{
				oidMarkQueues[(int)(oid & channelHash)].enqueue(oid);
			}

			this.pendingMarksCount += oids.length;
		}

		final synchronized boolean isMarkingComplete_New()
		{
			return this.pendingMarksCount == 0;
		}

		final synchronized void advanceMarking_New(final OidMarkQueue oidMarkQueue, final int amount)
		{
			if(this.pendingMarksCount < amount)
			{
				throw new RuntimeException(); // (07.07.2016 TM)EXCP: proper exception
			}

			/*
			 * Advance the oidMarkQueue not before the gc phase monitor has been locked and the amount has been validated.
			 * AND while the lock is held. Hence the channel must pass and update its queue instance in here, not outsied.
			 */
			oidMarkQueue.advanceTail(amount);
			this.pendingMarksCount -= amount;
		}

		final synchronized boolean isSweepMode_New()
		{
			return this.sweepingChannelsCount > 0;
		}

		final synchronized void beginSweepMode_New()
		{
			if(this.isSweepMode_New())
			{
				return;
			}

			for(int i = 0; i < this.sweepBoard.length; i++)
			{
				this.sweepBoard[i] = true;
			}
			this.sweepingChannelsCount = this.channelCount;
		}

		final synchronized boolean reportSweepingComplete_New(final StorageEntityCache<?> channel)
		{
			if(this.sweepBoard[channel.channelIndex()])
			{
				this.sweepBoard[channel.channelIndex()] = false;
				this.sweepingChannelsCount--;
			}

			return this.isSweepMode_New();
		}

		@Override
		public final void accept(final long oid)
		{
			// do not enqueue null oids, not even get the lock
			if(oid == Swizzle.nullId())
			{
				return;
			}

			this.enqueue_New(oid);
		}

		// (07.07.2016 TM)NOTE: end of new part



		final synchronized boolean isSweepMode()
		{
			return this.isSweepMode;
		}

		final synchronized void setCompletion()
		{
			this.gcHotPhaseComplete = this.gcColdPhaseComplete = this.gcComplete = true;
		}

		final synchronized void resetCompletion()
		{
			this.gcHotPhaseComplete = this.gcColdPhaseComplete = this.gcComplete = false;
		}

		final synchronized boolean isPartialComplete()
		{
			return this.gcHotPhaseComplete;
		}

		final synchronized boolean isComplete()
		{
			return this.gcComplete;
		}

		final synchronized void reset(final StorageEntityCache.Implementation[] colleagues)
		{
			this.resetGcPhaseState(colleagues);
			this.setCompletion();

			// truncate gray segments AFTER completion has been resetted so that no other channel marks entities again.
			for(final Implementation e : colleagues)
			{
				e.truncateGraySegments();
			}
		}

		final synchronized void resetGcPhaseState(final StorageEntityCache.Implementation[] colleagues)
		{
			if(!this.isSweepMode)
			{
				return;
			}
			this.isSweepMode = false;
			for(int i = 0; i < colleagues.length; i++)
			{
				colleagues[i].completedSweeping = false;
			}
		}

		final synchronized boolean isMarkPhaseComplete(final StorageEntityCache.Implementation[] colleagues)
		{
			// mode is switched only once by the first channel no notice mark phase completion (last to check in)
			if(this.isSweepMode)
			{
				return true;
			}

			/* note on tricky concurrency:
			 * because a lock on this instance is required to complete the gray chain (see #advanceGrayChain),
			 * checking all channels while having the lock is guaranteed to not let any gray marking slip through.
			 * Even if another channel is about to process its last gray item and mark an already checked channel in
			 * the process, it cannot complete its gray chain until this channel releases the lock.
			 * Hence, this channel will see the other channel as still having a gray item (its last one) and therefore
			 * will return false (meaning mark phase not complete).
			 * If it really was the last gray item of all channels, it will get recognized properly in the next check,
			 * when all channel will have nextGray == null and nothing more gets enqueued.
			 */
			for(int i = 0; i < colleagues.length; i++)
			{
				if(colleagues[i].isMarking())
				{
//					DEBUGStorage.println(i + " not finished mark phase yet.");
					return false;
				}
			}

			for(int i = 0; i < colleagues.length; i++)
			{
				colleagues[i].completedSweeping = false;
//				DEBUGStorage.println(colleagues[i].channelIndex() + " " + colleagues[i].DEBUG_grayCount);
//				colleagues[i].DEBUG_grayCount = 0;

//				synchronized(colleagues[i])
//				{
//					if(colleagues[i].DEBUG_marked < 1500000 && colleagues[i].DEBUG_marked > 0)
//					{
//						DEBUGStorage.println(i + " incomplete marking!");
//						for(int j = 0; j < colleagues.length; j++)
//						{
//							colleagues[j].DEBUG_gcState();
//						}
//						DEBUGStorage.println("x_x");
//					}
//					DEBUGStorage.println(i + " Gray chain processed. Marked " + colleagues[i].DEBUG_marked);
//					colleagues[i].DEBUG_marked = 0;
//				}

			}

			// switch mode (only once, see check above)
			this.isSweepMode = true;

			/*
			 * It is important to set the completion state here (end of mark phase) and not at the end of the sweep
			 * phase.
			 * Rationale:
			 * An entity update (e.g. store, etc.) during a sweep phase sets white entities to initial (light gray).
			 * That mark gets resetted in the next sweep phase.
			 * If the flags were set at the end of the sweep phase, the following might occur:
			 * - store during sweep1 phase
			 * - completion gets resetted
			 * - some entities get marked as initial but not resetted by the sweep as they were already visited
			 * - sweep1 finishes, setting completion1
			 * - sweep2 visits the entities, resets them to white, but does not delete them
			 * - sweep2 finishes, setting completion2
			 * - GC effectively stops until the next store
			 * However some (potentially meanwhile unreachable) entities did not get deleted as sweep2 just
			 * resetted the initial mark but did not delete the entities yet.
			 *
			 * If the completion state setting is located here, an ongoing sweep phase with an intermediate store
			 * won't nullify the store's state resetting.
			 */
			if(this.gcHotPhaseComplete)
			{
//				DEBUGStorage.println("Cold mark phase complete.");
				this.gcColdPhaseComplete = true;
			}
			else
			{
//				DEBUGStorage.println("Hot mark phase complete.");
				this.gcHotPhaseComplete = true;
			}

			return true;
		}

		final synchronized boolean isSweepPhaseComplete(final StorageEntityCache.Implementation[] colleagues)
		{
			// mode is switched only once by the first channel no notice mark phase completion (last to check in)
			if(!this.isSweepMode)
			{
				return true;
			}

			// simple completed-check: sooner or later, every channels completes sweeping without rollback.
			for(int i = 0; i < colleagues.length; i++)
			{
				if(!colleagues[i].completedSweeping)
				{
					return false;
				}
			}

			// switch modes in all channels while under the lock's protection
			for(int i = 0; i < colleagues.length; i++)
			{
				colleagues[i].completedSweeping = false;

				/*
				 * calling this method here is essential because the gray chain must be initialized while the lock
				 * on the gc phase monitor is still hold to guarantee there is at least one gray item
				 * before the next check for completed mark phase.
				 * Calling that complex logic for all channels in one channel thread at this point is
				 * no concurrency problem for the simple reason that this method can only be entered if
				 * all other channels have completed sweeping and wait for the last channel to end the sweep mode,
				 * meaning they aren't doing anything (mutating state) at the moment.
				 */
				colleagues[i].resetAfterSweep();
			}

			// switch mode (only once, see check above)
			this.isSweepMode = false;

//			DEBUGStorage.println("Sweep phase complete");

			/* after a cold mark phase (no new data) has been done, the following sweep will establish a stable
			 * state in which additional marks and sweeps won't cause any change. Hence, the GC can be considered
			 * "complete" (until the next mutation in entities like store or import) and turned off.
			 * This is indicated by the flag set here.
			 */
			if(this.gcColdPhaseComplete)
			{
				this.gcComplete = true;
				DEBUGStorage.println("GC complete");
			}

			return true;
		}

	}

	static final class GrayReferenceMarker implements _longProcedure
	{
		private final StorageEntityCache.Implementation[] entityCaches     ;
		private final int                                 hashChannelModulo;

		GrayReferenceMarker(
			final StorageEntityCache.Implementation[] entityCaches     ,
			final int                                 hashChannelModulo
		)
		{
			super();
			this.entityCaches      = entityCaches     ;
			this.hashChannelModulo = hashChannelModulo;
		}

		@Override
		public final void accept(final long objectId)
		{
			// abort marking of null-oid as soon as possible.
			if(objectId == Swizzle.nullId())
			{
				return;
			}
//			DEBUGStorage.println("marking " + objectId);
			this.entityCaches[Implementation.oidChannelIndex(objectId, this.hashChannelModulo)].markGray(objectId);
		}

	}

	// more or less a specifically implemented ring buffer
	static final class GraySegment
	{
		static final int MAX_SIZE = 500;

		int                            lowIndex ;
		int                            highIndex;
		boolean                        used     ;
		GraySegment                    next     ;
		StorageEntity.Implementation[] entities ;

		GraySegment()
		{
			super();
			this.entities = new StorageEntity.Implementation[MAX_SIZE];
		}

		final GraySegment addSegment()
		{
			final GraySegment newSegment = new GraySegment();
			newSegment.next = this.next;
			this.next = newSegment;
			newSegment.used = true;
			return newSegment;
		}

		final boolean hasElements()
		{
			return this.highIndex > this.lowIndex;
		}

		final boolean put(final StorageEntity.Implementation entity)
		{
			if(this.highIndex >= MAX_SIZE)
			{
				if(this.lowIndex < MAX_SIZE)
				{
					return false;
				}
				this.lowIndex = this.highIndex = 0;
				this.used = true;
			}
			this.entities[this.highIndex++] = entity;
			return true;
		}

		final StorageEntity.Implementation get()
		{
			// (07.07.2016 TM)NOTE: the safety of the low index is guaranteed by preceeding hasElements() calls.
			return this.entities[this.lowIndex];
		}

		final void advanceProcessed()
		{
			// (07.07.2016 TM)NOTE: the safety of the low index is guaranteed by preceeding hasElements() calls.
			this.entities[this.lowIndex++] = null;
		}

		final void clear()
		{
			final StorageEntity.Implementation[] entities = this.entities ;
			final int                            bound    = this.highIndex;

			for(int i = this.lowIndex; i < bound; i++)
			{
				entities[i] = null; // nulling out reference is important, e.g. for proper storage truncation.
			}
			this.lowIndex = this.highIndex = 0;
		}

	}

}
