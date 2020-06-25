package one.microstream.storage.types;

import one.microstream.collections.types.XGettingTable;

public interface StorageInventory extends StorageHashChannelPart
{
	public XGettingTable<Long, StorageDataInventoryFile> dataFiles();

	public StorageTransactionsAnalysis transactionsFileAnalysis();



	public final class Default implements StorageInventory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int                                           channelIndex    ;
		final XGettingTable<Long, StorageDataInventoryFile> dataFiles       ;
		final StorageTransactionsAnalysis                   transactionsFile;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final int                                           channelIndex    ,
			final XGettingTable<Long, StorageDataInventoryFile> dataFiles       ,
			final StorageTransactionsAnalysis                   transactionsFile
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
		public final XGettingTable<Long, StorageDataInventoryFile> dataFiles()
		{
			return this.dataFiles;
		}

		@Override
		public final StorageTransactionsAnalysis transactionsFileAnalysis()
		{
			return this.transactionsFile;
		}

	}

}
