package one.microstream.storage.io;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface ProtageWritableFile extends ProtageReadableFile
{
	@Override
	public ProtageWritableDirectory directory();
	
	// (27.10.2018 TM)TODO: what about a moveTo(File) to move and rename?
	
	public ProtageWritableFile moveTo(ProtageWritableDirectory destination);
	
	public long write(Iterable<? extends ByteBuffer> sources);
	
	public void delete();
	
	public <C extends Consumer<? super ProtageWritableFile>> C waitOnDelete(C callback);
	
	
	public boolean isDeleted();
	
	
	public abstract class Abstract<D extends ProtageWritableDirectory> implements ProtageWritableFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final D      directory ;
		private final String qualifier ;
		private final String identifier;
		private final String name      ;
		
		private boolean isDeleted;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final D directory, final String name)
		{
			super();
			this.directory  = notNull(directory);
			this.name       = notNull(name);
			this.qualifier  = directory.qualifyingIdentifier();
			this.identifier = this.qualifier + name;
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
		public final String qualifier()
		{
			return this.qualifier;
		}
		
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
		@Override
		public final synchronized boolean isDeleted()
		{
			return this.isDeleted;
		}
		
		@Override
		public void delete()
		{
			this.isDeleted = true;
		}
				
	}
	
}
