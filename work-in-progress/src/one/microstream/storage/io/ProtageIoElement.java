package one.microstream.storage.io;

/**
 * An element of the IO-level that can be uniquely identified.<br>
 * Examples:<br>
 * - A file system directory.<br>
 * - A file system file.<br>
 * - An URI to a server-managed ("cloud") BLOB.<br>
 * 
 * @author TM
 */
public interface ProtageIoElement
{
	/**
	 * The primary name of the directory, if applicable.
	 * 
	 * @see #qualifier()
	 * @see #identifier()
	 */
	public String name();
	
	/**
	 * The qualifier that, in combination with {@link #name()}, uniquely identifies the directory, if applicable.
	 * 
	 * @see #name()
	 * @see #identifier()
	 */
	public String qualifier();
	
	/**
	 * The identifier that uniquely identifies the directory. If applicable, a combination of {@link #qualifier()}
	 * and {@link #name()}.
	 * 
	 * 
	 * @see #qualifier()
	 * @see #name()
	 */
	public String identifier();
}
