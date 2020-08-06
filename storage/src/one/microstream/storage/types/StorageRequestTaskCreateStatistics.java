package one.microstream.storage.types;

import java.util.Date;

import one.microstream.collections.XUtilsCollection;
import one.microstream.time.XTime;

public interface StorageRequestTaskCreateStatistics extends StorageRequestTask
{
	public StorageRawFileStatistics result();



	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<StorageRawFileStatistics.ChannelStatistics>
	implements StorageRequestTaskCreateStatistics
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Date                                         creationTime  ;
		private final StorageRawFileStatistics.ChannelStatistics[] channelResults;

		private StorageRawFileStatistics result;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long timestamp   ,
			final int  channelCount
		)
		{
			super(timestamp, channelCount);
			this.channelResults = new StorageRawFileStatistics.ChannelStatistics[channelCount];
			this.creationTime = XTime.now();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final StorageRawFileStatistics.ChannelStatistics internalProcessBy(final StorageChannel channel)
		{
			return channel.createRawFileStatistics();
		}

		@Override
		protected synchronized void succeed(
			final StorageChannel                             channel,
			final StorageRawFileStatistics.ChannelStatistics result
		)
		{
			this.channelResults[channel.channelIndex()] = result;
		}

		@Override
		public synchronized StorageRawFileStatistics result()
		{
			if(this.result == null)
			{
				this.result = this.createResult();
			}
			return this.result;
		}

		private StorageRawFileStatistics createResult()
		{
			long fileCount       = 0;
			long liveDataLength  = 0;
			long totalDataLength = 0;

			for(final StorageRawFileStatistics.ChannelStatistics result : this.channelResults)
			{
				fileCount       += result.fileCount()      ;
				liveDataLength  += result.liveDataLength() ;
				totalDataLength += result.totalDataLength();
			}

			return StorageRawFileStatistics.New(
				this.creationTime                            ,
				fileCount                                    ,
				liveDataLength                               ,
				totalDataLength                              ,
				XUtilsCollection.toTable(this.channelResults)
			);
		}

	}
	
}
