package one.microstream.afs.types;

public interface ACreator extends ARoot.Creator
{
	public default ADirectory createDirectory(final ADirectory parent, final String identifier)
	{
		return ADirectory.New(parent, identifier);
	}
	
	public default AFile createFile(final ADirectory parent, final String identifier)
	{
		return AFile.New(parent, identifier);
	}
	
	public default AFile createFile(
		final ADirectory parent    ,
		final String     identifier,
		final String     name      ,
		final String     type
	)
	{
		return this.createFile(parent, identifier);
	}
	
	
	@FunctionalInterface
	public interface Creator
	{
		// yes, yes, Creator$Creator. Funny.
		public ACreator createCreator(AFileSystem parent);
	}
	
}
