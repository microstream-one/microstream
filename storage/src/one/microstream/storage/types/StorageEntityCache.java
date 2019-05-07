package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.log2pow2;
import static one.microstream.math.XMath.notNegative;
import static one.microstream.math.XMath.positive;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.functional.ThrowingProcedure;
import one.microstream.math.XMath;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.typing.XTypes;


public interface StorageEntityCache<I extends StorageEntityCacheItem<I>> extends StorageHashChannelPart
{
	public StorageTypeDictionary typeDictionary();

	public StorageEntityType<I> lookupType(long typeId);

	public boolean incrementalLiveCheck(long timeBudgetBound);

	public boolean incrementalGarbageCollection(long timeBudgetBound, StorageChannel channel);

	public boolean issuedGarbageCollection(long nanoTimeBudget, StorageChannel channel);

	public boolean issuedCacheCheck(long nanoTimeBudget, StorageEntityCacheEvaluator entityEvaluator);

	public void copyRoots(ChunksBuffer dataCollector);

	public long cacheSize();



	public final class Implementation
	implements StorageEntityCache<StorageEntity.Implementation>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		// (24.11.2017 TM)TODO: there seems to still be a GC race condition bug, albeit only very rarely.
		private static final boolean DEBUG_GC_ENABLED = false;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int                                 channelIndex        ;
		private final int                                 channelHashModulo   ;
		private final int                                 channelHashShift    ;
		private final long                                rootTypeId          ;
		private final long                                markingWaitTimeMs   ;
		        final StorageEntityCacheEvaluator         entityCacheEvaluator;
		private final StorageTypeDictionary               typeDictionary      ;
		private final StorageEntityMarkMonitor            markMonitor         ;
		private final StorageReferenceMarker              referenceMarker     ;
		private final StorageOidMarkQueue                 oidMarkQueue        ;
		private final long[]                              markingOidBuffer    ;
		private final StorageGCZombieOidHandler           zombieOidHandler    ;
		private final StorageRootOidSelector              rootOidSelector     ;
		private final RootEntityRootOidSelectionIterator  rootEntityIterator  ;

		// currently only used for entity iteration
		private       StorageFileManager.Implementation   fileManager         ; // pseudo-final

		private       StorageEntity.Implementation[]      oidHashTable        ;
		private       int                                 oidModulo           ; // long modulo makes not difference
		private       long                                oidSize             ;

		private       StorageEntityType.Implementation[]  tidHashTable        ;
		private       int                                 tidModulo           ;
		private       int                                 tidSize             ;

		private final StorageEntityType.Implementation    typeHead            ;
		private       StorageEntityType.Implementation    typeTail            ;
		private       StorageEntityType.Implementation    rootType            ;

		private       StorageEntity.Implementation        liveCursor          ;

		private       long                                usedCacheSize       ;
		private       boolean                             hasUpdatePendingSweep;

		// Statistics for debugging / monitoring / checking to compare with other channels and with the markmonitor
		private       long                                sweepGeneration     ;
		private       long                                lastSweepStart      ;
		private       long                                lastSweepEnd        ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final int                                 channelIndex         ,
			final int                                 channelCount         ,
			final StorageEntityCacheEvaluator         cacheEvaluator       ,
			final StorageTypeDictionary               typeDictionary       ,
			final StorageEntityMarkMonitor            markMonitor          ,
			final StorageGCZombieOidHandler           zombieOidHandler     ,
			final StorageRootOidSelector              rootOidSelector      ,
			final long                                rootTypeId           ,
			final StorageOidMarkQueue                 oidMarkQueue         ,
			final int                                 markingBufferLength  ,
			final long                                markingWaitTimeMs
		)
		{
			super();
			this.channelIndex          = notNegative(channelIndex)    ;
			this.rootTypeId            =             rootTypeId       ;
			this.entityCacheEvaluator  = notNull    (cacheEvaluator)  ;
			this.typeDictionary        = notNull    (typeDictionary)  ;
			this.markMonitor           = notNull    (markMonitor)     ;
			this.zombieOidHandler      = notNull    (zombieOidHandler);
			this.rootOidSelector       = notNull    (rootOidSelector) ;
			this.channelHashModulo     =             channelCount - 1 ;
			this.channelHashShift      = log2pow2   (channelCount)    ;
			this.oidMarkQueue          = notNull    (oidMarkQueue)    ;
			this.markingWaitTimeMs     = positive  (markingWaitTimeMs);
			this.markingOidBuffer      = new long[markingBufferLength];
			this.rootEntityIterator    = new RootEntityRootOidSelectionIterator(rootOidSelector);
			this.typeHead              = new StorageEntityType.Implementation(this.channelIndex);
			this.initializeState();
			this.referenceMarker       = markMonitor.provideReferenceMarker(this);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		final long sweepGeneration()
		{
			return this.sweepGeneration;
		}

		final long lastSweepStart()
		{
			return this.lastSweepStart;
		}

		final long lastSweepEnd()
		{
			return this.lastSweepEnd;
		}

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
			// live cursor may never be a head dummy-entity (but it may be a tail entity as this is checked)
			this.liveCursor = null;

			// (22.09.2016 TM)NOTE: old
//			this.liveCursor = this.typeHead.head;
		}

		// must use lock to keep other channels from marking while rebuild is in progress
		private void enlargeOidHashTable()
		{
			final int newModulo;
			final StorageEntity.Implementation[] newSlots =
				XMath.isGreaterThanOrEqualHighestPowerOf2(this.oidHashTable.length)
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
			final int                            newModulo = XMath.pow2BoundMaxed((int)this.oidSize) - 1;
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
			
			final StorageEntityTypeHandler typeHandler = this.typeDictionary.lookupTypeHandlerChecked(typeId);

			// explicit hash index for debug purposes. Creating types is not performance critical.
			final int hashIndex = tidHashIndex(typeId, this.tidModulo);

			// create and register
			final StorageEntityType.Implementation type = new StorageEntityType.Implementation(
				this.channelIndex,
				typeHandler,
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

		final void registerPendingStoreUpdate()
		{
			synchronized(this.markMonitor)
			{
				this.markMonitor.signalPendingStoreUpdate(this);
				this.markMonitor.resetCompletion();
			}
		}



		final long queryRootObjectId()
		{
			this.rootOidSelector.reset();
			this.rootType.iterateEntities(this.rootEntityIterator);
			return this.rootOidSelector.yield();
		}

		static final class RootEntityRootOidSelectionIterator
		implements ThrowingProcedure<StorageEntity.Implementation, RuntimeException>
		{
			final StorageRootOidSelector rootOidSelector;

			public RootEntityRootOidSelectionIterator(final StorageRootOidSelector rootOidSelector)
			{
				super();
				this.rootOidSelector = rootOidSelector;
			}

			@Override
			public void accept(final StorageEntity.Implementation e) throws RuntimeException
			{
				this.rootOidSelector.accept(e.objectId());
			}

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
			Persistence.validateObjectId(objectId);

			// validate channel for object Id
			if(this.oidChannelIndex(objectId) != this.channelIndex)
			{
				// (05.05.2014)EXCP: proper exception
				throw new RuntimeException("Invalid objectId " + objectId + " for hash channel " + this.channelIndex);
			}
		}
		
		final StorageIdAnalysis validateEntities()
		{
			long maxTid = 0, maxOid = 0, maxCid = 0;
			
			final EqHashEnum<Long> occuringTypeIds = EqHashEnum.New();

			// validate all entities via iteration by type. Simplifies debugging and requires less type pointer chasing
			for(StorageEntityType.Implementation type : this.tidHashTable)
			{
				while(type != null)
				{
					if(!type.isEmpty())
					{
						occuringTypeIds.add(type.typeId);
					}
					
					final StorageIdAnalysis idAnalysis = type.validateEntities();
					type = type.hashNext;

					final Long typeMaxTid = idAnalysis.highestIdsPerType().get(Persistence.IdType.TID);
					if(typeMaxTid != null && typeMaxTid >= maxTid)
					{
						maxTid = typeMaxTid;
					}

					final Long typeMaxOid = idAnalysis.highestIdsPerType().get(Persistence.IdType.OID);
					if(typeMaxOid != null && typeMaxOid >= maxOid)
					{
						maxOid = typeMaxOid;
					}

					final Long typeMaxCid = idAnalysis.highestIdsPerType().get(Persistence.IdType.CID);
					if(typeMaxCid != null && typeMaxCid >= maxCid)
					{
						maxCid = typeMaxCid;
					}
				}
			}

			return StorageIdAnalysis.New(maxTid, maxOid, maxCid, occuringTypeIds);
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

		final StorageEntity.Implementation putEntity(final long objectId, final StorageEntityType.Implementation type)
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
//			DEBUGStorage.println("looking for " + Binary.getEntityObjectId(entityAddress));
			final StorageEntity.Implementation entry;
			if((entry = this.getEntry(objectId)) != null)
			{
//				DEBUGStorage.println("updating entry " + entry);
				this.resetExistingEntityForUpdate(entry);
				return entry;
			}

//			DEBUGStorage.println("creating " + Binary.getEntityObjectId(entityAddress) + ", " + Binary.getEntityTypeId(entityAddress) + ", [" + Binary.getEntityLength(entityAddress) + "]");
			return this.createEntity(objectId, type);
		}

		final StorageEntity.Implementation putEntity(final long entityAddress)
		{
			/* (11.02.2019 TM)NOTE: On byte order switching:
			 * Theoreticaly, the storage engine (OGS) could also use the switchByteOrder mechanism implemented for
			 * communication (OGC). However, there are a lot stumbling blocks in the details that are currently not
			 * worth resolving for a feature that is most probably never required in the foreseeable future.
			 * This method is one of them. Instead of reading the raw value, additional work (performance loss)
			 * would have to be done to check if byte reversal is necessary. For now, this method here alons kills
			 * the byte order switching for storage usage. Should the need arise in the future, additional
			 * time can be invested to solve this.
			 */
			
//			DEBUGStorage.println("looking for " + Binary.getEntityObjectId(entityAddress));
			final StorageEntity.Implementation entry;
			if((entry = this.getEntry(Binary.getEntityObjectIdRawValue(entityAddress))) != null)
			{
//				DEBUGStorage.println("updating entry " + entry);
				this.resetExistingEntityForUpdate(entry);
				return entry;
			}

//			DEBUGStorage.println("creating " + Binary.getEntityObjectId(entityAddress) + ", " + Binary.getEntityTypeId(entityAddress) + ", [" + Binary.getEntityLength(entityAddress) + "]");
	
			/* the added try-catch showed no change in performance in a test.
			 * loading ~25 million entities took from 32 to 75 seconds and depends overwhelmingly on
			 * the state of the disc cache and other OS disturbances and hardly on the actual program code runtime.
			 * 
			 */
			try
			{
				return this.createEntity(
					Binary.getEntityObjectIdRawValue(entityAddress),
					this.getType(Binary.getEntityTypeIdRawValue(entityAddress))
				);
			}
			catch(final Exception e)
			{
				throw new StorageException(
					"Exception while creating entity ["
					+ Binary.getEntityLengthRawValue(entityAddress) + "]["
					+ Binary.getEntityTypeIdRawValue(entityAddress) + "]["
					+ Binary.getEntityObjectIdRawValue(entityAddress) + "]"
					, e
				);
			}

		}
					
		final StorageEntity.Implementation initialCreateEntity(final long entityAddress)
		{
			final StorageEntity.Implementation entity = this.createEntity(
				Binary.getEntityObjectIdRawValue(entityAddress),
				this.getType(Binary.getEntityTypeIdRawValue(entityAddress))
			);
			
			return entity;
		}

		private void resetExistingEntityForUpdate(final StorageEntity.Implementation entry)
		{
			// ensure the old data is not cached any longer
			this.ensureNoCachedData(entry);
//			this.markEntityForChangedData(entry);
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
		private void markEntityForChangedData(final StorageEntity.Implementation entry)
		{
			/*
			 * (01.08.2016 TM)NOTE:
			 * Having a sweep pending when data changes requires a distinction here to achieve correct behavior:
			 * A pending sweep means the marking phase of a gc cycle is complete and all reachable entities
			 * have already been marked.
			 * The data change did reset the GC completion state.
			 * The following sweep will complete the hot phase.
			 * Enqueing entities with references here would mean to have them safed in the next marking phase
			 * (potentially a final/cold gc sweep) even if they are not reachable at all. This would be an error.
			 * Their references do not have to be checked anyway because the marking has been completed beforehand
			 * and the purpose of re-marking references is not to safe some priorly unreachable entity by a race
			 * conditional store. It is only to prevent "slipped through" cases during live marking.
			 * Entities have, however, to be safed in the coming sweep because they might be new or referenced
			 * by an entity in the same store and have not been marked in the conventional way.
			 *
			 * In short:
			 * Changing data with a pending sweep (marking completed):
			 * - Mark everything black, no gray enqueing
			 *
			 * Changing data during incomplte marking:
			 * - Mark non-referential entities black
			 * - Mark referential entities gray (actually white would suffice) and enqueue them into the gray chain.
			 */
			if(this.hasUpdatePendingSweep)
			{
				if(entry.isGcBlack())
				{
					return;
				}

//				this.DEBUG_marked++;
				entry.markBlack();
				return;
			}


			// entities with references
			if(entry.hasReferences())
			{
//				if(entry.isGcBlack())
//				{
//					this.DEBUG_marked--;
//				}

				/*
				 * The gray state is still required even despite the oidMarkQueue
				 *
				 * Consider the following scenario of 4 threads:
				 * - Some (random) channels initiates a sweep (all channels get their sweep flag set)
				 * - channel #2 and #3 perform the sweep
				 * - a store task comes in to be processed
				 * - channel #0 and #1 see the task and do not sweep
				 * - the store task gets processed by all channels
				 * - all channels gray-mark in their entity update the stored entities and enqueue their OIDs to be marked
				 * - the task processing is completed
				 * - ch #0 and #1 continue doing housekeeping, see the sweep flag and perform their pending sweep.
				 * - ch #0 and #1 check gray entities in the sweep and (correctly!) rescue them as they are not white.
				 * - ch #0 and #1 complete their sweep and continue with marking, inluding marking the enqueued stored entities
				 *
				 * The gray state might be superfluous if entities are not gray marked if a sweep is pending,
				 * but this might complicate things and maybe there are other scenarios that wouldn't be covered
				 * correctly anymore. In the very least, the gray state is a safety net of indicating:
				 * The entity must not be collected, but it must be revisted.
				 */
				entry.markGray();

				// must mark via mark monitor to keep central mark count consistent. NEVER directly via the queue!
				this.markMonitor.enqueue(this.oidMarkQueue, entry.objectId());
				return;
			}

//			if(!entry.isGcBlack())
//			{
//				this.DEBUG_marked++;
//			}

			// entities without references
			entry.markBlack();
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

			// (17.11.2016 TM)NOTE: moved outside
//			this.markEntityForChangedData(entity);

			// must explicitly touch the entity to overwrite initial timestamp
			entity.touch();

			return entity;
		}

		final void deleteEntity(
			final StorageEntity.Implementation     entity        ,
			final StorageEntityType.Implementation type          ,
			final StorageEntity.Implementation     previousInType
		)
		{
//			DEBUGStorage.println(this.channelIndex + " deleting " + entity.objectId() + " " + entity.typeInFile.type.typeHandler().typeName());

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

		private void advanceMarking(final int oidsCount)
		{
			// it is crucial to enqueue cached references effectively before updating the pending marks count.
			this.referenceMarker.tryFlush();

			// must advance via central gc monitor to update the total pending mark count (0-case ignored).
			this.markMonitor.advanceMarking(this.oidMarkQueue, oidsCount);
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
//			DEBUGStorage.println(this.channelIndex + " marking ... (" + (timeBudgetBound - System.nanoTime()) + ")");

			final long                   evalTime        = System.currentTimeMillis();
			final StorageReferenceMarker referenceMarker = this.referenceMarker      ;
			final StorageOidMarkQueue    oidMarkQueue    = this.oidMarkQueue         ;
			final long[]                 oidsBuffer      = this.markingOidBuffer     ;

			// total amount of oids to mark in the current batch. Range: [0; oids.length]
			int oidsMarkAmount = 0;

			// index of next oid to be marked (and current amount of already marked oids). Range: [0; oidsMarkAmount]
			int oidsMarkIndex  = 0;

			// mark at least one entity, even if there no time, to avoid starvation
			do
			{
				// fetch next batch of oids to mark and advance gray queue if necessary
				if(oidsMarkIndex >= oidsMarkAmount)
				{
					// an incremented index always equals an element count
					this.advanceMarking(oidsMarkIndex);

					// reset oids index and fetch next batch
					oidsMarkIndex = 0;
					if((oidsMarkAmount = oidMarkQueue.getNext(oidsBuffer)) == 0)
					{
						// ran out of work before time ran out. So return true.
						return true;
					}
				}

				// get the entry for the current oid to be marked
				final StorageEntity.Implementation entry = this.getEntry(oidsBuffer[oidsMarkIndex++]);

				// externalized/modularized zombie oid handling
				if(entry == null)
				{
					this.zombieOidHandler.handleZombieOid(oidsBuffer[oidsMarkIndex - 1]);
					continue;
				}

//				DEBUGStorage.println(this.channelIndex + " marking " + entry.typeInFile.type.typeHandler().typeName() + " " + entry.objectId());

				// if the entry is already marked black (was redundantly enqueued), skip it and continue to the next
				if(entry.isGcBlack())
				{
//					if(entry.typeId() == 35)
//					{
//						DEBUGStorage.println(this.channelIndex + " already black Date " + entry.objectId());
//					}

					continue;
				}

				// enqueue all reference ids in the mark queue via the central gc monitor instance to account for channel concurrency
//				DEBUGStorage.println(this.channelIndex + " marking references of " + current.objectId() + " with cache size " + this.usedCacheSize);
				if(entry.iterateReferenceIds(referenceMarker))
				{
					// must check for clearing the cache again if marking required loading
					this.checkForCacheClear(entry, evalTime);
				}

				/* note on non-referencing entities
				 * - iterateReferenceIds already checks for references and returns false if none are present
				 * - no general touch here to not touch entities without references.
				 */

//				if(entry.typeId() == 35)
//				{
//					DEBUGStorage.println(this.channelIndex + " marking Date " + entry.objectId());
//				}

				// the entry has been fully processed (either has no references or got all its references gray-enqueued), so mark black.
				entry.markBlack();

//				DEBUGStorage.println(this.channelIndex + " marked " + current);
//				this.DEBUG_marked++;
			}
			while(System.nanoTime() < timeBudgetBound);

			// important: if time ran out, the last batch of processed oids has to be accounted for in the gray queue
			if(oidsMarkIndex > 0)
			{
				// an incremented index always equals an element count
				this.advanceMarking(oidsMarkIndex);
			}

//			DEBUGStorage.println(this.channelIndex + " incrementally marked " + DEBUG_marked);

			// time ran out, return false.
			return false;
		}


//		@Deprecated
//		int DEBUG_marked;

		private void sweep()
		{
			this.lastSweepStart = System.currentTimeMillis();

//			final HashTable<StorageEntityType<?>, Long> deletedEntities = HashTable.New();
//			final HashTable<StorageEntityType<?>, Long> rescuedEntities = HashTable.New();

			DEBUGStorage.println(this.channelIndex + " sweeping");

//			long DEBUG_safed = 0, DEBUG_collected = 0, DEBUG_lowest_collected = Long.MAX_VALUE, DEBUG_highest_collected = 0, DEBUG_safed_gray = 0;
//			final long DEBUG_starttime = System.nanoTime();

			final StorageEntityType.Implementation typeHead = this.typeHead;

			for(StorageEntityType.Implementation sweepType = typeHead; (sweepType = sweepType.next) != typeHead;)
			{
//				DEBUGStorage.println(this.channelIndex + " sweeping " + sweepType.typeHandler().typeName());

				// get next item and check for end of type (switch to next type required)
				for(StorageEntity.Implementation item, last = sweepType.head; (item = last.typeNext) != null;)
				{
//					DEBUGStorage.println(this.channelIndex + " sweep checking " + item.typeInFile.type.typeHandler().typeName() + " " + item.objectId());


					// actual sweep: white entities are deleted, non-white entities are marked white but not deleted
					if(item.isGcMarked())
					{
//						if(item.typeId() == 35)
//						{
//							DEBUGStorage.println(this.channelIndex + " saved Date " + item.objectId());
//						}
//						DEBUGStorage.println(this.channelIndex + " saved Date " + item.objectId());

//						if(item.isGcGray())
//						{
////							DEBUGStorage.println(this.channelIndex + " saving gray entity " + item.objectId() + " " + item.typeInFile.type.typeHandler().typeId()+" " + item.typeInFile.type.typeHandler().typeName() + " GC state = " + item.gcState);
//							DEBUG_safed_gray++;
//						}

//						DEBUGStorage.println(this.channelIndex + " saving " + item.objectId() + " (" + item.typeInFile.type.typeHandler().typeId() + " " + item.typeInFile.type.typeHandler().typeName() + ")");
//						rescuedEntities.put(sweepType, coalesce(rescuedEntities.get(sweepType), 0L) + 1L);
//						DEBUG_safed++;

						(last = item).markWhite(); // reset to white and advance one item
					}
					else
					{
//						if(item.typeId() == 35)
//						{
//							DEBUGStorage.println(this.channelIndex + " Collecting " + item.objectId() + " (" + item.typeInFile.type.typeHandler().typeId() + " " + item.typeInFile.type.typeHandler().typeName() + ")");
//						}
//						DEBUGStorage.println(this.channelIndex + " Collecting " + item.objectId() + " (" + item.typeInFile.type.typeHandler().typeId() + " " + item.typeInFile.type.typeHandler().typeName() + ")");


//						DEBUG_collected++;
////						deletedEntities.put(sweepType, coalesce(deletedEntities.get(sweepType), 0L) + 1L);
//						if(item.objectId() < DEBUG_lowest_collected)
//						{
//							DEBUG_lowest_collected = item.objectId();
//						}
//						if(item.objectId() >= DEBUG_highest_collected)
//						{
//							DEBUG_highest_collected = item.objectId();
//						}

						// otherwise white entity, so collect it
						this.deleteEntity(item, sweepType, last);
					}
				}
			}

			this.lastSweepEnd = System.currentTimeMillis();
			this.sweepGeneration++;

//			final long DEBUG_stoptime = System.nanoTime();
//			final VarString vs = VarString.New();
//			vs.add(this.channelIndex + " COMPLETED sweep #" + this.sweepGeneration + " @ " + this.lastSweepEnd);
//			vs.add(" Marked: ").add(this.DEBUG_marked);
//			this.DEBUG_marked = 0;
//			vs.add(" Safed " + DEBUG_safed + "(" + DEBUG_safed_gray + " gray), collected " + DEBUG_collected + ". Nanotime: " + new java.text.DecimalFormat("00,000,000,000").format(DEBUG_stoptime - DEBUG_starttime));
//			vs
//			.add(" Lowest collected: ").add(DEBUG_lowest_collected == Long.MAX_VALUE ? 0 : DEBUG_lowest_collected)
//			.add(" Highest collected: ").add(DEBUG_highest_collected)
//			.add(" used cache size: ").add(this.cacheSize())
//			;
//			for(final KeyValue<StorageEntityType<?>, Long> e : deletedEntities)
//			{
//				vs.lf().add(this.channelIndex + " deleted ").padLeft(Long.toString(e.value()), 8, ' ').blank().add(e.key().typeHandler().typeName());
//			}
//			for(final KeyValue<StorageEntityType<?>, Long> e : rescuedEntities)
//			{
//				vs.lf().add(this.channelIndex + " rescued ").padLeft(Long.toString(e.value()), 8, ' ').blank().add(e.key().typeHandler().typeName());
//			}
//			DEBUGStorage.println(vs.toString());
//			if(DEBUG_collected != 0)
//			{
//				System.err.println(this.channelIndex + " collected " + DEBUG_collected);
//			}

//			final StorageEntityType.Implementation typeDate = this.lookupType(35);
//			for(StorageEntity.Implementation e = typeDate.head; e != null; e = e.typeNext)
//			{
//				DEBUGStorage.println(this.channelIndex + " date " + e.objectId());
//			}

			// reset file cleanup cursor to first file in order to ensure the cleanup checks all files for the current state.
			this.fileManager.resetFileCleanupCursor();

			// signal mark monitor that the sweep is complete and provide this channel's valid rootOid
			final long channelRootOid = this.queryRootObjectId();
			this.markMonitor.completeSweep(this, this.rootOidSelector, channelRootOid);
		}


		final void internalPutEntities(
			final ByteBuffer                     chunk               ,
			final long                           chunkStoragePosition,
			final StorageDataFile.Implementation file
		)
		{
			final long chunkStartAddress = XMemory.getDirectByteBufferAddress(chunk);
			final long chunkLength       = chunk.limit();

			// calculated offset difference, may even be negative, doesn't matter
			final long storageBackset    = chunkStoragePosition - chunkStartAddress;
			final long chunkBoundAddress = chunkStartAddress    + chunkLength      ;

			// chunk's entities are iterated, put into the cache and have their current storage positions set/updated
			for(long adr = chunkStartAddress; adr < chunkBoundAddress; adr += Binary.getEntityLengthRawValue(adr))
			{
				final StorageEntity.Implementation entity = this.putEntity(adr);
				this.markEntityForChangedData(entity);
				entity.updateStorageInformation(
					X.checkArrayRange(Binary.getEntityLengthRawValue(adr)),
					file,
					XTypes.to_int(storageBackset + adr)
				);
			}
		}

		final void modifyUsedCacheSize(final long cacheChange)
		{
			this.usedCacheSize += cacheChange;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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

		public void postStorePutEntities(
			final ByteBuffer[]                   chunks                ,
			final long[]                         chunksStoragePositions,
			final StorageDataFile.Implementation dataFile
		)
			throws InterruptedException
		{
//			final long startTime = System.currentTimeMillis();
//			DEBUGStorage.println(this.channelIndex + " " + startTime +" doing post store updating entities.");

			this.hasUpdatePendingSweep = this.markMonitor.isPendingSweep(this);

			// reset completion here, too, in case the store happed before the sweep and the post-store happens after it
			this.markMonitor.resetCompletion();

			for(int i = 0; i < chunks.length; i++)
			{
				this.internalPutEntities(chunks[i], chunksStoragePositions[i], dataFile);
			}

			// must be done by the store task's cleanup, but as it is idempotent, call it here right away
			this.clearPendingStoreUpdate();

//			final long endTime = System.currentTimeMillis();
//			DEBUGStorage.println(this.channelIndex + " " + endTime +" completed post store updating entities.");
		}

		final void clearPendingStoreUpdate()
		{
			this.hasUpdatePendingSweep = false;
			this.markMonitor.clearPendingStoreUpdate(this);
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
			/* (18.07.2016 TM)TODO: ensure singleton root instance over all channels
			 * If there may be only one root instance, it should be guaranteed here to determine the valid
			 * one and ignore the rest.
			 * The tricky part is: this has to be done accross all channels and in a thread safe way.
			 * To achieve this, the MarkMonitor would have to do it in a centralized method.
			 * For that, it has to know the entity caches, which it does / can not currently.
			 * Also, letting one thread call methods of all channels would mean to break the strict thread locality
			 * of the channels (only the dedicated channel thread may operate on the EntityCache instances).
			 * Thread-local work of a channel would suddenly have to subject to a lock on the mark monitor
			 *
  			 * This issue is ignored for now, but must be fixed if root instances are to be replaceable.
  			 *
  			 * Clean solution:
  			 * Copy all roots, but not directly into a ChunksBuffer, but into a special intermediate data structure
  			 * with a OID->binary map and reported valid rootId of each channel.
			 */

			// iterate over all entities of all root types and copy their data
			this.rootType.iterateEntities(e ->
				e.copyCachedData(dataCollector)
			);
		}

		@Override
		public final boolean incrementalLiveCheck(final long timeBudgetBound)
		{
			return this.internalLiveCheck(timeBudgetBound, this.entityCacheEvaluator);
		}
		
		
		private static StorageEntity.Implementation getFirstReachableEntity(
			final StorageDataFile.Implementation startingFile
		)
		{
			StorageDataFile.Implementation file = startingFile;
			do
			{
				if(file.head.fileNext != startingFile.tail)
				{
					return file.head.fileNext;
				}
			}
			while((file = file.next) != startingFile);
			
			// no file contains any reachable (proper) entity. So return null.
			return null;
		}

		private boolean internalLiveCheck(final long timeBudgetBound, final StorageEntityCacheEvaluator evaluator)
		{
			// quick check before setting up the local stuff.
			if(this.usedCacheSize == 0)
			{
//				DEBUGStorage.println(this.channelIndex + " aborting live check (cache is empty)");
				return true;
			}

			final long evalTime = System.currentTimeMillis();

			final StorageEntity.Implementation   cursor;
			      StorageEntity.Implementation   tail  ;
			      StorageEntity.Implementation   entity;
			      StorageDataFile.Implementation file  ;

			if(this.liveCursor == null || !this.liveCursor.isProper() || this.liveCursor.isDeleted())
			{
				// cursor special cases: not set, yet or a head/tail instance or meanwhile deleted (= unreachable)
				cursor = getFirstReachableEntity(this.fileManager.currentStorageFile().next);
				
				// special special case: all files are (effectively) empty. Nothing to check. Prevent inifinite loop.
				if(cursor == null)
				{
					return true;
				}
			}
			else
			{
				// normal case: cursor points to a (still) reachable, proper entity.
				cursor = this.liveCursor;
			}
			file   = cursor.typeInFile.file;
			tail   = file.tail;
			entity = cursor;

//			final long DEBUG_live = 0, DEBUG_unlive = 0;

			// abort conditions for one housekeeping cycle: cursor is encountered again (full loop) or
			do
			{
				// if the end of one file is reached, proceed to the next file.
				if(entity == tail)
				{
					// proceed to next file
					file = file.next;
					tail = file.tail;
					entity = file.head.fileNext;
					continue;
				}

				// debug stuff
//				if(entity.isProper())
//				{
//					if(entity.isLive())
//					{
//						DEBUG_live++;
//					}
//					else
//					{
//						DEBUG_unlive++;
//					}
//				}

				// check for clearing the current entity's cache
				if(this.entityRequiresCacheClearing(entity, evaluator, evalTime))
				{
//					DEBUGStorage.println(this.channelIndex + " clearing entity " + entity);
					// entity has cached data but was deemed as having to be cleared, so clear it
					// use ensure method for that for the purpose of uniformity / simplicity
					this.ensureNoCachedData(entity);

					// check if this was the last entity in the cache, effectively suspending live check
					if(this.usedCacheSize == 0)
					{
						break;
					}
				}

				// made a full cycle. abort.
				if((entity = entity.fileNext) == cursor)
				{
					break;
				}

				// check time budget at the end to make 1 entity progress in any case to avoid starvation
			}
			while(System.nanoTime() < timeBudgetBound);

//			DEBUGStorage.println(this.channelIndex + " quits live check. Live = " + DEBUG_live + ", unlive = " + DEBUG_unlive + ", cache size = " + this.usedCacheSize + ", time left = " + (System.nanoTime() - timeBudgetBound));
			return this.quitLiveCheck(entity);
		}
		
		private boolean quitLiveCheck(final StorageEntity.Implementation entity)
		{
			if(this.usedCacheSize == 0)
			{
				this.resetLiveCursor();

				DEBUGStorage.println(this.channelIndex + " completed live check.");

				// report live check completed
				return true;
			}

			// keep last checked entity as a cursor / base / starting point for the next cycle's check
			this.liveCursor = entity;

//			DEBUGStorage.println(this.channelIndex + " suspends live check.");

			// report live check ends incomplete
			return false;
		}
		
		
		/* (16.06.2017 TM)NOTE:
		 * the reason for this method is merely to prevent a bug in JVM JITting:
		 * Without this method, the cache check would occasionally run indefinitely in certain data constellations.
		 * With this method, resulting in IDENTICAL logic (but slightly different byte code), the
		 * Infinite loop wasn't reproducible anymore.
		 * Since no multithreading is involved here, this can mean only one thing:
		 * The Jitting changed/ruined the logic described by the source code.
		 * Not cool :-[.
		 */
		private boolean entityRequiresCacheClearing(
			final StorageEntity.Implementation entity   ,
			final StorageEntityCacheEvaluator  evaluator,
			final long                         evalTime
		)
		{
			if(!entity.isLive())
			{
				return false;
			}
			
			return evaluator.clearEntityCache(this.usedCacheSize, evalTime, entity);
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
				X.coalesce(entityEvaluator, this.entityCacheEvaluator)
			);
		}

		/**
		 * Returns <code>true</code> if there are no more oids to mark and <code>false</code> if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		@Override
		public final boolean issuedGarbageCollection(final long nanoTimeBudgetBound, final StorageChannel channel)
		{
			if(!DEBUG_GC_ENABLED)
			{
				return true;
			}

//			DEBUGStorage.println(this.channelIndex() + " issued gc");

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
				 * The loop itself only checks for completion and time budget.
				 * Inside the loop, a simple if checks for newly enqueued oids to be marked/processed (= new work)
				 * If there aren't any, a wait on the queue is performed.
				 *
				 * Note:
				 * This covers spurious wakeups, however slightly complicated:
				 * On any wakeup, a check for time and completion is performed.
				 */
				waitForWork:
				while(System.nanoTime() < nanoTimeBudgetBound)
				{
					// check for completion on every attempt to wait for new work
					if(this.markMonitor.isComplete(this))
					{
						return true;
					}

					// check if marking has been completed while waiting.
					if(this.markMonitor.isMarkingComplete())
					{
						break waitForWork;
					}

					// better try for pending local mark oids to flush before checking/waiting for work
					this.referenceMarker.tryFlush();

//					DEBUGStorage.println(this.channelIndex() + " waiting for work.\n" + this.markMonitor.DEBUG_state());

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
							this.oidMarkQueue.wait(this.markingWaitTimeMs);
						}
						catch(final InterruptedException e)
						{
							// thread has been interrupted while trying to perform garbage collection. So abort and return.
							break performGC;
						}
					}
					// end of waiting, continue with waitForWork checks
				}
				// end of waitForWork, continue performGC
			}
			// end of performGC

			// either time ran out or thread was interrupted. In any case, report back the current state of the garbage collection.
			return this.markMonitor.isComplete(this);
		}

		/**
		 * Returns <code>true</code> if there are no more oids to mark and <code>false</code> if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		@Override
		public final boolean incrementalGarbageCollection(final long timeBudgetBound, final StorageChannel channel)
		{
			if(!DEBUG_GC_ENABLED)
			{
				return true;
			}

			try
			{
				return this.internalIncrementalGarbageCollection(timeBudgetBound, channel);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception in channel #" + this.channelIndex(), e);
			}
		}

		private boolean checkForGcCompletion()
		{
			if(this.markMonitor.isComplete(this))
			{
				// minimize hash table memory consumption if storage is potentially going to be inactive
				this.checkOidHashTableConsolidation();

				return true;
			}

			return false;
		}

		private final boolean internalIncrementalGarbageCollection(
			final long           timeBudgetBound,
			final StorageChannel channel
		)
		{
			// check for completion to abort no-op calls
			if(this.checkForGcCompletion())
			{
//				DEBUGStorage.println(this.channelIndex + " GC complete.");
				return true;
			}

			// check if there is sweeping to be done
			if(this.markMonitor.needsSweep(this))
			{
//				if(this.DEBUG_marked != 0)
//				{
//					DEBUGStorage.println(this.channelIndex() + " marked " + this.DEBUG_marked);
//					this.DEBUG_marked = 0;
//				}

				this.sweep();

				// must check for completion again, otherwise a channel might restart marking beyond a completed gc.
				if(this.checkForGcCompletion())
				{
					return true;
				}

				// check if there is still time to proceed with the next (second) marking right away
				if(System.nanoTime() >= timeBudgetBound)
				{
					return false;
				}
			}

			// otherwise, mark incrementally until work or time runs out
			if(this.incrementalMark(timeBudgetBound))
			{
				/* note:
				 * if the buffer length is chosen too low, this return is done countless times per millisecond.
				 * Initially, 500 was chosen, but 10000 or 50000 seem to be much more appropriate.
				 */
//				DEBUGStorage.println(this.channelIndex + " no more mark work");

				// work ran out
				return true;
			}

			// time ran out
			return false;
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
}
