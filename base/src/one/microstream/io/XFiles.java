package one.microstream.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.functional.XFunc;

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
	
	// File FielChannel creation //
	
	public static final FileChannel createWritingFileChannel(final File file) throws FileException, IOException
	{
		return createWritingFileChannel(file, false);
	}

	@SuppressWarnings("resource") // channel handles the closing, hacky JDK API tricking the JLS, so funny
	public static final FileChannel createWritingFileChannel(final File file, final boolean append)
		throws FileException, IOException
	{
		// seriously, no writeable FileChannel without unnecessary FOS first? No proper example googleable.
		return new FileOutputStream(ensureWriteableFile(file), append).getChannel();
	}

	@SuppressWarnings("resource") // channel handles the closing, hacky JDK API tricking the JLS, so funny
	public static final FileChannel createReadingFileChannel(final File file) throws IOException
	{
		// seriously, no writeable FileChannel without unnecessary FIS first? No proper example googleable.
		return new FileInputStream(file).getChannel();
	}
	
	// File writing //
		
	public static final void writeStringToFile(final File file, final String string) throws IOException
	{
		writeStringToFile(file, string, XChars.standardCharset());
	}

	public static final void writeStringToFile(final File file, final String string, final Charset charset)
		throws IOException
	{
		try(final FileOutputStream out = new FileOutputStream(ensureWriteableFile(file)))
		{
			out.write(string.getBytes(charset));
		}
	}

	public static final void mergeBinary(
		final Iterable<File>          sourceFiles,
		final File                    targetFile ,
		final Predicate<? super File> selector
	)
	{
		FileChannel channel = null;
		try
		{
			channel = createWritingFileChannel(targetFile, true);
			for(final File sourceFile : sourceFiles)
			{
				if(!selector.test(sourceFile))
				{
					continue;
				}
				final FileChannel sourceChannel = createReadingFileChannel(sourceFile);
				try
				{
					sourceChannel.transferTo(0, sourceChannel.size(), channel);
				}
				finally
				{
					XIO.closeSilent(sourceChannel);
				}
			}
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (28.10.2014)TODO: proper exception
		}
		finally
		{
			XIO.closeSilent(channel);
		}
	}

	public static final void mergeBinary(
		final Iterable<File> sourceFiles,
		final File           targetFile
	)
	{
		mergeBinary(sourceFiles, targetFile, XFunc.all());
	}
		
	public static void move(final File sourceFile, final File targetFile) throws IORuntimeException, RuntimeException
	{
		XIO.execute(() ->
			XPaths.move(sourceFile.toPath(), targetFile.toPath())
		);
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
