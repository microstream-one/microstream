package one.microstream.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		ZStorageInventoryFile sourceFile    ,
		long                 sourcePosition,
		long                 length
	);
	
	public void enqueueTruncatingItem(
		ZStorageInventoryFile file     ,
		long                 newLength
	);
	
	public void enqueueDeletionItem(
		ZStorageInventoryFile file
	);
	
//	public StorageFileUser fileUser();
}
