package one.microstream.afs.local;

import java.io.File;
import java.util.function.Predicate;

import one.microstream.afs.AWritableDirectory;
import one.microstream.afs.AWritableFile;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.io.XIO;


public interface LocalDirectory extends AWritableDirectory
{
	public File directory();
	
	@Override
	public LocalFile createFile(String fileName);
	
	
	
	public static LocalDirectory New(final File directory, final Predicate<? super File> isRelevantFile)
	{
		LocalFileSystem.validateExistingDirectory(directory);
		LocalFileSystem.validateIsDirectory(directory);
		
		final String qualifier  = XIO.ensureNormalizedPathSeperators(directory.getParent());
		final String name       = directory.getName();
		final String identifier = XIO.ensureTrailingSlash(qualifier) + name;
		final String qualIdent  = XIO.ensureTrailingSlash(identifier);
		
		final EqHashTable<String, LocalFile.Default>   files      = EqHashTable.New();
		final XGettingTable<String, LocalFile.Default> viewFiles  = files.view();
		
		final LocalDirectory.Default instance = new LocalDirectory.Default(
			directory, qualifier, name, identifier, qualIdent, files, viewFiles
		);
		instance.initializeFiles(isRelevantFile);
		
		return instance;
	}
	
	public class Default implements LocalDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final File   directory           ;
		private final String qualifier           ;
		private final String name                ;
		private final String identifier          ;
		private final String qualifyingIdentifier;
		
		private final EqHashTable<String, LocalFile.Default>   files    ;
		private final XGettingTable<String, LocalFile.Default> viewFiles;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final File                                                directory           ,
			final String                                              qualifier           ,
			final String                                              name                ,
			final String                                              identifier          ,
			final String                                              qualifyingIdentifier,
			final EqHashTable<String, LocalFile.Default>   files               ,
			final XGettingTable<String, LocalFile.Default> viewFiles
		)
		{
			super();
			this.directory            = directory           ;
			this.qualifier            = qualifier           ;
			this.name                 = name                ;
			this.identifier           = identifier          ;
			this.qualifyingIdentifier = qualifyingIdentifier;
			this.files                = files               ;
			this.viewFiles            = viewFiles           ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String parent()
		{
			return this.qualifier;
		}

		@Override
		public final String name()
		{
			return this.name;
		}
		
		@Override
		public final String path()
		{
			return this.identifier;
		}
		
		@Override
		public final String qualifier()
		{
			return this.qualifyingIdentifier;
		}

		@Override
		public final File directory()
		{
			return this.directory;
		}

		@Override
		public final XGettingTable<String, ? extends AWritableFile> files()
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
			return LocalFileSystem.createWriteableFile(this.directory, fileName);
		}
				
		@Override
		public synchronized LocalFile createFile(final String fileName)
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
				LocalFileSystem.validateWriteableFile(file);
				
				this.internalCreateFile(file, fileName);
			}
		}
		
		protected synchronized LocalFile internalCreateFile(final File file, final String fileName)
		{
			// file is created in closed state to allow a complete creation of a preliminary directory instance
			final LocalFile.Default createdFile = new LocalFile.Default(
				this, fileName, file, null, null
			);
			
			// success of the addition is guaranteed by the synchronization and the validation above.
			this.files.add(fileName, createdFile);
			
			return createdFile;
		}
		
	}
	
}
