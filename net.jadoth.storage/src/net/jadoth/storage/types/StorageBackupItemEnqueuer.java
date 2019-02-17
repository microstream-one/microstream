package net.jadoth.storage.types;

public interface StorageBackupItemEnqueuer
{
	public void enqueueCopyingItem(
		StorageInventoryFile sourceFile    ,
		long                     sourcePosition,
		long                     length        ,
		StorageInventoryFile targetFile
	);
	
	public void enqueueTruncatingItem(
		StorageInventoryFile file     ,
		long                     newLength
	);
}
