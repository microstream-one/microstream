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
			final long                       timestamp   ,
			final int                        channelCount, 
			final StorageOperationController controller
		)
		{
			super(timestamp, channelCount, controller);
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
