package one.microstream.afs.temp;

public interface AResolver<D, F>
{
	public ADirectory resolveDirectory(D directory);
	
	public AFile resolveFile(F file);
	
	public D resolve(ADirectory directory);
	
	public F resolve(AFile file);
	
	// (13.05.2020 TM)TODO: priv#49: does ensure~ really belong here?
	
	public ADirectory ensureDirectory(D directory);
	
	public AFile ensureFile(F file);
	
	
	
	public class Default<D, F> implements AResolver<D, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem         fileSystem  ;
		private final APathResolver<D, F> pathResolver;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final AFileSystem fileSystem, final APathResolver<D, F> pathResolver)
		{
			super();
			this.fileSystem   = fileSystem  ;
			this.pathResolver = pathResolver;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ADirectory resolveDirectory(final D directory)
		{
			final String[] path = this.pathResolver.resolveDirectoryToPath(directory);
			
			return this.fileSystem.resolveDirectoryPath(path);
		}

		@Override
		public AFile resolveFile(final F file)
		{
			final String[] path = this.pathResolver.resolveFileToPath(file);
			
			return this.fileSystem.resolveFilePath(path);
		}
		
		@Override
		public D resolve(final ADirectory directory)
		{
			// FIXME AResolver.Default#resolve()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public F resolve(final AFile file)
		{
			// FIXME AResolver.Default#resolve()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public ADirectory ensureDirectory(final D directory)
		{
			final String[] path = this.pathResolver.resolveDirectoryToPath(directory);
			
			return this.fileSystem.ensureDirectoryPath(path);
		}

		@Override
		public AFile ensureFile(final F file)
		{
			final String[] path = this.pathResolver.resolveFileToPath(file);
			
			return this.fileSystem.ensureFilePath(path);
		}
		
	}
}
