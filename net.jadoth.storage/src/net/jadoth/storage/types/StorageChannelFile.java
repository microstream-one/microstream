package net.jadoth.storage.types;

import java.io.File;
import java.nio.channels.FileChannel;

public interface StorageChannelFile extends StorageFile, StorageHashChannelPart
{
	// typing interface only, so far.
	
	public StorageLockedChannelFile lock();
	
	
	
	public final class Implementation implements StorageChannelFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int  channelIndex;
		final File file        ;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final int channelIndex, final File file)
		{
			super();
			this.channelIndex = channelIndex;
			this.file         = file        ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}
		
		public final File file()
		{
			return this.file;
		}

		@Override
		public final FileChannel channel()
		{
			return null;
		}
		
		@Override
		public String qualifier()
		{
			return this.file.getParent();
		}
		
		@Override
		public String identifier()
		{
			return this.file.getPath();
		}
		
		@Override
		public String name()
		{
			return this.file.getName();
		}
		
		@Override
		public boolean delete()
		{
			this.close();
			return this.file.delete();
		}
		
		@Override
		public boolean exists()
		{
			return this.file.exists();
		}
		
		@Override
		public StorageLockedChannelFile lock()
		{
			return StorageLockedChannelFile.New(this.channelIndex, this.file);
		}

	}
	
}
