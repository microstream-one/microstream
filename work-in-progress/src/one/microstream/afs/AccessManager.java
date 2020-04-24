package one.microstream.afs;

public interface AccessManager<D, F>
{
	public AReadableFile createDirectory(AMutableDirectory parent, D directory);
	
	public AReadableFile createFile(AMutableDirectory parent, F file);
	
	public AReadableFile useReading(AFile file, Object reader);
	
	public AWritableFile useWriting(AFile file, Object writer);
	
	public AMutableDirectory useMutating(ADirectory directory, Object mutator);
}