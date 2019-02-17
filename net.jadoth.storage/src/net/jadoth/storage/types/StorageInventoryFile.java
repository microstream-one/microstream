package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import net.jadoth.files.XFiles;


public interface StorageInventoryFile extends StorageLockedFile, StorageNumberedFile
{
	@Override
	public default StorageInventoryFile inventorize()
	{
		return this;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static int orderByNumber(
		final StorageInventoryFile file1,
		final StorageInventoryFile file2
	)
	{
		return Long.compare(file1.number(), file2.number());
	}

	public static StorageInventoryFile New(
		final int  channelIndex,
		final long number      ,
		final File file
	)
	{
		return new StorageInventoryFile.Implementation(
			channelIndex,
			number,
			file,
			Implementation.openLockedFileChannel(file)
		);
	}

	public class Implementation extends StorageLockedFile.Implementation implements StorageInventoryFile
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

		private final int  channelIndex;
		private final long number      ;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final int      channelIndex,
			final long     number      ,
			final File     file        ,
			final FileLock lock
		)
		{
			super(file, lock);
			this.channelIndex = channelIndex;
			this.number       = number      ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}
		
		@Override
		public final long number()
		{
			return this.number;
		}

	}

}
