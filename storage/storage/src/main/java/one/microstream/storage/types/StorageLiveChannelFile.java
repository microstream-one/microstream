package one.microstream.storage.types;

public interface StorageLiveChannelFile<F extends StorageLiveChannelFile<F>>
extends StorageLiveFile<F>, StorageTruncatableChannelFile
{
	// only typing interface so far.
	
	@Override
	public StorageBackupChannelFile ensureBackupFile(StorageBackupInventory backupInventory);
}
