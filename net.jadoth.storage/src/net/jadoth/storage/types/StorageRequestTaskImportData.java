package net.jadoth.storage.types;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.XArrays;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.concurrency.XThreads;
import net.jadoth.file.XFiles;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.storage.types.StorageDataFileItemIterator.ItemProcessor;
import net.jadoth.util.XVM;


public interface StorageRequestTaskImportData extends StorageRequestTask
{
	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskImportData, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		private static final int SOURCE_FILE_WAIT_TIME_MS = 100;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final XGettingEnum<File>                  importFiles           ;
		private final StorageEntityCache.Implementation[] entityCaches          ;
		private final StorageObjectIdRangeEvaluator       objectIdRangeEvaluator;
		
		// adding point for the reader
		private final SourceFileSlice[] sourceFileHeads;
		
		// starting point for the channels to process
		private final SourceFileSlice[] sourceFileTails;

		private volatile boolean complete   ;
		private volatile long    maxObjectId;
		private          Thread  readThread ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final long                          timestamp             ,
			final int                           channelCount          ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<File>            importFiles
		)
		{
			// every channel has to store at least a chunk header, so progress count is always equal to channel count
			super(timestamp, channelCount);
			this.importFiles = importFiles  ;

			this.entityCaches    = new StorageEntityCache.Implementation[channelCount];
			this.sourceFileTails = new SourceFileSlice[channelCount];
			for(int i = 0; i < channelCount; i++)
			{
				this.sourceFileTails[i] = new SourceFileSlice(i, null, null, null);
			}
			this.sourceFileHeads        = this.sourceFileTails.clone();
			this.objectIdRangeEvaluator = objectIdRangeEvaluator;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private boolean entityCacheCollectionNotComplete()
		{
			for(final StorageEntityCache.Implementation entityCache : this.entityCaches)
			{
				if(entityCache == null)
				{
					return true;
				}
			}
			return false;
		}

		private synchronized void ensureReaderThread()
		{
			if(this.readThread != null || this.entityCacheCollectionNotComplete())
			{
				return;
			}
			this.readThread = XThreads.start(this::readFiles);
		}

		final void readFiles()
		{
			final ItemReader itemReader = new ItemReader(this.entityCaches, this.sourceFileHeads);
			final StorageDataFileItemIterator iterator = StorageDataFileItemIterator.New(
				StorageDataFileItemIterator.BufferProvider.New(),
				itemReader
			);

			for(final File file : this.importFiles)
			{
//				DEBUGStorage.println("Reader reading source file " + file);
				try
				{
					// channel must be closed by StorageChannel after copying has been completed.
					final FileChannel channel = StorageLockedFile.openFileChannel(file).channel();
					itemReader.setSourceFile(file, channel);
					iterator.iterateStoredItems(channel, 0, channel.size());
					itemReader.completeCurrentSourceFile();
				}
				catch(final Exception e)
				{
					// (16.07.2014)EXCP: proper exception
					throw new RuntimeException("Exception while reading import file " + file, e);
				}
			}
//			DEBUGStorage.println("* completed reading source files");
			this.complete = true;
		}

		
		
		static final class ItemReader implements ItemProcessor
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final StorageEntityCache.Implementation[] entityCaches             ;
			private final SourceFileSlice[]                   sourceFileHeads          ;
			private final ChannelItem[]                       channelItems             ;
			private final int                                 channelHash              ;
			private       File                                file                     ;
			private       FileChannel                         sourceFileChannel        ;
			private       int                                 currentBatchChannel      ;
			private       long                                currentSourceFilePosition;
			private       long                                maxObjectId              ;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			public ItemReader(
				final StorageEntityCache.Implementation[] entityCaches   ,
				final SourceFileSlice[]                        sourceFileHeads
			)
			{
				super();
				this.entityCaches    = entityCaches                           ;
				this.sourceFileHeads = sourceFileHeads                        ;
				this.channelHash     = sourceFileHeads.length - 1             ;
				this.channelItems    = XArrays.fill(
					new ChannelItem[sourceFileHeads.length],
					() ->
						new ChannelItem().resetChains()
				);
			}
			
			@Override
			public boolean accept(final long address, final long availableItemLength)
			{
				final long length = BinaryPersistence.getEntityLength(address);

				// check for a gap
				if(length < 0)
				{
//					DEBUGStorage.println("Gap    @" + this.currentSourceFilePosition + " [" + -length + "]");

					// keep track of current source file position to offset the next batch correctly
					this.currentSourceFilePosition += X.checkArrayRange(-length);

					// batch is effectively interrupted by the gap, even if the next entity belongs to the same channel
					this.currentBatchChannel = -1;

					// signal to calling context that item has been processed completely
					return true;
				}

				// check for incomplete entity header
				if(availableItemLength < BinaryPersistence.entityHeaderLength())
				{
					// signal to calling context that entity cannot be processed and header must be reloaded
					return false;
				}

				final int intLength = X.checkArrayRange(length);

				// read and validate entity head information
				final long                             objectId     = BinaryPersistence.getEntityObjectId(address);
				final int                              channelIndex = (int)objectId & this.channelHash;
				final StorageEntityType.Implementation type         = this.entityCaches[channelIndex].validateEntity(
					intLength,
					BinaryPersistence.getEntityTypeId(address),
					objectId
				);

				// register entity accordingly (either new batch required or current batch can be enlarged)
				if(channelIndex != this.currentBatchChannel)
				{
					this.currentBatchChannel = channelIndex;
					this.startNewBatch(intLength, objectId, type);
				}
				else
				{
//					DEBUGStorage.println(
//						"Reader ADD batch Entity @" + this.currentSourceFilePosition
//						+ " [" + length + "] (" + this.currentBatchChannel + ") " + objectId
//					);
					this.addToCurrentBatch(intLength, objectId, type);
				}

				if(objectId >= this.maxObjectId)
				{
					this.maxObjectId = objectId;
				}

				// keep track of current source file position to offset the batch correctly
				this.currentSourceFilePosition += intLength;

				return true;
			}

			private void startNewBatch(
				final int                              length  ,
				final long                             objectId,
				final StorageEntityType.Implementation type
			)
			{
				final ChannelItem item = this.channelItems[this.currentBatchChannel];

//				DEBUGStorage.println(
//					"Reader NEW batch Entity @" + this.currentSourceFilePosition
//					+ " [" + length + "] (" + this.currentBatchChannel + ") " + objectId
//				);
				item.tailEntity = item.tailBatch = item.tailBatch.batchNext = new ImportBatch(
					this.currentSourceFilePosition,
					length,
					objectId,
					type
				);
			}

			private void addToCurrentBatch(
				final int                              length  ,
				final long                             objectId,
				final StorageEntityType.Implementation type
			)
			{
				final ChannelItem item = this.channelItems[this.currentBatchChannel];

				// intentionally ignores max file length for sake of import efficiency
				item.tailEntity = item.tailEntity.next = new ImportEntity(
					length,
					objectId,
					type
				);

				// update batch length and total file length
				item.tailBatch.batchLength += length;
			}

			final void setSourceFile(final File file, final FileChannel fileChannel)
			{
				// next source file is set up
				this.currentBatchChannel       =          -1; // invalid value to guarantee change on first entity.
				this.currentSourceFilePosition =           0; // source file starts at 0, of course.
				this.file                      =        file;
				this.sourceFileChannel         = fileChannel; // keep file channel reference.
			}

			final void completeCurrentSourceFile()
			{
				final SourceFileSlice[]  sourceFileHeads = this.sourceFileHeads;
				final ChannelItem[]      channelItems    = this.channelItems   ;
				for(int i = 0; i < sourceFileHeads.length; i++)
				{
					final SourceFileSlice  oldSourceFileHead = sourceFileHeads[i];
					final ChannelItem currentItem       = channelItems[i];

					sourceFileHeads[i] = sourceFileHeads[i].next =
						new SourceFileSlice(i, this.file, this.sourceFileChannel, currentItem.headBatch.batchNext)
					;
					currentItem.resetChains();

					// notify storage thread that a new source file is ready for processing
					synchronized(oldSourceFileHead)
					{
						oldSourceFileHead.notify();
					}
				}
			}

		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			/*
			 * signal the channel to prepare for the import
			 * (validate type dictionary, keep current head file and create a new one)
			 */
			synchronized(this.entityCaches)
			{
				this.entityCaches[channel.channelIndex()] = channel.prepareImportData();
			}

			/*
			 * the last thread to enter this method starts a single reader thread,
			 * all other threads return here right away
			 */
			this.ensureReaderThread();

			// the tail array is always initialized with an empty dummy source file which serves as an entry point.
			SourceFileSlice currentSourceFile = this.sourceFileTails[channel.channelIndex()];

			// quite a braces mountain, however it is logically necessary
			try
			{
				importLoop:
				while(true)
				{
					// acquire a lock on the channel-exclusive signalling instance to wait for the reader's notification
					synchronized(currentSourceFile)
					{
						// wait for the next batch to import (successor of the current batch)
						while(currentSourceFile.next == null)
						{
							if(this.complete)
							{
//								DEBUGStorage.println(channel.channelIndex() + " done importing.");
								// there will be no more next source file, so abort (task is complete)
								break importLoop;
							}
//							DEBUGStorage.println(channel.channelIndex() + " waiting for next on " + currentSourceFile);
							// better check again after some time, indefinite wait caused a deadlock once
							// (16.04.2016)TODO: isn't the above comment a bug? Test and change or comment better.
							currentSourceFile.wait(SOURCE_FILE_WAIT_TIME_MS);
							// note: completion adds an empty dummy source file to avoid special case handling here
						}
						// at this point, there definitely is a new/next batch to process, so advance tail and process
						currentSourceFile = currentSourceFile.next;
					}

//					DEBUGStorage.println(channel.channelIndex() + " importData() " + currentSourceFile);
					// process the batch outside the lock to not block the central reader thread by channel-local work
					channel.importData(currentSourceFile);
				}
			}
			catch(final InterruptedException e)
			{
				// being interrupted is a normal problem here, causing to abort the task, no further handling required.

				/* damn checked exceptios:
				 * for clean architecture wihtout maintenance-error-prone redundant code,
				 * the checked exception must be rethrown unchecked.
				 * See calling context (addProblem() and incrementCompletionProgress())
				 */
				/* (16.04.2016)TODO: if it is a normal problem, there should be a proper wrapping exception for it
				 * instead of hacking the JVM.
				 * Also, shouldn't an import be properly interruptible in the first place?
				 */
				XVM.throwUnchecked(e);

				// safety net error, may never be reached if the cheating method call works as intended.
				throw new Error(e);
			}

			return null;
		}

		@Override
		protected final void succeed(final StorageChannel channel, final Void result)
		{
			// evaluate (validate or update if possible) objectId before committing the import
			this.objectIdRangeEvaluator.evaluateObjectIdRange(0, this.maxObjectId);

			/* on success, signal the channel to commit the imported data (register entities in cache)
			 * All channels use the same timestamp (this task's issuing timestamp) for consistency checks
			 */
			channel.commitImportData(this.timestamp());
		}

		@Override
		protected void postCompletionSuccess(final StorageChannel channel, final Void result)
			throws InterruptedException
		{
			this.cleanUpResources();
		}

		@Override
		protected final void fail(final StorageChannel channel, final Void result)
		{
			// on failure/abort, signal channel to rollback (delete newly created files and revert to last head file)
			this.cleanUpResources();
			channel.rollbackImportData(this.problemForChannel(channel));
		}

		private void cleanUpResources()
		{
			for(final SourceFileSlice s : this.sourceFileTails)
			{
				// the first slice is a dummy with no FileChannel instance
				for(SourceFileSlice file = s; (file = file.next) != null;)
				{
//					DEBUGStorage.println("Closing silently: " + file);
					XFiles.closeSilent(file.fileChannel);
				}
			}
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// helper classes   //
	/////////////////////

	static final class ChannelItem
	{
		final ImportBatch  headBatch  = new ImportBatch();
		      ImportBatch  tailBatch ;
		      ImportEntity tailEntity;

		ChannelItem resetChains()
		{
			(this.tailBatch = this.headBatch).next = null;
			this.headBatch.batchNext = null;
			this.tailEntity = null; // gets assigned with the first actual batch
			return this;
		}
	}

	static final class SourceFileSlice implements StorageChannelImportSourceFile
	{
		final int             channelIndex;
		final File            file        ;
		final FileChannel     fileChannel ;
		final ImportBatch     headBatch   ;
		      SourceFileSlice next        ;

		SourceFileSlice(
			final int         channelIndex,
			final File        file        ,
			final FileChannel fileChannel ,
			final ImportBatch headBatch
		)
		{
			super();
			this.channelIndex = channelIndex;
			this.file         = file        ;
			this.fileChannel  = fileChannel ;
			this.headBatch    = headBatch   ;
		}

		@Override
		public File file()
		{
			return this.file;
		}

		@Override
		public final FileChannel fileChannel()
		{
			return this.fileChannel;
		}

		@Override
		public final void iterateBatches(final Consumer<? super StorageChannelImportBatch> iterator)
		{
			for(ImportBatch batch = this.headBatch; batch != null; batch = batch.batchNext)
			{
				iterator.accept(batch);
			}
		}

		@Override
		public String toString()
		{
			return Integer.toString(this.channelIndex) + " "
				+ (this.file == null ? "<Dummy>"  : this.file.toString() + " " + this.headBatch)
			;
		}

	}

	/* to optimize the common case where a batch contains only one entity, the batch itself is the first entity.
	 * The additional memory is insignificant if there are many entities and few large batches.
	 * If there are many short batches, then memory is saved by incorporating the first entity.
	 */
	static final class ImportBatch extends ImportEntity implements StorageChannelImportBatch
	{
		long        batchOffset;
	    long        batchLength;
		ImportBatch batchNext  ;

		ImportBatch()
		{
			super(0, 0, null);
		}

		ImportBatch(
			final long                             batchOffset       ,
			final int                              entityLength      ,
			final long                             objectId          ,
			final StorageEntityType.Implementation type
		)
		{
			super(entityLength, objectId, type);
			this.batchOffset = batchOffset ;
			this.batchLength = entityLength;
		}

		@Override
		public long fileOffset()
		{
			return this.batchOffset;
		}

		@Override
		public final long fileLength()
		{
			return this.batchLength;
		}

		@Override
		public final void iterateEntities(final Consumer<? super StorageChannelImportEntity> iterator)
		{
			for(ImportEntity e = this.first(); e != null; e = e.next)
			{
				iterator.accept(e);
			}
		}

		@Override
		public ImportEntity first()
		{
			return this.type != null  ? this  : this.batchNext;
		}

		@Override
		public String toString()
		{
			return "batch" + "[" + this.length + "]" + (this.batchNext == null ? "" : " " + this.batchNext.toString());
		}

	}

	static class ImportEntity implements StorageChannelImportEntity
	{
		final int                              length  ;
		final long                             objectId;
		final StorageEntityType.Implementation type    ;
		      ImportEntity                     next    ;

		ImportEntity(
			final int                              length  ,
			final long                             objectId,
			final StorageEntityType.Implementation type
		)
		{
			super();
			this.length   = length  ;
			this.objectId = objectId;
			this.type     = type    ;
		}

		@Override
		public final int length()
		{
			return this.length;
		}

		@Override
		public final StorageEntityType.Implementation type()
		{
			return this.type;
		}

		@Override
		public final long objectId()
		{
			return this.objectId;
		}

		@Override
		public final StorageChannelImportEntity next()
		{
			return this.next;
		}

	}

}
