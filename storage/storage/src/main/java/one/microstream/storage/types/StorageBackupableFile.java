package one.microstream.storage.types;

public interface StorageBackupableFile extends StorageFile
{
	public StorageBackupFile ensureBackupFile(StorageBackupInventory backupInventory);
}
