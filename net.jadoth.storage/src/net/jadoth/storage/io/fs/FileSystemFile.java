package net.jadoth.storage.io.fs;

import java.io.File;
import java.util.function.Consumer;

import net.jadoth.storage.io.ProtageFileChannel;
import net.jadoth.storage.io.ProtageReadableFile;
import net.jadoth.storage.io.ProtageReadingFileChannel;
import net.jadoth.storage.io.ProtageWritableDirectory;
import net.jadoth.storage.io.ProtageWritableFile;
import net.jadoth.storage.io.ProtageWritingFileChannel;


public interface FileSystemFile extends ProtageWritableFile
{
	@Override
	public FileSystemDirectory directory();
	
	public File file();
	
	
	
	// there is no publicly accessible constructor. Only directories can create file instances.
		
	public final class Implementation
	extends ProtageWritableFile.Implementation<FileSystemDirectory, FileSystemReadingChannel, FileSystemWritingChannel>
	implements FileSystemFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final File cachedFile; // yes, the file is derived from the name, not the other way around.
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final FileSystemDirectory directory ,
			final String              name      ,
			final File                cachedFile
		)
		{
			super(directory, name);
			this.cachedFile = cachedFile;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final File file()
		{
			return this.cachedFile;
		}

		@Override
		public long length()
		{
			return this.cachedFile.length();
		}

		@Override
		public boolean exists()
		{
			return this.cachedFile.exists();
		}

		@Override
		public ProtageReadingFileChannel createReadingChannel(
			final ProtageFileChannel.Owner owner,
			final String                   name
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#createReadingChannel()
		}

		@Override
		public ProtageWritingFileChannel createWritingChannel(
			final ProtageFileChannel.Owner owner,
			final String                   name
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#createWritingChannel()
		}

		@Override
		public int tryClose()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#tryClose()
		}

		@Override
		public int close()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#close()
		}

		@Override
		public int forceClose() throws RuntimeException
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#forceClose()
		}

		@Override
		public int tryDelete()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#tryDelete()
		}

		@Override
		public int delete()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#delete()
		}

		@Override
		public void forceDelete() throws RuntimeException
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#forceDelete()
		}

		@Override
		public void copyTo(final ProtageWritableFile target)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#copyTo()
		}

		@Override
		public void copyTo(final ProtageWritableFile target, final long sourcePosition, final long sourceLength)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#copyTo()
		}

		@Override
		public void moveTo(final ProtageWritableDirectory destination)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#moveTo()
		}
		
		@Override
		public <C extends Consumer<? super ProtageReadableFile>> C waitOnClose(final C callback)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME FileSystemFile.Implementation#waitOnClose()
		}
		
		@Override
		public <C extends Consumer<? super ProtageWritableFile>> C waitOnDelete(final C callback)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME FileSystemFile.Implementation#waitOnDelete()
		}
		
	}

}
