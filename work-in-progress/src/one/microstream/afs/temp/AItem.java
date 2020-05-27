package one.microstream.afs.temp;

import static one.microstream.X.notNull;

public interface AItem
{
	public AFileSystem fileSystem();
	
	/**
	 * The directory (identifying container) in which this item is located and in which
	 * no other item can have the same {@link #identifier()} as this item.
	 * 
	 * @see #identifier()
	 * @see #toPathString()
	 * 
	 * @return the item's parent directory.
	 */
	public ADirectory parent();
	
	/**
	 * The value that uniquely identifies the item globally in the whole file system.
	 * <p>
	 * Note that this value is usually a combination of the identifiers of {@link #parent()} directories
	 * and the local {@link #identifier()}, but such a relation is not mandatory.
	 * 
	 * @see #parent()
	 * @see #identifier()
	 * 
	 * @return the item's globally unique identifier.
	 */
	public String toPathString();
	
	public String[] toPath();

	/**
	 * The value that uniquely identifies the item locally in its {@link #parent()} directory.
	 * <p>
	 * Note that this value might be a combination of {@link #name()} and {@link #type()},
	 * but such a relation is not mandatory.
	 * 
	 * @see #parent()
	 * @see #toPathString()
	 * @see #name()
	 * @see #type()
	 * 
	 * @return the item's locally unique identifier.
	 */
	public String identifier();
	
	/**
	 * Queries whether the item represented by this instance actually physically exists on the underlying storage layer.
	 * 
	 * @return whether the item exists.
	 */
	public boolean exists();
		
	

	
	public static String[] buildItemPath(final AItem item)
	{
		// doing the quick loop twice is enormously faster than populating a dynamically growing collection.
		int depth = 0;
		for(AItem i = item; i != null; i = i.parent())
		{
			depth++;
		}
		
		final String[] path = new String[depth];
		for(AItem i = item; i != null; i = i.parent())
		{
			path[--depth] = item.identifier();
		}
		
		return path;
	}
	
		
	public abstract class Abstract implements AItem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String identifier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final String identifier)
		{
			super();
			this.identifier = notNull(identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
	}
	
		
	
	public static AItem actual(final AItem item)
	{
		return item instanceof AItem.Wrapper
			? ((AItem.Wrapper)item).actual()
			: item
		;
	}
	
	public interface Wrapper extends AItem
	{
		public AItem actual();
						
		public Object user();
	}
		
}
