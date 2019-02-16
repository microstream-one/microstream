package net.jadoth.storage.types;

import java.io.File;
import java.nio.channels.FileLock;

public interface StorageInventoryFile extends StorageLockedChannelFile, StorageNumberedFile
{
	public static int orderByNumber(final StorageInventoryFile file1, final StorageInventoryFile file2)
	{
		return Long.compare(file1.number(), file2.number());
	}


	public final class Implementation extends StorageLockedChannelFile.Implementation implements StorageInventoryFile
	{
		private final long number;

		public Implementation(final int channelIndex, final File file, final FileLock lock, final long number)
		{
			super(channelIndex, file, lock);
			this.number = number;
		}

		@Override
		public long number()
		{
			return this.number;
		}

	}

}
