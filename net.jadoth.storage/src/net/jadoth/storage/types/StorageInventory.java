package net.jadoth.storage.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XSequence;

public interface StorageInventory
{
	public XGettingSequence<StorageInventoryFile> dataFiles();

	public StorageTransactionsFileAnalysis transactionsFileAnalysis();



	public final class Implementation implements StorageInventory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final XSequence<StorageInventoryFile> dataFiles       ;
		final StorageTransactionsFileAnalysis transactionsFile;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final XSequence<StorageInventoryFile> dataFiles       ,
			final StorageTransactionsFileAnalysis transactionsFile
		)
		{
			super();
			this.dataFiles        = dataFiles       ;
			this.transactionsFile = transactionsFile;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final XGettingSequence<StorageInventoryFile> dataFiles()
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
