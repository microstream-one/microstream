package one.microstream.storage.types;

public interface StorageCreatableFile extends StorageFile
{
	public default boolean ensure()
	{
		return this.file().useWriting().ensureExists();
	}
}
