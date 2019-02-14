package net.jadoth.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueBackupItem(
		StorageLockedFile sourceFile    ,
		long              sourcePosition,
		long              length        ,
		StorageLockedFile targetFile
	);
}
