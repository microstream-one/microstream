package one.microstream.afs;

public interface AResolver<D, F>
{
	public ADirectory resolveDirectory(D directory);
	
	public AFile resolveFile(F file);
	
	
	
	public final class Default<D, F> implements AResolver<D, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem fileSystem;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final AFileSystem fileSystem)
		{
			super();
			this.fileSystem = fileSystem;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ADirectory resolveDirectory(final D directory)
		{
			// FIXME AResolver<D,F>#resolveDirectory()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public AFile resolveFile(final F file)
		{
			// FIXME AResolver<D,F>#resolveFile()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
}
