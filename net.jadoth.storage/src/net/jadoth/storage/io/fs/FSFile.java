package net.jadoth.storage.io.fs;

import java.io.File;

import net.jadoth.storage.io.ProtageFile;
import net.jadoth.storage.io.ProtageReadingFileChannel;
import net.jadoth.storage.io.ProtageWritableDirectory;
import net.jadoth.storage.io.ProtageWritableFile;
import net.jadoth.storage.io.ProtageWritingFileChannel;


/**
 * "FS" meaning "FileSystem", a {@link ProtageFile} framework implementation using file system files located on a drive.
 * 
 * @author TM
 */
public interface FSFile extends ProtageWritableFile
{
	// there is no publicly accessible constructor. Only directories can create file instances.
		
	public final class Implementation implements FSFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final FSDirectory directory ;
		private final String      name      ;
		private final File        cachedFile; // yes, the file is the derived thing, not the name.
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final FSDirectory directory ,
			final String          name      ,
			final File            cachedFile
		)
		{
			super();
			this.directory  = directory ;
			this.name       = name      ;
			this.cachedFile = cachedFile;
		}

		@Override
		public ProtageReadingFileChannel createReadingChannel()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#createReadingChannel()
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
		public int activeReadingChannels()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#activeReadingChannels()
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
		public String name()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageFile#name()
		}

		@Override
		public long length()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageFile#length()
		}

		@Override
		public boolean exists()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageFile#exists()
		}

		@Override
		public ProtageWritableDirectory directory()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#directory()
		}

		@Override
		public ProtageWritingFileChannel createWritingChannel()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#createWritingChannel()
		}

		@Override
		public int activeWritingChannels()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#activeWritingChannels()
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
		public boolean isMarkedForDeletion()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageWritableFile#isMarkedForDeletion()
		}
		
	}

}
