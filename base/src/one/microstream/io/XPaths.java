package one.microstream.io;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.functional.XFunc;
import one.microstream.memory.XMemory;

/**
 * @author Thomas Muenz
 *
 */
public final class XPaths
{
	/* (19.11.2019 TM)NOTE:
	 * "Path" must be the dumbest idea on earth for a name to represent a file or a directory.
	 * "Path" is way too generic. A physical way is also a path. A reference track is a path. The rules of
	 * a cult can be a "path". Etc etc.
	 * It's explicitely not a generic "can-be-anything-Path", it is designed to represent a FileSystem file.
	 * 
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
	 * Also, hilariously, their intellectual capacity only sufficed for exactely ONE interface.
	 * FileSystem HAD to be a class again. One with purely abstract methods (AKA an idiot's interface).
	 * Because may the god of idiots beware to ever make a proper API.
	 * 
	 * So now we are stuck with "Path" to indiscriminately talk about files and directories alike.
	 * Thanks to the JDK geniuses once again.
	 * But I refuse to name the variables "path" instead of "file".
	 * If there is a name of type String, the variable is "String name" and not "String string // this is a name".
	 * So "Path file" and "Path directory" it is.
	 * The same applies to method names. It's about ensuring a writeable FILE, an actual file, not a directory
	 * and not some pilgrim path on which you are allowed to write a diary or something like that.
	 */

	public static final Path Path(final String... items)
	{
		// because why make it simple...
		return Paths.get("", notNull(items));
	}

	public static final Path Path(final Path parent, final String... items)
	{
		/*
		 * They seem to really have made every mistake possible on the Path API.
		 * Not even a defined, reliable getter method for the string representation.
		 * Oh wait, there's #getFileName() ... but oh... wait... lol
		 */
		return Paths.get(parent.toString(), items);
	}
	
	/**
	 * Providing only the abused moreless debug-information method #toString is a horrible misconception since
	 * it conveys no clear message which string is desired. Why are the JDK guys so horribly bad at designing
	 * clean Java code structure? Why?
	 * 
	 * @param file
	 * @return
	 */
	public static String getFilePath(final Path file)
	{
		// because lol.
		return file != null
			? file.toString()
			: null
		;
	}
	
	public static String getFileName(final Path file)
	{
		// because lol.
		return file != null
			? file.getFileName().toString()
			: null
		;
	}

	// because the IDE-generated ", null" for their method drives one crazy when working with it.
	public static boolean isDirectory(final Path path) throws IOException
	{
		// file or directory
		return Files.isDirectory(path);
	}
	
	public static boolean isDirectoryUnchecked(final Path path) throws IORuntimeException
	{
		try
		{
			return isDirectory(path);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	// because the IDE-generated ", null" for their method drives one crazy when working with it.
	public static boolean exists(final Path path) throws IOException
	{
		// file or directory
		return Files.exists(path);
	}
	
	public static final boolean existsUnchecked(final Path directory) throws IORuntimeException
	{
		try
		{
			return exists(directory);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static final boolean delete(final Path path) throws IOException
	{
		// (25.11.2019 TM)FIXME: priv#157: XPaths#delete()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	public static final boolean deleteUnchecked(final Path path) throws IORuntimeException
	{
		try
		{
			return delete(path);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	// (25.11.2019 TM)FIXME: priv#157: really "children" or is "files" better after all?
	public static Path[] listChildren(final Path directory) throws IOException
	{
		return listChildren(directory, XFunc.all());
	}
	
	public static final Path[] listChildrenUnchecked(final Path directory) throws IORuntimeException
	{
		try
		{
			return listChildren(directory, XFunc.all());
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static Path[] listChildren(
		final Path                    directory,
		final Predicate<? super Path> selector
	)
		throws IOException
	{
		return listChildren(directory, BulkList.New(), selector).toArray(Path.class);
	}
	
	public static Path[] listChildrenUnchecked(
		final Path                    directory,
		final Predicate<? super Path> selector
	)
		throws IORuntimeException
	{
		try
		{
			return listChildren(directory, selector);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static <C extends XAddingCollection<? super Path>> C listChildren(
		final Path directory,
		final C   target
	)
		throws IOException
	{
		return iterateChildren(directory, target);
	}
	
	public static final <C extends XAddingCollection<? super Path>> C listChildrenUnchecked(
		final Path directory,
		final C   target
	)
		throws IORuntimeException
	{
		try
		{
			return listChildren(directory, target);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static <C extends XAddingCollection<? super Path>> C listChildren(
		final Path                    directory,
		final C                       target   ,
		final Predicate<? super Path> selector
	)
		throws IOException
	{
		return iterateChildren(directory, target, selector);
	}
	
	public static final <C extends XAddingCollection<? super Path>> C listChildrenUnchecked(
		final Path                    directory,
		final C                       target   ,
		final Predicate<? super Path> selector
	)
		throws IORuntimeException
	{
		try
		{
			return listChildren(directory, target, selector);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static <C extends Consumer<? super Path>> C iterateChildren(
		final Path directory,
		final C    logic
	)
		throws IOException
	{
		return iterateChildren(directory, logic, XFunc.all());
	}
	
	public static <C extends Consumer<? super Path>> C iterateChildren(
		final Path                    directory,
		final C                       logic    ,
		final Predicate<? super Path> selector
	)
		throws IOException
	{
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
		{
	        for(final Path p : stream)
	        {
	        	if(!selector.test(p))
	        	{
	        		continue;
	        	}
	        	logic.accept(p);
	        }
	    }
		
		return logic;
	}
	
	public static <C extends Consumer<? super Path>> C iterateChildrenUnchecked(
		final Path directory,
		final C    logic
	)
		throws IORuntimeException
	{
		try
		{
			return iterateChildren(directory, logic);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static <C extends Consumer<? super Path>> C iterateChildrenUnchecked(
		final Path                    directory,
		final C                       logic    ,
		final Predicate<? super Path> selector
	)
		throws IORuntimeException
	{
		try
		{
			return iterateChildren(directory, logic, selector);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static boolean hasNoFiles(final Path directory) throws IOException
	{
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
		{
	        return !stream.iterator().hasNext();
	    }
		catch(final IOException e)
		{
			throw e;
		}
	}
	
	public static final boolean hasNoFilesUnchecked(final Path directory) throws IORuntimeException
	{
		try
		{
			return hasNoFiles(directory);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static final long sizeUnchecked(final Path file) throws IORuntimeException
	{
		try
		{
			return Files.size(file);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static final <P extends Path> P ensureDirectory(final P directory) throws IOException
	{
		// Let's hope calling this on an already existing directory is not too much overhead ...
		Files.createDirectories(directory);

		return directory;
	}
	
	public static final <P extends Path> P ensureDirectoryUnchecked(final P directory) throws IORuntimeException
	{
		try
		{
			return ensureDirectory(directory);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static final <P extends Path> P ensureDirectoryAndFile(final P file) throws IOException
	{
		final Path parent;
		if((parent = file.getParent()) != null)
		{
			ensureDirectory(parent);
		}
		
		return ensureFile(file);
	}

	public static final <P extends Path> P ensureDirectoryAndFileUnchecked(final P directory) throws IORuntimeException
	{
		try
		{
			return ensureDirectoryAndFile(directory);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

	public static final <P extends Path> P ensureFile(final P file) throws IOException
	{
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
				throw e;
			}
		}
		
		return file;
	}
	
	public static final <P extends Path> P ensureFileUnchecked(final P directory) throws IORuntimeException
	{
		try
		{
			return ensureFile(directory);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
		
	public static final <P extends Path> P ensureWriteableFile(final P file) throws IOException, FilePathException
	{
		ensureFile(file);
		
		if(!Files.isWritable(file))
		{
			throw new FilePathException(file, "Unwritable file");
		}
		
		return file;
	}
	
	public static final <P extends Path> P ensureWriteableFileUnchecked(final P directory) throws IORuntimeException
	{
		try
		{
			return ensureWriteableFile(directory);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
	
	public static FileChannel openFileChannelReading(final Path file)
		throws IOException
	{
		return FileChannel.open(file, READ);
	}
	
	public static FileChannel openFileChannelWriting(final Path file)
		throws IOException
	{
		return FileChannel.open(file, WRITE);
	}
	
	public static FileChannel openFileChannelRW(final Path file)
		throws IOException
	{
		return FileChannel.open(file, READ, WRITE);
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
	
	public static FileChannel openFileChannelRW(final Path file, final OpenOption... options)
		throws IOException
	{
		return FileChannel.open(file, XArrays.ensureContained(options, READ, WRITE));
	}
	
	public static final <T> T readOneShot(final Path file, final IoOperationSR<FileChannel, T> operation)
		throws IOException
	{
		return XIO.performClosingOperation(
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
		return readOneShot(file, XIO::readFile);
	}
	
	public static final <T> T writeOneShot(
		final Path                          file     ,
		final IoOperationSR<FileChannel, T> operation
	)
		throws IOException
	{
		ensureWriteableFileUnchecked(file);
		
		return XIO.performClosingOperation(
			openFileChannelWriting(file),
			operation
		);
	}
	
	public static final long write(final Path file, final String string)
		throws IOException
	{
		return write(file, string, XChars.standardCharset());
	}
	
	public static final long write(final Path file, final String string, final Charset charset)
		throws IOException
	{
		final byte[] bytes = string.getBytes(charset);

		return write(file, bytes);
	}
	
	public static final long write(final Path file, final byte[] bytes)
		throws IOException
	{
		final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
		final Long writeCount = write(file, dbb);
		XMemory.deallocateDirectByteBuffer(dbb);
		
		return writeCount;
	}
	
	public static long write(final Path file, final ByteBuffer buffer)
		throws IOException
	{
		return writeOneShot(file, fc ->
			XIO.write(fc, buffer)
		);
	}
	
	public static final long writePositioned(final Path file, final long filePosition, final String string)
		throws IOException
	{
		return writePositioned(file, filePosition, string, XChars.standardCharset());
	}
	
	public static final long writePositioned(final Path file, final long filePosition, final String string, final Charset charset)
		throws IOException
	{
		final byte[] bytes = string.getBytes(charset);

		return writePositioned(file, filePosition, bytes);
	}
	
	public static final long writePositioned(final Path file, final long filePosition, final byte[] bytes)
		throws IOException
	{
		final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
		final Long writeCount = writePositioned(file, filePosition, dbb);
		XMemory.deallocateDirectByteBuffer(dbb);
		
		return writeCount;
	}
	
	public static long writePositioned(final Path file, final long filePosition, final ByteBuffer buffer)
		throws IOException
	{
		return writeOneShot(file, fc ->
			XIO.writePositioned(fc, filePosition, buffer)
		);
	}
	
	public static final long writeAppending(final Path file, final String string)
		throws IOException
	{
		return writeAppending(file, string, XChars.standardCharset());
	}
	
	public static final long writeAppending(final Path file, final String string, final Charset charset)
		throws IOException
	{
		final byte[] bytes = string.getBytes(charset);

		return writeAppending(file, bytes);
	}
	
	public static final long writeAppending(final Path file, final byte[] bytes)
		throws IOException
	{
		final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
		final Long writeCount = writeAppending(file, dbb);
		XMemory.deallocateDirectByteBuffer(dbb);
		
		return writeCount;
	}
	
	public static long writeAppending(final Path file, final ByteBuffer buffer)
		throws IOException
	{
		return writeOneShot(file, fc ->
			XIO.writeAppending(fc, buffer)
		);
	}
	
	public static void move(final Path sourceFile, final Path targetFile)
		throws IOException, RuntimeException
	{
		try
		{
			Files.move(sourceFile, targetFile);
		}
		catch(final IOException e)
		{
			throw e;
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
	private XPaths()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
