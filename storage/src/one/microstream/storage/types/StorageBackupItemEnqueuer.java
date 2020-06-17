package one.microstream.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		StorageLiveFile<?> sourceFile    ,
		long               sourcePosition,
		long               length
	);
	
	public void enqueueTruncatingItem(
		StorageLiveFile<?> file     ,
		long               newLength
	);
	
	public void enqueueDeletionItem(
		StorageLiveFile<?> file
	);
	
//	public StorageFileUser fileUser();
}
