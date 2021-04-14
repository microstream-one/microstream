package one.microstream.storage.types;

public interface StorageBackupInventory
{
	public StorageBackupDataFile ensureDataFile(StorageDataFile file);
	
	public StorageBackupTransactionsFile ensureTransactionsFile(StorageTransactionsFile file);
}
