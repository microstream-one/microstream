package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.log2pow2;
import static one.microstream.math.XMath.notNegative;
import static one.microstream.math.XMath.positive;

import java.nio.ByteBuffer;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.Set_long;
import one.microstream.functional.ThrowingProcedure;
import one.microstream.functional._longPredicate;
import one.microstream.math.XMath;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.types.ObjectIdsProcessor;
import one.microstream.persistence.types.ObjectIdsSelector;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionGarbageCollector;
import one.microstream.storage.exceptions.StorageExceptionInitialization;
import one.microstream.util.logging.Logging;


public interface StorageEntityCache<E extends StorageEntity> extends StorageChannelResetablePart
{
	public StorageTypeDictionary typeDictionary();

	public StorageEntityType<E> lookupType(long typeId);

	public boolean incrementalEntityCacheCheck(long nanoTimeBudgetBound);

	public boolean incrementalGarbageCollection(long nanoTimeBudgetBound, StorageChannel channel);

	public boolean issuedGarbageCollection(long nanoTimeBudgetBound, StorageChannel channel);

	public boolean issuedEntityCacheCheck(long nanoTimeBudgetBound, StorageEntityCacheEvaluator entityEvaluator);

	public void copyRoots(ChunksBuffer dataCollector);

	public long cacheSize();
	
	public long clearCache();
	
	@Override
	public void reset();


	
	public final class Default
	implements StorageEntityCache<StorageEntity.Default>, Unpersistable
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		
		private static boolean gcEnabled = true;
				
		public static void setGarbageCollectionEnabled(final boolean enabled)
		{
			gcEnabled = enabled;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// state 1.0: immutable or stateless (as far as this implementation is concerned)

		private final int                                channelIndex        ;
		private final int                                channelHashModulo   ;
		private final int                                channelHashShift    ;
		private final long                               rootTypeId          ;
		private final long                               markingWaitTimeMs   ;
		        final StorageEntityCacheEvaluator        entityCacheEvaluator;
		private final StorageTypeDictionary              typeDictionary      ;
		private final long[]                             markingOidBuffer    ;
		private final StorageGCZombieOidHandler          zombieOidHandler    ;
		private final StorageRootOidSelector             rootOidSelector     ;
		private final RootEntityRootOidSelectionIterator rootEntityIterator  ;
		private final StorageEventLogger                 eventLogger         ;
		private       StorageFileManager.Default         fileManager         ; // pseudo-final
		
		
		// state 2.0: final references to mutable instances, i.e. content must be cleared on reset
		
		private final StorageEntityMarkMonitor  markMonitor    ;
		private final StorageObjectIdMarkQueue  oidMarkQueue   ; // resetting handled by markMonitor
		private final StorageReferenceMarker    referenceMarker; // resetting must be handled here.
		
		private final ObjectIdsSelector liveObjectIdChecker;

		
		// state 3.0: mutable fields. Must be cleared on reset.
		
		private StorageEntity.Default liveCursor;
		
		private long    usedCacheSize;
		private boolean hasUpdatePendingSweep;
		
		// Statistics for debugging / monitoring / checking to compare with other channels and with the markmonitor
		private long sweepGeneration, lastSweepStart, lastSweepEnd;
		
		
		// state 3.1: variable length content
		
		private       StorageEntity.Default[]     oidHashTable ;
		private       int                         oidModulo    ; // long modulo makes not difference
		private       long                        oidSize      ;
		
		private       StorageEntityType.Default[] tidHashTable ;
		private       int                         tidModulo    ;
		private       int                         tidSize      ;
		
		private final StorageEntityType.Default   typeHead     ; // effective immutable, so no reset
		private       StorageEntityType.Default   typeTail     ;
		private       StorageEntityType.Default   rootType     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final int                         channelIndex       ,
			final int                         channelCount       ,
			final StorageEntityCacheEvaluator cacheEvaluator     ,
			final StorageTypeDictionary       typeDictionary     ,
			final StorageEntityMarkMonitor    markMonitor        ,
			final StorageGCZombieOidHandler   zombieOidHandler   ,
			final StorageRootOidSelector      rootOidSelector    ,
			final long                        rootTypeId         ,
			final StorageObjectIdMarkQueue    oidMarkQueue       ,
			final StorageEventLogger          eventLogger        ,
			final ObjectIdsSelector           liveObjectIdChecker,
			final long                        markingWaitTimeMs  ,
			final int                         markingBufferLength
		)
		{
			super();
			this.channelIndex         = notNegative(channelIndex)     ;
			this.channelHashShift     = log2pow2   (channelCount)     ;
			this.entityCacheEvaluator = notNull    (cacheEvaluator)   ;
			this.typeDictionary       = notNull    (typeDictionary)   ;
			this.markMonitor          = notNull    (markMonitor)      ;
			this.zombieOidHandler     = notNull    (zombieOidHandler) ;
			this.rootOidSelector      = notNull    (rootOidSelector)  ;
			this.rootTypeId           =             rootTypeId        ;
			this.oidMarkQueue         = notNull    (oidMarkQueue)     ;
			this.eventLogger          =             eventLogger       ;
			this.markingWaitTimeMs    = positive   (markingWaitTimeMs);
			
			// derived values
			
			this.channelHashModulo  = channelCount - 1;
			this.markingOidBuffer   = new long[markingBufferLength];
			this.rootEntityIterator = new RootEntityRootOidSelectionIterator(rootOidSelector);
			this.typeHead           = new StorageEntityType.Default(this.channelIndex);
			
			// initializing mutable (operational) state.
			this.reset();
			
			// create reference marker at the very end to have all state properly initialized beforehand.
			this.referenceMarker = markMonitor.provideReferenceMarker(this);
			
			this.liveObjectIdChecker = notNull(liveObjectIdChecker);
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

		final void initializeStorageManager(final StorageFileManager.Default fileManager)
		{
			if(this.fileManager != null && this.fileManager != fileManager)
			{
				throw new StorageExceptionInitialization("File manager already initialized.");
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

		@Override
		public final synchronized void reset()
		{
			this.clearCache();
			
			this.markMonitor.reset();
			
			this.oidHashTable   = new StorageEntity.Default[1];
			this.oidModulo      = this.oidHashTable.length - 1;
			this.oidSize        = 0;

			this.tidHashTable   = new StorageEntityType.Default[1];
			this.tidModulo      = this.tidHashTable.length - 1;
			this.tidSize        = 0;

			(this.typeTail      = this.typeHead).next = null;

			this.resetLiveCursor();

			this.usedCacheSize  = 0L;

			// create a new root type instance on every clear. Everything else is not worth the reset&register-hassle.
			this.rootType       = this.getType(this.rootTypeId);

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
			final StorageEntity.Default[] newSlots =
				XMath.isGreaterThanOrEqualHighestPowerOf2(this.oidHashTable.length)
				? new StorageEntity.Default[newModulo = Integer.MAX_VALUE] // perfect hash range special case
				: new StorageEntity.Default[(newModulo = (this.oidModulo + 1 << 1) - 1) + 1] // 1111 :D
			;
			rebuildOidHashSlots(this.oidHashTable, newSlots, this.channelHashShift, newModulo);
			this.oidHashTable = newSlots;
			this.oidModulo    = newModulo;
			
			logger.debug("Enlarged StorageEntityCache to {} entries!", newSlots.length);
		}

		private static void rebuildOidHashSlots(
			final StorageEntity.Default[] oldSlots     ,
			final StorageEntity.Default[] newSlots     ,
			final int                            bitShiftCount,
			final int                            newModulo
		)
		{
			for(StorageEntity.Default entry : oldSlots)
			{
				for(StorageEntity.Default next; entry != null; entry = next)
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
			final StorageEntity.Default[] newSlots  = new StorageEntity.Default[newModulo + 1];
			rebuildOidHashSlots(this.oidHashTable, newSlots, this.channelHashShift, newModulo);
			this.oidHashTable = newSlots;
			this.oidModulo    = newModulo;
			
			logger.debug("Consolidated StorageEntityCache to {} entries!", newSlots.length);
		}

		private void rebuildTidHashTable()
		{
			final int newModulo;
			final StorageEntityType.Default[] newSlots =
				new StorageEntityType.Default[(newModulo = (this.tidModulo + 1 << 1) - 1) + 1]
			;

			for(StorageEntityType.Default entries : this.tidHashTable)
			{
				for(StorageEntityType.Default next; entries != null; entries = next)
				{
					next = entries.hashNext;
					entries.hashNext = newSlots[tidHashIndex(entries.typeId, newModulo)];
					newSlots[tidHashIndex(entries.typeId, newModulo)] = entries;
				}
			}
			this.tidHashTable = newSlots;
			this.tidModulo    = newModulo;
		}

		final StorageEntityType.Default getType(final long typeId)
		{
			final StorageEntityType.Default type;
			if((type = this.lookupType(typeId)) != null)
			{
				return type;
			}
			return this.addNewType(typeId);
		}

		private StorageEntityType.Default addNewType(final long typeId)
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
			final StorageEntityType.Default type = new StorageEntityType.Default(
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
			// (09.08.2015 TM)NOTE: included channel hash mod bit shifting to properly distribute in hash table
			return (int)(value >>> bitShiftCount & modulo);
		}

		static final int tidHashIndex(final long tid, final int tidModulo)
		{
			return hash(tid, tidModulo);
		}

		static final int oidHashIndex(final long objectId, final int bitShiftCount, final int oidModulo)
		{
			return hashNormalized(objectId, bitShiftCount, oidModulo);
		}

		static final int oidChannelIndex(final long objectId, final int channelHashModulo)
		{
			return hash(objectId, channelHashModulo);
		}

		private int oidHashIndex(final long objectId)
		{
			return oidHashIndex(objectId, this.channelHashShift, this.oidModulo);
		}

		private int oidChannelIndex(final long objectId)
		{
			return oidChannelIndex(objectId, this.channelHashModulo);
		}

		private StorageEntity.Default getOidHashChainHead(final long objectId)
		{
			return this.oidHashTable[this.oidHashIndex(objectId)];
		}

		private void setOidHashChainHead(final long objectId, final StorageEntity.Default head)
		{
			this.oidHashTable[this.oidHashIndex(objectId)] = head;
		}


		/* Note on synchronization:
		 * This method does not need to be synchronized (locked), as it is exclusively always called by the
		 * channel's inherent thread which is also the same that rebuilds the hashTables, so it can never work
		 * on old cached instances.
		 */
		final void unregisterEntity(final StorageEntity.Default item)
		{
			StorageEntity.Default entry;
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
		public final StorageEntity.Default getEntry(final long objectId)
		{
			for(StorageEntity.Default e = this.getOidHashChainHead(objectId); e != null; e = e.hashNext)
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
		implements ThrowingProcedure<StorageEntity.Default, RuntimeException>
		{
			final StorageRootOidSelector rootOidSelector;

			public RootEntityRootOidSelectionIterator(final StorageRootOidSelector rootOidSelector)
			{
				super();
				this.rootOidSelector = rootOidSelector;
			}

			@Override
			public void accept(final StorageEntity.Default e) throws RuntimeException
			{
				this.rootOidSelector.accept(e.objectId());
			}

		}

		private void ensureNoCachedData(final StorageEntity.Default entry)
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
				throw new StorageExceptionConsistency("Invalid objectId " + objectId + " for hash channel " + this.channelIndex);
			}
		}
		
		final StorageIdAnalysis validateEntities()
		{
			long maxTid = 0, maxOid = 0, maxCid = 0;
			
			final EqHashEnum<Long> occuringTypeIds = EqHashEnum.New();

			// validate all entities via iteration by type. Simplifies debugging and requires less type pointer chasing
			for(StorageEntityType.Default type : this.tidHashTable)
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

		final StorageEntityType.Default validateEntity(
			final long length,
			final long typeId,
			final long objcId
		)
		{
			final StorageEntityType.Default type;
			final StorageEntity.Default entry = this.getEntry(objcId);

			if(entry != null)
			{
				if((type = entry.typeInFile.type).typeId != typeId)
				{
					throw new StorageExceptionConsistency(
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

		final StorageEntity.Default putEntity(final long objectId, final StorageEntityType.Default type)
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
			final StorageEntity.Default entry;
			if((entry = this.getEntry(objectId)) != null)
			{
//				DEBUGStorage.println("updating entry " + entry);
				this.resetExistingEntityForUpdate(entry);
				return entry;
			}

//			DEBUGStorage.println("creating " + Binary.getEntityObjectId(entityAddress) + ", " + Binary.getEntityTypeId(entityAddress) + ", [" + Binary.getEntityLength(entityAddress) + "]");
			return this.createEntity(objectId, type);
		}

		final StorageEntity.Default putEntity(final long entityAddress)
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
								
			final StorageEntity.Default entry;
			if((entry = this.getEntry(Binary.getEntityObjectIdRawValue(entityAddress))) != null)
			{
				final long entityTypeId = Binary.getEntityTypeIdRawValue(entityAddress);
				if(entry.typeId() == entityTypeId) {
					this.resetExistingEntityForUpdate(entry);
					return entry;
				}
				
				logger.debug("Entity {} typeId changed, old: {}, new: {}",
					entry.objectId(),
					entry.typeId(),
					entityTypeId);
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
					
		final StorageEntity.Default initialCreateEntity(final long entityAddress)
		{
			final StorageEntity.Default entity = this.createEntity(
				Binary.getEntityObjectIdRawValue(entityAddress),
				this.getType(Binary.getEntityTypeIdRawValue(entityAddress))
			);
			
			return entity;
		}

		private void resetExistingEntityForUpdate(final StorageEntity.Default entry)
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
		 */
		private void markEntityForChangedData(final StorageEntity.Default entry)
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
		private StorageEntity.Default createEntity(
			final long                             objectId,
			final StorageEntityType.Default type
		)
		{
			// increment size and check for necessary (and reasonable) rebuild
			if(this.oidSize >= this.oidModulo && this.oidModulo < Integer.MAX_VALUE)
			{
				this.enlargeOidHashTable();
			}

			// create and put entry
			final StorageEntity.Default entity;
			this.setOidHashChainHead(
				objectId,
				entity = StorageEntity.Default.New(
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
			final StorageEntity.Default     entity        ,
			final StorageEntityType.Default type          ,
			final StorageEntity.Default     previousInType
		)
		{
			logger.debug("Deleting entity {}, typeId: {}", entity.objectId(), entity.typeId());
			
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
						
			// decrement size, otherwise the the cache can't shrink
			this.oidSize--;
		}

		void checkForCacheClear(final StorageEntity.Default entry, final long evalTime)
		{
			if(this.entityCacheEvaluator.clearEntityCache(this.usedCacheSize, evalTime, entry))
			{
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
		 * Returns {@code true} if there are no more oids to mark and {@code false} if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		private boolean incrementalMark(final long nanoTimeBudgetBound)
		{
			final long                     evalTime        = System.currentTimeMillis();
			final StorageReferenceMarker   referenceMarker = this.referenceMarker      ;
			final StorageObjectIdMarkQueue oidMarkQueue    = this.oidMarkQueue         ;
			final long[]                   oidsBuffer      = this.markingOidBuffer     ;

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
				final StorageEntity.Default entry = this.getEntry(oidsBuffer[oidsMarkIndex++]);

				// externalized/modularized zombie oid handling
				if(entry == null)
				{
					if(!this.zombieOidHandler.handleZombieOid(oidsBuffer[oidsMarkIndex - 1]))
					{
						// if the handler didn't throw an exception but didn't say it's handled, either, then log it.
						logger.warn("Storage GC marking encountered zombie ObjectId {}", oidsBuffer[oidsMarkIndex - 1]);
						this.eventLogger.logGarbageCollectorEncounteredZombieObjectId(oidsBuffer[oidsMarkIndex - 1]);
					}
					continue;
				}
				
				// if the entry is already marked black (was redundantly enqueued), skip it and continue to the next
				if(entry.isGcBlack())
				{
					continue;
				}

				// enqueue all reference ids in the mark queue via the central gc monitor instance to account for channel concurrency
				if(entry.iterateReferenceIds(referenceMarker))
				{
					// must check for clearing the cache again if marking required loading
					this.checkForCacheClear(entry, evalTime);
				}

				/*
				 * note on non-referencing entities
				 * - iterateReferenceIds already checks for references and returns false if none are present
				 * - no general touch here to not touch entities without references.
				 */

				// the entry has been fully processed (either has no references or got all its references gray-enqueued), so mark black.
				entry.markBlack();
			}
			while(System.nanoTime() < nanoTimeBudgetBound);

			// important: if time ran out, the last batch of processed oids has to be accounted for in the gray queue
			if(oidsMarkIndex > 0)
			{
				// an incremented index always equals an element count
				this.advanceMarking(oidsMarkIndex);
			}

			// time ran out, return false.
			return false;
		}

		/**
		 * If an entity (its OID) is still reachable in the application, it may not be deleted.
		 * Otherwise, data might be lost since the entity still exists for the application and thus
		 * is only stored by reference but not in full. The passed predicate is a connection to the
		 * application's object registry to perform the required check.
		 * 
		 * @param isReachableInApplication
		 */
		final void sweep(final _longPredicate isReachableInApplication)
		{
			this.lastSweepStart = System.currentTimeMillis();
			final StorageEntityType.Default typeHead = this.typeHead;

			for(StorageEntityType.Default sweepType = typeHead; (sweepType = sweepType.next) != typeHead;)
			{
				// get next item and check for end of type (switch to next type required)
				for(StorageEntity.Default item, last = sweepType.head; (item = last.typeNext) != null;)
				{
					// actual sweep: white entities are deleted, non-white entities are marked white but not deleted
					if(item.isGcMarked() || isReachableInApplication.test(item.objectId))
					{
						// reset to white and advance one item
						(last = item).markWhite();
					}
					else
					{
						// otherwise white entity, so collect it
						this.deleteEntity(item, sweepType, last);
					}
				}
			}

			this.lastSweepEnd = System.currentTimeMillis();
			this.sweepGeneration++;

			// reset file cleanup cursor to first file in order to ensure the cleanup checks all files for the current state.
			this.fileManager.restartFileCleanupCursor();

			// signal mark monitor that the sweep is complete and provide this channel's valid rootOid
			final long channelRootOid = this.queryRootObjectId();
			this.markMonitor.completeSweep(this, this.rootOidSelector, channelRootOid);
		}
		
		private boolean sweep()
		{
			return this.liveObjectIdChecker.processSelected(new ApplicationCallback(this.typeHead));
		}

		final class ApplicationCallback implements ObjectIdsProcessor
		{
			final StorageEntityType.Default typeHead;

			ApplicationCallback(final StorageEntityType.Default typeHead)
			{
				super();
				this.typeHead = typeHead;
			}

			@Override
			public void processObjectIdsByFilter(final _longPredicate objectIdsSelector)
			{
				Default.this.sweep(objectIdsSelector);
			}

			@Override
			public Set_long provideObjectIdsBaseSet()
			{
				final Set_long sweepCandicateObjectIds = Set_long.New(1000);

				final StorageEntityType.Default typeHead = this.typeHead;
				for(StorageEntityType.Default sweepType = typeHead; (sweepType = sweepType.next) != typeHead;)
				{
					// get next item and check for end of type (switch to next type required)
					for(StorageEntity.Default item = sweepType.head; (item = item.typeNext) != null;)
					{
						if(!item.isGcMarked())
						{
							sweepCandicateObjectIds.add(item.objectId);
						}
					}
				}

				return sweepCandicateObjectIds;
			}

		}
		

		private static final long MAX_INT_BOUND = 1L + Integer.MAX_VALUE;
		
		final static int validateStoragePosition(
			final StorageEntity.Default entity       ,
			final long                  storageOffset
		)
		{
			if(storageOffset < MAX_INT_BOUND)
			{
				return (int)storageOffset;
			}
			
			/* (06.02.2020 TM)EXCP: fix storage position limitation
			 * This has, of course, to be removed/fixed.
			 * The solution is to spread a single store's buffers over multiple files if needed.
			 * However, this is not trivial to do:
			 * A single store must be split into multiple stores,
			 * each store must get its own transactions entry OR a kind of store continuation entry, etc.
			 * 
			 * This effort is not worth it with the transition of the much more advanced storage management 2.0
			 * concept in mind. There, store chunks will be spread over multiple store files anyway and
			 * archive files will be split according to oid range and entity length automatically.
			 * 
			 * For now, this is simply a technical limitation and one more good reason to implement the
			 * improved storage concept sooner rather than later.
			 */
			throw new StorageException(
				"Storage position for entity " + entity.objectId()
				+ " exceeds the technical int value limit of " + Integer.MAX_VALUE + "."
				+ " This happens when a single store grows too big."
				+ " This limitation will be removed in a future version."
			);
		}

		final void internalPutEntities(
			final ByteBuffer                  chunk               ,
			final long                        chunkStoragePosition,
			final StorageLiveDataFile.Default file
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
				final StorageEntity.Default entity = this.putEntity(adr);
				this.markEntityForChangedData(entity);
				entity.updateStorageInformation(
					X.checkArrayRange(Binary.getEntityLengthRawValue(adr)),
					validateStoragePosition(entity, storageBackset + adr)
				);
				file.appendEntry(entity);
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
		public final long clearCache()
		{
			if(this.usedCacheSize == 0)
			{
				return 0;
			}
			
			final long currentUsedCacheSize = this.usedCacheSize;
			
			this.internalCacheCheck(Long.MAX_VALUE, (s, t, e) -> true);
			
			return currentUsedCacheSize;
		}

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.typeDictionary;
		}

		public void postStorePutEntities(
			final ByteBuffer[]                chunks                ,
			final long[]                      chunksStoragePositions,
			final StorageLiveDataFile.Default dataFile
		)
			throws InterruptedException
		{
			this.hasUpdatePendingSweep = this.markMonitor.isPendingSweep(this);

			// reset completion here, too, in case the store happed before the sweep and the post-store happens after it
			this.markMonitor.resetCompletion();

			for(int i = 0; i < chunks.length; i++)
			{
				this.internalPutEntities(chunks[i], chunksStoragePositions[i], dataFile);
			}

			// must be done by the store task's cleanup, but as it is idempotent, call it here right away
			this.clearPendingStoreUpdate();
		}

		final void clearPendingStoreUpdate()
		{
			// (21.02.2020 TM)NOTE: this potentially gets called after reset(), so it must be accordingly robust.
			
			this.hasUpdatePendingSweep = false;
			this.markMonitor.clearPendingStoreUpdate(this);
		}

		@Override
		public final StorageEntityType.Default lookupType(final long typeId)
		{
			for(StorageEntityType.Default typeEntry = this.tidHashTable[tidHashIndex(typeId, this.tidModulo)];
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
		public final boolean incrementalEntityCacheCheck(
			final long                        nanoTimeBudgetBound
		)
		{
			return this.internalCacheCheck(nanoTimeBudgetBound, this.entityCacheEvaluator);
		}

		private boolean internalCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator evaluator
		)
		{
			// quick check before setting up the local stuff.
			if(this.usedCacheSize == 0)
			{
				return true;
			}

			final long evaluationTime = System.currentTimeMillis();
			final StorageEntity.Default cursor;
			      StorageEntity.Default tail  ;
			      StorageEntity.Default entity;
			      StorageLiveDataFile.Default file;

			if(this.liveCursor == null || !this.liveCursor.isProper() || this.liveCursor.isDeleted())
			{
				// cursor special cases: not set, yet or a head/tail instance or meanwhile deleted (= unreachable)
				cursor = this.fileManager.getFirstEntity();
				
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

			// abort condition is checkd at the end to guarantee one entity progress to avoid starvation
			do
			{
				// if the end of one file is reached, the next file gets checked. The last file connects to the first.
				if(entity == tail)
				{
					// proceed to next file
					file = file.next;
					tail = file.tail;
					entity = file.head.fileNext;
					
					// jumps to loop condition check. The next file's first entry might be the cursor!
					continue;
				}

				// check for clearing the current entity's cache
				if(this.entityRequiresCacheClearing(entity, evaluator, evaluationTime))
				{
					// entity has cached data but was deemed as having to be cleared, so clear it
					// use ensure method for that for the purpose of uniformity / simplicity
					this.ensureNoCachedData(entity);

					// check if this was the last entity in the cache, effectively suspending live check
					if(this.usedCacheSize == 0)
					{
						break;
					}
				}
				
				entity = entity.fileNext;
			}
			while(entity != cursor && System.nanoTime() < nanoTimeBudgetBound);
			// abort conditions for one housekeeping cycle: cursor is encountered again (full loop) or time is up.

			return this.quitLiveCheck(entity);
		}
		
		private boolean quitLiveCheck(final StorageEntity.Default entity)
		{
			if(this.usedCacheSize == 0)
			{
				this.resetLiveCursor();
				
				logger.trace("StorageChannel#{} completed live check", this.channelIndex);
				this.eventLogger.logLiveCheckComplete(this);

				// report live check completed
				return true;
			}

			// keep last checked entity as a cursor / base / starting point for the next cycle's check
			this.liveCursor = entity;

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
			final StorageEntity.Default       entity   ,
			final StorageEntityCacheEvaluator evaluator,
			final long                        evalTime
		)
		{
			if(!entity.isLive())
			{
				return false;
			}
			
			return evaluator.clearEntityCache(this.usedCacheSize, evalTime, entity);
		}

		// CHECKSTYLE.OFF: FinalParameters: this method is just an outsourced scroll-helper
		static final StorageEntity.Default getNextLiveEntity(StorageEntity.Default entity)
		{
			while(entity != null && !entity.isLive())
			{
				entity = entity.typeNext;
			}
			return entity;
		}
		// CHECKSTYLE.ON: FinalParameters

		@Override
		public boolean issuedEntityCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.internalCacheCheck(
				nanoTimeBudgetBound,
				X.coalesce(entityEvaluator, this.entityCacheEvaluator)
			);
		}

		/**
		 * Returns {@code true} if there are no more oids to mark and {@code false} if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		@Override
		public final boolean issuedGarbageCollection(
			final long           nanoTimeBudgetBound,
			final StorageChannel channel
		)
		{
			if(!gcEnabled)
			{
				return true;
			}

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
		 * Returns {@code true} if there are no more oids to mark and {@code false} if time ran out.
		 * (Meaning the returned boolean effectively means "Was there enough time?")
		 */
		@Override
		public final boolean incrementalGarbageCollection(
			final long           nanoTimeBudgetBound,
			final StorageChannel channel
		)
		{
			if(!gcEnabled)
			{
				return true;
			}

			try
			{
				return this.internalIncrementalGarbageCollection(nanoTimeBudgetBound, channel);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionGarbageCollector("Exception in channel #" + this.channelIndex(), e);
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
			final long           nanoTimeBudgetBound,
			final StorageChannel channel
		)
		{
			// check for completion to abort no-op calls
			if(this.checkForGcCompletion())
			{
				return true;
			}

			// check if there is sweeping to be done
			if(this.markMonitor.needsSweep(this))
			{
				if(!this.sweep())
				{
					// sweep aborted due to locked object registry. Retry on next attempt.
					return false;
				}

				// must check for completion again, otherwise a channel might restart marking beyond a completed gc.
				if(this.checkForGcCompletion())
				{
					return true;
				}

				// check if there is still time to proceed with the next (second) marking right away
				if(System.nanoTime() >= nanoTimeBudgetBound)
				{
					return false;
				}
			}

			// otherwise, mark incrementally until work or time runs out
			if(this.incrementalMark(nanoTimeBudgetBound))
			{
				/* note:
				 * if the markingOidBuffer length is too low, this return is done countless times per millisecond.
				 * Initially, 500 was chosen, but 10000 or 50000 seem to be much more appropriate.
				 */

				// work ran out
				return true;
			}

			// time ran out
			return false;
		}

	}
	
}
