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

import java.util.concurrent.atomic.AtomicBoolean;

import one.microstream.X;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.concurrency.XThreads;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionImportFailed;

public interface StorageRequestTaskImportData<S> extends StorageRequestTask
{
	public static abstract class Abstract<S>
	extends    StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskImportData<S>, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		/* (14.11.2019 TM)TODO: weird waiting time
		 * This should be removed or at least configurable.
		 */
		private static final int SOURCE_WAIT_TIME_MS = 100;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XGettingEnum<S>               sources               ;
		private final StorageEntityCache.Default[]  entityCaches          ;
		private final StorageObjectIdRangeEvaluator objectIdRangeEvaluator;
		
		// adding point for the reader
		private final StorageImportSource.Abstract[] sourceHeads;
		
		// starting point for the channels to process
		private final StorageImportSource.Abstract[] sourceTails;

		private final    AtomicBoolean complete  = new AtomicBoolean();
		private volatile long          maxObjectId;
		private          Thread        readThread ;
		Abstract(
			final long                          timestamp             ,
			final int                           channelCount          ,
			final StorageOperationController    controller            ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<S>               sources
		)
		{
			// every channel has to store at least a chunk header, so progress count is always equal to channel count
			super(timestamp, channelCount, controller);
			this.sources                = sources;
			this.objectIdRangeEvaluator = objectIdRangeEvaluator;
			this.entityCaches           = new StorageEntityCache.Default[channelCount];
			this.sourceTails            = this.createImportSources(channelCount);
			this.sourceHeads            = this.sourceTails.clone();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private StorageImportSource.Abstract[] createImportSources(final int channelCount)
		{
			final StorageImportSource.Abstract[] inputSources = new StorageImportSource.Abstract[channelCount];
			for(int i = 0; i < channelCount; i++)
			{
				inputSources[i] = this.createImportSource(i, null, null);
			}
			
			return inputSources;
		}
		
		protected abstract StorageImportSource.Abstract createImportSource(
			int                               channelIndex,
			S                                 source      ,
			StorageChannelImportBatch.Default headBatch
		);
		
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
			this.readThread = XThreads.start((Runnable)this::readSources);
		}

		final void readSources()
		{
			final ItemReader itemReader = new ItemReader(this.entityCaches, this.sourceHeads);

			for(final S source : this.sources)
			{
				try
				{
					itemReader.setSource(source);
					this.iterateSource(source, itemReader);
					itemReader.completeCurrentSource();
				}
				catch(final Exception e)
				{
					throw new StorageExceptionImportFailed("Exception while reading import source " + source, e);
				}
			}
			this.complete.set(true);
		}
		
		protected abstract void iterateSource(S source, ItemAcceptor itemAcceptor);

		@FunctionalInterface
		public interface ItemAcceptor
		{
			public boolean accept(long address, long availableItemLength);
		}
		
		private final class ItemReader implements ItemAcceptor
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final StorageEntityCache.Default[]   entityCaches         ;
			private final StorageImportSource.Abstract[] sourceHeads          ;
			private final ChannelItem[]                  channelItems         ;
			private final int                            channelHash          ;
			private       S                              source               ;
			private       int                            currentBatchChannel  ;
			private       long                           currentSourcePosition;
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			public ItemReader(
				final StorageEntityCache.Default[]   entityCaches,
				final StorageImportSource.Abstract[] sourceHeads
			)
			{
				super();
				this.entityCaches = entityCaches          ;
				this.sourceHeads  = sourceHeads           ;
				this.channelHash  = sourceHeads.length - 1;
				this.channelItems = XArrays.fill(
					new ChannelItem[sourceHeads.length],
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

					// keep track of current source position to offset the next batch correctly
					this.currentSourcePosition += X.checkArrayRange(-length);

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
					this.addToCurrentBatch(intLength, objectId, type);
				}

				if(objectId >= StorageRequestTaskImportData.Abstract.this.maxObjectId)
				{
					 StorageRequestTaskImportData.Abstract.this.maxObjectId = objectId;
				}

				// keep track of current source position to offset the batch correctly
				this.currentSourcePosition += intLength;

				return true;
			}

			private void startNewBatch(
				final int                       length  ,
				final long                      objectId,
				final StorageEntityType.Default type
			)
			{
				final ChannelItem item = this.channelItems[this.currentBatchChannel];

				item.tailEntity = item.tailBatch = item.tailBatch.batchNext = new StorageChannelImportBatch.Default(
					this.currentSourcePosition,
					length,
					objectId,
					type
				);
			}

			private void addToCurrentBatch(
				final int                       length  ,
				final long                      objectId,
				final StorageEntityType.Default type
			)
			{
				final ChannelItem item = this.channelItems[this.currentBatchChannel];

				// intentionally ignores max file length for sake of import efficiency
				item.tailEntity = item.tailEntity.next = new StorageChannelImportEntity.Default(
					length,
					objectId,
					type
				);

				// update batch length and total length
				item.tailBatch.batchLength += length;
			}

			final void setSource(final S source)
			{
				// next source is set up
				this.currentBatchChannel   =     -1; // invalid value to guarantee change on first entity.
				this.currentSourcePosition =      0; // source starts at 0, of course.
				this.source                = source;
			}

			final void completeCurrentSource()
			{
				final StorageImportSource.Abstract[] sourceHeads  = this.sourceHeads ;
				final ChannelItem[]                  channelItems = this.channelItems;
				for(int i = 0; i < sourceHeads.length; i++)
				{
					final StorageImportSource.Abstract oldSourceHead = sourceHeads[i];
					final ChannelItem                  currentItem   = channelItems[i];

					sourceHeads[i] = sourceHeads[i].next =
						StorageRequestTaskImportData.Abstract.this
							.createImportSource(i, this.source, currentItem.headBatch.batchNext)
					;
					currentItem.resetChains();

					// notify storage thread that a new source is ready for processing
					synchronized(oldSourceHead)
					{
						oldSourceHead.notifyAll();
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

			// the tail array is always initialized with an empty dummy source which serves as an entry point.
			StorageImportSource.Abstract currentSource = this.sourceTails[channel.channelIndex()];

			// quite a braces mountain, however it is logically necessary
			try
			{
				importLoop:
				while(true)
				{
					// acquire a lock on the channel-exclusive signalling instance to wait for the reader's notification
					synchronized(currentSource)
					{
						// wait for the next batch to import (successor of the current batch)
						while(currentSource.next == null)
						{
							if(this.complete.get())
							{
								// there will be no more next source, so abort (task is complete)
								break importLoop;
							}
							// better check again after some time, indefinite wait caused a deadlock once
							// (16.04.2016)TODO: isn't the above comment a bug? Test and change or comment better.
							currentSource.wait(SOURCE_WAIT_TIME_MS);
							// note: completion adds an empty dummy source to avoid special case handling here
						}
						// at this point, there definitely is a new/next batch to process, so advance tail and process
						currentSource = currentSource.next;
					}

					// process the batch outside the lock to not block the central reader thread by channel-local work
					channel.importData(currentSource);
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
			final DisruptionCollectorExecuting<StorageImportSource> closer = DisruptionCollectorExecuting.New(
				StorageImportSource::close
			);
			
			for(final StorageImportSource.Abstract tail : this.sourceTails)
			{
				// the first slice is a dummy with no FileChannel instance
				for(StorageImportSource.Abstract source = tail; (source = source.next) != null;)
				{
					closer.executeOn(source);
				}
			}
			
			if(closer.hasDisruptions())
			{
				throw new StorageException(closer.toMultiCauseException());
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
			
			final StorageChannelImportBatch.Default  headBatch  = new StorageChannelImportBatch.Default();
			      StorageChannelImportBatch.Default  tailBatch ;
			      StorageChannelImportEntity.Default tailEntity;
			
			
			
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
		
	}
	
}
