package one.microstream.storage.types;

public interface StorageBackupInventory
{
	public StorageBackupDataFile ensureDataFile(StorageLiveDataFile file);
	
	public StorageBackupTransactionsFile ensureTransactionsFile(StorageLiveTransactionsFile file);
}
