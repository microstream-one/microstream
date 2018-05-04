package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import net.jadoth.math.XMath;
import net.jadoth.storage.exceptions.StorageException;



public interface StorageDataFile<I extends StorageEntityCacheItem<I>> extends StorageLockedChannelFile
{
	public void enqueueEntry(I entry);

	public void remove(StorageEntity.Implementation entity);

	public void loadEntityData(I entity, long length, long cacheChange);

	public long totalLength();

	public long dataLength();

	public double dataFillRatio();

	public long number();

	public long exportTo(StorageLockedFile file);

	public long exportTo(StorageLockedFile file, long sourceOffset, long length);

	public StorageDataFile<I> incrementUserCount();

	public boolean decrementUserCount();

	public boolean hasNoUsers();

	public boolean isHeadFile();



	@Override
	public File file();

	/**
	 * Querying method to check if a storage file consists of only one singular live entity.
	 * This is necessary to avoid dissolving files that are oversized because of one single oversized
	 * entity.
	 *
	 * @return {@literal true} if the file containts exactely one live entity.
	 */
	public boolean hasSingleEntity();



	public final class Implementation implements StorageDataFile<StorageEntity.Implementation>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int INITIAL_TYPE_IN_FILE_ARRAY_LENGTH = 8;



		///////////////////////////////////////////////////////////////////////////
		// static methods   //
		/////////////////////

		public static final StorageDataFile.Implementation Dummy()
		{
			return new StorageDataFile.Implementation(null, null);
		}

		public static final StorageDataFile.Implementation New(
			final StorageFileManager.Implementation parent,
			final StorageInventoryFile              file
		)
		{
			return new StorageDataFile.Implementation(parent, file);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageFileManager.Implementation parent ;

		final StorageInventoryFile file;

		final StorageEntity.Implementation head = StorageEntity.Implementation.createDummy();
		final StorageEntity.Implementation tail = StorageEntity.Implementation.createDummy();

		private int  users           = 1; // data files always start with exactely one user, their parent channel.
		private long fileTotalLength;
		private long fileDataLength ;

		StorageDataFile.Implementation next, prev;

		private TypeInFile[] typeInFileSlots = new TypeInFile[INITIAL_TYPE_IN_FILE_ARRAY_LENGTH];
		private int          typeInFileRange = this.typeInFileSlots.length - 1                  ;
		private int          typeInFileCount                                                    ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		private Implementation(final StorageFileManager.Implementation parent, final StorageInventoryFile file)
		{
			super();
			this.parent        = parent   ;
			this.file          = file     ;
			this.head.fileNext = this.tail;
			this.tail.filePrev = this.head;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final TypeInFile typeInFile(final StorageEntityType.Implementation type)
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

		private TypeInFile createTypeInFile(final StorageEntityType.Implementation type)
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

		final void registerGap(final int length)
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

		final void terminate(final StorageFileWriter writer) throws IOException
		{
			this.close();
			writer.delete(this);
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
		public final long exportTo(final StorageLockedFile file)
		{
			return this.exportTo(file, 0, this.fileTotalLength);
		}

		@Override
		public final long exportTo(final StorageLockedFile file, final long sourceOffset, final long length)
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
				throw new RuntimeException(e); // (23.10.2014 TM)EXCP: proper exception
			}
		}


		@Override
		public final long number()
		{
			return this.file.number();
		}

		@Override
		public final long totalLength()
		{
			return this.fileTotalLength;
		}

		@Override
		public final long dataLength()
		{
			return this.fileDataLength;
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
		public void remove(final StorageEntity.Implementation entity)
		{
			// disjoin entity from chain and decrement data length
			(entity.fileNext.filePrev = entity.filePrev).fileNext = entity.fileNext;
			this.decrementDataLength(entity.length);
		}

		final void removeHeadBoundChain(final StorageEntity.Implementation newFirst, final long copylength)
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
			final StorageEntity.Implementation first,
			final StorageEntity.Implementation last
		)
		{
			// increase content length by length of chain
			this.increaseContentLength(last.storagePosition - first.storagePosition + last.length);

			// enqueue whole chain
			(this.tail.filePrev.fileNext = first).filePrev = this.tail.filePrev;
			(this.tail.filePrev          = last ).fileNext = this.tail;
		}

		final void decrementDataLength(final long value)
		{
			this.fileDataLength -= value;
		}

		@Override
		public final void enqueueEntry(final StorageEntity.Implementation entry)
		{
			(entry.fileNext = this.tail).filePrev = (entry.filePrev = this.tail.filePrev).fileNext = entry;
		}

		@Override
		public final void loadEntityData(
			final StorageEntity.Implementation entity     ,
			final long                         length     ,
			final long                         cacheChange
		)
		{
			this.parent.loadData(this, entity, length, cacheChange);
		}

		@Override
		public synchronized StorageDataFile.Implementation incrementUserCount()
		{
			this.users++;
			return this;
		}

		@Override
		public synchronized boolean decrementUserCount()
		{
			return --this.users == 0;
		}

		@Override
		public synchronized boolean hasNoUsers()
		{
			return this.users == 0;
		}

		@Override
		public boolean isHeadFile()
		{
			return this.parent.isHeadFile(this);
		}

		@Override
		public int channelIndex()
		{
			return this.file.channelIndex();
		}

		@Override
		public FileChannel fileChannel()
		{
			return this.file.fileChannel();
		}

		@Override
		public File file()
		{
			return this.file.file();
		}

		@Override
		public final String toString()
		{
			return this.getClass().getSimpleName() + " " + this.file.file()
				+ " (" + this.fileDataLength + " / " + this.fileTotalLength
				+ ", " + XMath.fractionToPercent(this.dataFillRatio()) + ")"
			;
		}

	}

}
