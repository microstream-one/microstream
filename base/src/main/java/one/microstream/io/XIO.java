package one.microstream.io;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.functional.XFunc;
import one.microstream.memory.XMemory;
import one.microstream.util.UtilStackTrace;

public final class XIO
{
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public static char fileSuffixSeparator()
	{
		return '.';
	}
	
	public static char filePathSeparator()
	{
		return '/';
	}
	
	public static String addFileSuffix(final String fileName, final String fileSuffix)
	{
		return fileSuffix != null
			? fileName + fileSuffixSeparator() + fileSuffix
			: fileName
		;
	}
	
	public static String getFileSuffix(final Path file)
	{
		return getFileSuffix(getFileName(file));
	}
	
	public static String getFileSuffix(final String fileName)
	{
		if(XChars.hasNoContent(fileName))
		{
			return null;
		}
		
		final int fileSuffixSeparatorIndex = fileName.lastIndexOf(fileSuffixSeparator());
		if(fileSuffixSeparatorIndex < 0)
		{
			return null;
		}
		
		return fileName.substring(fileSuffixSeparatorIndex + 1);
	}
	
	public static String getFilePrefix(final Path file)
	{
		return getFilePrefix(getFileName(file));
	}
	
	public static String getFilePrefix(final String fileName)
	{
		if(XChars.hasNoContent(fileName))
		{
			return null;
		}
		
		final int fileSuffixSeparatorIndex = fileName.lastIndexOf(fileSuffixSeparator());
		if(fileSuffixSeparatorIndex < 0)
		{
			return fileName;
		}
		
		return fileName.substring(0, fileSuffixSeparatorIndex);
	}
	
	
	public static void unchecked(final IoOperation operation)
		throws IORuntimeException
	{
		try
		{
			operation.execute();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <T> T unchecked(final IoOperationR<T> operation)
		throws IORuntimeException
	{
		try
		{
			return operation.executeR();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <S> void unchecked(final IoOperationS<S> operation, final S subject)
		throws IORuntimeException
	{
		try
		{
			operation.executeS(subject);
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <S, R> R unchecked(final IoOperationSR<S, R> operation, final S subject)
		throws IORuntimeException
	{
		try
		{
			return operation.executeSR(subject);
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
		
	public static final <C extends Closeable> C close(
		final C         closable  ,
		final Throwable suppressed
	)
		throws IOException
	{
		if(closable == null)
		{
			return null;
		}
		
		try
		{
			closable.close();
		}
		catch(final IOException e)
		{
			if(suppressed != null)
			{
				e.addSuppressed(suppressed);
			}
			throw e;
		}
		
		return closable;
	}
	
	public static final <C extends AutoCloseable> C close(
		final C         closable  ,
		final Throwable suppressed
	)
		throws Exception
	{
		if(closable == null)
		{
			return null;
		}
		
		try
		{
			closable.close();
		}
		catch(final Exception e)
		{
			if(suppressed != null)
			{
				e.addSuppressed(suppressed);
			}
			throw e;
		}
		
		return closable;
	}
	
	
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
	
		
	
	/* (19.11.2019 TM)NOTE:
	 * "Path" is not the greatest idea on earth for a name to represent a file or a directory.
	 * "Path" is way too generic.
	 * It's explicitly not a generic "can-be-anything-Path", it is designed to represent a FileSystem file.
	 *
	 */

	public static final Path Path(final String path)
	{
		return Path(FileSystems.getDefault(), path);
	}
	
	public static final Path Path(final FileSystem fileSystem, final String path)
	{
		// just for completeness' sake and ease of workflow
		return fileSystem.getPath(path);
	}
	
	public static final Path Path(final String... items)
	{
		return Path(FileSystems.getDefault(), items);
	}
	
	public static final Path Path(final FileSystem fileSystem, final String... items)
	{
		if(items == null)
		{
			// (07.03.2022 TM)NOTE: not sure what to do here in that case.
			throw new NullPointerException();
		}
		
		/*
		 * To work around the JDK behavior of conveniently ignoring empty strings in the path items.
		 * This is a critical bug if a leading separator is used to define an absolut path.
		 * Consider:
		 * - "/mydir" gets parsed to the separator-independent path items {"", "mydir"}.
		 * - that array is passed here and on to Paths#get
		 */
		if(items.length > 0 && "".equals(items[0]))
		{
			return fileSystem.getPath(Character.toString(XIO.filePathSeparator()), items);
		}
		
		/* (07.03.2022 TM)XXX: Explaining comment missing
		 * Why did this become necessary?
		 * The previous version...
		 * return fileSystem.getPath("", notNull(items));
		 * ... worked fine in tests.
		 * Also potential null pointer access warning.
		 * Since this is more complex code than the previous version, I added an explicit null check above.
		 */
		return items.length == 1
			? fileSystem.getPath(items[0])
			: fileSystem.getPath(items[0], Arrays.copyOfRange(items, 1, items.length))
		;
	}

	/**
	 * Creates a sub-path under the passed {@code parent} {@link Path} inside the same {@link FileSystem}.
	 * <p>
	 * Note that this is fundamentally different to {@link #Path(String...)} or {@link Paths#get(String, String...)}
	 * since those two end up using {@code FileSystems.getDefault()}, no matter the {@link FileSystem} that the passed
	 * parent {@link Path} is associated with.
	 * 
	 * @param  parent the {@code parent} {@link Path} of the new sub-path.
	 * @param  items the path items defining the sub-path under the passed {@code parent} {@link Path}.
	 * @return a sub-path under the passed {@code parent} {@link Path}.
	 */
	public static final Path Path(final Path parent, final String... items)
	{
		if(parent == null)
		{
			return Path(items);
		}
		
		return parent.getFileSystem().getPath(parent.toString(), items);
	}
	
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
	
	public static String[] splitPath(final Path path)
	{
		/*
		 * Note on algorithm:
		 * Path#iterator does not work, because it omits the root element.
		 * Prepending the root element does not work because it has a trailing separator in its toString
		 * representation (which is inconsistent to all other Path elements) and there is no proper "getIdentifier"
		 * method or such in Path.
		 * Besides, Path only stores a plain String and every operation has to inefficiently deconstruct that string.
		 * 
		 * So the only reasonable and performance-wise best approach in the first place is to split the string
		 * directly.
		 * 
		 * But :
		 * String#split cannot be used since the separator might be a regex meta character.
		 * It could be quoted, but all this regex business gets into the realm of cracking a nut with a sledgehammer.
		 * 
		 * So a simpler, more direct and in the end much faster approach is used.
		 * This might very well become relevant if lots of Paths (e.g. tens of thousands when scanning a drive) have
		 * to be processed.
		 */
		
		// local variables for debugging purposes. Should be jitted out, anyway.
		final String pathString = path.toString();
		final String separator  = path.getFileSystem().getSeparator();
		
		return XChars.splitSimple(pathString, separator);
	}
	
	public static final VarString assemblePath(
		final VarString       vs       ,
		final CharSequence... elements
	)
	{
		return XChars.assembleSeparated(vs, XIO.filePathSeparator(), elements);
	}

	public static boolean isDirectory(final Path path) throws IOException
	{
		// file or directory
		return Files.isDirectory(path);
	}

	public static boolean exists(final Path path) throws IOException
	{
		// file or directory
		return Files.exists(path);
	}
	
	public static long size(final Path file) throws IOException
	{
		// file only
		return Files.size(file);
	}
	
	public static final boolean delete(final Path path) throws IOException
	{
		return Files.deleteIfExists(path);
	}
	
	
	
	public static Path[] listEntries(final Path directory) throws IOException
	{
		return listEntries(directory, XFunc.all());
	}
	
	public static Path[] listEntries(
		final Path                    directory,
		final Predicate<? super Path> selector
	)
		throws IOException
	{
		return listEntries(directory, BulkList.New(), selector).toArray(Path.class);
	}
	
	public static <C extends Consumer<? super Path>> C listEntries(
		final Path directory,
		final C    target
	)
		throws IOException
	{
		return iterateEntries(directory, target);
	}
	
	public static <C extends Consumer<? super Path>> C listEntries(
		final Path                    directory,
		final C                       target   ,
		final Predicate<? super Path> selector
	)
		throws IOException
	{
		return iterateEntries(directory, target, selector);
	}
	
	/**
	 * Warning: this (because of using Files.newDirectoryStream) does some weird file opening/locking stuff.
	 * <p>
	 * Also see: https://stackoverflow.com/questions/48311252/a-bit-strange-behaviour-of-files-delete-and-files-deleteifexists
	 * 
	 * @param <C> the consumer type
	 * @param directory the directory to iterate
	 * @param logic the itaration logic
	 * @return the given logic
	 * @throws IOException if an IO error occurs
	 */
	public static <C extends Consumer<? super Path>> C iterateEntries(
		final Path directory,
		final C    logic
	)
		throws IOException
	{
		return iterateEntries(directory, logic, XFunc.all());
	}
	
	/**
	 * Warning: this (because of using Files.newDirectoryStream) does some weird file opening/locking stuff.
	 * <p>
	 * Also see: https://stackoverflow.com/questions/48311252/a-bit-strange-behaviour-of-files-delete-and-files-deleteifexists
	 * 
	 * @param <C> the consumer type
	 * @param directory the directory to iterate
	 * @param logic the itaration logic
	 * @param selector filter predicate
	 * @return the given logic
	 * @throws IOException if an IO error occurs
	 */
	public static <C extends Consumer<? super Path>> C iterateEntries(
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
	
	public static final long lastModified(final Path file) throws IOException
	{
		return Files.getLastModifiedTime(file).toMillis();
	}
	
	public static String toAbsoluteNormalizedPath(final Path file)
	{
		return file.toAbsolutePath().normalize().toString();
	}
	
	
	
	public static final <P extends Path> P ensureDirectory(final P directory) throws IOException
	{
		// Let's hope calling this on an already existing directory is not too much overhead ...
		Files.createDirectories(directory);

		return directory;
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
		
	public static final <P extends Path> P ensureWriteableFile(final P file) throws IOException, FilePathException
	{
		ensureFile(file);
		
		if(!Files.isWritable(file))
		{
			throw new FilePathException(file, "Unwritable file");
		}
		
		return file;
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
		return openFileChannel(file, XArrays.ensureContained(options, READ));
	}
	
	public static FileChannel openFileChannelWriting(final Path file, final OpenOption... options)
		throws IOException
	{
		return openFileChannel(file, XArrays.ensureContained(options, WRITE));
	}
	
	public static FileChannel openFileChannelRW(final Path file, final OpenOption... options)
		throws IOException
	{
		return openFileChannelWriting(file, XArrays.ensureContained(options, READ));
	}
	
	public static FileChannel openFileChannel(final Path file, final OpenOption... options)
		throws IOException
	{
		return FileChannel.open(file, options);
	}
	
	
	
	public static final <T> T readOneShot(final Path file, final IoOperationSR<FileChannel, T> operation)
		throws IOException
	{
		return XIO.performClosingOperation(
			openFileChannelReading(file),
			operation
		);
	}
	
	
	/**
	 * Extreme convenience method. Normally, methods handling files should not accept file path strings, but only
	 * properly typed file instances like {@link Path}.
	 * However, for a convenience method, there is not much safety won writing
	 * {@code readString(Path("./my/path/myFile.txt"))}, only verbosity.<br>
	 * So when already using a convenience method, anyway, why not make it really convenient and accept file path
	 * strings right away?
	 * 
	 * @param filePath the source file path
	 * @return the contents of the file
	 * @throws IOException if an IO error occurs
	 */
	public static String readString(final String filePath)
		throws IOException
	{
		return readString(Path(filePath));
	}
	
	/**
	 * Extreme convenience method. Normally, methods handling files should not accept file path strings, but only
	 * properly typed file instances like {@link Path}.
	 * However, for a convenience method, there is not much safety won writing
	 * {@code readString(Path("./my/path/myFile.txt"))}, only verbosity.<br>
	 * So when already using a convenience method, anyway, why not make it really convienent and accept file path
	 * strings right away?
	 * 
	 * @param filePath the source file path
	 * @param charSet the charset to use
	 * @return the contents of the file
	 * @throws IOException if an IO error occurs
	 */
	public static String readString(final String filePath, final Charset charSet)
		throws IOException
	{
		return readString(Path(filePath), charSet);
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
	
	public static String readString(final FileChannel fileChannel)
		throws IOException
	{
		return readString(fileChannel, XChars.standardCharset());
	}
	
	public static String readString(final FileChannel fileChannel, final Charset charSet)
		throws IOException
	{
		final byte[] bytes = read_bytes(fileChannel);
		
		return XChars.String(bytes, charSet);
	}
	
	
	public static byte[] read_bytes(final Path file)
		throws IOException
	{
		final ByteBuffer content = read(file);
		final byte[]     bytes   = XMemory.toArray(content);
		XMemory.deallocateDirectByteBuffer(content);
		
		return bytes;
	}
	
	public static byte[] read_bytes(final FileChannel fileChannel)
		throws IOException
	{
		final ByteBuffer bb = XIO.read(fileChannel);
		
		final byte[] bytes = XMemory.toArray(bb);
		XMemory.deallocateDirectByteBuffer(bb);
		
		return bytes;
	}
	
	public static ByteBuffer read(final Path file) throws IOException
	{
		return readOneShot(file, XIO::read);
	}
	
	
	
	public static final <T> T writeOneShot(
		final Path                          file     ,
		final IoOperationSR<FileChannel, T> operation
	)
		throws IOException
	{
		ensureWriteableFile(file);
		
		return XIO.performClosingOperation(
			openFileChannelWriting(file),
			operation
		);
	}
		
	/**
	 * Writes the contents of the string to the file.
	 * <p>
	 * <b>Attention:</b> Internally this method opens a new FileChannel to operate on!
	 * 
	 * @param file the file to write to
	 * @param string the string to write
	 * @return number of actual written bytes
	 * @throws IOException if an IO error occurs
	 */
	public static final long write(final Path file, final String string)
		throws IOException
	{
		return write(file, string, XChars.standardCharset());
	}
	
	/**
	 * Writes the contents of the string to the file.
	 * <p>
	 * <b>Attention:</b> Internally this method opens a new FileChannel to operate on!
	 * 
	 * @param file the file to write to
	 * @param string the string to write
	 * @param charset the charset which is used to decode the string
	 * @return number of actual written bytes
	 * @throws IOException if an IO error occurs
	 */
	public static final long write(final Path file, final String string, final Charset charset)
		throws IOException
	{
		final byte[] bytes = string.getBytes(charset);

		return write(file, bytes);
	}
	
	/**
	 * Writes the contents of the array to the file.
	 * <p>
	 * <b>Attention:</b> Internally this method opens a new FileChannel to operate on!
	 * 
	 * @param file the file to write to
	 * @param bytes the bytes to write
	 * @return number of actual written bytes
	 * @throws IOException if an IO error occurs
	 */
	public static final long write(final Path file, final byte[] bytes)
		throws IOException
	{
		final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
		final Long writeCount = write(file, dbb);
		XMemory.deallocateDirectByteBuffer(dbb);
		
		return writeCount;
	}
	
	/**
	 * Writes the contents of the buffer to the file.
	 * <p>
	 * <b>Attention:</b> Internally this method opens a new FileChannel to operate on!
	 * 
	 * @param file the file to write to
	 * @param buffer the buffer to write
	 * @return number of actual written bytes
	 * @throws IOException if an IO error occurs
	 */
	public static long write(final Path file, final ByteBuffer buffer)
		throws IOException
	{
		return writeOneShot(file, fc ->
			XIO.write(fc, buffer)
		);
	}
	
	/**
	 * Truncates the file to the given size
	 * <p>
	 * <b>Attention:</b> Internally this method opens a new FileChannel to operate on!
	 *
	 * @param file file to be truncated
	 * @param newSize new Size, must be zero or greater
	 * @throws IOException if an IO error occurs
	 */
	public static void truncate(final Path file, final long newSize)
		throws IOException
	{
		writeOneShot(file, fc ->
			fc.truncate(newSize)
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
	
	
	
	public static final void mergeBinary(
		final Iterable<Path>          sourceFiles,
		final Path                    targetFile ,
		final Predicate<? super Path> selector
	)
	{
		FileChannel channel = null;
		try
		{
			Throwable suppressed = null;
			try
			{
				channel = openFileChannelWriting(targetFile, StandardOpenOption.APPEND);
				for(final Path sourceFile : sourceFiles)
				{
					if(!selector.test(sourceFile))
					{
						continue;
					}
					
					try(final FileChannel sourceChannel = openFileChannelReading(sourceFile))
					{
						sourceChannel.transferTo(0, sourceChannel.size(), channel);
					}
				}
			}
			catch(final IOException e)
			{
				suppressed = e;
			}
			finally
			{
				XIO.close(channel, suppressed);
			}
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

	public static final void mergeBinary(
		final Iterable<Path> sourceFiles,
		final Path           targetFile
	)
	{
		mergeBinary(sourceFiles, targetFile, XFunc.all());
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
	
	public static final ByteBuffer wrapInDirectByteBuffer(final byte[] bytes)
	{
		final ByteBuffer dbb = ByteBuffer.allocateDirect(bytes.length);
		dbb.put(bytes);
		dbb.flip();
		
		return dbb;
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
	 * @param fileChannel the target file channel
	 * @param byteBuffers the source data buffers
	 * @return the number of written bytes
	 * @throws IOException if an IO error occurs
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
			// file channel position is implicitely advanced by the amount of written bytes.
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
	 * @param fileChannel the target file channel
	 * @param byteBuffers the source data buffers
	 * @return the number of written bytes
	 * @throws IOException if an IO error occurs
	 */
	public static long appendAllGuaranteed(final FileChannel fileChannel, final ByteBuffer[] byteBuffers)
		throws IOException
	{
		final long oldLength  = fileChannel.size();
		final long writeCount = XIO.appendAll(fileChannel, byteBuffers);
		
		// this is the right place for a data-safety-securing force/flush.
		fileChannel.force(false);
		
		final long newTotalLength = fileChannel.size();
		
		if(newTotalLength != oldLength + writeCount)
		{
			throw new IOException(
				"Inconsistent post-write file length:"
				+ " New total length " + newTotalLength +
				" is not equal " + oldLength + " + " + writeCount + " (old length and write count)"
			);
		}
		
		return writeCount;
	}
	
	public static long writeAppending(final FileChannel fileChannel, final ByteBuffer buffer)
		throws IOException
	{
		// appending logic
		return writePositioned(fileChannel, fileChannel.size(), buffer);
	}
	
	public static long writePositioned(
		final FileChannel fileChannel ,
		final long        filePosition,
		final ByteBuffer  buffer
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
		return writeToChannel(fileChannel, buffer);
	}
	
	public static long write(
		final FileChannel                    fileChannel,
		final Iterable<? extends ByteBuffer> buffers
	)
		throws IOException
	{
		long writeCount = 0;
		
		for(final ByteBuffer buffer : buffers)
		{
			writeCount += writeToChannel(fileChannel, buffer);
		}
		
		return writeCount;
	}

	public static void truncate(
		final FileChannel fileChannel,
		final long        newSize
	)
		throws IOException
	{
		fileChannel.truncate(newSize);
	}
	
	private static long writeToChannel(
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
		
	public static ByteBuffer read(final FileChannel fileChannel)
		throws IOException
	{
		return read(fileChannel, 0);
	}
	
	public static ByteBuffer read(
		final FileChannel fileChannel ,
		final long        filePosition
	)
		throws IOException
	{
		return read(fileChannel, filePosition, fileChannel.size());
	}
	
	public static ByteBuffer read(
		final FileChannel fileChannel ,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		// always hilarious to see that a low-level IO-tool has a int size limitation. Geniuses.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(X.checkArrayRange(length));
		
		read(fileChannel, dbb, filePosition, dbb.limit());
		
		dbb.flip();
		
		return dbb;
	}
	
	public static long read(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer
	)
		throws IOException
	{
		return read(fileChannel, targetBuffer, 0, fileChannel.size());
	}
		
	public static long read(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		if(targetBuffer.remaining() < length)
		{
			throw new IllegalArgumentException(
				"Provided target buffer has not enough space remaining to load the file content: "
				+ targetBuffer.remaining() + " < " + length
			);
		}
		
		return internalRead(fileChannel, targetBuffer, filePosition, length);
	}
	
	public static long read(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition
	)
		throws IOException
	{
		return internalRead(fileChannel, targetBuffer, filePosition, targetBuffer.remaining());
	}
	
	private static long internalRead(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition,
		final long        effectiveLength
	)
		throws IOException
	{
		if(effectiveLength == 0L)
		{
			/*
			 * no-op
			 */
			return 0L;
		}
		
		final int  targetLimit = X.checkArrayRange(targetBuffer.position() + effectiveLength);
		final long fileLength  = fileChannel.size();
		
		X.validateRange(fileLength, filePosition, effectiveLength);
		long fileOffset = filePosition;
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
	
	
	/**
	 * Uses {@link #openFileChannelReading(Path)}, {@link #openFileChannelWriting(Path, OpenOption...)}
	 * and {@link #copyFile(FileChannel, FileChannel)} to copy the contents of the specified {@code sourceFile}
	 * to the specified {@code targetFile}.<br>
	 * {@link #ensureDirectoryAndFile(Path)} is intentionally <b>NOT</b> called in order to not swallow problems
	 * in the calling context's logic.<p>
	 * <b>Important note</b>:<br>
	 * This method is a fix for the JDK method {@link Files#copy(Path, Path, java.nio.file.CopyOption...)},
	 * which throws an exception about another process having locked "the file" (without specifying
	 * which one it means) if the process owns a lock on the source file. Since this means the process locks
	 * itself out of using the source file if it has secured the source file for its exclusive use.
	 * As a consequence, the JDK method cannot be used if a file is locked and should generally not be
	 * trusted.
	 * <p>
	 * For any special needs like copying from and/or to a position and/or only a part of the file and/or using
	 * custom OpenOptions and/or modifying file timestamps and or performing pre- or post-actions, it is strongly
	 * suggested to write a custom tailored version of a copying method. Covering all conceivable cases would result
	 * in an overly complicated one-size-fits-all attempt and we all know how well those work in practice.
	 * 
	 * @param sourceFile the source file whose content shall be copied.
	 * @param targetFile the target file that shall receive the copied content. Must already exist!
	 * @param targetChannelOpenOptions the {@link OpenOption}s (see {@link StandardOpenOption}) to be passed to
	 *        {@link #openFileChannelWriting(Path, OpenOption...)}. May be null / empty.
	 * 
	 * @return the number of bytes written by {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)}.
	 * 
	 * @throws IOException if an IO error occurs
	 * 
	 * @see #ensureFile(Path)
	 * @see #ensureDirectoryAndFile(Path)
	 * @see StandardOpenOption
	 * @see #openFileChannelReading(Path)
	 * @see #openFileChannelWriting(Path)
	 * @see #copyFile(FileChannel, FileChannel)
	 * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
	 */
	public static long copyFile(
		final Path          sourceFile              ,
		final Path          targetFile              ,
		final OpenOption... targetChannelOpenOptions
	)
		throws IOException
	{
		
		try(
			final FileChannel sourceChannel = openFileChannelReading(sourceFile);
			final FileChannel targetChannel = openFileChannelWriting(targetFile, targetChannelOpenOptions);
		)
		{
			return copyFile(sourceChannel, targetChannel);
		}
	}
	
	/**
	 * Alias for {@code targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size())}.<br>
	 * 
	 * @param sourceChannel an open and readable channel to the source file whose content shall be copied.
	 * @param targetChannel an open and writeable channel to the target file that shall receive the copied content.
	 * 
	 * @return The number of bytes, possibly zero, that were actually transferred.
	 * 
	 * @throws IOException as specified by {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)}
	 * 
	 * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
	 * @see #copyFile(Path, Path, OpenOption...)
	 */
	public static long copyFile(
		final FileChannel sourceChannel,
		final FileChannel targetChannel
	)
		throws IOException
	{
		return copyFile(sourceChannel, 0, targetChannel);
	}
	
	/**
	 * Uses the sourceChannel's current position!
	 * @param sourceChannel an open and readable channel to the source file whose content shall be copied.
	 * @param targetChannel an open and writeable channel to the target file that shall receive the copied content.
	 * @param targetPosition the position to write to in the target channel
	 * @return the number of written bytes
	 * @throws IOException as specified by {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)}
	 */
	public static long copyFile(
		final FileChannel sourceChannel ,
		final FileChannel targetChannel ,
		final long        targetPosition
	)
		throws IOException
	{
		return targetChannel.transferFrom(sourceChannel, targetPosition, sourceChannel.size());
	}
	
	public static long copyFile(
		final FileChannel sourceChannel ,
		final long        sourcePosition,
		final FileChannel targetChannel
	)
		throws IOException
	{
		return copyFile(sourceChannel, sourcePosition, sourceChannel.size() - sourcePosition, targetChannel);
	}
	
	public static long copyFile(
		final FileChannel sourceChannel ,
		final long        sourcePosition,
		final long        length        ,
		final FileChannel targetChannel
	)
		throws IOException
	{
		return sourceChannel.transferTo(sourcePosition, length, targetChannel);
	}
	
	public static long copyFile(
		final FileChannel sourceChannel ,
		final FileChannel targetChannel ,
		final long        targetPosition,
		final long        length
	)
		throws IOException
	{
		return targetChannel.transferFrom(sourceChannel, targetPosition, length);
	}
	
	// breaks naming conventions intentionally to indicate a modification of called methods instead of a type
	public static final class unchecked
	{
		public static final <C extends Closeable> C close(
			final C closable
		)
			throws IORuntimeException
		{
			if(closable == null)
			{
				return null;
			}
			
			try
			{
				closable.close();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return closable;
		}
		
		public static final <C extends AutoCloseable> C close(
			final C closable
		)
			throws RuntimeException
		{
			if(closable == null)
			{
				return null;
			}
			
			try
			{
				closable.close();
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
			
			return closable;
		}
		
		public static final <C extends Closeable> C close(
			final C         closable  ,
			final Throwable suppressed
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.close(closable, suppressed);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <C extends AutoCloseable> C close(
			final C         closable  ,
			final Throwable suppressed
		)
			throws RuntimeException
		{
			try
			{
				return XIO.close(closable, suppressed);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		
		public static final long size(final FileChannel fileChannel) throws IORuntimeException
		{
			try
			{
				return fileChannel.size();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static boolean isDirectory(final Path path) throws IORuntimeException
		{
			try
			{
				return XIO.isDirectory(path);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final boolean exists(final Path path) throws IORuntimeException
		{
			try
			{
				return XIO.exists(path);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final long size(final Path file) throws IORuntimeException
		{
			try
			{
				return XIO.size(file);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final boolean delete(final Path path) throws IORuntimeException
		{
			try
			{
				return XIO.delete(path);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final Path[] listEntries(final Path directory) throws IORuntimeException
		{
			try
			{
				return XIO.listEntries(directory, XFunc.all());
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static Path[] listEntries(
			final Path                    directory,
			final Predicate<? super Path> selector
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.listEntries(directory, selector);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <C extends Consumer<? super Path>> C listEntries(
			final Path directory,
			final C    target
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.listEntries(directory, target);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <C extends Consumer<? super Path>> C listEntries(
			final Path                    directory,
			final C                       target   ,
			final Predicate<? super Path> selector
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.listEntries(directory, target, selector);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		/**
		 * Warning: this (because of using Files.newDirectoryStream) does some weird file opening/locking stuff.
		 * <p>
		 * Also see: https://stackoverflow.com/questions/48311252/a-bit-strange-behaviour-of-files-delete-and-files-deleteifexists
		 * 
		 * @param <C> the consumer type
		 * @param directory the source directory
		 * @param logic the iteration logic
		 * @return the given logic
		 * @throws IORuntimeException when an IO error occurs
		 */
		public static <C extends Consumer<? super Path>> C iterateEntries(
			final Path directory,
			final C    logic
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.iterateEntries(directory, logic);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		/**
		 * Warning: this (because of using Files.newDirectoryStream) does some weird file opening/locking stuff.
		 * <p>
		 * Also see: https://stackoverflow.com/questions/48311252/a-bit-strange-behaviour-of-files-delete-and-files-deleteifexists
		 * 
		 * @param <C> the consumer type
		 * @param directory the source directory
		 * @param logic the iteration logic
		 * @param selector filter predicate
		 * @return the given logic
		 * @throws IORuntimeException when an IO error occurs
		 */
		public static <C extends Consumer<? super Path>> C iterateEntries(
			final Path                    directory,
			final C                       logic    ,
			final Predicate<? super Path> selector
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.iterateEntries(directory, logic, selector);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final boolean hasNoFiles(final Path directory) throws IORuntimeException
		{
			try
			{
				return XIO.hasNoFiles(directory);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final long lastModified(final Path file) throws IORuntimeException
		{
			try
			{
				return XIO.lastModified(file);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <P extends Path> P ensureDirectory(final P directory) throws IORuntimeException
		{
			try
			{
				return XIO.ensureDirectory(directory);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		public static final <P extends Path> P ensureDirectoryAndFile(final P file) throws IORuntimeException
		{
			try
			{
				return XIO.ensureDirectoryAndFile(file);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <P extends Path> P ensureFile(final P file) throws IORuntimeException
		{
			try
			{
				return XIO.ensureFile(file);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <P extends Path> P ensureWriteableFile(final P file) throws IORuntimeException
		{
			try
			{
				return XIO.ensureWriteableFile(file);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static void move(final Path sourceFile, final Path targetFile)
			throws IORuntimeException, RuntimeException
		{
			try
			{
				XIO.move(sourceFile, targetFile);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException when called
		 */
		private unchecked()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XIO()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
