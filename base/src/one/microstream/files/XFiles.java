package one.microstream.files;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.functional.XFunc;
import one.microstream.io.IoOperationSR;
import one.microstream.io.XIO;
import one.microstream.memory.XMemory;

/**
 * @author Thomas Muenz
 *
 */
public final class XFiles // Yes, yes. X-Files. Very funny and all that.
{
	///////////////////////////////////////////////////////////////////////////
	// java.nio.channels.FileChannel //
	//////////////////////////////////
	
	public static ByteBuffer determineLastNonEmpty(final ByteBuffer[] byteBuffers)
	{
		for(int i = byteBuffers.length - 1; i >= 0; i--)
		{
			if(byteBuffers[i].hasRemaining())
			{
				return byteBuffers[i];
			}
		}
		
		// either the array is empty or only contains empty buffers. Either way, no suitable buffer found.
		return null;
	}
	
	/**
	 * Sets the passed {@link FileChannel}'s position to its current length and repeatedly calls
	 * {@link FileChannel#write(ByteBuffer[])} until the last non-empty buffer has no remaining bytes.<br>
	 * This is necessary because JDK's {@link FileChannel#write(ByteBuffer[])} seems to arbitrarily stop processing
	 * the passed {@link ByteBuffer}s even though they have remaining bytes left to be written.
	 * <p>
	 * The reason for this behavior is unknown, but looking at countless other issues in the JDK code,
	 * one might guess... .
	 * 
	 * @param fileChannel
	 * @param byteBuffers
	 * @throws IOException
	 */
	public static long appendAll(final FileChannel fileChannel, final ByteBuffer[] byteBuffers)
		throws IOException
	{
		// determine last non-empty buffer to be used as a write-completion check point
		final ByteBuffer lastNonEmpty = determineLastNonEmpty(byteBuffers);
		if(lastNonEmpty == null)
		{
			return 0L;
		}
		
		final long oldLength = fileChannel.size();
		
		long writeCount = 0;
		fileChannel.position(oldLength);
		while(lastNonEmpty.hasRemaining())
		{
			writeCount += fileChannel.write(byteBuffers);
		}
		
		return writeCount;
	}
	
	/**
	 * Calls {@link #appendAll(FileChannel, ByteBuffer[])}, then {@link FileChannel#force(boolean)}, then validates
	 * if the actual new file size is really exactely what it should be based on old file size and the amount of bytes
	 * written.<p>
	 * In short: this method "guarantees" that every byte contained in the passed {@link ByteBuffer}s was appended
	 * to the passed {@link FileChannel} and actually reached the physical file.
	 * 
	 * @param fileChannel
	 * @param byteBuffers
	 * @throws IOException
	 */
	public static long appendAllGuaranteed(final FileChannel fileChannel, final ByteBuffer[] byteBuffers)
		throws IOException
	{
		final long oldLength  = fileChannel.size();
		final long writeCount = XFiles.appendAll(fileChannel, byteBuffers);
		
		// this is the right place for a data-safety-securing force/flush.
		fileChannel.force(false);
		
		final long newTotalLength = fileChannel.size();
		
		if(newTotalLength != oldLength + writeCount)
		{
			 // (01.10.2014)EXCP: proper exception
			throw new IOException(
				"Inconsistent post-write file length:"
				+ " New total length " + newTotalLength +
				" is not equal " + oldLength + " + " + writeCount + " (old length and write count)"
			);
		}
		
		return writeCount;
	}
	
	public static long writeAppend(final FileChannel fileChannel, final ByteBuffer buffer)
		throws IOException
	{
		// appending logic
		return write(fileChannel, buffer, fileChannel.size());
	}
	
	public static long write(
		final FileChannel fileChannel ,
		final ByteBuffer  buffer      ,
		final long        filePosition
	)
		throws IOException
	{
		fileChannel.position(filePosition);
		
		return write(fileChannel, buffer);
	}
	
	public static long write(
		final FileChannel fileChannel,
		final ByteBuffer  buffer
	)
		throws IOException
	{
		long writeCount = 0;
		while(buffer.hasRemaining())
		{
			writeCount += fileChannel.write(buffer);
		}
		
		return writeCount;
	}
	
	public static final <T> T performClosingOperation(
		final FileChannel                   fileChannel,
		final IoOperationSR<FileChannel, T> operation
	)
		throws IOException
	{
		try
		{
			return operation.executeSR(fileChannel);
		}
		finally
		{
			fileChannel.close();
		}
	}
		
	public static ByteBuffer readFile(final FileChannel fileChannel)
		throws IOException
	{
		return readFile(fileChannel, 0, fileChannel.size());
	}
	
	public static ByteBuffer readFile(
		final FileChannel fileChannel,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		// always hilarious to see that a low-level IO-tool has a int size limitation. Geniuses.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(X.checkArrayRange(length));
		
		readFile(fileChannel, dbb, filePosition, dbb.limit());
		
		dbb.flip();
		
		return dbb;
	}
	
	public static long readFile(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer
	)
		throws IOException
	{
		return readFile(fileChannel, targetBuffer, 0, fileChannel.size());
	}
		
	public static long readFile(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		if(targetBuffer.remaining() < length)
		{
			// (20.11.2019 TM)EXCP: proper exception
			throw new IllegalArgumentException(
				"Provided target buffer has not enough space remaining to load the file content: "
				+ targetBuffer.remaining() + " < " + length
			);
		}

		final int  targetLimit = X.checkArrayRange(targetBuffer.position() + length);
		final long fileLength  = fileChannel.size();
		
		long fileOffset = X.validateRange(fileLength, filePosition, length);
		targetBuffer.limit(targetLimit);
		
		// reading should be done in one fell swoop, but better be sure
		long readCount = 0;
		while(targetBuffer.hasRemaining())
		{
			readCount += fileChannel.read(targetBuffer, fileOffset);
			fileOffset = filePosition + readCount;
		}

		return readCount;
	}
	
	
	
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
	// java.util.File // javaUtilFileMarker
	///////////////////

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
	
	// javaUtilFileMarker
		
	public static void move(final File sourceFile, final File targetFile) throws IORuntimeException, RuntimeException
	{
		move(sourceFile.toPath(), targetFile.toPath());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// java.nio.file.Path //
	///////////////////////
	
	// (21.11.2019 TM)FIXME: priv#157: replicate remaining File util methods
		
	public static final Path ensureWriteableFile(final Path file) throws FilePathException
	{
		/* (19.11.2019 TM)NOTE:
		 * "Path" must be the dumbest idea on earth for a name to represent a file or a directory.
		 * "Path" is way too generic. A physical way is also a path. A reference track is a path. The rules of
		 * a cult can be a "path". Etc etc.
		 * It is traceable that they needed another short and unique type name after "File" was already
		 * taken by their clumsy first attempt, but still: Who talks (primarily!) about "paths" when referring to
		 * files and directories? No one. Of course, every file has a uniquely identifying path in the file system,
		 * but the concept a file is more than just being a path. It has content, attributes, a "primary" name,
		 * a suffix, etc.
		 * A "Path" does not indicate all this. All the concept of a "Path" stands for is:
		 * "follow me and you will get to your destination".
		 * When an API forces people to talk about "paths" when they actually mean files, it's nothing but a
		 * complication.
		 * At least they finally understood to design with interfaces instead of classes (halleluja!), but they did
		 * it on a very basic, beginner-like level. The proper solution would have been:
		 * interface FileItem (or "FSItem" if you must)
		 * interface File extends FileItem
		 * interface Directory extends FileItem
		 * 
		 * With Directory having methods like
		 * iterateFiles
		 * iterateDirectories
		 * iterateItems
		 * etc.
		 * Proper typing. It would have been, could be wonderful.
		 * But noooo. A singular, diffusely general "Path" is the best they could have come up with.
		 * And clumsiest-possible API like Files.newDirectoryStream(mustHappenToBeADirectoryOrElseYouAreInTrouble).
		 * 
		 * So now we are stuck with "Path" to indiscriminately talk about files and directories alike.
		 * Thanks to the JDK geniuses once again.
		 * But I refuse to name the variables "path" instead of "file".
		 * If there is a name of type String, the variable is "String name" and not "String string // this is a name".
		 * So "Path file" and "Path directory" it is.
		 * The same applies to method names. It's about ensuring a writeable FILE, an actual file, not a directory
		 * and not some pilgrim path on which you are allowed to write a diary or something like that.
		 */
		
		if(Files.notExists(file))
		{
			try
			{
				Files.createFile(file);
			}
			catch(final FileAlreadyExistsException e)
			{
				// alright then
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		if(!Files.isWritable(file))
		{
			throw new FilePathException(file, "Unwritable file");
		}
		
		return file;
	}
	
	public static FileChannel openFileChannelReading(final Path file, final OpenOption... options)
		throws IOException
	{
		return FileChannel.open(file, XArrays.ensureContained(options, READ));
	}
	
	public static FileChannel openFileChannelWriting(final Path file, final OpenOption... options)
		throws IOException
	{
		return FileChannel.open(file, XArrays.ensureContained(options, WRITE));
	}
	
	public static final <T> T readOneShot(final Path file, final IoOperationSR<FileChannel, T> operation)
		throws IOException
	{
		return performClosingOperation(
			openFileChannelReading(file),
			operation
		);
	}
	
	public static String readString(final Path file)
		throws IOException
	{
		return readString(file, XChars.standardCharset());
	}
	
	public static String readString(final Path file, final Charset charSet)
		throws IOException
	{
		final byte[] bytes = read_bytes(file);
		
		return XChars.String(bytes, charSet);
	}
	
	public static byte[] read_bytes(final Path file)
		throws IOException
	{
		final ByteBuffer content = readFile(file);
		final byte[]     bytes   = XMemory.toArray(content);
		XMemory.deallocateDirectByteBuffer(content);
		
		return bytes;
	}
	
	public static ByteBuffer readFile(final Path file) throws IOException
	{
		return readOneShot(file, XFiles::readFile);
	}
	
	public static final <T> T writeOneShot(
		final Path                          file     ,
		final IoOperationSR<FileChannel, T> operation
	)
		throws IOException
	{
		return performClosingOperation(
			openFileChannelWriting(file),
			operation
		);
	}
	
	public static final long writeAppend(final Path file, final String string)
		throws IOException
	{
		return writeAppend(file, string, XChars.standardCharset());
	}
	
	public static final long writeAppend(final Path file, final String string, final Charset charset)
		throws IOException
	{
		final byte[] bytes = string.getBytes(charset);

		return writeAppend(file, bytes);
	}
	
	public static final long writeAppend(final Path file, final byte[] bytes)
		throws IOException
	{
		final ByteBuffer dbb = ByteBuffer.allocateDirect(bytes.length);
		dbb.put(bytes);
		dbb.flip();
		
		final Long writeCount = writeOneShot(file, fc ->
			writeAppend(fc, dbb)
		);
		
		XMemory.deallocateDirectByteBuffer(dbb);
		
		return writeCount;
	}
	
	public static long writeAppend(final Path file, final ByteBuffer buffer)
		throws IOException
	{
		return writeOneShot(file, fc ->
			writeAppend(fc, buffer)
		);
	}
	
	public static void move(final Path sourceFile, final Path targetFile) throws IORuntimeException, RuntimeException
	{
		try
		{
			Files.move(sourceFile, targetFile);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
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
