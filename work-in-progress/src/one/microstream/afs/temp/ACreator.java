package one.microstream.afs.temp;

public interface ACreator extends ARoot.Creator
{
	@Override
	public ARoot createRootDirectory(AFileSystem fileSystem, String identifier);
	
	public ADirectory createDirectory(ADirectory parent, String identifier);
	
	public AFile createFile(ADirectory parent, String identifier);
	
	public AFile createFile(ADirectory parent, String name, String type);
	
	public AFile createFile(ADirectory parent, String identifier, String name, String type);
}
