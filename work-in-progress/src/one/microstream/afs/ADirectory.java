package one.microstream.afs;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingTable;

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
	
	public XGettingTable<String, ? extends ADirectory> directories();
	
	public XGettingTable<String, ? extends AFile> files();
	
	public default <C extends Consumer<? super AItem>> C iterateItems(final C iterator)
	{
		synchronized(this)
		{
			this.directories().values().iterate(iterator);
			this.files().values().iterate(iterator);
		}
				
		return iterator;
	}
	
	public ADirectory createDirectory(String name);
	
	public AFile createFile(String name);
	
	public default boolean contains(final AItem item)
	{
		// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
		synchronized(this)
		{
			return item.parent() == this;
		}
	}
	
	public default boolean contains(final ADirectory directory)
	{
		return this.contains((AItem)directory);
	}
	
	public default boolean contains(final AFile file)
	{
		return this.contains((AItem)file);
	}
	
	public default boolean containsItem(final String itemName)
	{
		synchronized(this)
		{
			return this.containsFile(itemName)
				|| this.containsDirectory(itemName)
			;
		}
	}
	
	public default boolean containsDirectory(final String directoryName)
	{
		synchronized(this)
		{
			return this.directories().get(directoryName) != null;
		}
	}
	
	public default boolean containsFile(final String fileName)
	{
		synchronized(this)
		{
			return this.files().get(fileName) != null;
		}
	}
	
}
