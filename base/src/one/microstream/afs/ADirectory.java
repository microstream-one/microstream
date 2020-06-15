package one.microstream.afs;

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
	@Override
	public default String toPathString()
	{
		return this.fileSystem().assemblePath(this);
	}

	@Override
	public default String[] toPath()
	{
		return this.fileSystem().buildPath(this);
	}
	
	public <R> R accessDirectories(Function<? super XGettingTable<String, ? extends ADirectory>, R> logic);
	
	public <R> R accessFiles(Function<? super XGettingTable<String, ? extends AFile>, R> logic);
	
	public <S, R> R accessDirectories(S subject, BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic);
	
	public <S, R> R accessFiles(S subject, BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic);
	
	public boolean registerObserver(ADirectory.Observer observer);
	
	public boolean removeObserver(ADirectory.Observer observer);
	
	public <C extends Consumer<? super ADirectory.Observer>> C iterateObservers(C logic);
	
	public default boolean ensure()
	{
		return this.fileSystem().ioHandler().ensure(this);
	}
	
	public ADirectory ensureDirectory(String identifier);
	
	public default AFile ensureFile(final String identifier)
	{
		return this.ensureFile(identifier, null, null);
	}
	
	public default AFile ensureFile(final String name, final String type)
	{
		return this.ensureFile(null, name, type);
	}
	
	public AFile ensureFile(String identifier, String name, String type);
		
	public AItem getItem(String identifier);
		
	public ADirectory getDirectory(String identifier);
	
	public AFile getFile(String identifier);
	
	public <C extends Consumer<? super AItem>> C iterateItems(C iterator);
	
	public <C extends Consumer<? super ADirectory>> C iterateDirectories(C iterator);
	
	public <C extends Consumer<? super AFile>> C iterateFiles(C iterator);
	
	public boolean contains(AItem item);
	
	public default boolean contains(final ADirectory directory)
	{
		return this.contains((AItem)directory);
	}
	
	public default boolean contains(final AFile file)
	{
		return this.contains((AItem)file);
	}

	public boolean containsDeep(AItem item);
	
	public default boolean containsDeep(final ADirectory directory)
	{
		return this.containsDeep((AItem)directory);
	}
	
	public default boolean containsDeep(final AFile file)
	{
		return this.containsDeep((AItem)file);
	}
	
	
	
	public boolean containsItem(String itemName);
	
	public boolean containsDirectory(String directoryName);
	
	public boolean containsFile(String fileName);
		
	
	@Override
	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
	
	@Override
	public default boolean exists()
	{
		return this.fileSystem().ioHandler().exists(this);
	}
	
	/* (03.06.2020 TM)FIXME: priv#49: directory mutation:
	 * - move directory to directory
	 * - delete directory
	 * - rename directory
	 * 
	 * each is only allowed if there are no uses for that directory
	 */
	
	
	public abstract class Abstract
	extends AItem.Abstract
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

		private EqHashTable<String, ADirectory> directories = emptyTable();
		private EqHashTable<String, AFile>      files       = emptyTable();

		// memory-optimized array because there should usually be no or very few observers (<= 10).
		private ADirectory.Observer[] observers = NO_OBSERVERS;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final String identifier)
		{
			super(identifier);
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
		public <C extends Consumer<? super ADirectory>> C iterateDirectories(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.directories.values().iterate(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super AFile>> C iterateFiles(final C iterator)
		{
			synchronized(this.mutex())
			{
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
		public boolean containsDeep(final AItem item)
		{
			// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
			synchronized(this.mutex())
			{
				for(AItem i = item; (i = i.parent()) != null;)
				{
					if(i == this)
					{
						return true;
					}
				}
				
				return false;
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
		
		private void register(final String identifier, final ADirectory directory)
		{
			if(this.directories == ADirectory.Abstract.<ADirectory>emptyTable())
			{
				this.directories = EqHashTable.New();
			}
			this.directories.add(identifier, directory);
		}
		
		private void register(final String identifier, final AFile file)
		{
			if(this.files == ADirectory.Abstract.<AFile>emptyTable())
			{
				this.files = EqHashTable.New();
			}
			this.files.add(identifier, file);
		}
		
		@Override
		public final ADirectory ensureDirectory(final String identifier)
		{
			synchronized(this.mutex())
			{
				ADirectory directory = this.directories.get(identifier);
				if(directory == null)
				{
					directory = this.fileSystem().creator().createDirectory(this, identifier);
					this.register(identifier, directory);
				}
				
				return directory;
			}
		}
		
		@Override
		public final AFile ensureFile(final String identifier, final String name, final String type)
		{
			final String effIdentifier = identifier != null
				? identifier
				: this.fileSystem().deriveFileIdentifier(name, type)
			;
			
			synchronized(this.mutex())
			{
				AFile file = this.files.get(effIdentifier);
				if(file == null)
				{
					file = this.fileSystem().creator().createFile(this, effIdentifier, name, type);
					this.register(effIdentifier, file);
				}
				
				return file;
			}
		}
		
		@Override
		public final ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// length 0 means no path element at all, length 1 means this is the last element on the path.
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
	
	
	public static ADirectory New(final ADirectory parent, final String identifier)
	{
		return new ADirectory.Default(
			notNull(parent),
			notNull(identifier)
		);
	}
	
	public class Default extends Abstract
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ADirectory parent;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final ADirectory parent, final String identifier)
		{
			super(identifier);
			this.parent = parent;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ADirectory parent()
		{
			return this.parent;
		}
		
		@Override
		public final AFileSystem fileSystem()
		{
			return this.parent.fileSystem();
		}
		
	}
		
	// (19.05.2020 TM)TODO: priv#49: call directory Observer directory methods
	public interface Observer
	{
		public void onBeforeFileCreate(AWritableFile fileToCreate);
		
		public void onAfterFileCreate(AWritableFile createdFile);
		
		
		public void onBeforeFileMove(AWritableFile fileToMove, AWritableFile targetFile);
		
		public void onAfterFileMove(AWritableFile movedFile, AWritableFile targetFile);
		
		
		public void onBeforeFileDelete(AWritableFile fileToDelete);
		
		public void onAfterFileDelete(AWritableFile deletedFile, boolean result);
		


		public void onBeforeDirectoryCreate(ADirectory directoryToCreate);
		
		public void onAfterDirectoryCreate(ADirectory createdDirectory);
		
		
		public void onBeforeDirectoryMove(ADirectory directoryToMove, ADirectory targetDirectory);
		
		public void onAfterDirectoryMove(ADirectory movedDirectory, ADirectory sourceDirectory);
		
		
		public void onBeforeDirectoryDelete(ADirectory directoryToDelete);
		
		public void onAfterDirectoryDelete(ADirectory deletedDirectory, boolean result);
		
	}
	
	
	public static ADirectory actual(final ADirectory directory)
	{
		return directory instanceof ADirectory.Wrapper
			? ((ADirectory.Wrapper)directory).actual()
			: directory
		;
	}
	
	public interface Wrapper extends ADirectory, AItem.Wrapper
	{
		@Override
		public ADirectory actual();
		
	}
	
}
