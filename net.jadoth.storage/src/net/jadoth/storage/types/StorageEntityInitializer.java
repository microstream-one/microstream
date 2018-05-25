package net.jadoth.storage.types;

public interface StorageEntityInitializer
{
	public boolean initialRegisterEntityCachable(long address, int filePosition);
	
	public boolean initialRegisterEntityUncachable(long address, int filePosition);
}
