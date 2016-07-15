package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;
import static net.jadoth.math.JadothMath.log2pow2;
import static net.jadoth.math.JadothMath.notNegative;

import java.nio.ByteBuffer;

import net.jadoth.Jadoth;
import net.jadoth.math.JadothMath;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.ChunksBuffer;
import net.jadoth.swizzling.types.Swizzle;


	public final class StorageEntityCache_Implementation_New implements StorageEntityCache<StorageEntity.Implementation>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int                                 channelIndex         ;
		private final int                                 channelHashModulo    ;
		private final int                                 channelHashShift     ;
		private final long                                rootTypeId           ;
		        final StorageEntityCacheEvaluator         entityCacheEvaluator ;
		private final StorageTypeDictionary               typeDictionary       ;
		private final GcMonitor                           gcMonitor            ;
		private final OidMarkQueue                        oidMarkQueue         ;
		private final long[]                              markingOidBuffer     ;
		private final StorageGCZombieOidHandler           zombieOidHandler     ;
		private final StorageRootOidSelector              rootOidSelector      ;

		// currently only used for entity iteration
		private       StorageFileManager.Implementation   fileManager          ; // pseudo-final

		// cache function pointers because this::method creates a new instance on every call (tested, at least in Java 8).
		private final StorageEntity.MaxObjectId           maxObjectId       = new StorageEntity.MaxObjectId()    ;
		private final StorageEntity.MinObjectId           minObjectId       = new StorageEntity.MinObjectId()    ;

		private       StorageEntity.Implementation[]      oidHashTable     ;
		private       int                                 oidModulo        ; // long modulo makes not difference
		private       long                                oidSize          ;

		private       StorageEntityType.Implementation[]  tidHashTable     ;
		private       int                                 tidModulo        ;
		private       int                                 tidSize          ;

		private final StorageEntityType.Implementation    typeHead         ;
		private       StorageEntityType.Implementation    typeTail         ;
		private       StorageEntityType.Implementation    rootType         ;

		private       StorageEntity.Implementation        liveCursor       ;

		private       long                                usedCacheSize    ;

//		long DEBUG_grayCount = 0;
//		int DEBUG_marked = 0;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public StorageEntityCache_Implementation_New(
			final int                                   channelIndex       ,
			final int                                   channelCount       ,
			final StorageEntityCacheEvaluator           cacheEvaluator     ,
			final StorageTypeDictionary                 typeDictionary     ,
			final GcMonitor                             gcMonitor          ,
			final StorageGCZombieOidHandler             zombieOidHandler   ,
			final StorageRootOidSelector                rootOidSelector    ,
			final long                                  rootTypeId         ,
			final OidMarkQueue                          oidMarkQueue       ,
			final int                                   markingBufferLength
		)
		{
			super();
			this.channelIndex          = notNegative(channelIndex)    ;
			this.rootTypeId            =             rootTypeId       ;
			this.entityCacheEvaluator  = notNull    (cacheEvaluator)  ;
			this.typeDictionary        = notNull    (typeDictionary)  ;
			this.gcMonitor             = notNull    (gcMonitor)       ;
			this.zombieOidHandler      = notNull    (zombieOidHandler);
			this.rootOidSelector       = notNull    (rootOidSelector) ;
			this.channelHashModulo     =             channelCount - 1 ;
			this.channelHashShift      = log2pow2   (channelCount)    ;
			this.oidMarkQueue          = notNull    (oidMarkQueue)    ;
			this.markingOidBuffer      = new long[markingBufferLength];

			this.typeHead              = new StorageEntityType.Implementation(this.channelIndex);

			this.initializeState();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void initializeStorageManager(final StorageFileManager.Implementation fileManager)
		{
			if(this.fileManager != null && this.fileManager != fileManager)
			{
				throw new RuntimeException();
			}
			this.fileManager = fileManager;
		}

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
		}

		final void clearState()
		{
			// must lock independently of gcPhaseMonitor to avoid deadlock!
			this.resetState();
		}

		private void resetLiveCursor()
		{
			this.liveCursor = this.typeHead.head;
		}

		// must use lock to keep other channels from marking while rebuild is in progress
		private void enlargeOidHashTable()
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

		private void checkOidHashTableConsolidation()
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

		private void rebuildTidHashTable()
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

		final void resetGarbageCollectionCompletionForEntityUpdate()
		{
			synchronized(this.gcMonitor)
			{
				this.gcMonitor.signalPendingStoreUpdate(this);
				this.gcMonitor.resetCompletion();
			}

		}



		@Override
		public final synchronized long queryRootObjectId()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME StorageEntityCache_Implementation_New#queryRootObjectId()
		}

		@Override
		public final synchronized long getHighestRootInstanceObjectId()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final synchronized long getLowestRootInstanceObjectId()
		{
			throw new UnsupportedOperationException();
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
			final StorageEntityType.Implementation type
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
				this.updatePutEntity(entry);
				return entry;
			}

//			DEBUGStorage.println("creating " + BinaryPersistence.getEntityObjectId(entityAddress) + ", " + BinaryPersistence.getEntityTypeId(entityAddress) + ", [" + BinaryPersistence.getEntityLength(entityAddress) + "]");
			return this.createEntity(objectId, type);
		}

		final StorageEntity.Implementation updatePutEntity(final long entityAddress)
		{
			// ensure (lookup or create) complete entity item for storing
//			DEBUGStorage.println("looking for " + BinaryPersistence.getEntityObjectId(entityAddress));
			final StorageEntity.Implementation entry;
			if((entry = this.getEntry(BinaryPersistence.getEntityObjectId(entityAddress))) != null)
			{
//				DEBUGStorage.println("updating entry " + entry);
				// same as updatePutEntity() but without the retro-reference-marking
				this.updatePutEntity(entry);
				return entry;
			}

//			DEBUGStorage.println("creating " + BinaryPersistence.getEntityObjectId(entityAddress) + ", " + BinaryPersistence.getEntityTypeId(entityAddress) + ", [" + BinaryPersistence.getEntityLength(entityAddress) + "]");
			return this.createEntity(
				BinaryPersistence.getEntityObjectId(entityAddress),
				this.getType(BinaryPersistence.getEntityTypeId(entityAddress))
			);
		}

		private void updatePutEntity(final StorageEntity.Implementation entry)
		{
			// ensure the old data is not cached any longer
			this.ensureNoCachedData(entry);
			this.ensureGray(entry);
			entry.detachFromFile();
		}


		/**
		 * This purpose of this method is to handle the tricky interaction between data updates (stores/imports) and GC.
		 * When new data comes in, it has to be ensured that the updated entities' references are revisited.
		 * See "doomed kept alive" and "slipped through" cases.
		 * This means an entity with references that is already black (saved from sweep and has its references iterated)
		 * must be demoted to gray (saved from sweep but references not handled yet) and have its OID enqueued in the gray chain.
		 *
		 * If the entity has no reference, it can be marked black right away. This either anticipates/replaces the black marking
		 * by the GC and should actually not be necessary, however as the effort to do it at this point is rather minimal, it's done
		 * nonetheless.
		 *
		 * @param entry
		 */
		private void ensureGray(final StorageEntity.Implementation entry)
		{
			if(!entry.hasReferences())
			{
				entry.markBlack();
				return;
			}

			entry.markGray();
			this.oidMarkQueue.enqueueAndNotify(entry.objectId());
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
			final StorageEntityType.Implementation type
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

			this.ensureGray(entity);

			return entity;
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

		private void sweep()
		{
//			DEBUGStorage.println(this.channelIndex + " sweeping a little (" + (timeBudgetBound - System.nanoTime()) + ")");
//			int DEBUG_safed = 0, DEBUG_collected = 0;
//			final long DEBUG_starttime = System.nanoTime();

			final StorageEntityType.Implementation typeHead = this.typeHead;

			for(StorageEntityType.Implementation sweepType = typeHead; (sweepType = sweepType.next) != typeHead;)
			{
				// get next item and check for end of type (switch to next type required)
				for(StorageEntity.Implementation item, last = sweepType.head; (item = last.typeNext) != null;)
				{
					// actual sweep: white entities are deleted, non-white entities are marked white but not deleted
					if(item.isGcMarked())
					{
//						DEBUGStorage.println("Saving " + item);
						(last = item).markWhite(); // reset to white and advance one item
//						DEBUG_safed++;
					}
					else
					{
						// otherwise white entity, so collect it
//						DEBUGStorage.println("Collecting " + item.objectId() + " (" + item.type.type.typeHandler().typeId() + " " + item.type.type.typeHandler().typeName() + ")");
						this.deleteEntity(item, sweepType, last);
						// decrement entity count (strictly only once per remove as guaranteed by check above)
//						DEBUG_collected++;
						/* even if another thread concurrently grays the item, it can only cause a single
						 * "pre-death" grace cycle as the item is no longer reachable from now on for future marking.
						 */
					}
				}
			}

			// reset file cleanup cursor to first file in order to ensure the cleanup checks all files for the current state.
			this.fileManager.resetFileCleanupCursor();
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

		/**
		 * Returns <code>true</code> if there are no more oids to mark and <code>false</code> if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 *
		 * @param timeBudgetBound
		 * @return
		 */
		private boolean incrementalMark(final long timeBudgetBound)
		{
			final long         evalTime = System.currentTimeMillis();
			final GcMonitor    gc       = this.gcMonitor            ;
			final OidMarkQueue q        = this.oidMarkQueue         ;
			final long[]       oids     = this.markingOidBuffer     ;

			int oidsToMarkCount = 0;
			int oidsIndex       = 0;

			// mark at least one entity, even if there no time, to avoid starvation
			do
			{
				// fetch next batch of oids to mark and advance gray queue if necessary
				if(oidsIndex >= oidsToMarkCount)
				{
					if(oidsIndex > 0)
					{
						// must advance via central gc monitor to update the total pending mark count
						gc.advanceMarking_New(q, oidsIndex);
					}

					// reset oids index and fetch next batch
					oidsIndex = 0;
					if((oidsToMarkCount = q.getNext(oids)) == 0)
					{
						// ran out of work before time ran out. So return true.
						return true;
					}
				}

				// get the entry for the current oid to be marked
				final StorageEntity.Implementation entry = this.getEntry(oids[oidsIndex++]);

				if(entry == null)
				{
					if(this.zombieOidHandler.ignoreZombieOid(oids[oidsIndex - 1]))
					{
						continue;
					}
					// (29.07.2014 TM)EXCP: proper exception
					throw new RuntimeException("Zombie OID " + oids[oidsIndex - 1]);
				}

				// if the entry is already marked black (was redundantly enqueued), skip it and continue to the next
				if(entry.isGcBlack())
				{
					continue;
				}

				// note: iterateReferenceIds already checks for references and returns false if none are present

				// enqueue all reference ids in the mark queue via the central gc monitor instance to account for channel concurrency
//				DEBUGStorage.println(this.channelIndex + " marking references of " + current.objectId() + " with cache size " + this.usedCacheSize);
				if(entry.iterateReferenceIds(gc))
				{
					// must check for clearing the cache again if marking required loading
					this.checkForCacheClear(entry, evalTime);
				}


				// note: if the cached data was already present, to NOT touch, otherwise it might never time out

				// the entry has been fully processed (either has no references or got all its references gray-enqueued), so mark black.
				entry.markBlack();

//				DEBUGStorage.println(this.channelIndex + " marked " + current);
//				DEBUG_marked++;
			}
			while(System.nanoTime() < timeBudgetBound);

			// important: if time ran out, the last batch of processed oids has to be accounted for in the gray queue
			if(oidsIndex > 0)
			{
				gc.advanceMarking_New(q, oidsIndex);
			}

//			DEBUGStorage.println(this.channelIndex + " incrementally marked " + DEBUG_marked);

			// time ran out, return false.
			return false;
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
//			final long validRootsId = this.validRootIdCalculator.determineValidRootId(this.colleagues);
//			this.rootType.removeAll(this.rootsDeleter.setValidRootId(validRootsId));

			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME WIP.Implementation#enclosing_method()
		}



		final void internalUpdateEntities(
			final ByteBuffer                     chunk               ,
			final long                           chunkStoragePosition,
			final StorageDataFile.Implementation file
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
				this.updatePutEntity(adr)
				.updateStorageInformation(
					Jadoth.checkArrayRange(BinaryPersistence.getEntityLength(adr)),
					file,
					Jadoth.to_int(storageBackset + adr)
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
			for(int i = 0; i < chunks.length; i++)
			{
				this.internalUpdateEntities(chunks[i], chunksStoragePositions[i], dataFile);
			}

			// must be done by the store task's cleanup, but as it is idempotent, call it here right away
			this.cleanupPendingStoreUpdate();
		}

		final void cleanupPendingStoreUpdate()
		{
			// (14.07.2016 TM)NOTE: dummy stub to prepare for new StorageEntityCache implementation
			this.gcMonitor.clearPendingStoreUpdate(this);
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
				Jadoth.coalesce(entityEvaluator, this.entityCacheEvaluator)
			);
		}

		/**
		 * Returns <code>true</code> if there are no more oids to mark and <code>false</code> if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		@Override
		public final boolean incrementalGarbageCollection(final long timeBudgetBound, final StorageChannel channel)
		{
//			if(!DEBUG_GC_ENABLED)
//			{
//				return true;
//			}

			if(this.gcMonitor.isComplete(this))
			{
				// minimize hash table memory consumption if storage is potentially going to be inactive
				this.checkOidHashTableConsolidation();
				return true;
			}

			if(this.gcMonitor.needsSweep(this))
			{
				this.sweep();

				if(System.nanoTime() >= timeBudgetBound)
				{
					return false;
				}
			}

			if(this.incrementalMark(timeBudgetBound))
			{
				return true;
			}

			return false;
		}


		/**
		 * Returns <code>true</code> if there are no more oids to mark and <code>false</code> if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		@Override
		public final boolean issuedGarbageCollection(final long nanoTimeBudgetBound, final StorageChannel channel)
		{
//			if(!DEBUG_GC_ENABLED)
//			{
//				return true;
//			}

//			DEBUGStorage.println(this.hashIndex() + " issued gc (" + this.debugGcState() + ")");

			// check time budget first for explicitly issued calls.
			performGC:
			while(System.nanoTime() < nanoTimeBudgetBound)
			{
				// call gc for the given time budget and evaluate result
				if(!this.incrementalGarbageCollection(nanoTimeBudgetBound, channel))
				{
					// if the call returned indicating that time ran out, return accordingly immediately.
					return false;
				}
				// reaching here means the gc loop ran out of work before it ran out of time. Check why.


				/*
				 * This is a tricky wait loop:
				 * In the gc phase monitor implementation, first a lock on the monitor itself is obtained and then on the mark queue.
				 * Meaning the code here can't just lock the mark queue and then lock the monitor to check for completion inside the loop.
				 * This would cause a deadlock.
				 *
				 * So the loop has to be split:
				 * The loop only checks for completion and time budget.
				 * Inside the loop, a simple if checks for newly fed oids (queue elements) to be processed (= new work)
				 * If there aren't any, a tiny wait on the queue is performed.
				 *
				 * Note: this is a defense against spurious wakeups, however slightly complicated due to the deadlock matter:
				 * On any wakeup, a check for time and completion is perf
				 *
				 */
				waitForWork:
				while(System.nanoTime() < nanoTimeBudgetBound)
				{
					// check for completion on every attempt to wait for new work
					if(this.gcMonitor.isComplete(this))
					{
						return true;
					}

					// check/wait for missing oids to mark, which have to be provided by other channels' marking.
					synchronized(this.oidMarkQueue)
					{
						// if the mark queue is empty and there is still time, wait for new
						if(this.oidMarkQueue.hasElements())
						{
							break waitForWork;
						}

						try
						{
							this.oidMarkQueue.wait(10);
						}
						catch(final InterruptedException e)
						{
							// thread has been interrupted while trying to perform garbage collection. So abort and return.
							break performGC;
						}
					}
					// end of waiting, perform checks again
				}
				// end of waitForWork, continue performGC
			}
			// end of performGC

			// either time ran out or thread was interrupted. In any case, report back the current state of the garbage collection.
			return this.gcMonitor.isComplete(this);
		}



		@Deprecated
		@Override
		public void markGray(final long objectId)
		{
			// (14.07.2016 TM)TODO: remove markGray() after switch to new implementation
			throw new UnsupportedOperationException();
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