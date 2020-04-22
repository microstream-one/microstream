package one.microstream.afs;

public interface AFileSystem<D, F>
{
	public ADirectory resolveDirectory(D directory);
	
	public AFile resolveFile(F file);
	
}
