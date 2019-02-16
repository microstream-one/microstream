package net.jadoth.storage.types;

public interface StorageNumberedFile extends StorageFile, StorageHashChannelPart
{
	public long number();
}
