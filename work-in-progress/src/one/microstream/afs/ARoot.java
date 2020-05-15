package one.microstream.afs;

public interface ARoot extends ADirectory
{
	/* (13.05.2020 TM)FIXME: priv#49: ARoot containing the protocol
	 * E.g.
	 * https://
	 * file://
	 */
	
	public String protocol();
	
	
	
	@FunctionalInterface
	public interface Creator
	{
		public ARoot createRootDirectory(AFileSystem fileSystem, String identifier);
	}
	
}
