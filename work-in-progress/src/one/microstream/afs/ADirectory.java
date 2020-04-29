package one.microstream.afs;

import static one.microstream.X.mayNull;

import java.util.function.Consumer;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XTable;

public interface ADirectory extends AItem
{
	public XGettingTable<String, ? extends ADirectory> directories();
	
	public XGettingTable<String, ? extends AFile> files();
	
	public boolean registerObserver(ADirectory.Observer observer);
	
	public boolean removeObserver(ADirectory.Observer observer);
	
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

	
	
	public abstract class Abstract<D extends ADirectory, F extends AFile>
	extends AItem.Abstract<D>
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
			final D      parent    ,
			final String identifier
		)
		{
			super(mayNull(parent), identifier);
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
		public synchronized final boolean registerObserver(final ADirectory.Observer observer)
		{
			return this.observers.add(observer);
		}
		
		@Override
		public synchronized final boolean removeObserver(final ADirectory.Observer observer)
		{
			return this.observers.removeOne(observer);
		}
		
	}
	
	public abstract class AbstractSubjectWrapping<S, D extends ADirectory, F extends AFile>
	extends ADirectory.Abstract<D, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final S subject;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractSubjectWrapping(
			final S      subject   ,
			final D      parent    ,
			final String identifier
		)
		{
			super(parent, identifier);
			this.subject = subject;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final S wrapped()
		{
			return this.subject;
		}
		
	}
	
	public abstract class AbstractRegistering<
		S,
		D extends ADirectory,
		F extends AFile,
		U extends AUsedDirectory,
		M extends AMutableDirectory
	>
		extends ADirectory.AbstractSubjectWrapping<S, D, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final HashTable<Object, U>       users   = HashTable.New()          ;
		private final AMutableDirectory.Entry<M> mutator = AMutableDirectory.Entry();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
	
		protected AbstractRegistering(
			final S      subject   ,
			final D      parent    ,
			final String identifier
		)
		{
			super(subject, parent, identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public synchronized <T> T accessUsers(final Function<? super XTable<Object, U>, T> accessor)
		{
			// freely access readers table, but protected under the lock for this instance
			return accessor.apply(this.users);
		}
		
		public synchronized <T> T accessMutator(final Function<? super AMutableDirectory.Entry<M>, T> accessor)
		{
			// freely access writer entry, but protected under the lock for this instance
			return accessor.apply(this.mutator);
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
	
	public interface Wrapper extends AItem.Wrapper
	{
		@Override
		public ADirectory actual();
		
	}
	
}
