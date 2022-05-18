package one.microstream.afs.types;

/*-
 * #%L
 * microstream-afs
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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.chars.XChars;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.io.XIO;
import one.microstream.memory.XMemory;

public class AFS
{
	public static XGettingEnum<AItem> listItems(
		final ADirectory               directory,
		final Predicate<? super AItem> selector
	)
	{
		return listFiles(directory, selector, HashEnum.New());
	}
	
	public static <C extends Consumer<? super AItem>> C listItems(
		final ADirectory               directory,
		final Predicate<? super AItem> selector ,
		final C                        collector
	)
	{
		directory.iterateItems(f ->
		{
			if(selector.test(f))
			{
				collector.accept(f);
			}
		});

		return collector;
	}

	public static XGettingEnum<ADirectory> listDirectories(
		final ADirectory                    directory,
		final Predicate<? super ADirectory> selector
	)
	{
		return listDirectories(directory, selector, HashEnum.New());
	}
	
	public static <C extends Consumer<? super ADirectory>> C listDirectories(
		final ADirectory                    directory,
		final Predicate<? super ADirectory> selector ,
		final C                             collector
	)
	{
		directory.iterateDirectories(f ->
		{
			if(selector.test(f))
			{
				collector.accept(f);
			}
		});

		return collector;
	}
	
	public static XGettingEnum<AFile> listFiles(
		final ADirectory               directory,
		final Predicate<? super AFile> selector
	)
	{
		return listFiles(directory, selector, HashEnum.New());
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
	
	public static void executeWriting(
		final AFile                           file ,
		final Consumer<? super AWritableFile> logic
	)
	{
		executeWriting(file, file.defaultUser(), logic);
	}
	
	public static void executeWriting(
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
	
	
	public static <R> R applyWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return applyWriting(file, file.defaultUser(), logic);
	}
	
	public static <R> R applyWriting(
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
	
	public static <R> R tryApplyWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryApplyWriting(file, file.defaultUser(), logic);
	}
	
	public static <R> R tryApplyWritingDefaulting(
		final AFile                              file        ,
		final R                                  defaultValue,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryApplyWritingDefaulting(file, file.defaultUser(), defaultValue, logic);
	}
	
	public static <R> R tryApplyWriting(
		final AFile                              file ,
		final Object                             user ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryApplyWritingDefaulting(file, user, null, logic);
	}
	
	public static <R> R tryApplyWritingDefaulting(
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
	
	// (06.06.2020 TM)TODO: priv#49: need waitingUse~ and waitingExecute~ as well(?).
	
	
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
	 * @throws UnsupportedOperationException when called
	 */
	private AFS()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
