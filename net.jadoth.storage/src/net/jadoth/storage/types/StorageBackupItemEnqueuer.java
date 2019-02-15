package net.jadoth.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		StorageLockedFile sourceFile    ,
		long              sourcePosition,
		long              length        ,
		StorageLockedFile targetFile
	);
	
	public void enqueueTruncatingItem(
		StorageLockedFile file     ,
		long              newLength
	);
}
