package one.microstream.afs.temp;

import static one.microstream.X.notNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingTable;

public interface ADirectory extends AItem, AResolving
{
	// (20.05.2020 TM)TODO: priv#49: remove if really not needed
//	public XGettingTable<String, ? extends ADirectory> directories();
//
//	public XGettingTable<String, ? extends AFile> files();

	public <R> R accessDirectories(Function<? super XGettingTable<String, ? extends ADirectory>, R> logic);
	
	public <R> R accessFiles(Function<? super XGettingTable<String, ? extends AFile>, R> logic);
	
	public <S, R> R accessDirectories(S subject, BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic);
	
	public <S, R> R accessFiles(S subject, BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic);
	
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
	
	public AItem getItem(String identifier);
		
	public ADirectory getDirectory(String identifier);
	
	public AFile getFile(String identifier);
	
	public <C extends Consumer<? super AItem>> C iterateItems(C iterator);
	
	public boolean contains(AItem item);
	
	public default boolean contains(final ADirectory directory)
	{
		return this.contains((AItem)directory);
	}
	
	public default boolean contains(final AFile file)
	{
		return this.contains((AItem)file);
	}
	
	public boolean containsItem(String itemName);
	
	public boolean containsDirectory(String directoryName);
	
	public boolean containsFile(String fileName);
	
	// (20.04.2020 TM)TODO: #containsDeeps
	
	@Override
	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
	
	
	
	public abstract class Abstract<D extends ADirectory, F extends AFile>
	extends AItem.Abstract<D>
	implements ADirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// static fields //
		//////////////////
		
		private static final ADirectory.Observer[] NO_OBSERVERS = new ADirectory.Observer[0];
		private static final EqHashTable<String, Object> EMPTY = EqHashTable.NewCustom(0);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		@SuppressWarnings("unchecked") // safe due to not containing any elements
		static <T> EqHashTable<String, T> emptyTable()
		{
			return (EqHashTable<String, T>)EMPTY;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// (20.05.2020 TM)FIXME: priv#49: how are those populated?
		private final EqHashTable<String, D> directories = emptyTable();
		private final EqHashTable<String, F> files       = emptyTable();

		// memory-optimized array because there should usually be no or very few observers (<= 10).
		private ADirectory.Observer[] observers = NO_OBSERVERS;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final AFileSystem fileSystem,
			final D           parent
		)
		{
			super(fileSystem, parent);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final Object mutex()
		{
			return this.observers;
		}
		
		@Override
		public final AItem getItem(final String identifier)
		{
			synchronized(this.mutex())
			{
				final AFile file = this.getFile(identifier);
				if(file != null)
				{
					return file;
				}
				
				return this.getDirectory(identifier);
			}
		}
		
		private ADirectory internalGetDirectory(final String identifier)
		{
			return this.directories.get(identifier);
		}
		
		@Override
		public final ADirectory getDirectory(final String identifier)
		{
			synchronized(this.mutex())
			{
				return this.internalGetDirectory(identifier);
			}
		}
		
		@Override
		public final AFile getFile(final String identifier)
		{
			synchronized(this.mutex())
			{
				return this.files.get(identifier);
			}
		}
		
		@Override
		public final <C extends Consumer<? super AItem>> C iterateItems(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.directories.values().iterate(iterator);
				this.files.values().iterate(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public final boolean contains(final AItem item)
		{
			// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
			synchronized(this.mutex())
			{
				return item.parent() == this;
			}
		}
		
		@Override
		public final boolean containsItem(final String itemName)
		{
			synchronized(this.mutex())
			{
				return this.containsFile(itemName)
					|| this.containsDirectory(itemName)
				;
			}
		}
		
		@Override
		public final boolean containsDirectory(final String directoryName)
		{
			synchronized(this.mutex())
			{
				return this.directories.get(directoryName) != null;
			}
		}
		
		@Override
		public final boolean containsFile(final String fileName)
		{
			synchronized(this.mutex())
			{
				return this.files.get(fileName) != null;
			}
		}
		
		@Override
		public final ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// (19.05.2020 TM)TODO: priv#49: 1 or 0? bounds? must test and comment.
			if(length == 1)
			{
				if(pathElements[offset].equals(this.identifier()))
				{
					return this;
				}
				
				// (19.05.2020 TM)EXCP: proper exception
				throw new RuntimeException(
					"Inconsistent path: identifier of this (\"" + this.identifier() +
					"\") does not match the specified identifier \"" + pathElements[offset] + "\"."
				);
				
			}
			
			synchronized(this.fileSystem())
			{
				ADirectory directory = null;
				for(int o = offset, l = length; l > 0; o++, l--)
				{
					// lock not required since all mutating operations require a central lock on filesystem.
					directory = this.internalGetDirectory(pathElements[o]);
					if(directory == null)
					{
						// (14.05.2020 TM)EXCP: proper exception
						throw new RuntimeException(
							VarString.New()
							.add("Unresolvable path element \"")
							.add(pathElements[offset])
							.add("\" in path \"")
							.addAll(pathElements, VarString::commaSpace)
							.deleteLast(2)
							.add('"', '.')
							.toString()
						);
					}
				}
				
				return directory;
			}
		}
		
		@Override
		public final <R> R accessDirectories(
			final Function<? super XGettingTable<String, ? extends ADirectory>, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.directories);
			}
		}
		
		@Override
		public final <R> R accessFiles(
			final Function<? super XGettingTable<String, ? extends AFile>, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.files);
			}
		}

		@Override
		public final<S, R> R accessDirectories(
			final S                                                                     subject,
			final BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.directories, subject);
			}
		}
		
		@Override
		public final<S, R> R accessFiles(
			final S                                                                subject,
			final BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.files, subject);
			}
		}
		
		@Override
		public final boolean registerObserver(final ADirectory.Observer observer)
		{
			synchronized(this.mutex())
			{
				// best performance and common case for first observer
				if(this.observers == NO_OBSERVERS)
				{
					this.observers = X.Array(observer);
					return true;
				}

				// general case: if not yet contained, add.
				if(!XArrays.contains(this.observers, observer))
				{
					this.observers = XArrays.add(this.observers, observer);
					return true;
				}

				// already contained
				return false;
			}
		}
		
		@Override
		public final boolean removeObserver(final ADirectory.Observer observer)
		{
			synchronized(this.mutex())
			{
				// best performance and special (also weirdly common) case for last/sole observer.
				if(this.observers.length == 1 && this.observers[0] == observer)
				{
					this.observers = NO_OBSERVERS;
					return true;
				}

				// cannot be contained in empty array. Should happen a lot, worth checking.
				if(this.observers == NO_OBSERVERS)
				{
					return false;
				}

				// general case: remove if contained.
				final int index = XArrays.indexOf(observer, this.observers);
				if(index >= 0)
				{
					XArrays.remove(this.observers, index);
					return true;
				}
				
				// not contained.
				return false;
			}
		}
		
		@Override
		public final <C extends Consumer<? super Observer>> C iterateObservers(final C logic)
		{
			synchronized(this.mutex())
			{
				return XArrays.iterate(this.observers, logic);
			}
		}
		
	}
		
	// (19.05.2020 TM)TODO: priv#49: call Observer methods
	public interface Observer
	{
		public void onBeforeCreateFile(String identifier, String name, String type);
		
		public void onAfterCreateFile(AFile createdFile, long creationTime);
		
		
		public void onBeforeMoveFile(AFile fileToMove, ADirectory targetDirectory);
		
		public void onAfterMoveFile(AFile movedFile, ADirectory sourceDirectory, long deletionTime);
		
		
		public void onBeforeDeleteFile(AFile fileToDelete);
		
		public void onAfterDeleteFile(AFile deletedFile, long deletionTime);
		


		public void onBeforeCreateDirectory(String identifier);
		
		public void onAfterCreateDirectory(ADirectory createdDirectory, long creationTime);
		
		
		public void onBeforeMoveDirectory(ADirectory directoryToMove, ADirectory targetDirectory);
		
		public void onAfterMoveDirectory(ADirectory movedDirectory, ADirectory sourceDirectory, long deletionTime);
		
		
		public void onBeforeDeleteDirectory(ADirectory directoryToDelete);
		
		public void onAfterDeleteDirectory(ADirectory deletedDirectory, long deletionTime);
		
	}

	// (07.05.2020 TM)FIXME: priv#49: remove all the ADirectory wrapper stuff if really no longer needed.
	
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
		
		
		
		public abstract class Abstract<S> implements ADirectory.Wrapper, ADirectory
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final ADirectory actual;
			private final S          subject;
			
						
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final ADirectory actual, final S subject)
			{
				super();
				this.actual  = notNull(actual) ;
				this.subject = notNull(subject);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public S subject()
			{
				return this.subject;
			}
			
			@Override
			public ADirectory actual()
			{
				return this.actual;
			}
			
			@Override
			public AFileSystem fileSystem()
			{
				return this.actual.fileSystem();
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
