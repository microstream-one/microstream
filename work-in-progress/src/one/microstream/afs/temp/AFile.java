package one.microstream.afs.temp;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.XArrays;

public interface AFile extends AItem
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
	
	
	/**
	 * A simple String representing the "name" of the file. While no two files can have the same {@link #identifier()}
	 * inside a given directory, any number of files can have the same name.<p>
	 * Depending on the file system implementation, {@link #name()} might be the same value as {@link #identifier()},
	 * but while {@link #identifier()} is guaranteed to be a unique local identifier for any file system,
	 * {@link #name()} is not.
	 * 
	 * @see #toPathString()
	 * @see #identifier()
	 * @see #type()
	 * 
	 * @return the file's name.
	 */
	public default String name()
	{
		return this.fileSystem().getFileName(this);
	}
	
	/**
	 * An optional String defining the type of the file's content.
	 * <p>
	 * If such an information makes no sense for a certain file system, this value may be <code>null</code>.
	 * 
	 * @return the file's type.
	 */
	public default String type()
	{
		return this.fileSystem().getFileType(this);
	}
	
	/**
	 * Returns the size in bytes of this file's content, without any space required for file metadata (name etc.).
	 * 
	 * @return the size in bytes of this file's content.
	 */
	public default long size()
	{
		return this.fileSystem().ioHandler().size(this);
	}
	
	public boolean registerObserver(AFile.Observer observer);
	
	public boolean removeObserver(AFile.Observer observer);
	
	public <C extends Consumer<? super AFile.Observer>> C iterateObservers(C logic);
	
	public default AReadableFile useReading(final Object user)
	{
		return this.fileSystem().accessManager().useReading(this, user);
	}
	
	public default AWritableFile useWriting(final Object user)
	{
		return this.fileSystem().accessManager().useWriting(this, user);
	}
	
	public default AReadableFile useReading()
	{
		return this.fileSystem().accessManager().useReading(this);
	}
	
	public default AWritableFile useWriting()
	{
		return this.fileSystem().accessManager().useWriting(this);
	}
	
	@Override
	public default boolean exists()
	{
		return this.fileSystem().ioHandler().exists(this);
	}
		
	
	
	public static AFile New(
		final ADirectory  parent    ,
		final String      identifier
	)
	{
		return new AFile.Default(
			notNull(parent),
			notNull(identifier)
		);
	}
	
	public class Default
	extends AItem.Abstract
	implements AFile
	{
		///////////////////////////////////////////////////////////////////////////
		// static fields //
		//////////////////
		
		private static final AFile.Observer[] NO_OBSERVERS = new AFile.Observer[0];
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final ADirectory parent;
		
		// memory-optimized array because there should usually be no or very few observers (<= 10).
		private AFile.Observer[] observers = NO_OBSERVERS;
		
		
		
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
		
		protected final Object mutex()
		{
			return this.observers;
		}
		
		@Override
		public final boolean registerObserver(final AFile.Observer observer)
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
		public final boolean removeObserver(final AFile.Observer observer)
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
		public void onBeforeFileWrite(AWritableFile targetFile, Iterable<? extends ByteBuffer> sources);

		public void onAfterFileWrite(AWritableFile targetFile, Iterable<? extends ByteBuffer> sources, long writeTime);
		
		
		public void onBeforeFileMove(AFile fileToMove, ADirectory targetDirectory);
		
		public void onAfterFileMove(AFile movedFile, ADirectory sourceDirectory, long deletionTime);
		
		
		public void onBeforeFileCloseReading(AReadableFile fileToClose);
		
		public void onAfterFileCloseReading(AReadableFile closedFile);
		
		
		public void onBeforeFileCloseWriting(AWritableFile fileToClose);
		
		public void onAfterFileCloseWriting(AWritableFile closedFile);
		
		
		public void onBeforeFileDelete(AFile fileToDelete);
		
		public void onAfterFileDelete(AFile deletedFile, long deletionTime);
		
	}

	public static AFile actual(final AFile file)
	{
		return file instanceof AFile.Wrapper
			? ((AFile.Wrapper)file).actual()
			: file
		;
	}
		
	public interface Wrapper extends AFile, AItem.Wrapper
	{
		@Override
		public AFile actual();
		
		public abstract class Abstract<U> implements AFile.Wrapper
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFile actual;
			private final U     user  ;
			
						
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final AFile actual, final U user)
			{
				super();
				this.actual  = notNull(actual) ;
				this.user    = notNull(user)   ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public U user()
			{
				return this.user;
			}
			
			@Override
			public AFile actual()
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
			public String identifier()
			{
				return this.actual.identifier();
			}

			@Override
			public String name()
			{
				return this.actual.name();
			}

			@Override
			public String type()
			{
				return this.actual.type();
			}

			@Override
			public boolean exists()
			{
				return this.actual.exists();
			}
			
		}
		
	}
	
}
