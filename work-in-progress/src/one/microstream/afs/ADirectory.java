package one.microstream.afs;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ADirectory extends AItem
{
	/**
	 * The identifier String that can be used as a qualifier for a file contained in this directory.<p>
	 * Depending on the underlying binary storage's adressing concept, this might be equal to {@link #identifier()}
	 * or it might add a kind of separator. For example for a file system, the qualifying identifier of a directory
	 * is the directory path plus a trailing slash ('/').
	 */
	public default String qualifier()
	{
		return this.identifier();
	}
	
	public AItem getItem(String name);
	
	public ADirectory getDirectory(String name);
	
	public AFile getFile(String name);
	
	public <C extends Consumer<? super AItem>> C iterateItems(C iterator);
	
	public void iterateDirectories(Consumer<? super ADirectory> iterator);
	
	public <C extends Consumer<? super AFile>> C iterateFiles(C iterator);
	
	public ADirectory createDirectory(String name);
	
	public AFile createFile(String name);
	
	public default boolean contains(final AItem item)
	{
		return this.contains(item.name());
	}
	
	public default boolean contains(final ADirectory directory)
	{
		return this.contains(directory.name());
	}
	
	public default boolean contains(final AFile file)
	{
		return this.contains(file.name());
	}
	
	public default boolean contains(final String itemName)
	{
		return this.getItem(itemName) != null;
	}
	
	
	// (17.04.2020 TM)FIXME: priv#49: external static locking mechanism required? reasonable? why here?
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
		// deadlock-prevention strategy: order directories by identifier
		if(directory1.identifier().compareTo(directory2.identifier()) >= 0)
		{
			synchronized(directory1)
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
