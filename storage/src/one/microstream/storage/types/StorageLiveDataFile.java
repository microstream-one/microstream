package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import java.io.IOException;
import java.util.function.Consumer;

import one.microstream.afs.AFile;
import one.microstream.collections.XArrays;
import one.microstream.math.XMath;
import one.microstream.storage.exceptions.StorageException;

public interface StorageLiveDataFile<I extends StorageEntityCacheItem<I>> extends StorageDataFile, StorageLiveFile
{
	public long size();
	
	public void close();
	
	

	public boolean hasUsers();
	
	public boolean executeIfUnsued(Consumer<? super StorageLiveDataFile<?>> action);
	
	public boolean registerUsage(StorageFileUser fileUser);
	
	public boolean clearUsages(StorageFileUser fileUser);
	
	public boolean unregisterUsage(StorageFileUser fileUser);
		
	public boolean unregisterUsageClosing(
		StorageFileUser                          fileUser     ,
		Consumer<? super StorageLiveDataFile<?>> closingAction
	);
	
	public void prependEntry(I entry);
	
	public void appendEntry(I entry);

	public void remove(StorageEntity.Default entity);

	public void loadEntityData(I entity, long length, long cacheChange);

	public long totalLength();

	public long dataLength();

	public double dataFillRatio();
	
	public long exportTo(ZStorageLockedFile file);

	public long exportTo(ZStorageLockedFile file, long sourceOffset, long length);

	public boolean isHeadFile();


	/**
	 * Querying method to check if a storage file consists of only one singular live entity.
	 * This is necessary to avoid dissolving files that are oversized because of one single oversized
	 * entity.
	 *
	 * @return {@literal true} if the file containts exactely one live entity.
	 */
	public boolean hasSingleEntity();

	
	
	public static StorageLiveDataFile<StorageEntity.Default> New(
		final StorageFileManager.Default parent      ,
		final AFile                      file        ,
		final int                        channelIndex,
		final long                       number
	)
	{
		return new StorageLiveDataFile.Default(
		        notNull(parent)      ,
			    notNull(file)        ,
			notNegative(channelIndex),
			notNegative(number)
		);
	}
	
	public class Default
	extends StorageDataFile.Abstract
	implements StorageLiveDataFile<StorageEntity.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int INITIAL_TYPE_IN_FILE_ARRAY_LENGTH = 8;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileManager.Default parent;

		final StorageEntity.Default head = StorageEntity.Default.createDummy();
		final StorageEntity.Default tail = StorageEntity.Default.createDummy();
		
		private long fileTotalLength;
		private long fileDataLength ;

		ZStorageDataFile.Default next, prev;

		private TypeInFile[] typeInFileSlots = new TypeInFile[INITIAL_TYPE_IN_FILE_ARRAY_LENGTH];
		private int          typeInFileRange = this.typeInFileSlots.length - 1                  ;
		private int          typeInFileCount                                                    ;
		
		private Usage[] usages     = new Usage[1];
		private int     usagesSize = 0;
		
		static final class Usage
		{
			StorageFileUser user ;
			int             count;
			
			Usage(final StorageFileUser user)
			{
				super();
				this.user = user;
			}
			
			final boolean increment()
			{
				return ++this.count == 1;
			}
			
			final boolean decrement()
			{
				return --this.count == 0;
			}
			
		}

		
		
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
			super(file, channelIndex, number);
			
			this.parent        = parent   ;
			this.head.fileNext = this.tail;
			this.tail.filePrev = this.head;
			
			// must register parent user before anything else can use the instance, of course.
			this.registerUsage(parent);
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		

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



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized long size()
		{
			this.internalOpen();
			
			return this.access().size();
		}
		
		@Override
		public final synchronized void close()
		{
			this.internalClose();
		}

		@Override
		public final long exportTo(final ZStorageLockedFile file)
		{
			return this.exportTo(file, 0, this.fileTotalLength);
		}

		@Override
		public final long exportTo(final ZStorageLockedFile file, final long sourceOffset, final long length)
		{
			/*
			 * export copies directly without detour over parent/writer.
			 * This behavior has design reasons, e.g. an export must be always possible, even if the writer
			 * instance is strictly read-only. It also has performance reasons as the per-type export
			 * calls this method for every single entity, hence less pointer chasing and no channel forcing
			 * speeds up things considerably for millions or billions of entities.
			 */
			try
			{
				return this.fileChannel().transferTo(sourceOffset, length, file.fileChannel());
			}
			catch(final IOException e)
			{
				// (23.10.2014 TM)EXCP: proper exception
				throw new StorageException(e);
			}
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

		@Override
		public void remove(final StorageEntity.Default entity)
		{
			// disjoin entity from chain and decrement data length
			(entity.fileNext.filePrev = entity.filePrev).fileNext = entity.fileNext;
			this.decrementDataLength(entity.length);
		}

		final void removeHeadBoundChain(final StorageEntity.Default newFirst, final long copylength)
		{
			// check for special case of completely clearing a file (no new first means empty file)

			// these checks are moreless for debugging purposes, as it can never yield true if the logic is correct.
			if(newFirst == this.tail)
			{
				if(copylength != this.dataLength())
				{
					// (11.02.2015 TM)TODO: proper exception
					throw new StorageException("Inconsistent file clearing transfer length of " + copylength + " in " + this);
				}
			}
			else if(copylength >= this.dataLength())
			{
				// (11.02.2015 TM)TODO: proper exception
				throw new StorageException("Inconsistent file partial transfer length of " + copylength + " in " + this);
			}

			// copy length can't be derived from newFirst's position because of potantial gap before it.
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

		@Override
		public final void prependEntry(final StorageEntity.Default entry)
		{
			// moved here from StorageEntity.Default#updateStorageInformation
			entry.typeInFile = this.typeInFile(entry.typeInFile.type);
			
			// entry gets appended after the start (the head), hence reverse-building the order.
			(entry.filePrev = this.head).fileNext = (entry.fileNext = this.head.fileNext).filePrev = entry;
		}

		@Override
		public final void appendEntry(final StorageEntity.Default entry)
		{
			// moved here from StorageEntity.Default#updateStorageInformation
			entry.typeInFile = this.typeInFile(entry.typeInFile.type);
			
//			// entry gets appended before the end (the tail), hence forward-building the order
			(entry.fileNext = this.tail).filePrev = (entry.filePrev = this.tail.filePrev).fileNext = entry;
		}

		@Override
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
		public final synchronized boolean hasUsers()
		{
			return this.usagesSize != 0;
		}
		
		@Override
		public final synchronized boolean executeIfUnsued(
			final Consumer<? super StorageLiveDataFile<?>> action
		)
		{
			if(this.hasUsers())
			{
				return false;
			}
			
			action.accept(this);
			
			return true;
		}

		@Override
		public final synchronized boolean registerUsage(final StorageFileUser fileUser)
		{
			return this.ensureEntry(fileUser).increment();
		}
		
		private Usage ensureEntry(final StorageFileUser fileUser)
		{
			/* note: for very short arrays (~ 5 references), arrays are faster than hash tables.
			 * The planned/expected user count for storage files should be something around 2-3.
			 */
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					return this.usages[i];
				}
			}
			
			if(this.usagesSize >= this.usages.length)
			{
				this.usages = XArrays.enlarge(this.usages, this.usages.length * 2);
			}
			
			return this.usages[this.usagesSize++] = new Usage(fileUser);
		}
		
		@Override
		public final synchronized boolean clearUsages(final StorageFileUser fileUser)
		{
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					XArrays.removeFromIndex(this.usages, this.usagesSize--, i);
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public final synchronized boolean unregisterUsage(final StorageFileUser fileUser)
		{
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					if(this.usages[i].decrement())
					{
						XArrays.removeFromIndex(this.usages, this.usagesSize--, i);
						return true;
					}
					
					return false;
				}
			}
			
			// (29.11.2019 TM)EXCP: proper exception
			throw new StorageException(StorageFileUser.class.getSimpleName() + " not found " + fileUser);
		}
				
		@Override
		public boolean unregisterUsageClosing(
			final StorageFileUser                          fileUser     ,
			final Consumer<? super StorageLiveDataFile<?>> closingAction
		)
		{
			if(this.unregisterUsage(fileUser) && !this.hasUsers())
			{
				if(closingAction != null)
				{
					closingAction.accept(this);
				}
				
				this.internalClose();
				return true;
			}
			
			return false;
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
