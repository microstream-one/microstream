package one.microstream.afs;

public interface ACreator
{
	// (30.04.2020 TM)FIXME: priv#49: creator with subject types D/F?
	
	public ADirectory createDirectory(ADirectory parent, String identifier);
	
	public AFile createFile(ADirectory parent, String identifier);
	
	public AFile createFile(ADirectory parent, String name, String type);
	
	public AFile createFile(ADirectory parent, String identifier, String name, String type);
}
