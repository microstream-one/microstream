package one.microstream.storage.types;

import java.nio.channels.FileChannel;
import java.util.function.Consumer;

public interface ZStorageChannelImportSourceFile extends ZStorageInventoryFile
{
	@Override
	public FileChannel fileChannel();

	public void iterateBatches(Consumer<? super StorageChannelImportBatch> iterator);


}
