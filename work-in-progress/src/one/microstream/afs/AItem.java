package one.microstream.afs;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

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
	
	/**
	 * Returns the low-level file representation instance, whatever that might be for a particular specific file system.
	 * <br>
	 * Examples:
	 * <ul>
	 * <li>{@link Path}</li>
	 * <li>{@link File}</li>
	 * <li>{@link URL}</li>
	 * <li>{@link URI}</li>
	 * </ul>
	 * @return
	 */
	public Object subject();
	
	
	
	public abstract class Abstract<D extends ADirectory, S> implements AItem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final D parent ;
		private final S subject;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final D parent, final S subject)
		{
			super();
			this.parent  =  mayNull(parent) ;
			this.subject =  notNull(subject);
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
		public final S subject()
		{
			return this.subject;
		}
		
	}
	
	public interface Wrapper
	{
		public AItem actual();
	}
		
}
