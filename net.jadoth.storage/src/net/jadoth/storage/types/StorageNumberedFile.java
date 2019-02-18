package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;

public interface StorageNumberedFile extends StorageChannelFile
{
	public long number();
	
	public StorageInventoryFile inventorize();
	

	
	public static int orderByNumber(
		final StorageNumberedFile file1,
		final StorageNumberedFile file2
	)
	{
		return Long.compare(file1.number(), file2.number());
	}
	
	
	
	public static StorageNumberedFile New(final int channelIndex, final long number, final File file)
	{
		return new StorageNumberedFile.Implementation(
			channelIndex ,
			number       ,
			notNull(file)
		);
	}
	
	public final class Implementation implements StorageNumberedFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int  channelIndex;
		final long number      ;
		final File file        ;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final int channelIndex, final long number, final File file)
		{
			super();
			this.channelIndex = channelIndex;
			this.number       = number      ;
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
		
		@Override
		public final long number()
		{
			return this.number;
		}
		
		public final File file()
		{
			return this.file;
		}
		
		@Override
		public final long length()
		{
			return this.file.length();
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
			return this.file.delete();
		}
		
		@Override
		public boolean exists()
		{
			return this.file.exists();
		}
		
		@Override
		public StorageInventoryFile inventorize()
		{
			return StorageInventoryFile.New(this.channelIndex, this.number, this.file);
		}

	}
	
}
