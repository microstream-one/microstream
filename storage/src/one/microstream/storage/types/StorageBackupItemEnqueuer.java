package one.microstream.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		StorageLiveChannelFile<?> sourceFile    ,
		long                      sourcePosition,
		long                      length
	);
	
	public void enqueueTruncatingItem(
		StorageLiveChannelFile<?> file     ,
		long                      newLength
	);
	
	public void enqueueDeletionItem(
		StorageLiveChannelFile<?> file
	);
	
//	public StorageFileUser fileUser();
}
