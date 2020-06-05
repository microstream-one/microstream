package one.microstream.afs;

public interface AResolver<D, F>
{
	public AFileSystem fileSystem();
	
	public String[] resolveDirectoryToPath(D directory);
	
	public String[] resolveFileToPath(F file);
	
	public D resolve(ADirectory directory);
	
	public F resolve(AFile file);

	public default ADirectory resolveDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);
		
		return this.fileSystem().resolveDirectoryPath(path);
	}

	public default AFile resolveFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);
		
		return this.fileSystem().resolveFilePath(path);
	}
	
	// (13.05.2020 TM)TODO: priv#49: does ensure~ really belong here?

	public default ADirectory ensureDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);
		
		return this.fileSystem().ensureDirectoryPath(path);
	}

	public default AFile ensureFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);
		
		return this.fileSystem().ensureFilePath(path);
	}
		
}
