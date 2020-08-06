package one.microstream.storage.types;

import java.util.function.Consumer;


public interface StorageImportSourceFile extends StorageClosableFile
{
	public void iterateBatches(Consumer<? super StorageChannelImportBatch> iterator);

}
