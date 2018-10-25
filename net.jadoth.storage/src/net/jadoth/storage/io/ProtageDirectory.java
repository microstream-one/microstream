package net.jadoth.storage.io;

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
}
