package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashEnum;
import one.microstream.collections.XSort;
import one.microstream.persistence.types.Persistence;

public interface StorageChannelTaskInitialize extends StorageChannelTask
{
	public StorageIdAnalysis idAnalysis();



	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<StorageInventory[]>
	implements StorageChannelTaskInitialize
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageOperationController operationController;
		private final StorageInventory[]         result             ;

		private Long consistentStoreTimestamp   ;
		private Long commonTaskHeadFileTimestamp;

		private long maxEntityObjectOid  ;
		private long maxEntityConstantOid;
		private long maxEntityTypeOid    ; // this is NOT the highest TID, but the highest TID used as an entity ID
		
		private final EqHashEnum<Long> occuringTypeIds = EqHashEnum.New();



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final long                       timestamp          ,
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			super(timestamp, channelCount);
			this.operationController = notNull(operationController)      ;
			this.result              = new StorageInventory[channelCount];
		}

		private synchronized long getConsistentStoreTimestamp()
		{
			if(this.consistentStoreTimestamp == null)
			{
				this.checkAllTransactionsFilesMissing();
				this.consistentStoreTimestamp = this.determineConsistentStoreTimestamp();
//				DEBUGStorage.println("Initialization last file consistent timestamp: " + this.consistentStoreTimestamp);
			}
			return this.consistentStoreTimestamp;
		}

		private synchronized long getCommonTaskHeadFileTimestamp()
		{
			if(this.commonTaskHeadFileTimestamp == null)
			{
				// the max size offset is moreless just for aesthetical reasons (yes indeed).
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
					// (03.09.2014)EXCP: proper exception
					throw new RuntimeException("Mixed (inconsistent) transactions file existances.");
				}
			}
			return firstIsNull;
		}

//		private void checkAllChannelsEmpty()
//		{
//			for(int i = 0; i < this.result.length; i++)
//			{
//				if(!this.result[i].dataFiles().isEmpty())
//				{
//					// (12.06.2014)EXCP: proper exception
//					throw new RuntimeException("Channel " + i + " is not empty while all before are.");
//				}
//			}
//		}

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
					// (10.06.2014)EXCP: proper exception
					throw new RuntimeException("Inconsistent store timestamps between channels");
				}
			}
			return firstChannelLastTimestamp;
		}

		private static boolean isCompatibleTimestamp(
			final long                            candidatetimestamp,
			final StorageTransactionsFileAnalysis transactionsFile
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
//			DEBUGStorage.println("initializing storage");
			this.result[channel.channelIndex()] = channel.readStorage();
			return this.result;
		}

		@Override
		protected final void succeed(final StorageChannel channel, final StorageInventory[] result)
		{
//			DEBUGStorage.println("Channel " + channel.hashIndex() + " successfully completed initialization task, initialization storage");

			final StorageIdAnalysis idAnalysis = channel.initializeStorage(
				this.getCommonTaskHeadFileTimestamp(),
				this.getConsistentStoreTimestamp()   ,
				result[channel.channelIndex()]
			);
//			DEBUGStorage.println("Channel " + channel.hashIndex() + " initialized storage, activating controller");

			this.updateIdAnalysis(idAnalysis);

			this.operationController.activate();
//			DEBUGStorage.println("Channel " + channel.hashIndex() + " completed initialization");
		}

		@Override
		protected final void fail(final StorageChannel channel, final StorageInventory[] result)
		{
			// (09.06.2014)TODO: reset entity cache and file manager in here or comment why not here.
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

	}

}
