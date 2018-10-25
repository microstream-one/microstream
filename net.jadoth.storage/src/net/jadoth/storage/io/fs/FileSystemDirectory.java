package net.jadoth.storage.io.fs;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.storage.io.ProtageWritableDirectory;
import net.jadoth.storage.io.ProtageWritableFile;


public interface FileSystemDirectory extends ProtageWritableDirectory
{
	public File directory();
	
	@Override
	public FileSystemFile createFile(String fileName);
	
	
	
	public static FileSystemDirectory New(final File directory, final Predicate<? super File> isRelevantFile)
	{
		ProtageFileSystem.validateExistingDirectory(directory);
		ProtageFileSystem.validateIsDirectory(directory);
		
		final String                                               parentPath = directory.getParent();
		final String                                               name       = directory.getName();
		final EqHashTable<String, FileSystemFile.Implementation>   files      = EqHashTable.New();
		final XGettingTable<String, FileSystemFile.Implementation> viewFiles  = files.view();
		
		final FileSystemDirectory.Implementation instance = new FileSystemDirectory.Implementation(
			directory, parentPath, name, parentPath, files, viewFiles
		);
		instance.initializeFiles(isRelevantFile);
		
		return instance;
	}
	
	public class Implementation implements FileSystemDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final File   directory          ;
		private final String cachedParentPath   ;
		private final String cachedDirectoryName;
		private final String cachedPathName     ;
		
		private final EqHashTable<String, FileSystemFile.Implementation>   files    ;
		private final XGettingTable<String, FileSystemFile.Implementation> viewFiles;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final File                                                 directory ,
			final String                                               parentPath,
			final String                                               name      ,
			final String                                               path      ,
			final EqHashTable<String, FileSystemFile.Implementation>   files     ,
			final XGettingTable<String, FileSystemFile.Implementation> viewFiles
		)
		{
			super();
			this.directory           = directory ;
			this.cachedParentPath    = parentPath;
			this.cachedDirectoryName = name      ;
			this.cachedPathName      = path      ;
			this.files               = files     ;
			this.viewFiles           = viewFiles ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String qualifier()
		{
			return this.cachedParentPath;
		}

		@Override
		public final String name()
		{
			return this.cachedDirectoryName;
		}
		
		@Override
		public final String identifier()
		{
			return this.cachedPathName;
		}

		@Override
		public final File directory()
		{
			return this.directory;
		}

		@Override
		public final XGettingTable<String, ? extends ProtageWritableFile> files()
		{
			return this.viewFiles;
		}
		
		@Override
		public boolean contains(final String fileName)
		{
			return this.files.keys().contains(fileName);
		}
		
		protected void validateNotYetContained(final String fileName)
		{
			if(!this.contains(fileName))
			{
				return;
			}
			
			// (15.10.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"File \"" + fileName + "\" already exist in directory \"" + this.name() + "\"."
			);
		}
		
		protected File internalCreateSystemFile(final String fileName)
		{
			this.validateNotYetContained(fileName);
			return ProtageFileSystem.createWriteableFile(this.directory, fileName);
		}
				
		@Override
		public synchronized FileSystemFile createFile(final String fileName)
		{
			final File file = this.internalCreateSystemFile(fileName);
			return this.internalCreateFile(file, fileName);
		}
		
		final synchronized void initializeFiles(final Predicate<? super File> isRelevantFile)
		{
			for(final File file : this.directory.listFiles())
			{
				if(!isRelevantFile.test(file))
				{
					continue;
				}
				
				final String fileName = file.getName();
				this.validateNotYetContained(fileName);
				ProtageFileSystem.validateWriteableFile(file);
				
				this.internalCreateFile(file, fileName);
			}
		}
		
		protected synchronized FileSystemFile internalCreateFile(final File file, final String fileName)
		{
			// file is created in closed state to allow a complete creation of a preliminary directory instance
			final FileSystemFile.Implementation createdFile = new FileSystemFile.Implementation(
				this, fileName, file, null, null
			);
			
			// success of the addition is guaranteed by the synchronization and the validation above.
			this.files.add(fileName, createdFile);
			
			return createdFile;
		}
		
	}
	
}
