package one.microstream.afs;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingTable;

public interface ADirectory extends AItem
{
	public XGettingTable<String, ? extends ADirectory> directories();
	
	public XGettingTable<String, ? extends AFile> files();
	
	public boolean registerObserver(ADirectory.Observer observer);
	
	public boolean removeObserver(ADirectory.Observer observer);
	
	public <C extends Consumer<? super ADirectory.Observer>> C iterateObservers(C logic);
	
	// (21.04.2020 TM)FIXME: priv#49: Convenience-relaying methods?
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

	
	
	public abstract class Abstract<D extends ADirectory, F extends AFile, S>
	extends AItem.Abstract<D, S>
	implements ADirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<String, D>        directories;
		private final EqHashTable<String, F>        files      ;
		private final HashEnum<ADirectory.Observer> observers  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final D parent ,
			final S subject
		)
		{
			super(parent, subject);
			this.directories = EqHashTable.New();
			this.files       = EqHashTable.New();
			this.observers   = HashEnum.New()   ;
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
		
		@Override
		public final synchronized boolean registerObserver(final ADirectory.Observer observer)
		{
			return this.observers.add(observer);
		}
		
		@Override
		public final synchronized boolean removeObserver(final ADirectory.Observer observer)
		{
			return this.observers.removeOne(observer);
		}
		
		@Override
		public final synchronized <C extends Consumer<? super Observer>> C iterateObservers(final C logic)
		{
			return this.observers.iterate(logic);
		}
		
	}
			
	public interface Observer
	{
		public void onBeforeCreateFile(String identifier, String name, String type);
		
		public void onAfterCreateFile(AFile createdFile, long creationTime);
		
		
		public void onBeforeMoveFile(AFile fileToMove, AMutableDirectory targetDirectory);
		
		public void onAfterMoveFile(AFile movedFile, AMutableDirectory sourceDirectory, long deletionTime);
		
		
		public void onBeforeDeleteFile(AFile fileToDelete);
		
		public void onAfterDeleteFile(AFile deletedFile, long deletionTime);
		


		public void onBeforeCreateDirectory(String identifier);
		
		public void onAfterCreateDirectory(ADirectory createdDirectory, long creationTime);
		
		
		public void onBeforeMoveDirectory(ADirectory directoryToMove, AMutableDirectory targetDirectory);
		
		public void onAfterMoveDirectory(ADirectory movedDirectory, AMutableDirectory sourceDirectory, long deletionTime);
		
		
		public void onBeforeDeleteDirectory(ADirectory directoryToDelete);
		
		public void onAfterDeleteDirectory(ADirectory deletedDirectory, long deletionTime);
		
	}

	public static ADirectory actual(final ADirectory directory)
	{
		return directory instanceof ADirectory.Wrapper
			? ((ADirectory.Wrapper)directory).actual()
			: directory
		;
	}
	
	public interface Wrapper extends AItem.Wrapper
	{
		@Override
		public ADirectory actual();
		
		
		
		public abstract class Abstract implements ADirectory.Wrapper, ADirectory
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final ADirectory actual;
			
						
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final ADirectory actual)
			{
				super();
				this.actual = notNull(actual);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public Object subject()
			{
				return this.actual.subject();
			}
			
			@Override
			public ADirectory actual()
			{
				return this.actual;
			}

			@Override
			public XGettingTable<String, ? extends ADirectory> directories()
			{
				return this.actual.directories();
			}

			@Override
			public XGettingTable<String, ? extends AFile> files()
			{
				return this.actual.files();
			}

			@Override
			public boolean registerObserver(final Observer observer)
			{
				return this.actual.registerObserver(observer);
			}

			@Override
			public boolean removeObserver(final Observer observer)
			{
				return this.actual.removeObserver(observer);
			}
			
			@Override
			public <C extends Consumer<? super Observer>> C iterateObservers(final C logic)
			{
				return this.actual.iterateObservers(logic);
			}

			@Override
			public ADirectory parent()
			{
				return this.actual.parent();
			}

			@Override
			public String path()
			{
				return this.actual.path();
			}

			@Override
			public String identifier()
			{
				return this.actual.identifier();
			}

			@Override
			public boolean exists()
			{
				return this.actual.exists();
			}
			
		}
		
	}
	
}
