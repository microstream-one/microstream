package one.microstream.afs.temp;

public interface AResolving
{
	public default ADirectory resolveDirectoryPath(final String... pathElements)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
		
	public default AFile resolveFilePath(final String... pathElements)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile resolveFilePath(final String[] directoryPathElements, final String fileIdentifier)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public default AFile resolveFilePath(final String[] directoryPathElements, final int offset, final int length, final String fileIdentifier)
	{
		final ADirectory directory = this.resolveDirectoryPath(directoryPathElements, offset, length);
		
		return directory.getFile(fileIdentifier);
	}
}
