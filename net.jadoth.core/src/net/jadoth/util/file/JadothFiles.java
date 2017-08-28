package net.jadoth.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.util.chars.JadothChars;
import net.jadoth.util.chars.VarString;

/**
 * @author Thomas Muenz
 *
 */
public final class JadothFiles
{
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

	public static String packageStringToFolderPathString(final String packageString)
	{
		return JadothChars.ensureCharAtEnd(packageString.replaceAll("\\.", "/"), '/');
	}


	public static final char[] readCharsFromFile(final File file) throws IOException
	{
		// sadly the geniuses wrapped generic char[] operations inside the String value type class, so it must be hacked
		return JadothChars.getChars(readStringFromFile(file));
	}

	public static final char[] readCharsFromFile(final File file, final Consumer<? super IOException> exceptionHandler)
	{
		return readCharsFromFile(file, Charset.defaultCharset(), exceptionHandler);
	}

	public static final char[] readCharsFromFile(
		final File                           file            ,
		final Charset                        charset         ,
		final Consumer<? super IOException> exceptionHandler
	)
	{
		// sadly the geniuses wrapped generic char[] operations inside the String value type class, so it must be hacked
		final String content;

		try
		{
			content = readStringFromFile(file, charset);
		}
		catch(final IOException e)
		{
			exceptionHandler.accept(e);

			// if the handler did not rethrow the exception, the calling context must be okay with receiving null.
			return null;
		}

		return JadothChars.getChars(content);
	}

	public static final char[] readCharsFromFile(final File file, final Charset charset) throws IOException
	{
		// sadly the geniuses wrapped generic char[] operations inside the String value type class, so it must be hacked
		return JadothChars.getChars(readStringFromFile(file, charset));
	}


	public static final String readStringFromFile(final File file) throws IOException
	{
		return readStringFromFile(file, Charset.defaultCharset());
	}

	public static final String readStringFromFile(final File file, final Charset charset) throws IOException
	{
		try(final FileInputStream fis = new FileInputStream(file))
		{
			return JadothChars.readStringFromInputStream(fis, charset);
		}
	}

	public static final byte[] readBytesFromFile(final File file) throws IOException
	{
		try(final FileInputStream fis = new FileInputStream(file))
		{
			return JadothChars.readAllBytesFromInputStream(fis).toByteArray();
		}
	}

	public static final void writeStringToFile(final File file, final String string) throws IOException
	{
		writeStringToFile(file, string, Charset.defaultCharset());
	}

	public static final void writeStringToFile(final File file, final String string, final Charset charset)
		throws IOException
	{
		try(final FileOutputStream out = new FileOutputStream(ensureWriteableFile(file)))
		{
			out.write(string.getBytes(charset));
		}
	}


	public static final String buildFilePath(final String... items)
	{
		return VarString.New().list("/", items).toString();
	}

	public static final File buildFile(final String... items)
	{
		return new File(buildFilePath(items));
	}

	public static final File buildFile(final File parent, final String... items)
	{
		return new File(parent, buildFilePath(items));
	}

	public static final String getSuffixlessFileName(final File file)
	{
		final String filename = file.getName();
		final int    dotIndex = filename.lastIndexOf('.');
		return dotIndex < 0 ? filename : filename.substring(0, dotIndex);
	}



	@SuppressWarnings("resource") // channel handles the closing, unjustified warning
	public static void copyFile(final File in, final File out) throws IOException
	{
		final FileChannel inChannel  = new FileInputStream(in).getChannel();
		final FileChannel outChannel = new FileOutputStream(out).getChannel();
		try
		{
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
		catch(final IOException e)
		{
			throw e;
		}
		finally
		{
			if(inChannel != null)
			{
				inChannel.close();
			}
			if(outChannel != null)
			{
				outChannel.close();
			}
		}
	}

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
					Jadoth.closeSilent(sourceChannel);
				}
			}
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (28.10.2014)TODO: proper exception
		}
		finally
		{
			Jadoth.closeSilent(channel);
		}
	}

	public static final void mergeBinary(
		final Iterable<File>         sourceFiles,
		final File                   targetFile
	)
	{
		mergeBinary(sourceFiles, targetFile, JadothPredicates.all());
	}


	private JadothFiles()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
