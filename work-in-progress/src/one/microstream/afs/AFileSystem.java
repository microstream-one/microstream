package one.microstream.afs;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;

public interface AFileSystem
{
	/* (30.04.2020 TM)FIXME: priv#49: "protocol" here or in AccessManager
	 * Or is "protocol" a trait of a root directory?
	 * With a FileSystem instance being able to contain roots with different protocols?
	 * Example:
	 * "file://C:/"
	 * "file://C:/"
	 * "https://some.cloudstorage.com/storage12343534/"
	 * 
	 * HMMM....
	 */
	
	// (04.05.2020 TM)TODO: priv#49: #resolve methods with root String?
	
	/* (04.05.2020 TM)TODO: priv#49: #resolve methods with single String that gets parsed?
	 * Or is that the job of an APathResolver?
	 */
	
	public default ADirectory resolveDirectoryPath(final String... pathElements)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
		
	public default AFile resolveFilePath(final String... pathElements)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile resolveFilePath(final String[] directoryPathElements, final String fileIdentifier)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public AFile resolveFilePath(String[] directoryPathElements, int offset, int length, String fileIdentifier);
	
	
	
	public default ADirectory ensureDirectoryPath(final String... pathElements)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory ensureDirectoryPath(String[] pathElements, int offset, int length);
		
	public default AFile ensureFilePath(final String... pathElements)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile ensureFilePath(final String[] directoryPathElements, final String fileIdentifier)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public AFile ensureFilePath(String[] directoryPathElements, int offset, int length, String fileIdentifier);
	
	public AccessManager accessManager();
	
	public IoHandler ioHandler();

	// implicitely #close PLUS the AFS-management-level aspect
	public default ActionReport release(final AReadableFile file)
	{
		synchronized(file)
		{
			final boolean wasClosed   = this.ioHandler().close(file);
			final boolean wasReleased = this.accessManager().unregister(file);
			
			return wasClosed
				? wasReleased
					? ActionReport.FULL_ACTION
					: null // impossible / inconsistent
				: wasReleased
					? ActionReport.PART_ACTION
					: ActionReport.NO_ACTION
			;
		}
	}
		
	
	
	public static AFileSystem New(
		final ACreator              creator             ,
		final AccessManager.Creator accessManagerCreator,
		final IoHandler             ioHandler
	)
	{
		return new AFileSystem.Default(
			EqHashTable.New()            ,
			notNull(creator)             ,
			notNull(accessManagerCreator),
			notNull(ioHandler)
		);
	}
	
	public final class Default implements AFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// (30.04.2020 TM)FIXME: priv#49: ARoot extends ADirectory?
		// (09.05.2020 TM)FIXME: priv#49: How to register roots? remove? collection logic?
		private final EqHashTable<String, ADirectory> rootDirectories;
		private final ACreator                        creator        ;
		private final AccessManager                   accessManager  ;
		private final IoHandler                       ioHandler      ;
		
		// (09.05.2020 TM)FIXME: priv#49: Lock FileSystem for creating new Items or just their parent directory?
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final EqHashTable<String, ADirectory> rootDirectories     ,
			final ACreator                        creator             ,
			final AccessManager.Creator           accessManagerCreator,
			final IoHandler                       ioHandler
		)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.creator         = creator          ;
			this.ioHandler       = ioHandler        ;
			
			// called at the very last just in case the creator needs some of the other state
			this.accessManager = accessManagerCreator.createAccessManager(this);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public AccessManager accessManager()
		{
			return this.accessManager;
		}
		
		@Override
		public IoHandler ioHandler()
		{
			return this.ioHandler;
		}
				
		@Override
		public final synchronized ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// FIXME AFileSystem.Abstract#resolveDirectoryPath()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized ADirectory ensureDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// FIXME AFileSystem.Abstract#resolveDirectoryPath()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized AFile resolveFilePath(
			final String[] directoryPathElements,
			final int      offset               ,
			final int      length               ,
			final String   fileIdentifier
		)
		{
			final ADirectory directory = this.resolveDirectoryPath(directoryPathElements, offset, length);
			
			return directory == null
				? null
				: directory.getFile(fileIdentifier)
			;
		}
		
		@Override
		public final synchronized AFile ensureFilePath(
			final String[] directoryPathElements,
			final int      offset               ,
			final int      length               ,
			final String   fileIdentifier
		)
		{
			final ADirectory directory = this.ensureDirectoryPath(directoryPathElements, offset, length);
			
			synchronized(directory)
			{
				AFile file = directory.getFile(fileIdentifier);
				if(file == null)
				{
					file = this.accessManager.executeMutating(directory, d ->
						this.creator.createFile(d, fileIdentifier)
					);
				}
				
				return file;
			}
		}
		
	}
	
}
