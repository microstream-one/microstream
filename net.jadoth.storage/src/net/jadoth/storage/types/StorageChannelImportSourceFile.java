package net.jadoth.storage.types;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

public interface StorageChannelImportSourceFile extends StorageFile
{
	@Override
	public File file();

	@Override
	public FileChannel fileChannel();

	public void iterateBatches(Consumer<? super StorageChannelImportBatch> iterator);


}
