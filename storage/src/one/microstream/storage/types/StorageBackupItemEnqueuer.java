package one.microstream.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		StorageInventoryFile sourceFile    ,
		long                 sourcePosition,
		long                 length
	);
	
	public void enqueueTruncatingItem(
		StorageInventoryFile file     ,
		long                 newLength
	);
	
	public void enqueueDeletionItem(
		StorageInventoryFile file
	);
	
//	public StorageFileUser fileUser();
}
