package net.jadoth.storage.io.fs;

import java.io.File;
import java.nio.channels.FileLock;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.storage.io.ProtageWritableDirectory;
import net.jadoth.storage.io.ProtageWritableFile;


public interface FileSystemDirectory extends ProtageWritableDirectory
{
	public File directory();
	
	@Override
	public FileSystemFile createFile(String fileName);
	
	
	public static FileSystemDirectory New(final File directory)
	{
		/* (15.10.2018 TM)TODO: SystemDirectory#New
		 *  - validate existing and is directory.
		 *  - create instance.
		 *  - iterate existing children and create SystemFile instances from them.
		 *  - add all created file instances to the table.
		 *  - return instance.
		 */
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SystemDirectory#New()
	}
	
	public class Implementation implements FileSystemDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final File   directory          ;
		private final String cachedDirectoryName;
		
		private final EqHashTable<String, FileSystemFile.Implementation>   files    ;
		private final XGettingTable<String, FileSystemFile.Implementation> viewFiles;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final File                                                 directory,
			final String                                               name     ,
			final EqHashTable<String, FileSystemFile.Implementation>   files    ,
			final XGettingTable<String, FileSystemFile.Implementation> viewFiles
		)
		{
			super();
			this.directory           = directory;
			this.cachedDirectoryName = name     ;
			this.files               = files    ;
			this.viewFiles           = viewFiles;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.cachedDirectoryName;
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
			final File     file = this.internalCreateSystemFile(fileName);
			final FileLock lock = ProtageFileSystem.openFileChannel(file);
			
			final FileSystemFile.Implementation createdFile = new FileSystemFile.Implementation(
				this    ,
				fileName,
				file    ,
				lock    ,
				lock.channel()
			);
			
			// success of the addition is guaranteed by the synchronization and the validation above.
			this.files.add(fileName, createdFile);
			
			return createdFile;
		}
		
	}
	
}
