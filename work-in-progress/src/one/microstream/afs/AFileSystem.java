package one.microstream.afs;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;

public interface AFileSystem extends AccessManager
{
	// (30.04.2020 TM)FIXME: priv#49: "protocol" here or in AccessManager
	
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
	
		
	
	
	public static AFileSystem New(
		final ACreator      creator      ,
		final AccessManager accessManager
	)
	{
		return new AFileSystem.Default(
			EqHashTable.New()     ,
			notNull(creator)      ,
			notNull(accessManager)
		);
	}
	
	public final class Default implements AFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// (30.04.2020 TM)FIXME: priv#49: ARoot?
		private final EqHashTable<String, ADirectory> rootDirectories;
		private final ACreator                        creator        ;
		private final AccessManager                   accessManager  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final EqHashTable<String, ADirectory> rootDirectories,
			final ACreator                        creator        ,
			final AccessManager                   accessManager
		)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.creator         = creator          ;
			this.accessManager   = accessManager    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final synchronized boolean isUsed(final ADirectory directory)
		{
			// FIXME AFileSystem.Abstract#isUsed()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized boolean isMutating(final ADirectory directory)
		{
			// FIXME AFileSystem.Abstract#isMutating()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized boolean isReading(final AFile file)
		{
			// FIXME AFileSystem.Abstract#isReading()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized boolean isWriting(final AFile file)
		{
			// FIXME AFileSystem.Abstract#isWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		
		
		@Override
		public final synchronized boolean isUsed(final ADirectory directory, final Object owner)
		{
			// FIXME AFileSystem.Abstract#isUsed()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized boolean isMutating(final ADirectory directory, final Object owner)
		{
			// FIXME AFileSystem.Abstract#isMutating()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized boolean isReading(final AFile file, final Object owner)
		{
			// FIXME AFileSystem.Abstract#isReading()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public final synchronized boolean isWriting(final AFile file, final Object owner)
		{
			// FIXME AFileSystem.Abstract#isWriting()
			throw new one.microstream.meta.NotImplementedYetError();
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
		
		@Override
		public AUsedDirectory use(final ADirectory directory, final Object mutator)
		{
			return this.accessManager.use(directory, mutator);
		}
		
		@Override
		public AMutableDirectory useMutating(final ADirectory directory, final Object mutator)
		{
			return this.accessManager.useMutating(directory, mutator);
		}
		
		@Override
		public AReadableFile useReading(final AFile file, final Object reader)
		{
			return this.accessManager.useReading(file, reader);
		}
		
		@Override
		public AWritableFile useWriting(final AFile file, final Object writer)
		{
			return this.accessManager.useWriting(file, writer);
		}
		
	}
	
}
