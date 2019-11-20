package one.microstream.arm;

import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.storage.types.StorageRequestTaskCreator;
import one.microstream.storage.types.StorageRequestTaskStoreEntities;
import one.microstream.storage.types.StorageTimestampProvider;
import one.microstream.util.BufferSizeProviderIncremental;

public class MainTestStoreTask
{
	
	public static void main(final String[] args)
	{
		final StorageRequestTaskCreator taskCreator = new StorageRequestTaskCreator.Default(
			new StorageTimestampProvider.Default()
		);
		
		final ChunksBuffer chunksBuffer = ChunksBuffer.New(
			new ChunksBuffer[1],
			BufferSizeProviderIncremental.New()
		);
		
		final StorageRequestTaskStoreEntities task = taskCreator.createSaveTask(chunksBuffer);
		
		System.out.println(task.problems());
	}
		
}
