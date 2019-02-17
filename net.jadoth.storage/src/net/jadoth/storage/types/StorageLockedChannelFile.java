package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import net.jadoth.files.XFiles;


public interface StorageLockedChannelFile extends StorageLockedFile, StorageChannelFile
{
	@Override
	public int channelIndex();

	@Override
	public FileChannel channel();
	
	@Override
	public default StorageLockedChannelFile lock()
	{
		return this;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static StorageLockedChannelFile New(
		final int  channelIndex,
		final File file
	)
	{
		return new StorageLockedChannelFile.Implementation(
			channelIndex,
			file,
			Implementation.openLockedFileChannel(file)
		);
	}

	public class Implementation extends StorageLockedFile.Implementation implements StorageLockedChannelFile
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
				
		public static final FileLock openLockedFileChannel(final File file)
		{
//			DEBUGStorage.println("Thread " + Thread.currentThread().getName() + " opening channel for " + file);
			FileChannel channel = null;
			try
			{
				final FileLock fileLock = StorageLockedFile.openFileChannel(file);
				channel = fileLock.channel();
				channel.position(channel.size());
				return fileLock;
			}
			catch(final IOException e)
			{
				XFiles.closeSilent(channel);
				throw new RuntimeException(e); // (04.05.2013)EXCP: proper exception
			}
		}
		
		
		
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
