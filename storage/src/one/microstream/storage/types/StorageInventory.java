package one.microstream.storage.types;

import one.microstream.collections.types.XGettingTable;

public interface StorageInventory extends StorageHashChannelPart
{
	public XGettingTable<Long, StorageDataInventoryFile> dataFiles();

	public StorageTransactionsAnalysis transactionsFileAnalysis();


	
	public static StorageInventory New(
		final int                                           channelIndex        ,
		final XGettingTable<Long, StorageDataInventoryFile> dataFiles           ,
		final StorageTransactionsAnalysis                   transactionsAnalysis
	)
	{
		return new StorageInventory.Default(
			channelIndex        ,
			dataFiles           ,
			transactionsAnalysis
		);
	}

	public final class Default implements StorageInventory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int                                           channelIndex        ;
		final XGettingTable<Long, StorageDataInventoryFile> dataFiles           ;
		final StorageTransactionsAnalysis                   transactionsAnalysis;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final int                                           channelIndex        ,
			final XGettingTable<Long, StorageDataInventoryFile> dataFiles           ,
			final StorageTransactionsAnalysis                   transactionsAnalysis
		)
		{
			super();
			this.channelIndex         = channelIndex        ;
			this.dataFiles            = dataFiles           ;
			this.transactionsAnalysis = transactionsAnalysis;
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
			return this.transactionsAnalysis;
		}

	}

}
