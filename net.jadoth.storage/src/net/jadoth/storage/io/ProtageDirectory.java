package net.jadoth.storage.io;

import java.util.function.Supplier;

import net.jadoth.collections.types.XGettingTable;

public interface ProtageDirectory
{
	/**
	 * The primary name of the directory, if applicable.
	 * @return
	 * 
	 * @see #qualifier()
	 * @see #identifier()
	 */
	public String name();
	
	/**
	 * The qualifier that, in combination with {@link #name()}, uniquely identifies the directory, if applicable.
	 * @return
	 * 
	 * @see #name()
	 * @see #identifier()
	 */
	public String qualifier();
	
	/**
	 * The identifier that uniquely identifies the directory. If applicable, a combination of {@link #qualifier()}
	 * and {@link #name()}.
	 * 
	 * @return
	 * 
	 * @see #qualifier()
	 * @see #name()
	 */
	public String identifier();
	
	public XGettingTable<String, ? extends ProtageFile> files();
	
	public ProtageFile createFile(String fileName);
	
	public default boolean contains(final ProtageFile file)
	{
		return this.contains(file.name());
	}
	
	public boolean contains(String fileName);
	
	
	/**
	 * A simple deadlock prevention strategy:<br>
	 * - directories are locked before files
	 * - directories with higher identifiers are locked before those with lower identifier
	 * - files with higher identifiers are locked before those with lower identifier
	 * 
	 * @param directory1
	 * @param directory2
	 * @param logic
	 */
	public static ProtageWritableFile executeLocked(
		final ProtageWritableDirectory      directory1,
		final ProtageWritableDirectory      directory2,
		final Supplier<ProtageWritableFile> logic
	)
	{
		synchronized(directory1)
		{
			// deadlock-prevention strategy
			if(directory1.identifier().compareTo(directory2.identifier()) >= 0)
			{
				synchronized(directory2)
				{
					return logic.get();
				}
			}
		}
		
		synchronized(directory2)
		{
			synchronized(directory1)
			{
				return logic.get();
			}
		}
	}
	
}
