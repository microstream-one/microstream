package one.microstream.afs;

public interface AItem
{
	/**
	 * The name of the item that locally uniquely identifies it in its {@link #parent()} directory.
	 * 
	 * @see #parent()
	 * @see #identifier()
	 */
	public String name();
	
	/**
	 * The directory (identifying container) in which this item is located and in which
	 * no other item can have the same {@link #name()}.
	 * 
	 * @see #name()
	 * @see #identifier()
	 */
	public ADirectory parent();
	
	/**
	 * The identifier that uniquely identifies the item in the whole file system.
	 * 
	 * @see #parent()
	 * @see #name()
	 */
	public String identifier();
}
