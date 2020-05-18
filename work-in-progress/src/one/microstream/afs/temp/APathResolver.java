package one.microstream.afs.temp;

public interface APathResolver<D, F>
{
	public String[] resolveDirectoryToPath(D directory);
	
	public String[] resolveFileToPath(F file);
	
}
