package one.microstream.afs;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

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
	
	
	/**
	 * Note: {@code identifier} can be {@literal ""} since local file paths might start with a "/".
	 * @param fileSystem
	 * @param protocol
	 * @param identifier
	 * 
	 * @return
	 */
	public static ARoot New(
		final AFileSystem fileSystem,
		final String      protocol  ,
		final String      identifier
	)
	{
		return new ARoot.Default(
			notNull(fileSystem),
			notNull(protocol)  ,
			notNull(identifier) // may be ""
		);
	}
	
	public final class Default extends ADirectory.Abstract implements ARoot
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem fileSystem;
		private final String      protocol  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final AFileSystem fileSystem,
			final String      protocol  ,
			final String      identifier
		)
		{
			super(identifier);
			this.protocol   = protocol  ;
			this.fileSystem = fileSystem;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final AFileSystem fileSystem()
		{
			return this.fileSystem;
		}
		
		@Override
		public final String protocol()
		{
			return this.protocol;
		}
		
		@Override
		public final ADirectory parent()
		{
			return null;
		}
		
	}
	
}
