package one.microstream.afs;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.collections.HashEnum;

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
	
	public boolean registerObserver(AFile.Observer observer);
	
	public boolean removeObserver(AFile.Observer observer);
		
	
	
	public abstract class Abstract<D extends ADirectory>
	extends AItem.Abstract<D>
	implements AFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String                   name     ;
		private final String                   type     ;
		private final HashEnum<AFile.Observer> observers;
		
		
		
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
			this.name      = coalesce(name, identifier);
			this.type      =  mayNull(type);
			this.observers = HashEnum.New();
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
		
		@Override
		public final synchronized boolean registerObserver(final AFile.Observer observer)
		{
			return this.observers.add(observer);
		}
		
		@Override
		public final synchronized boolean removeObserver(final AFile.Observer observer)
		{
			return this.observers.removeOne(observer);
		}
		
	}
	
	public abstract class AbstractSubjectWrapping<S, D extends ADirectory>
	extends AFile.Abstract<D>
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
			final String identifier,
			final String name      ,
			final String type
		)
		{
			super(parent, identifier, name, type);
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
		
	public interface Observer
	{
		public void onBeforeFileWrite(AWritableFile targetFile, Iterable<? extends ByteBuffer> sources);

		public void onAfterFileWrite(AWritableFile targetFile, Iterable<? extends ByteBuffer> sources, long writeTime);
		
		
		public void onBeforeFileMove(AFile fileToMove, AMutableDirectory targetDirectory);
		
		public void onAfterFileMove(AFile movedFile, AMutableDirectory sourceDirectory, long deletionTime);
		
		
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
	
	public interface Wrapper extends AItem.Wrapper, AFile
	{
		@Override
		public AFile actual();
		
		
		
		public abstract class Abstract implements AFile.Wrapper
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFile actual;
			
						
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final AFile actual)
			{
				super();
				this.actual = notNull(actual);
			}
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public AFile actual()
			{
				return this.actual;
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