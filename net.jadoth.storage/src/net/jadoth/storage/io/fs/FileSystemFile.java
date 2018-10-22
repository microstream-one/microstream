package net.jadoth.storage.io.fs;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.function.Consumer;

import net.jadoth.storage.io.ProtageReadableFile;
import net.jadoth.storage.io.ProtageWritableDirectory;
import net.jadoth.storage.io.ProtageWritableFile;


public interface FileSystemFile extends ProtageWritableFile
{
	@Override
	public FileSystemDirectory directory();
	
	public File file();
	
	
	
	// there is no publicly accessible constructor. Only directories can create file instances.
		
	public final class Implementation extends ProtageWritableFile.Implementation<FileSystemDirectory>
	implements FileSystemFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final File        cachedFile; // yes, the file is derived from the name, not the other way around.
		private final FileLock    lock      ;
		private final FileChannel channel   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final FileSystemDirectory directory ,
			final String              name      ,
			final File                cachedFile,
			final FileLock            lock      ,
			final FileChannel         channel
		)
		{
			super(directory, name);
			this.cachedFile = cachedFile;
			this.lock       = lock      ;
			this.channel    = channel   ;
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
		public synchronized long length()
		{
			return this.cachedFile.length();
		}

		@Override
		public synchronized boolean exists()
		{
			return this.cachedFile.exists();
		}

		@Override
		public synchronized void open()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#open()
		}

		@Override
		public synchronized boolean isOpen()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#isOpen()
		}

		@Override
		public synchronized boolean isClosed()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#isClosed()
		}

		@Override
		public synchronized int tryClose()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#tryClose()
		}

		@Override
		public synchronized int close()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#close()
		}

		@Override
		public synchronized int forceClose() throws RuntimeException
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#forceClose()
		}

		@Override
		public synchronized int tryDelete()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#tryDelete()
		}

		@Override
		public synchronized int delete()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#delete()
		}

		@Override
		public synchronized void forceDelete() throws RuntimeException
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#forceDelete()
		}

		@Override
		public synchronized long read(final ByteBuffer target, final long position)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#read()
		}
		
		@Override
		public synchronized void copyTo(final ProtageWritableFile target)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#copyTo()
		}

		@Override
		public synchronized void copyTo(final ProtageWritableFile target, final long sourcePosition, final long sourceLength)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#copyTo()
		}

		@Override
		public synchronized void moveTo(final ProtageWritableDirectory destination)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#moveTo()
		}

		@Override
		public synchronized long write(final Iterable<? extends ByteBuffer> sources)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#write()
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
