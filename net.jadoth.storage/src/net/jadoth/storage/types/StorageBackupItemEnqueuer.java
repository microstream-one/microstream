package net.jadoth.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		StorageLockedChannelFile sourceFile    ,
		long                     sourcePosition,
		long                     length        ,
		StorageLockedChannelFile targetFile
	);
	
	public void enqueueTruncatingItem(
		StorageLockedChannelFile file     ,
		long                     newLength
	);
}
