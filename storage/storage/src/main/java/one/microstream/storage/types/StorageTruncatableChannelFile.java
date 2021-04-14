package one.microstream.storage.types;

public interface StorageTruncatableChannelFile extends StorageChannelFile
{
	public void truncate(long newLength);
}
