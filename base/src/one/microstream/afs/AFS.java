package one.microstream.afs;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingList;
import one.microstream.io.XIO;
import one.microstream.memory.XMemory;

public class AFS
{
	public static XGettingList<AFile> listFiles(
		final ADirectory               directory,
		final Predicate<? super AFile> selector
	)
	{
		return listFiles(directory, selector, BulkList.New());
	}
	
	public static <C extends Consumer<? super AFile>> C listFiles(
		final ADirectory               directory,
		final Predicate<? super AFile> selector ,
		final C                        collector
	)
	{
		directory.iterateFiles(f ->
		{
			if(selector.test(f))
			{
				collector.accept(f);
			}
		});

		return collector;
	}
	
	public static <D extends ADirectory> D ensureExists(final D directory)
	{
		if(!directory.exists())
		{
			directory.ensureExists();
		}
		
		return directory;
	}
	
	
	public static String readString(final AFile file)
	{
		return readString(file, XChars.standardCharset());
	}
	
	public static String readString(final AFile file, final Charset charSet)
	{
		final byte[] bytes = read_bytes(file);
		
		return XChars.String(bytes, charSet);
	}
	
	public static byte[] read_bytes(final AFile file)
	{
		final ByteBuffer content = apply(file, f -> f.readBytes());
		final byte[]     bytes   = XMemory.toArray(content);
		XMemory.deallocateDirectByteBuffer(content);
		
		return bytes;
	}
	
	public static final long writeString(final AFile file, final String string)
	{
		return writeString(file, string, XChars.standardCharset());
	}
	
	public static final long writeString(final AFile file, final String string, final Charset charset)
	{
		final byte[] bytes = string.getBytes(charset);

		return write_bytes(file, bytes);
	}
	
	public static final long write_bytes(final AFile file, final byte[] bytes)
	{
		final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
		final Long writeCount = writeBytes(file, dbb);
		XMemory.deallocateDirectByteBuffer(dbb);
		
		return writeCount;
	}

	public static <R> R apply(
		final AFile                              file ,
		final Function<? super AReadableFile, R> logic
	)
	{
		final AReadableFile rFile = file.useReading();
		try
		{
			return logic.apply(rFile);
		}
		finally
		{
			rFile.release();
		}
	}
	
	public static void execute(
		final AFile                           file ,
		final Consumer<? super AReadableFile> logic
	)
	{
		final AReadableFile rFile = file.useReading();
		try
		{
			logic.accept(rFile);
		}
		finally
		{
			rFile.release();
		}
	}
	
	public static void execute(
		final AFile                           file ,
		final Object                          user ,
		final Consumer<? super AReadableFile> logic
	)
	{
		final AReadableFile rFile = file.useReading(user);
		try
		{
			logic.accept(rFile);
		}
		finally
		{
			rFile.release();
		}
	}
	
	public static long writeBytes(
		final AFile      file ,
		final ByteBuffer bytes
	)
	{
		final AWritableFile wFile = file.useWriting();
		try
		{
			return wFile.writeBytes(bytes);
		}
		finally
		{
			wFile.release();
		}
	}
	
	public static void applyWriting(
		final AFile                           file ,
		final Consumer<? super AWritableFile> logic
	)
	{
		applyWriting(file, file.defaultUser(), logic);
	}
	
	public static void applyWriting(
		final AFile                           file ,
		final Object                          user ,
		final Consumer<? super AWritableFile> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = file.useWriting(user);
		try
		{
			logic.accept(writableFile);
		}
		finally
		{
			writableFile.release();
		}
	}
	
	
	public static <R> R executeWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return executeWriting(file, file.defaultUser(), logic);
	}
	
	public static <R> R executeWriting(
		final AFile                              file ,
		final Object                             user ,
		final Function<? super AWritableFile, R> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = file.useWriting(user);
		try
		{
			return logic.apply(writableFile);
		}
		finally
		{
			writableFile.release();
		}
	}
	
	public static <R> R tryExecuteWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryExecuteWriting(file, file.defaultUser(), logic);
	}
	
	public static <R> R tryExecuteWritingDefaulting(
		final AFile                              file        ,
		final R                                  defaultValue,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryExecuteWritingDefaulting(file, file.defaultUser(), defaultValue, logic);
	}
	
	public static <R> R tryExecuteWriting(
		final AFile                              file ,
		final Object                             user ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryExecuteWritingDefaulting(file, user, null, logic);
	}
	
	public static <R> R tryExecuteWritingDefaulting(
		final AFile                              file       ,
		final Object                             user       ,
		final R                                  defaultValue,
		final Function<? super AWritableFile, R> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = file.tryUseWriting(user);
		if(writableFile == null)
		{
			return defaultValue;
		}
		
		try
		{
			return logic.apply(writableFile);
		}
		finally
		{
			writableFile.release();
		}
	}
	
	// (06.06.2020 TM)FIXME: priv#49: need waitingUse~ and waitingExecute~ as well(?).
	
	
	public static void close(final AReadableFile file, final Throwable cause)
	{
		if(file == null)
		{
			return;
		}
		
		try
		{
			file.close();
		}
		catch(final Throwable t)
		{
			if(cause != null)
			{
				t.addSuppressed(cause);
			}
			throw t;
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
	private AFS()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
