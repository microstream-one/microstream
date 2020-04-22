package one.microstream.afs;

import static one.microstream.X.mayNull;

import java.util.function.Consumer;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;

public interface ADirectory extends AItem
{
	public XGettingTable<String, ? extends ADirectory> directories();
	
	public XGettingTable<String, ? extends AFile> files();
	
	// (21.04.2020 TM)FIXME: priv#49: Move to an "ACreator" or such
//	public ADirectory createDirectory(String name);
//	public AFile createFile(String name);
	
	/**
	 * The identifier String that can be used as a qualifier for a file contained in this directory.<p>
	 * Depending on the underlying binary storage's adressing concept, this might be equal to {@link #path()}
	 * or it might add a kind of separator. For example for a local file system, the qualifying identifier
	 * of a directory is the directory path plus a joining slash ('/').
	 */
	public default String qualifier()
	{
		return this.path();
	}
	
	public default AItem getItem(final String identifier)
	{
		synchronized(this)
		{
			final AFile file = this.getFile(identifier);
			if(file != null)
			{
				return file;
			}
			
			return this.getDirectory(identifier);
		}
	}
	
	public default ADirectory getDirectory(final String identifier)
	{
		synchronized(this)
		{
			return this.directories().get(identifier);
		}
	}
	
	public default AFile getFile(final String identifier)
	{
		synchronized(this)
		{
			return this.files().get(identifier);
		}
	}
	
	public default <C extends Consumer<? super AItem>> C iterateItems(final C iterator)
	{
		synchronized(this)
		{
			this.directories().values().iterate(iterator);
			this.files().values().iterate(iterator);
		}
				
		return iterator;
	}
	
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
	
	// (20.04.2020 TM)TODO: #containsDeeps

	
	
	public abstract class Abstract<D extends ADirectory, F extends AFile>
	extends AItem.Abstract<D>
	implements ADirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<String, D> directories = EqHashTable.New();
		
		private final EqHashTable<String, F> files = EqHashTable.New();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final D      parent    ,
			final String identifier
		)
		{
			super(mayNull(parent), identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final XGettingTable<String, ? extends D> directories()
		{
			return this.directories;
		}
		
		@Override
		public final XGettingTable<String, ? extends F> files()
		{
			return this.files;
		}
		
	}
	
	public abstract class AbstractWrapper<W, D extends ADirectory, F extends AFile>
	extends ADirectory.Abstract<D, F>
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
			final String identifier
		)
		{
			super(parent, identifier);
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
