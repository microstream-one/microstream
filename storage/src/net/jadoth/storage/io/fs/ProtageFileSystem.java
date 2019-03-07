package net.jadoth.storage.io.fs;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import net.jadoth.files.FileException;
import net.jadoth.files.XFiles;

public final class ProtageFileSystem
{
	public static final File createWriteableFile(final File directory, final String fileName)
	{
		final File file = new File(directory, fileName);
		XFiles.ensureFile(file);
		
		return validateWriteableFile(file);
	}
	
	public static final File validateWriteableFile(final File file)
	{
		if(!file.canWrite())
		{
			// (25.10.2018 TM)EXCP: proper exception
			throw new FileException(file, "Unwritable file");
		}
		
		return file;
	}
	
	public static final File validateExistingFile(final File file)
	{
		if(!file.exists())
		{
			// (25.10.2018 TM)EXCP: proper exception
			throw new RuntimeException("File does not exist: " + file);
		}
		
		return file;
	}
	
	public static final File validateExistingDirectory(final File directory)
	{
		if(!directory.exists())
		{
			// (25.10.2018 TM)EXCP: proper exception
			throw new RuntimeException("Directory does not exist: " + directory);
		}
		
		return directory;
	}
	
	public static final File validateIsDirectory(final File directory)
	{
		if(!directory.isDirectory())
		{
			// (25.10.2018 TM)EXCP: proper exception
			throw new RuntimeException("Not a directory: " + directory);
		}
		
		return directory;
	}
	
	@SuppressWarnings("resource") // resource closed internally by FileChannel (JDK tricking Java compiler ^^)
	public static FileLock openFileChannel(final File file)
	{
		// the file is always completely and unshared locked.
		final FileLock lock;
		FileChannel channel = null;
		try
		{
			// resource closed internally by FileChannel (JDK tricking Java compiler ^^)
			channel = new RandomAccessFile(file, "rw").getChannel();

			/*
			 * Tests showed that Java file locks even on Windows don't work properly:
			 * They only prevent other Java processes from acquiring another lock.
			 * But other applications (e.g. Hexeditor) can still open and write to the file.
			 * This basically makes any attempt to secure the file useless.
			 * Not only on linux which seems to be complete crap when it comes to locking files,
			 * but also on windows.
			 * As there is no alternative available and it at least works within Java, it is kept nevertheless.
			 */
			lock = channel.tryLock();
			if(lock == null)
			{
				throw new RuntimeException("File seems to be already locked: " + file);
			}
		}
		catch(final Exception e)
		{
			XFiles.closeSilent(channel);
			// (28.06.2014)EXCP: proper exception
			throw new RuntimeException("Cannot obtain lock for file " + file, e);
		}

		return lock;
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private ProtageFileSystem()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
