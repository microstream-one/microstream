package one.microstream.afs.temp;

import static one.microstream.X.coalesce;

public interface ARoot extends ADirectory
{
	/**
	 * E.g.
	 * https://
	 * file://
	 */
	public String protocol();
	
	
	
	@FunctionalInterface
	public interface Creator
	{
		public ARoot createRootDirectory(AFileSystem fileSystem, String protocol, String identifier);
		
		public default ARoot createRootDirectory(final AFileSystem fileSystem, final String identifier)
		{
			return this.createRootDirectory(
				fileSystem,
				coalesce(this.protocol(), fileSystem.defaultProtocol()),
				identifier
			);
		}
		
		public default String protocol()
		{
			return null;
		}
	}
	
}
