package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.nio.channels.FileLock;


public interface StorageInventoryFile extends StorageLockedFile, StorageNumberedFile
{
	@Override
	public default StorageInventoryFile inventorize()
	{
		return this;
	}
	
	

	public static StorageInventoryFile New(
		final int  channelIndex,
		final long number      ,
		final File file
	)
	{
		return new StorageInventoryFile.Default(
			channelIndex,
			number,
			notNull(file),
			StorageLockedFile.openLockedFileChannel(file)
		);
	}

	public class Default
	extends StorageLockedFile.Default
	implements StorageInventoryFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int  channelIndex;
		private final long number      ;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
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
