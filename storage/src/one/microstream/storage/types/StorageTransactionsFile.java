package one.microstream.storage.types;

import one.microstream.afs.AFile;

public interface StorageTransactionsFile extends StorageChannelFile
{
	@FunctionalInterface
	public interface Creator<F extends StorageTransactionsFile>
	{
		public F createTransactionsFile(AFile file, int channelIndex);
	}
}

