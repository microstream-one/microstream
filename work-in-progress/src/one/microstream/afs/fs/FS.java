package one.microstream.afs.fs;

import java.io.File;
import java.nio.channels.FileLock;

import one.microstream.io.FileException;
import one.microstream.io.XIO;

public final class FS
{
	public static final File createWriteableFile(final File directory, final String fileName)
	{
		final File file = new File(directory, fileName);
		XIO.unchecked.ensureFile(file.toPath());
		
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
	
	public static FileLock openFileChannel(final File file)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ProtageFileSystem#openFileChannel()
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private FS()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
