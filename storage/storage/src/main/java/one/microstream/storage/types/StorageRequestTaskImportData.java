package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.concurrency.XThreads;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionImportFailed;
import one.microstream.storage.types.StorageDataFileItemIterator.ItemProcessor;


public interface StorageRequestTaskImportData extends StorageRequestTask
{
	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskImportData, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		/* (14.11.2019 TM)TODO: weird waiting time
		 * This should be removed or at least configurable.
		 */
		private static final int SOURCE_FILE_WAIT_TIME_MS = 100;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XGettingEnum<AFile>           importFiles           ;
		private final StorageEntityCache.Default[]  entityCaches          ;
		private final StorageObjectIdRangeEvaluator objectIdRangeEvaluator;
		
		// adding point for the reader
		private final SourceFileSlice[] sourceFileHeads;
		
		// starting point for the channels to process
		private final SourceFileSlice[] sourceFileTails;

		private AtomicBoolean    complete  = new AtomicBoolean();
		private volatile long    maxObjectId; //TODO Check, why it is not assigned?
		private          Thread  readThread ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                          timestamp             ,
			final int                           channelCount          ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<AFile>           importFiles, 
			final StorageOperationController    controller
		)
		{
			// every channel has to store at least a chunk header, so progress count is always equal to channel count
			super(timestamp, channelCount, controller);
			this.importFiles            = importFiles;
			this.objectIdRangeEvaluator = objectIdRangeEvaluator;
			this.entityCaches           = new StorageEntityCache.Default[channelCount];
			this.sourceFileTails        = createSourceFileSlices(channelCount);
			this.sourceFileHeads        = this.sourceFileTails.clone();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private static SourceFileSlice[] createSourceFileSlices(final int channelCount)
		{
			final SourceFileSlice[] sourceFileTails = new SourceFileSlice[channelCount];
			for(int i = 0; i < channelCount; i++)
			{
				sourceFileTails[i] = new SourceFileSlice(i, null, null);
			}
			
			return sourceFileTails;
		}

		private boolean entityCacheCollectionNotComplete()
		{
			for(final StorageEntityCache.Default entityCache : this.entityCaches)
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

			for(final AFile file : this.importFiles)
			{
//				DEBUGStorage.println("Reader reading source file " + file);
				try
				{
					itemReader.setSourceFile(file);
					AFS.execute(file, rf -> iterator.iterateStoredItems(rf));
					itemReader.completeCurrentSourceFile();
				}
				catch(final Exception e)
				{
					throw new StorageExceptionImportFailed("Exception while reading import file " + file, e);
				}
			}
//			DEBUGStorage.println("* completed reading source files");
			this.complete.set(true);
		}

		
		
		static final class ItemReader implements ItemProcessor
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final StorageEntityCache.Default[] entityCaches             ;
			private final SourceFileSlice[]            sourceFileHeads          ;
			private final ChannelItem[]                channelItems             ;
			private final int                          channelHash              ;
			private       AFile                        file                     ;
			private       int                          currentBatchChannel      ;
			private       long                         currentSourceFilePosition;
			private       long                         maxObjectId              ;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			public ItemReader(
				final StorageEntityCache.Default[] entityCaches   ,
				final SourceFileSlice[]            sourceFileHeads
			)
			{
				super();
				this.entityCaches    = entityCaches              ;
				this.sourceFileHeads = sourceFileHeads           ;
				this.channelHash     = sourceFileHeads.length - 1;
				this.channelItems    = XArrays.fill(
					new ChannelItem[sourceFileHeads.length],
					() ->
						new ChannelItem().resetChains()
				);
			}
			
			@Override
			public boolean accept(final long address, final long availableItemLength)
			{
				final long length = Binary.getEntityLengthRawValue(address);

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
				if(availableItemLength < Binary.entityHeaderLength())
				{
					// signal to calling context that entity cannot be processed and header must be reloaded
					return false;
				}

				final int intLength = X.checkArrayRange(length);

				// read and validate entity head information
				final long                      objectId     = Binary.getEntityObjectIdRawValue(address);
				final int                       channelIndex = (int)objectId & this.channelHash;
				final StorageEntityType.Default type         = this.entityCaches[channelIndex].validateEntity(
					intLength,
					Binary.getEntityTypeIdRawValue(address),
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
				final StorageEntityType.Default type
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
				final StorageEntityType.Default type
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

			final void setSourceFile(final AFile file)
			{
				// next source file is set up
				this.currentBatchChannel       =   -1; // invalid value to guarantee change on first entity.
				this.currentSourceFilePosition =    0; // source file starts at 0, of course.
				this.file                      = file;
			}

			final void completeCurrentSourceFile()
			{
				final SourceFileSlice[]  sourceFileHeads = this.sourceFileHeads;
				final ChannelItem[]      channelItems    = this.channelItems   ;
				for(int i = 0; i < sourceFileHeads.length; i++)
				{
					final SourceFileSlice oldSourceFileHead = sourceFileHeads[i];
					final ChannelItem     currentItem       = channelItems[i];

					sourceFileHeads[i] = sourceFileHeads[i].next =
						new SourceFileSlice(i, this.file, currentItem.headBatch.batchNext)
					;
					currentItem.resetChains();

					// notify storage thread that a new source file is ready for processing
					synchronized(oldSourceFileHead)
					{
						oldSourceFileHead.notifyAll();
					}
				}
			}

		}

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
							if(this.complete.get())
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

				/* (16.04.2016)TODO: storage import interruption handling.
				 * Shouldn't an import be properly interruptible in the first place?
				 * Either change code or comment accordingly.
				 */
				throw new StorageException(e);
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
			final DisruptionCollectorExecuting<StorageClosableFile> closer = DisruptionCollectorExecuting.New(fc ->
				fc.close()
			);
			
			for(final SourceFileSlice s : this.sourceFileTails)
			{
				// the first slice is a dummy with no FileChannel instance
				for(SourceFileSlice file = s; (file = file.next) != null;)
				{
//					DEBUGStorage.println("Closing: " + file);
					closer.executeOn(file);
				}
			}
			
			if(closer.hasDisruptions())
			{
				throw new StorageException(closer.toMultiCauseException());
			}
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// helper classes //
	///////////////////

	static final class ChannelItem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final ImportBatch  headBatch  = new ImportBatch();
		      ImportBatch  tailBatch ;
		      ImportEntity tailEntity;
		      
		      
		      
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		ChannelItem resetChains()
		{
			(this.tailBatch = this.headBatch).next = null;
			this.headBatch.batchNext = null;
			this.tailEntity = null; // gets assigned with the first actual batch
			return this;
		}
		
	}

	static final class SourceFileSlice
	extends StorageChannelFile.Abstract
	implements StorageImportSourceFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final ImportBatch     headBatch   ;
		      SourceFileSlice next        ;
		      
		      
		      
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		SourceFileSlice(
			final int         channelIndex,
			final AFile       file        ,
			final ImportBatch headBatch
		)
		{
			super(file, channelIndex);
			this.headBatch = headBatch;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
			return Integer.toString(this.channelIndex()) + " "
				+ (this.file() == null ? "<Dummy>"  : this.file().toPathString() + " " + this.headBatch)
			;
		}

	}

	/* to optimize the common case where a batch contains only one entity, the batch itself is the first entity.
	 * The additional memory is insignificant if there are many entities and few large batches.
	 * If there are many short batches, then memory is saved by incorporating the first entity.
	 */
	static final class ImportBatch extends ImportEntity implements StorageChannelImportBatch
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		long        batchOffset;
	    long        batchLength;
		ImportBatch batchNext  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ImportBatch()
		{
			super(0, 0, null);
		}

		ImportBatch(
			final long                             batchOffset       ,
			final int                              entityLength      ,
			final long                             objectId          ,
			final StorageEntityType.Default type
		)
		{
			super(entityLength, objectId, type);
			this.batchOffset = batchOffset ;
			this.batchLength = entityLength;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public final ImportEntity first()
		{
			return this.type != null  ? this  : this.batchNext;
		}

		@Override
		public final String toString()
		{
			return "batch" + "[" + this.length + "]" + (this.batchNext == null ? "" : " " + this.batchNext.toString());
		}

	}

	static class ImportEntity implements StorageChannelImportEntity
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final int                       length  ;
		final long                      objectId;
		final StorageEntityType.Default type    ;
		      ImportEntity              next    ;
		      
		      
		      
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ImportEntity(
			final int                       length  ,
			final long                      objectId,
			final StorageEntityType.Default type
		)
		{
			super();
			this.length   = length  ;
			this.objectId = objectId;
			this.type     = type    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int length()
		{
			return this.length;
		}

		@Override
		public final StorageEntityType.Default type()
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
