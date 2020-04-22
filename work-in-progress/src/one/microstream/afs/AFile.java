package one.microstream.afs;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

public interface AFile extends AItem
{
	/**
	 * A simple String representing the "name" of the file. While no two files can have the same {@link #identifier()}
	 * inside a given directory, any number of files can have the same name.<p>
	 * Depending on the file system implementation, {@link #name()} might be the same value as {@link #identifier()},
	 * but while {@link #identifier()} is guaranteed to be a unique local identifier for any file system,
	 * {@link #name()} is not.
	 * 
	 * @see #path()
	 * @see #identifier()
	 * @see #type()
	 * 
	 * @return the file's name.
	 */
	public String name();
	
	/**
	 * An optional String defining the type of the file's content.
	 * <p>
	 * If such an information makes no sense for a certain file system, this value may be <code>null</code>.
	 * 
	 * @return the file's type.
	 */
	public String type();
	
	/**
	 * Returns the length in bytes of this file's content. Without any space required for file metadata (name etc.).
	 * 
	 * @return the length in bytes of this file's content.
	 */
	public long length();
		
	
	
	public abstract class Abstract<D extends ADirectory>
	extends AItem.Abstract<D>
	implements AFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String name;
		private final String type;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final D      parent,
			final String identifier,
			final String name,
			final String type
		)
		{
			super(notNull(parent), identifier);
			this.name = coalesce(name, identifier);
			this.type =  mayNull(type)            ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String name()
		{
			return this.name;
		}
		
		@Override
		public final String type()
		{
			return this.type;
		}
		
		
	}
	
	public abstract class AbstractWrapper<W, D extends ADirectory>
	extends AFile.Abstract<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final W wrapped;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractWrapper(
			final W      wrapped   ,
			final D      parent    ,
			final String identifier,
			final String name      ,
			final String type
		)
		{
			super(parent, identifier, name, type);
			this.wrapped = wrapped;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final W wrapped()
		{
			return this.wrapped;
		}
		
	}
	
}
