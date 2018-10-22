package net.jadoth.storage.io;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface ProtageWritableFile extends ProtageReadableFile
{
	@Override
	public ProtageWritableDirectory directory();
	
	public long write(Iterable<? extends ByteBuffer> sources);
	
	/**
	 * Attempts to delete the file.
	 * If any amount of {@link ProtageFileChannel} instances are still actively accessing the file,
	 * it is neither deleted nor marked for deletion.
	 * 
	 * @return the amount of still actively accessing {@link ProtageFileChannel} instances.
	 */
	public int tryDelete();
	
	/**
	 * {@link #tryDelete} with mark for deletion if not possible.
	 * @return
	 */
	public int delete();
	
	
	/**
	 * Delete now without delay, closing all resources, or throw exception if not possible.
	 * @throws RuntimeException
	 */
	public void forceDelete() throws RuntimeException; // (15.10.2018 TM)EXCP: proper exception


	public <C extends Consumer<? super ProtageWritableFile>> C waitOnDelete(C callback);
	
	public boolean isMarkedForDeletion();
	
	public boolean isDeleted();
	
	
	public abstract class Implementation<D extends ProtageWritableDirectory> implements ProtageWritableFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final D      directory;
		private final String name     ;
		
		private boolean pendingDelete;
		private boolean isDeleted    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(final D directory, final String name)
		{
			super();
			this.directory = notNull(directory);
			this.name      = notNull(name);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final D directory()
		{
			return this.directory;
		}

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public synchronized final boolean isMarkedForDeletion()
		{
			return this.pendingDelete;
		}
		
		@Override
		public synchronized final boolean isDeleted()
		{
			return this.isDeleted;
		}
				
	}
	
}
