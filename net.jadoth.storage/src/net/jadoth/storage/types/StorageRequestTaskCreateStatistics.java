package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.function.Supplier;

import net.jadoth.collections.XUtilsCollection;
import net.jadoth.file.JadothFiles;
import net.jadoth.storage.exceptions.StorageExceptionExportFailed;
import net.jadoth.time.JadothTime;

public interface StorageRequestTaskCreateStatistics extends StorageRequestTask
{
	public StorageRawFileStatistics result();



	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<StorageRawFileStatistics.ChannelStatistics>
	implements StorageRequestTaskCreateStatistics
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final Date                                         creationTime  ;
		private final StorageRawFileStatistics.ChannelStatistics[] channelResults;

		private StorageRawFileStatistics result;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final long timestamp   ,
			final int  channelCount
		)
		{
			super(timestamp, channelCount);
			this.channelResults = new StorageRawFileStatistics.ChannelStatistics[channelCount];
			this.creationTime = JadothTime.now();
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

		private StorageRawFileStatistics.Implementation createResult()
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

			return new StorageRawFileStatistics.Implementation(
				this.creationTime                            ,
				fileCount                                    ,
				liveDataLength                               ,
				totalDataLength                              ,
				XUtilsCollection.toTable(this.channelResults)
			);
		}

	}

	static final class ExportItem implements Supplier<FileChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		        final    int                         channelCount  ;
		        final    StorageEntityTypeHandler<?> type          ;
		        final    File                        file          ;
		private volatile int                         currentChannel;
		private volatile FileChannel                 channel       ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		ExportItem(final int channelCount, final StorageEntityTypeHandler<?> type, final File file)
		{
			super();
			this.channelCount = channelCount;
			this.type         = type        ;
			this.file         = file        ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final synchronized void incrementProgress()
		{
			this.currentChannel++;
			this.notifyAll();
		}

		final boolean isCurrentChannel(final StorageChannel channel)
		{
			return this.currentChannel == channel.channelIndex();
		}

		final boolean isLastChannel(final StorageChannel channel)
		{
			return this.channelCount == channel.channelIndex();
		}

		@Override
		public final FileChannel get()
		{
			/* (25.01.2014)TODO: Storage ByType export append mode: is append mode (default) really a good idea?
			 * makes for example export/import cycles to the same files not repeatable.
			 * Maybe the file provider should make such decisions
			 */
			if(this.channel == null)
			{
				try
				{
					this.channel = JadothFiles.createWritingFileChannel(this.file);
				}
				catch(final IOException e)
				{
					throw new StorageExceptionExportFailed(e);
				}
			}
			return this.channel;
		}

		final void closeChannel()
		{
			try
			{
				if(this.channel != null)
				{
					this.channel.close();
				}
			}
			catch(final IOException e)
			{
				throw new StorageExceptionExportFailed(e);
			}
		}

	}

}
