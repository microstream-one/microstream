package one.microstream.afs;

public interface ACreator
{
	// (30.04.2020 TM)FIXME: priv#49: creator with subject types D/F?
	
	public ADirectory createDirectory(AMutableDirectory parent, String identifier);
	
	public AFile createFile(AMutableDirectory parent, String identifier);
	
	public AFile createFile(AMutableDirectory parent, String name, String type);
	
	public AFile createFile(AMutableDirectory parent, String identifier, String name, String type);
}
