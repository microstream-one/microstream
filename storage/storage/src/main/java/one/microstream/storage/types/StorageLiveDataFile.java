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
import static one.microstream.math.XMath.notNegative;

import java.util.function.Consumer;

import one.microstream.afs.types.AFile;
import one.microstream.math.XMath;
import one.microstream.storage.exceptions.StorageExceptionConsistency;

public interface StorageLiveDataFile
extends StorageDataFile, StorageLiveChannelFile<StorageLiveDataFile>, StorageCreatableFile
{
	public long totalLength();

	public long dataLength();

	public double dataFillRatio();

	public boolean isHeadFile();


	/**
	 * Querying method to check if a storage file consists of only one singular live entity.
	 * This is necessary to avoid dissolving files that are oversized because of one single oversized
	 * entity.
	 *
	 * @return {@literal true} if the file contains exactly one live entity.
	 */
	public boolean hasSingleEntity();
	
	@Override
	public default StorageBackupDataFile ensureBackupFile(final StorageBackupInventory backupInventory)
	{
		return backupInventory.ensureDataFile(this);
	}
	
	public static StorageLiveDataFile.Default New(
		final StorageFileManager.Default parent       ,
		final StorageDataInventoryFile   inventoryFile
	)
	{
		return new StorageLiveDataFile.Default(
		        notNull(parent)                      ,
			    notNull(inventoryFile.file())        ,
			notNegative(inventoryFile.channelIndex()),
			notNegative(inventoryFile.number())
		);
	}
	
	public class Default
	extends StorageLiveFile.Abstract<StorageLiveDataFile>
	implements StorageLiveDataFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int INITIAL_TYPE_IN_FILE_ARRAY_LENGTH = 8;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int channelIndex;
		
		private final long number;
		
		private final StorageFileManager.Default parent;

		final StorageEntity.Default head = StorageEntity.Default.createDummy();
		final StorageEntity.Default tail = StorageEntity.Default.createDummy();
		
		private long fileTotalLength;
		private long fileDataLength ;

		StorageLiveDataFile.Default next, prev;

		private TypeInFile[] typeInFileSlots = new TypeInFile[INITIAL_TYPE_IN_FILE_ARRAY_LENGTH];
		private int          typeInFileRange = this.typeInFileSlots.length - 1                  ;
		private int          typeInFileCount                                                    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final StorageFileManager.Default parent      ,
			final AFile                      file        ,
			final int                        channelIndex,
			final long                       number
		)
		{
			super(file);
			
			this.channelIndex  = channelIndex;
			this.number        = number      ;
			this.parent        = parent      ;
			this.head.fileNext = this.tail   ;
			this.tail.filePrev = this.head   ;
			
			// must register parent user before anything else can use the instance, of course.
			this.registerUsage(parent);
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}
		
		@Override
		public final long number()
		{
			return this.number;
		}
	
		@Override
		public long dataLength()
		{
			return this.fileDataLength;
		}
		
		@Override
		public long totalLength()
		{
			return this.fileTotalLength;
		}
		

		final TypeInFile typeInFile(final StorageEntityType.Default type)
		{
			// identity equality is enough as every type has a unique instance per channel
			for(TypeInFile t = this.typeInFileSlots[System.identityHashCode(type) & this.typeInFileRange]; t != null; t = t.hashNext)
			{
				if(t.type == type)
				{
					return t;
				}
			}
			return this.createTypeInFile(type);
		}

		private TypeInFile createTypeInFile(final StorageEntityType.Default type)
		{
			if(this.typeInFileCount == this.typeInFileRange)
			{
				this.rebuildTypeInFileTable();
			}
			final TypeInFile newInstance = this.typeInFileSlots[System.identityHashCode(type) & this.typeInFileRange] =
				new TypeInFile(type, this, this.typeInFileSlots[System.identityHashCode(type) & this.typeInFileRange])
			;
			this.typeInFileCount++;
			return newInstance;
		}

		private void rebuildTypeInFileTable()
		{
			final int newModulo;
			final TypeInFile[] newSlots = new TypeInFile[(newModulo = (this.typeInFileRange + 1 << 1) - 1) + 1];

			for(TypeInFile entries : this.typeInFileSlots)
			{
				for(TypeInFile next; entries != null; entries = next)
				{
					next = entries.hashNext;
					entries.hashNext = newSlots[System.identityHashCode(entries) & newModulo];
					newSlots[System.identityHashCode(entries) & newModulo] = entries;
				}
			}
			this.typeInFileSlots = newSlots;
			this.typeInFileRange   = newModulo;
		}


		final void detach()
		{
			(this.prev.next = this.next).prev = this.prev;
		}

		final void registerGapLength(final long length)
		{
			this.fileTotalLength += length;
		}

		final boolean needsRetirement(final StorageDataFileEvaluator configuration)
		{
			return configuration.needsRetirement(this.fileTotalLength);
		}

		final boolean hasNoDataBytes()
		{
			return this.fileDataLength == 0L;
		}

		final boolean hasNoBytes()
		{
			return this.fileTotalLength == 0L;
		}

		final boolean hasOnlyGapBytes()
		{
			return this.fileDataLength == 0L && this.fileTotalLength > 0L;
		}

		final boolean hasContent()
		{
			// file is really only empty if there are no more entities to transfer
			return this.fileDataLength != 0L;
		}

		final void increaseContentLength(final long byteCount)
		{
			this.fileTotalLength += byteCount;
			this.fileDataLength  += byteCount;
		}

		public boolean executeIfUnsuedData(final Consumer<? super StorageLiveDataFile.Default> action)
		{
			// cheat :D
			return this.executeIfUnsued(file ->
				action.accept(this)
			);
		}
		
		public final boolean unregisterUsageClosingData(
			final StorageFileUser                               fileUser     ,
			final Consumer<? super StorageLiveDataFile.Default> closingAction
		)
		{
			return this.unregisterUsageClosing(fileUser, file ->
				closingAction.accept(this)
			);
		}
		
		@Override
		protected synchronized boolean internalOpenWriting()
		{
			final boolean wasNewlyOpened = super.internalOpenWriting();
			if(!wasNewlyOpened)
			{
				return false;
			}

			final long expectedSize = this.totalLength();
			if(expectedSize == 0)
			{
				// size validation is skipped for expected size 0. Applies when initially opening data files.
				return true;
			}
			
			final long actualSize = this.ensureReadable().size();

			if(actualSize != expectedSize)
			{
				throw new StorageExceptionConsistency(
					"Reopened file has inconsistent size: Expected size " + expectedSize
					+ " != actual size " + actualSize + "."
				);
			}
			
			return true;
		}

		@Override
		public final double dataFillRatio()
		{
			return (double)this.fileDataLength / this.fileTotalLength;
		}

		@Override
		public final boolean hasSingleEntity()
		{
			// if the first entity (always head's next) amounts to the whole data length, it must be the only entity.
			return this.head.fileNext.length == this.fileDataLength;
		}

		public final void remove(final StorageEntity.Default entity)
		{
			// disjoin entity from chain and decrement data length
			(entity.fileNext.filePrev = entity.filePrev).fileNext = entity.fileNext;
			this.decrementDataLength(entity.length);
		}

		final void removeHeadBoundChain(final StorageEntity.Default newFirst, final long copylength)
		{
			// check for special case of completely clearing a file (no new first means empty file)

			// these checks are more or less for debugging purposes, as it can never yield true if the logic is correct.
			if(newFirst == this.tail)
			{
				if(copylength != this.dataLength())
				{
					throw new StorageExceptionConsistency("Inconsistent file clearing transfer length of " + copylength + " in " + this);
				}
			}
			else if(copylength >= this.dataLength())
			{
				throw new StorageExceptionConsistency("Inconsistent file partial transfer length of " + copylength + " in " + this);
			}

			// copy length can't be derived from newFirst's position because of potential gap before it.
			this.decrementDataLength(copylength);
			(this.head.fileNext = newFirst).filePrev = this.head;
		}

		final void addChainToTail(
			final StorageEntity.Default first,
			final StorageEntity.Default last
		)
		{
			// enqueue whole chain
			(this.tail.filePrev.fileNext = first).filePrev = this.tail.filePrev;
			(this.tail.filePrev          = last ).fileNext = this.tail;
		}

		final void decrementDataLength(final long value)
		{
			this.fileDataLength -= value;
		}

		public final void prependEntry(final StorageEntity.Default entry)
		{
			// moved here from StorageEntity.Default#updateStorageInformation
			entry.typeInFile = this.typeInFile(entry.typeInFile.type);
			
			// entry gets appended after the start (the head), hence reverse-building the order.
			(entry.filePrev = this.head).fileNext = (entry.fileNext = this.head.fileNext).filePrev = entry;
		}

		public final void appendEntry(final StorageEntity.Default entry)
		{
			// moved here from StorageEntity.Default#updateStorageInformation
			entry.typeInFile = this.typeInFile(entry.typeInFile.type);

            // entry gets appended before the end (the tail), hence forward-building the order
			(entry.fileNext = this.tail).filePrev = (entry.filePrev = this.tail.filePrev).fileNext = entry;
		}

		public final void loadEntityData(
			final StorageEntity.Default entity     ,
			final long                  length     ,
			final long                  cacheChange
		)
		{
			this.parent.loadData(this, entity, length, cacheChange);
		}
		
		@Override
		public boolean isHeadFile()
		{
			return this.parent.isHeadFile(this);
		}

		@Override
		public final String toString()
		{
			return this.getClass().getSimpleName() + " " + this.file().identifier()
				+ " (" + this.fileDataLength + " / " + this.fileTotalLength
				+ ", " + XMath.fractionToPercent(this.dataFillRatio()) + ")"
			;
		}
		
	}
	
}
