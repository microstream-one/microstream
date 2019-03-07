package one.microstream.storage.types;

public interface StorageChannelImportEntity
{
	public int length();

	public StorageEntityType.Implementation type();

	public long objectId();
	
	public StorageChannelImportEntity next();
}
