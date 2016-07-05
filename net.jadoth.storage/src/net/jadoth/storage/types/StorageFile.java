package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;


public interface StorageFile
{
	/* (02.12.2014)TODO: Abstract IO layer completely
	 * using the StorageFile type of this supertype and and writer abstraction.
	 * Maybe even to a point of defining a StorageDirectory or probably leave the directory concept away.
	 */
	
	public File file();

	public FileChannel fileChannel();
	
	/**
	 * Returns a string uniquely identifying the file represented by this instance.
	 * 
	 * @return this file's unique identifier.
	 * @see #name()
	 */
	public default String identifier()
	{
		return this.file().getPath();
	}
	
	/**
	 * Return a compact string containing a specific, but not necessarily unique
	 * name of the file represented by this instance. Might be the same string
	 * returned by {@link #identifier()}.
	 * 
	 * @return this file's name.
	 * @see #identifier()
	 */
	public default String name()
	{
		return this.file().getName();
	}
	
	public default long length()
	{
		try
		{
			return this.fileChannel().size();
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (08.12.2014)EXCP: proper exception
		}
	}
	
	public default boolean isEmpty()
	{
		return this.length() == 0;
	}
	
	public default boolean delete()
	{
		this.close();
		return this.file().delete();
	}
	
	public default boolean isOpen()
	{
		return this.fileChannel().isOpen();
	}
	
	public default StorageFile flush()
	{
		try
		{
			this.fileChannel().force(false);
			return this;
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // damn checked exception
		}
	}
	
	public default void close()
	{
		try
		{
			this.fileChannel().close();
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // damn checked exception
		}
	}
	
	
	public static void closeSilent(final StorageFile file)
	{
		if(file == null)
		{
			return;
		}
		try
		{
			file.close();
		}
		catch(final Exception t)
		{
			// sshhh, silence!
		}
	}

}
