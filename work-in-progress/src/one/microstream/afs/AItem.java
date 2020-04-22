package one.microstream.afs;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

public interface AItem
{
	/**
	 * The directory (identifying container) in which this item is located and in which
	 * no other item can have the same {@link #identifier()} as this item.
	 * 
	 * @see #identifier()
	 * @see #path()
	 * 
	 * @return the item's parent directory.
	 */
	public ADirectory parent();
	
	/**
	 * The value that uniquely identifies the item globally in the whole file system.
	 * <p>
	 * Note that this value might be a combination of the identifiers of {@link #parent()} directories
	 * and the local {@link #identifier()}, but such a relation is not mandatory.
	 * 
	 * @see #parent()
	 * @see #identifier()
	 * 
	 * @return the item's globally unique identifier.
	 */
	public String path();

	/**
	 * The value that uniquely identifies the item locally in its {@link #parent()} directory.
	 * <p>
	 * Note that this value might be a combination of {@link #name()} and {@link #type()},
	 * but such a relation is not mandatory.
	 * 
	 * @see #parent()
	 * @see #path()
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
	
	
	
	public abstract class Abstract<D extends ADirectory> implements AItem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final D parent;
		
		private final String identifier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final D parent, final String identifier)
		{
			super();
			this.parent     =  mayNull(parent)    ;
			this.identifier =  notNull(identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final D parent()
		{
			return this.parent;
		}
		
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
	}
		
}
