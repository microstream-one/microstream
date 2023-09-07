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

import java.util.Objects;
import java.util.stream.Stream;

import one.microstream.collections.EqHashEnum;
import one.microstream.collections.XSort;
import one.microstream.persistence.types.Persistence;
import one.microstream.storage.exceptions.StorageExceptionConsistency;

public interface StorageChannelTaskInitialize extends StorageChannelTask
{
	public StorageIdAnalysis idAnalysis();

	public long latestTimestamp();


	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<StorageInventory[]>
	implements StorageChannelTaskInitialize
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageOperationController operationController;
		private final StorageInventory[]         result             ;

		private Long consistentStoreTimestamp   ;
		private Long commonTaskHeadFileTimestamp;
		private Long latestTimestamp               ;
		
		private long maxEntityObjectOid  ;
		private long maxEntityConstantOid;
		private long maxEntityTypeOid    ; // this is NOT the highest TID, but the highest TID used as an entity ID
		
		private final EqHashEnum<Long> occuringTypeIds = EqHashEnum.New();

		



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final long                       timestamp          ,
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			super(timestamp, channelCount, operationController);
			this.operationController = notNull(operationController)      ;
			this.result              = new StorageInventory[channelCount];
		}

		private synchronized long getConsistentStoreTimestamp()
		{
			if(this.consistentStoreTimestamp == null)
			{
				this.checkAllTransactionsFilesMissing();
				this.consistentStoreTimestamp = this.determineConsistentStoreTimestamp();
			}
			return this.consistentStoreTimestamp;
		}

		private synchronized long getCommonTaskHeadFileTimestamp()
		{
			if(this.commonTaskHeadFileTimestamp == null)
			{
				// the max size offset is more or less just for aesthetically reasons.
				long maxResultFileCount = 0;
				for(final StorageInventory si : this.result)
				{
					if(si.dataFiles().size() > maxResultFileCount)
					{
						maxResultFileCount = si.dataFiles().size();
					}
				}
				this.commonTaskHeadFileTimestamp = this.timestamp() + maxResultFileCount;
			}
			return this.commonTaskHeadFileTimestamp;
		}


		private synchronized void updateIdAnalysis(final StorageIdAnalysis idAnalysis)
		{
			final Long typeMaxTid = idAnalysis.highestIdsPerType().get(Persistence.IdType.TID);
			if(typeMaxTid != null && typeMaxTid >= this.maxEntityTypeOid)
			{
				this.maxEntityTypeOid = typeMaxTid;
			}

			final Long typeMaxOid = idAnalysis.highestIdsPerType().get(Persistence.IdType.OID);
			if(typeMaxOid != null && typeMaxOid >= this.maxEntityObjectOid)
			{
				this.maxEntityObjectOid = typeMaxOid;
			}

			final Long typeMaxCid = idAnalysis.highestIdsPerType().get(Persistence.IdType.CID);
			if(typeMaxCid != null && typeMaxCid >= this.maxEntityConstantOid)
			{
				this.maxEntityConstantOid = typeMaxCid;
			}
			
			this.occuringTypeIds.addAll(idAnalysis.occuringTypeIds());
			this.occuringTypeIds.sort(XSort::compare);
		}



		private boolean checkAllTransactionsFilesMissing()
		{
			final boolean firstIsNull = this.result[0].transactionsFileAnalysis() == null;
			for(int i = 1; i < this.result.length; i++)
			{
				if(this.result[i].transactionsFileAnalysis() == null != firstIsNull)
				{
					throw new StorageExceptionConsistency("Mixed (inconsistent) transactions file existances.");
				}
			}
			return firstIsNull;
		}

		private long determineConsistentStoreTimestamp()
		{
			if(this.checkAllTransactionsFilesMissing())
			{
				return 0;
			}

			final long firstChannelLatestTimestamp = this.result[0].transactionsFileAnalysis().headFileLatestTimestamp();

			for(final StorageInventory inventory : this.result)
			{
				if(!isCompatibleTimestamp(firstChannelLatestTimestamp, inventory.transactionsFileAnalysis()))
				{
					return this.fallbackDetermineConsistentStoreTimestamp();
				}
			}
			return firstChannelLatestTimestamp;
		}

		private long fallbackDetermineConsistentStoreTimestamp()
		{
			final long firstChannelLastTimestamp = this.result[0].transactionsFileAnalysis().headFileLastConsistentStoreTimestamp();

			for(final StorageInventory inventory : this.result)
			{
				if(!isCompatibleTimestamp(firstChannelLastTimestamp, inventory.transactionsFileAnalysis()))
				{
					throw new StorageExceptionConsistency("Inconsistent store timestamps between channels");
				}
			}
			return firstChannelLastTimestamp;
		}

		private static boolean isCompatibleTimestamp(
			final long                        candidatetimestamp,
			final StorageTransactionsAnalysis transactionsFile
		)
		{
			return transactionsFile.headFileLatestTimestamp()              == candidatetimestamp
				|| transactionsFile.headFileLastConsistentStoreTimestamp() == candidatetimestamp
			;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final StorageInventory[] internalProcessBy(final StorageChannel channel)
		{
			this.result[channel.channelIndex()] = channel.readStorage();
			return this.result;
		}

		@Override
		protected final void succeed(final StorageChannel channel, final StorageInventory[] result)
		{

			final StorageIdAnalysis idAnalysis = channel.initializeStorage(
				this.getCommonTaskHeadFileTimestamp(),
				this.getConsistentStoreTimestamp()   ,
				result[channel.channelIndex()]
			);
			
			this.updateIdAnalysis(idAnalysis);
			
			//Some storage targets like SQL will create "files" only if there is some data written.
			//The transactionsFileAnalysis may be null if a new storage has been created
			//and the transactions log is empty.
			this.latestTimestamp = Stream.of(result)
				.filter( r -> Objects.nonNull(r.transactionsFileAnalysis()))
				.mapToLong( r -> r.transactionsFileAnalysis().maxTimestamp())
				.max()
				.orElse(0L);
			
			this.operationController.activate();
		}

		@Override
		protected final void fail(final StorageChannel channel, final StorageInventory[] result)
		{
			// (09.06.2014 TM)TODO: reset entity cache and file manager in here or comment why not here.
			// channels won't get activated and thus will terminate automatically
		}

		@Override
		public synchronized StorageIdAnalysis idAnalysis()
		{
			return StorageIdAnalysis.New(
				this.maxEntityTypeOid    ,
				this.maxEntityObjectOid  ,
				this.maxEntityConstantOid,
				this.occuringTypeIds
			);
		}

		@Override
		public long latestTimestamp()
		{
			return this.latestTimestamp;
		}

	}

}
