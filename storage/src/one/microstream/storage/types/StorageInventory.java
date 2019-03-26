package one.microstream.storage.types;

import one.microstream.collections.types.XGettingTable;

public interface StorageInventory extends StorageHashChannelPart
{
	public XGettingTable<Long, StorageInventoryFile> dataFiles();

	public StorageTransactionsFileAnalysis transactionsFileAnalysis();



	public final class Implementation implements StorageInventory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final int                                       channelIndex    ;
		final XGettingTable<Long, StorageInventoryFile> dataFiles       ;
		final StorageTransactionsFileAnalysis           transactionsFile;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final int                                       channelIndex    ,
			final XGettingTable<Long, StorageInventoryFile> dataFiles       ,
			final StorageTransactionsFileAnalysis           transactionsFile
		)
		{
			super();
			this.channelIndex     = channelIndex    ;
			this.dataFiles        = dataFiles       ;
			this.transactionsFile = transactionsFile;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}

		@Override
		public final XGettingTable<Long, StorageInventoryFile> dataFiles()
		{
			return this.dataFiles;
		}

		@Override
		public final StorageTransactionsFileAnalysis transactionsFileAnalysis()
		{
			return this.transactionsFile;
		}

	}

}
