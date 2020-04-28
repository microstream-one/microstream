package one.microstream.afs;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;

public interface AFileSystem extends AccessManager
{
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
	
	
	public abstract class Abstract<
		D extends ADirectory,
		R extends AReadableFile,
		W extends AWritableFile,
		F extends AFile.AbstractRegistering<?, D, R, W>
	>
		implements AFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<String, D> rootDirectories;
		private final AccessManager          accessManager  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AccessManager accessManager)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.accessManager   = notNull(accessManager);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// FIXME AFileSystem.Abstract#resolveDirectoryPath()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public AFile resolveFilePath(
			final String[] directoryPathElements,
			final int      offset               ,
			final int      length               ,
			final String   fileIdentifier
		)
		{
			final ADirectory directory = this.resolveDirectoryPath(directoryPathElements, offset, length);
			
			synchronized(directory)
			{
				AFile file = directory.getFile(fileIdentifier);
				if(file == null)
				{
					// (28.04.2020 TM)FIXME: priv#49: transaction-like use mutating
					file = this.accessManager.createFile(directory, fileIdentifier);
				}
				
				return file;
			}
		}
		

		@Override
		public AReadableFile createDirectory(final AMutableDirectory parent, final String identifier)
		{
			return this.accessManager.createDirectory(parent, identifier);
		}
		
		@Override
		public AReadableFile createFile(final AMutableDirectory parent, final String identifier)
		{
			return this.accessManager.createFile(parent, identifier);
		}
		
		@Override
		public AReadableFile createFile(final AMutableDirectory parent, final String name, final String type)
		{
			return this.accessManager.createFile(parent, name, type);
		}
		
		@Override
		public AReadableFile createFile(final AMutableDirectory parent, final String identifier, final String name, final String type)
		{
			return this.accessManager.createFile(parent, identifier, name, type);
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
		
		@Override
		public AMutableDirectory useMutating(final ADirectory directory, final Object mutator)
		{
			return this.accessManager.useMutating(directory, mutator);
		}
		
	}
	
}
