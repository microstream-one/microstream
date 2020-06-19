package one.microstream.storage.types;

public interface StorageTruncatableFile extends StorageBackupableFile
{
	public void truncate(long newLength);
}
