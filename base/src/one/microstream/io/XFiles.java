package one.microstream.io;

import java.io.File;
import java.io.IOException;

import one.microstream.chars.VarString;

/**
 * @author Thomas Muenz
 *
 */
public final class XFiles // Yes, yes. X-Files. Very funny and all that.
{
	///////////////////////////////////////////////////////////////////////////
	// Generic path string utility logic //
	//////////////////////////////////////
	
	public static final String ensureNormalizedPathSeperators(final String path)
	{
		if(path.indexOf('\\') < 0)
		{
			return path;
		}
		
		return path.replace('\\', '/');
	}
	
	public static final String ensureTrailingSlash(final String path)
	{
		if(path.charAt(path.length() - 1) == '/')
		{
			return path;
		}
		
		return path + '/';
	}
	
	public static final String buildFilePath(final String... items)
	{
		return VarString.New().list("/", items).toString();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// java.util.File //
	///////////////////
	
	// File management //

	public static final File File(final String... items)
	{
		return new File(buildFilePath(items));
	}

	public static final File File(final File parent, final String... items)
	{
		return new File(parent, buildFilePath(items));
	}
	
	public static boolean hasNoFiles(final File file)
	{
		final File[] files = file.listFiles();
		
		return files == null || files.length == 0;
	}
	
	public static final File ensureDirectory(final File directory) throws DirectoryException
	{
		try
		{
			if(directory.exists())
			{
				return directory;
			}
			
			synchronized(directory)
			{
				if(!directory.mkdirs())
				{
					// check again in case it has been created in the meantime (race condition)
					if(!directory.exists())
					{
						throw new DirectoryException(directory, "Directory could not have been created.");
					}
				}
			}
		}
		catch(final SecurityException e)
		{
			throw new DirectoryException(directory, e);
		}

		return directory;
	}

	public static final File ensureDirectoryAndFile(final File file) throws FileException
	{
		final File parent;
		if((parent = file.getParentFile()) != null)
		{
			ensureDirectory(parent);
		}
		
		return ensureFile(file);
	}

	public static final File ensureFile(final File file) throws FileException
	{
		try
		{
			file.createNewFile();
		}
		catch(final IOException e)
		{
			throw new FileException(file, e);
		}
		return file;
	}

	public static final File ensureWriteableFile(final File file) throws FileException
	{
		try
		{
			file.createNewFile();
		}
		catch(final IOException e)
		{
			throw new FileException(file, e);
		}

		if(!file.canWrite())
		{
			throw new FileException(file, "Unwritable file");
		}

		return file;
	}
				
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XFiles()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
