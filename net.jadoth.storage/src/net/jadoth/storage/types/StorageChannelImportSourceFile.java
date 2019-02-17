package net.jadoth.storage.types;

import java.nio.channels.FileChannel;
import java.util.function.Consumer;

public interface StorageChannelImportSourceFile extends StorageInventoryFile
{
	@Override
	public FileChannel channel();

	public void iterateBatches(Consumer<? super StorageChannelImportBatch> iterator);


}
