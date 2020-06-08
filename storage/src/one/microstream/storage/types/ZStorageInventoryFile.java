package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.channels.FileLock;
import java.nio.file.Path;


public interface ZStorageInventoryFile extends ZStorageLockedFile, ZStorageNumberedFile
{
	@Override
	public default ZStorageInventoryFile inventorize()
	{
		return this;
	}
	
	

	public static ZStorageInventoryFile New(
		final int  channelIndex,
		final long number      ,
		final Path file
	)
	{
		return new ZStorageInventoryFile.Default(
			channelIndex,
			number,
			notNull(file),
			ZStorageLockedFile.openLockedFileChannel(file)
		);
	}

	public class Default
	extends ZStorageLockedFile.Default
	implements ZStorageInventoryFile
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
			final Path     file        ,
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
