package net.jadoth.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueBackupItem(StorageLockedFile file, long position, long length);
}
