package net.jadoth.storage.types;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


public interface StorageLockedChannelFile extends StorageLockedFile, StorageHashChannelPart
{
	@Override
	public int channelIndex();

	@Override
	public File file();

	@Override
	public FileChannel fileChannel();



	public static StorageLockedChannelFile New(
		final int         channelIndex,
		final File        file        ,
		final FileLock    lock
	)
	{
		return new StorageLockedChannelFile.Implementation(channelIndex, file, lock);
	}

	public class Implementation extends StorageLockedFile.Implementation implements StorageLockedChannelFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int channelIndex;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final int         channelIndex,
			final File        file        ,
			final FileLock    lock
		)
		{
			super(file, lock);
			this.channelIndex = channelIndex;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}

	}

}
