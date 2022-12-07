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

import one.microstream.afs.types.AWritableFile;
import one.microstream.functional.ThrowingProcedure;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.MemoryRangeReader;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;


/**
 * Public API level type of a storage entity. Used for custom evaluators, filters, etc.
 * Does intentionally not provide any means to load/access the entity's data or change any if its state.
 * This is purely a querying interface, not a means to manipulate data via bypassing normal channels to do so.
 *
 * 
 */
public interface StorageEntity
{
	/**
	 * @return The entity's data length, meaning the pure content length without any header or meta data.
	 *
	 */
	public long dataLength();

	/**
	 * @return The entity's type id.
	 *
	 */
	public long typeId();

	/**
	 * @return The entity's biunique identifying id number.
	 *
	 */
	public long objectId();

	/**
	 * @return The information if this entity's type has reference fields (regardless of a particular entity's actual data).
	 *
	 */
	public boolean hasReferences();

	/**
	 * The length this entity occupies in the cache. This might be vary, even for fixed length typed, from the values
	 * returned by {@link #dataLength()} as only parts of an entity (e.g. only references) might be loaded into cache
	 * and because the cache might hold the header/meta data of an entity as well.
	 *
	 *  @return The length this entity occupies in the cache.
	 */
	public long cachedDataLength();

	/**
	 * The approximate system time that this particular entity has been last touched.
	 * The returned value is compatible to the value returned by {@link System#currentTimeMillis()}.
	 * 
	 *  @return approximate system time that this particular entity has been last touched
	 */
	public long lastTouched();

	public long storagePosition();

	public StorageLiveDataFile storageFile();

	public void copyCachedData(MemoryRangeReader entityDataCollector);

	public long clearCache();

	public boolean iterateReferenceIds(PersistenceObjectIdAcceptor referenceIdIterator);

	public long exportTo(AWritableFile file);



	// (27.07.2015 TM)TODO: move internal/default  StorageEntity implementation to StorageEntityCacheItem
	public final class Default implements StorageEntity
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		// Quite a lot, concept for minimizing overhead and pushing the data off-heap already exists.
//		private static final int MEMORY_CONSUMPTION_BYTES = // 84/120 bytes (+/-coops).
//			XMemory.byteSizeInstance(StorageEntity.Default.class)
//			+ XMemory.byteSizeReference() // take into account oid hash table slot in entity cache
//		;
		
		// currently not used, but might come in handy for other/future intentions
//		public static final int memoryConsumptionBytes()
//		{
//			return MEMORY_CONSUMPTION_BYTES;
//		}

		// enough for ~17 years since class initialization with 256ms resolution.
		private static final long TOUCHED_SHIFT_COUNT  = 8;
		private static final long TOUCHED_START_OFFSET = System.currentTimeMillis();

		/*
		 * GC state meaning:
		 *
		 * black: reachable from root and no references to white nodes (already processed)
		 * gray : reachable from root (marked by procssing) but still to be processed
		 * white: not yet marked, potentially unreachable/"condemned"
		 *
		 * For garbage collection algorithm, see http://en.wikipedia.org/wiki/Garbage_collection_(computer_science)
		 */

		// (14.07.2016 TM)TODO: remove useless initial state after switch to new StorageEntityCache implementation
		static final byte GC_BLACK      = +2; // fully handled by marking
		static final byte GC_GRAY       = +1; // marked but waiting for reference iteration marking
		static final byte GC_INITIAL    =  0; // created/updated. Not marked, but not to be deleted in current GC round.
		static final byte GC_WHITE      = -1; // not marked


		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		/* Handling the cache as direct memory instead of a byte array has several reasons:
		 *
		 * 1.)
		 * the cache never holds any references, but only "plain" data (bytes with primitive values),
		 * so there can never be any interference with the JVM's managed references.
		 *
		 * 2.)
		 * The JLS specifies that arrays do not need to physically span adjacent bytes in memory.
		 * While this is still probably always the case, it's more "cleaner" to handle a range of plain bytes
		 * consisting of variable size element (1,2,4,8,X) as a direct range in memory instead of abusing a
		 * byte array for it.
		 *
		 * 3.)
		 * Having a memory address unifies iteration logic to always work on addresses (like when
		 * using a direct byte buffer) instead of having to always implement two variations
		 * (one for direct memory address for direct byte buffers and one for byte array plus index offset).
		 *
		 * 4.)
		 * this spares the byte array object header, which is 24 bytes each without compressed oops
		 * (16 byte object header plus 4 byte array length plus 4 byte padding overhead).
		 * With millions or billions of (mostly small) entities in cache, this can make a significant saving.
		 *
		 * 5.)
		 * It also increases performance, as no object has to be created, registered, gc-checked (and whatever else
		 * they might do internally), etc. and the array memory range does not have to be nulled out but instead always
		 * gets filled with the actual data right after allocation.
		 * Also, reference iteration via byte array plus index offset is most probably slower than just iterating
		 * over pure memory addresses.
		 *
		 * 6.)
		 * It especially speeds up garbage collection as directly managed memory is not subject to garbage collection.
		 *
		 * The only downside is that this circumvention of memory handling ignores the memory limit
		 * (because the geniuses made Bits#reserveMemory package private ... and who puts the memory limit handling
		 * in the "Bits" util class anyway?).
		 * On the other hand, this even is a rather desired behavior because for something important like a
		 * storing process ("database") that manages entity cache memory consumption anyway, it's more desirable
		 * to take as much memory as it needs for spikes and let the OS swap if necessary instead of just dying
		 * because of some unfortunate temporary memory limit hitting. Yes, this assumed that harddisc drive is
		 * virtually "unlimited" (or at least not fillable by the running application before it is checked again).
		 */

		final long objectId       ;
		long       cacheAddress   ; // oid or address to cached data or 0 for deleted flagging
		int        storagePosition; // the absolute position in the storage file
		int        length         ; // the item's total length
		int        lastTouched    ; // age timestamp for cache clearing evaluation. See methods and constants.
		byte       gcState        ; // gc state
		boolean    isDeleted      ;
		boolean    onlyRefsCached ;
		byte       referenceCount ; // could be combined with hasReferences to a short with ~10 bits free for ref count

		// reference to the type meta data instance and the parent channel file
		TypeInFile typeInFile     ;


		/* (28.11.2016 TM)FIXME: all-same filePrev
		 *  There was a case once, when a issued live check hung in an infinite loop, where
		 *  the live cursor entity and all its successors had the SAME filePrev reference, which is clearly an error.
		 *  Write a test that checks for same filePrev references and let it run as part of the housekeeping.
		 */

		StorageEntity.Default
			filePrev, // the prev in the file, potentially with a gap in between. Required for entity reassignment.
			fileNext, // the next in the file, potentially with a gap in between.
			hashNext, // the next in the oid-lookup hash collision one-way lane.
			typeNext  // the next of the same type. Required for per-type iteration (e.g. export)
		;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		static final Default createDummy()
		{
			return createDummy(null);
		}

		static final Default createDummy(final TypeInFile type)
		{
			/*
			 * dummy entry has "no" object id or "null object id"
			 * length remains at initial value of 0, so it will never be deemed "too big"
			 * typeNext null: explicit statement that head does not reference itself
			 *
			 * Note that there are (2 * channelCount) dummy entity instances, all with an OID of 0:
			 * - every channel has a dummy type to serve as a type head which in turn has a dummy entity as entity head.
			 * - every channel has a root type which has a dummy entity as entity head
			 * also see the TOdO "consolidate rootType and typeHead ..."
			 */
			return new Default(
				0                ,
				type             ,
				null             ,
				(byte)0
			);
		}

		static Default New(
			final long       objectId            ,
			final TypeInFile type                ,
			final Default    hashNext            ,
			final boolean    hasReferences       ,
			final long       simpleReferenceCount
		)
		{
			return new Default(
				objectId                                                    ,
				type                                                        ,
				hashNext                                                    ,
				calculateReferenceCount(hasReferences, simpleReferenceCount)
			);
		}

		/**
		 * Central constructor that maps all initially externally settable values. All others are only internally
		 * initialized (JVM default value or value determined in the constructor)
		 */
		private Default(
			final long                  objectId      ,
			final TypeInFile            type          ,
			final StorageEntity.Default hashNext      ,
			final byte                  referenceCount
		)
		{
			super();
			this.objectId       = objectId         ;
			this.hashNext       = hashNext         ;
			this.lastTouched    = Integer.MAX_VALUE; // initially "touched in eternity", especially for dummy entities.
			this.typeInFile     = type             ;
			this.referenceCount = referenceCount   ;
			this.gcState        = GC_INITIAL       ;
		}

		private static byte calculateReferenceCount(final boolean hasReferences, final long simpleReferenceCount)
		{
			if(!hasReferences)
			{
				return 0; // means no references (value type)
			}
			if(simpleReferenceCount <= 0 || simpleReferenceCount > Byte.MAX_VALUE)
			{
				return -1; // means "normal" references (simple references concept not applicable)
			}
			return (byte)simpleReferenceCount; // apply simple references concept
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final boolean isGcGray()
		{
			return this.gcState == GC_GRAY;
		}

		final void markInitial()
		{
			this.gcState = GC_INITIAL;
		}

		final void markGray()
		{
			this.gcState = GC_GRAY;
		}

		final boolean isGcBlack()
		{
			return this.gcState == GC_BLACK;
		}

		final void markBlack()
		{
			this.gcState = GC_BLACK;
		}

		final void markWhite()
		{
			this.gcState = GC_WHITE;
		}

		final boolean isGcAlreadyHandled()
		{
			return this.gcState >= GC_GRAY;
		}

		final boolean isGcMarked()
		{
			return this.gcState >= GC_INITIAL;
		}

		final boolean hasOnlySimpleReferencesLoaded()
		{
			return this.onlyRefsCached;
		}

		final void setSimpleReferencesLoaded()
		{
			this.onlyRefsCached = true;
		}

		final void clearSimpleReferencesLoaded()
		{
			this.onlyRefsCached = false;
		}

		final int simpleReferenceCount()
		{
			return this.referenceCount;
		}

		final boolean hasSimpleReferences()
		{
			return this.referenceCount > 0; // >0 is important: 0 means no refs, <0 means normal refs!
		}

		final boolean isLive()
		{
			return this.cacheAddress != 0;
		}

		private long calculateSimpleReferenceCacheLength()
		{
			return Binary.entityTotalLength(
				Binary.referenceBinaryLength(this.simpleReferenceCount())
			);
		}

		private boolean ensureCachedReferenceData()
		{
			if(this.isLive())
			{
				// already data present which means at least all reference data must be available already, so abort.
				return false;
			}


			// load only simple references if applicable, otherwise load entity data completely
			if(this.hasSimpleReferences())
			{
				this.loadData(this.calculateSimpleReferenceCacheLength());
				this.setSimpleReferencesLoaded();
			}
			else
			{
				this.internalLoadFullEntityData();
			}
			return true;
		}

		private void ensureCachedFullData()
		{
			if(this.hasOnlySimpleReferencesLoaded())
			{
				// if simple refs have been loaded, clear the cache, load complete length and account for cache increase
				this.internalLoadData(this.length, this.length - this.clearCache());
			}
			else if(!this.isLive())
			{
				// if no cached data is present, just load all entity data completely
				this.internalLoadFullEntityData();
			}
			// already fully loaded, nothing to do

			// (05.04.2016 TM)NOTE: suboptimal order and unnecessary double loading
//			if(this.isLive() && !this.onlyRefsCached)
//			{
//				// already fully loaded, abort
//				return;
//			}
//			else if(this.hasSimpleReferencesLoaded())
//			{
//				// if simple refs have been loaded, clear the cache, load complete length and account for cache increase
//				this.internalLoadData(this.length, this.length - this.clearCache());
//			}
//			// if no cached data is present, just load all entity data completely
//			this.internalLoadFullEntityData();
		}

		private void loadData(final long length)
		{
			this.internalLoadData(length, length);
		}

		private void internalLoadData(final long length, final long cacheChange)
		{
			this.typeInFile.file.loadEntityData(this, length, cacheChange);
		}

		private void internalLoadFullEntityData()
		{
			this.internalLoadData(this.length, this.length);

			// (05.04.2016 TM)TODO: should not be needed here
			this.clearSimpleReferencesLoaded();
		}

		final void detachFromFile()
		{
			this.typeInFile.file.remove(this);
		}

		final void putCacheData(final long sourceAddress, final long length)
		{
			XMemory.copyRange(sourceAddress, this.cacheAddress = XMemory.allocate(length), length);
		}

		final void updateStorageInformation(
			final int length         ,
			final int storagePosition
		)
		{
			this.storagePosition = storagePosition;
			this.length          = length         ;
		}

		final boolean isProper()
		{
			return this.typeInFile != null;
		}

		final boolean isExisting()
		{
			return !this.isDeleted() && this.isProper();
		}

		/**
		 * Note that a deleted entity can never be live as the deletion logic clears the entity's cache first.
		 *
		 */
		final boolean isDeleted()
		{
			return this.isDeleted;
		}

		final void setDeleted()
		{
			this.isDeleted = true;
		}

		final long cacheAddress()
		{
			return this.cacheAddress;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long dataLength()
		{
			return this.length;
		}

		@Override
		public long typeId()
		{
			return this.typeInFile.type.typeId;
		}


		@Override
		public final long storagePosition()
		{
			return this.storagePosition;
		}

		@Override
		public final long cachedDataLength()
		{
			return this.hasOnlySimpleReferencesLoaded() ? this.calculateSimpleReferenceCacheLength() : this.length;
		}

		@Override
		public final boolean hasReferences()
		{
			return this.referenceCount != 0; // both "normal"/"full" (<0) or simple references are loaded.
		}

		@Override
		public final long objectId()
		{
			/* (08.08.2015 TM)NOTE: tests showed no performance penalty for using this method instead of the field itself.
			 * Probably due to this currently being the method's sole implementation and getting lined by the JVM.
			 */
			return this.objectId;
		}

		final void touch()
		{
			this.lastTouched = (int)(System.currentTimeMillis() - TOUCHED_START_OFFSET >>> TOUCHED_SHIFT_COUNT);
		}

		@Override
		public final long lastTouched()
		{
			return TOUCHED_START_OFFSET + ((long)this.lastTouched << TOUCHED_SHIFT_COUNT);
		}

		@Override
		public final StorageLiveDataFile storageFile()
		{
			return this.typeInFile.file;
		}

		@Override
		public final boolean iterateReferenceIds(final PersistenceObjectIdAcceptor referenceIdIterator)
		{
			if(!this.hasReferences())
			{
				// if type has no references at all, abort right away.
				return false;
			}

			final boolean requiredLoading = this.ensureCachedReferenceData();

			/*
			 * must touch for two reasons:
			 * - to update the initial "in eternity" timestamp and make their loaded data unloadable in the future
			 * - to cache-favor entities with references over entities without.
			 */
			this.touch();

			this.typeInFile.type.iterateEntityReferenceIds(this, referenceIdIterator);
			return requiredLoading;
		}

		@Override
		public final long exportTo(final AWritableFile file)
		{
			return this.typeInFile.file.copyTo(file, this.storagePosition, this.length);
		}

		@Override
		public final void copyCachedData(final MemoryRangeReader entityDataCollector)
		{
			this.ensureCachedFullData();
			this.touch();
//			final byte[] buffer = DEBUGStorage.extractMemory(this.cacheAddress(), 32);
			entityDataCollector.readMemory(this.cacheAddress(), this.length);
		}

		@Override
		public final long clearCache()
		{
//			if(this.cacheAddress != 0)
//			{
//				DEBUGStorage.println("Clearing cache of " + this.objectId + " @" + this.cacheAddress);
//				System.out.flush();
//			}
			final long currentDataLength = this.cachedDataLength();
			XMemory.free(this.cacheAddress());
			this.cacheAddress = 0;
			this.onlyRefsCached = false;
			return currentDataLength;
		}

		@Override
		public final String toString()
		{
			return this.objectId()
				+ (this.isLive() ? " L" : "  ")
				+ " GC[" + (this.isGcGray() ? 'G' : ' ') + (this.isGcBlack() ? 'B' : ' ') + ']'
				+ " [" + this.length + "]"
			;
		}

		/* just-in-case finalize() logic is not a good idea because:
		 * 1.) Cache memory consumption must be handled correctly anyway even without/before this method
		 * 2.) This method has a ton of negative aspects associated with it (googleable)
		 * 3.) Above all: every instance of a class implementing it causes the creation of a java.lang.ref.Finalizer
		 *     which requires 64 byte in COOPS mode. Which is almost ridiculous.
		 */

	}

	public final class MaxObjectId implements ThrowingProcedure<StorageEntity, RuntimeException>
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

	public final class MinObjectId implements ThrowingProcedure<StorageEntity, RuntimeException>
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


//	public static void main(final String[] args)
//	{
//		System.out.println(Memory.byteSizeInstance(StorageEntity.Default.class));
//	}

}
