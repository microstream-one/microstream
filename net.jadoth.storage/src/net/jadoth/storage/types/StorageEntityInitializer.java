package net.jadoth.storage.types;

public interface StorageEntityInitializer<F extends StorageDataFile<?>>
{
	public void initializeEntity(F file, long entityMemoryAddress, long entityStoragePosition);
}
