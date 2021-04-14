package one.microstream.storage.types;

import one.microstream.afs.types.AFile;

public interface StorageTransactionsFile extends StorageChannelFile, StorageBackupableFile
{
	@Override
	public default StorageBackupTransactionsFile ensureBackupFile(final StorageBackupInventory creator)
	{
		return creator.ensureTransactionsFile(this);
	}
	
	
	
	@FunctionalInterface
	public interface Creator<F extends StorageTransactionsFile>
	{
		public F createTransactionsFile(AFile file, int channelIndex);
	}
}

