package one.microstream.storage.types;

public interface StorageChannelImportEntity
{
	public int length();

	public StorageEntityType.Default type();

	public long objectId();
	
	public StorageChannelImportEntity next();
}
