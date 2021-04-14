package one.microstream.storage.types;

import java.util.function.Consumer;

public interface StorageChannelImportBatch
{
	public long fileOffset();

	public long fileLength();

	public void iterateEntities(Consumer<? super StorageChannelImportEntity> iterator);

	public StorageChannelImportEntity first();
}
