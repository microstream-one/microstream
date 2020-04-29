package one.microstream.afs;

public interface APathResolver<D, F>
{
	public String[] resolveDirectoryToPath(D directory);
	
	public String[] resolveFileToPath(F file);
	
}
