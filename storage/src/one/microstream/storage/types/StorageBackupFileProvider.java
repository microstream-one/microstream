package one.microstream.storage.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.text.SimpleDateFormat;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.storage.types.StorageFileProvider.Defaults;


public interface StorageBackupFileProvider
{
	public StorageBackupDataFile provideBackupFile(
		StorageDataFile dataFile
	);
	
	public StorageBackupTransactionsFile provideBackupFile(
		StorageTransactionsFile transactionsFile
	);
	
	public StorageBackupDataFile provideDeletionTargetFile(
		StorageDataFile fileToBeDeleted
	);
	
	public StorageBackupTransactionsFile provideDeletionTargetFile(
		StorageTransactionsFile fileToBeDeleted
		
	);
	
	public StorageBackupDataFile provideTruncationBackupTargetFile(
		StorageDataFile fileToBeTruncated,
		long            newLength
	);
	
	
	public static StorageBackupFileProvider New(
		final ADirectory                        backupDirectory    ,
		final ADirectory                        deletionDirectory  ,
		final ADirectory                        truncationDirectory,
		final StorageDirectoryStructureProvider structureProvider
	)
	{
		return new StorageBackupFileProvider.Default(
			notNull(backupDirectory),
			mayNull(deletionDirectory),
			mayNull(truncationDirectory),
			notNull(structureProvider)
		);
	}
	
	public final class Default implements StorageBackupFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ADirectory
			backupDirectory    ,
			deletionDirectory  ,
			truncationDirectory
		;
		
		private final StorageDirectoryStructureProvider structureProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ADirectory                        backupDirectory    ,
			final ADirectory                        deletionDirectory  ,
			final ADirectory                        truncationDirectory,
			final StorageDirectoryStructureProvider structureProvider
		)
		{
			super();
			this.backupDirectory     = backupDirectory    ;
			this.deletionDirectory   = deletionDirectory  ;
			this.truncationDirectory = truncationDirectory;
			this.structureProvider   = structureProvider  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private ADirectory provideDirectory(final ADirectory baseDirectory, final StorageChannelFile dataFile)
		{
			return this.structureProvider.provideChannelDirectory(
				baseDirectory,
				dataFile.channelIndex()
			);
		}
		
		private AFile provideFile(
			final ADirectory         baseDirectory,
			final StorageChannelFile dataFile     ,
			final String             newName      ,
			final String             newType
		)
		{
			final String effectiveName = coalesce(newName, dataFile.file().name());
			final String effectiveType = coalesce(newType, dataFile.file().type());
			
			final ADirectory directory  = this.provideDirectory(baseDirectory, dataFile);
			final AFile      backupFile = directory.ensureFile(effectiveName, effectiveType);
			
			return backupFile;
		}
		
		private StorageBackupDataFile provideDataFile(
			final ADirectory      baseDirectory,
			final StorageDataFile dataFile
		)
		{
			return this.provideDataFile(baseDirectory, dataFile, null, null);
		}
		
		private StorageBackupDataFile provideDataFile(
			final ADirectory      baseDirectory,
			final StorageDataFile dataFile     ,
			final String          newName      ,
			final String          newType
		)
		{
			final AFile backupFile = this.provideFile(baseDirectory, dataFile, newName, newType);
			
			return StorageBackupDataFile.New(backupFile, dataFile.channelIndex(), dataFile.number());
		}
		
		private StorageBackupTransactionsFile provideTransactionsFile(
			final ADirectory              baseDirectory   ,
			final StorageTransactionsFile transactionsFile
		)
		{
			return this.provideTransactionsFile(baseDirectory, transactionsFile, null, null);
		}
		
		private StorageBackupTransactionsFile provideTransactionsFile(
			final ADirectory              baseDirectory   ,
			final StorageTransactionsFile transactionsFile,
			final String                  newName         ,
			final String                  newType
		)
		{
			final AFile backupFile = this.provideFile(baseDirectory, transactionsFile, newName, newType);
			
			return StorageBackupTransactionsFile.New(backupFile, transactionsFile.channelIndex());
		}

		@Override
		public StorageBackupDataFile provideBackupFile(final StorageDataFile dataFile)
		{
			return this.provideDataFile(this.backupDirectory, dataFile);
		}

		@Override
		public StorageBackupTransactionsFile provideBackupFile(
			final StorageTransactionsFile transactionsFile
		)
		{
			return this.provideTransactionsFile(this.backupDirectory, transactionsFile);
		}

		@Override
		public StorageBackupDataFile provideDeletionTargetFile(
			final StorageDataFile fileToBeDeleted
		)
		{
			// maybe null, indicating to not backup the file
			if(this.deletionDirectory == null)
			{
				return null;
			}
			
			final String fileName = addDeletionFileNameTag(fileToBeDeleted.file().name());
			
			return this.provideDataFile(this.deletionDirectory, fileToBeDeleted, fileName, null);
		}

		@Override
		public StorageBackupTransactionsFile provideDeletionTargetFile(
			final StorageTransactionsFile fileToBeDeleted
		)
		{
			// maybe null, indicating to not backup the file
			if(this.deletionDirectory == null)
			{
				return null;
			}
			
			final String fileName = addDeletionFileNameTag(fileToBeDeleted.file().name());
			
			return this.provideTransactionsFile(this.deletionDirectory, fileToBeDeleted, fileName, null);
		}
		
		protected static String addDeletionFileNameTag(final String currentName)
		{
			final SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss_SSS");
			final String newFileName = currentName + sdf.format(System.currentTimeMillis());
			
			return newFileName;
		}

		@Override
		public StorageBackupDataFile provideTruncationBackupTargetFile(
			final StorageDataFile fileToBeTruncated,
			final long            newLength
		)
		{
			// maybe null, indicating to not backup the file
			if(this.truncationDirectory == null)
			{
				return null;
			}
			
			final String fileName = addTruncationFileNameTag(
				fileToBeTruncated.file().name(),
				fileToBeTruncated.size(),
				newLength
			);
			
			return this.provideDataFile(this.truncationDirectory, fileToBeTruncated, fileName, null);
		}
		
		protected static String addTruncationFileNameTag(
			final String truncationFileNameRaw,
			final long   oldLength            ,
			final long   newLength
		)
		{
			return truncationFileNameRaw + "_truncated_from_" + oldLength + "_to_" + newLength
				+ "_@" + System.currentTimeMillis()
			;
		}
		
	}
	
	public static StorageBackupFileProvider.Builder<?> Builder()
	{
		return Builder(NioFileSystem.New());
	}
	
	public static StorageBackupFileProvider.Builder<?> Builder(final AFileSystem fileSystem)
	{
		return new StorageBackupFileProvider.Builder.Default<>(fileSystem);
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public AFileSystem fileSystem();
		
		public ADirectory backupDirectory();

		public B setBackupDirectory(ADirectory backupDirectory);

		public ADirectory deletionDirectory();

		public B setDeletionDirectory(ADirectory deletionDirectory);

		public ADirectory truncationDirectory();

		public B setTruncationDirectory(ADirectory truncationDirectory);

		public StorageDirectoryStructureProvider directoryStructureProvider();

		public B setDirectoryStructureProvider(StorageDirectoryStructureProvider directoryStructureProvider);
		
		public StorageBackupFileProvider createBackupFileProvider();
		
		
		
		public class Default<B extends Builder.Default<?>> implements StorageBackupFileProvider.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFileSystem fileSystem;
			
			private ADirectory
				backupDirectory    ,
				deletionDirectory  ,
				truncationDirectory
			;
			
			private StorageDirectoryStructureProvider structureProvider;
			
			

			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			Default(final AFileSystem fileSystem)
			{
				super();
				this.fileSystem = fileSystem;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@SuppressWarnings("unchecked")
			protected final B $()
			{
				return (B)this;
			}
			
			@Override
			public final AFileSystem fileSystem()
			{
				return this.fileSystem;
			}

			@Override
			public ADirectory backupDirectory()
			{
				return this.backupDirectory;
			}

			@Override
			public B setBackupDirectory(final ADirectory backupDirectory)
			{
				this.backupDirectory = this.fileSystem.validateMember(backupDirectory);
				return this.$();
			}

			@Override
			public ADirectory deletionDirectory()
			{
				return this.deletionDirectory;
			}

			@Override
			public B setDeletionDirectory(final ADirectory deletionDirectory)
			{
				this.deletionDirectory = this.fileSystem.validateMember(deletionDirectory);
				return this.$();
			}

			@Override
			public ADirectory truncationDirectory()
			{
				return this.truncationDirectory;
			}

			@Override
			public B setTruncationDirectory(final ADirectory truncationDirectory)
			{
				this.truncationDirectory = this.fileSystem.validateMember(truncationDirectory);
				return this.$();
			}
			
			@Override
			public StorageDirectoryStructureProvider directoryStructureProvider()
			{
				return this.structureProvider;
			}

			@Override
			public B setDirectoryStructureProvider(final StorageDirectoryStructureProvider directoryStructureProvider)
			{
				this.structureProvider = directoryStructureProvider;
				return this.$();
			}
			
			
			protected ADirectory getBaseDirectory()
			{
				if(this.backupDirectory != null)
				{
					return this.backupDirectory;
				}
				
				// note: relative root directory inside the current working directory
				return this.fileSystem.ensureRoot(Defaults.defaultStorageDirectory());
			}
			
			protected ADirectory getDeletionDirectory()
			{
				if(this.deletionDirectory != null)
				{
					return this.deletionDirectory;
				}
				
				// Defaults method is the single location to control behavior.
				final String nameDeletionDirectory = Defaults.defaultDeletionDirectory();

				// note: relative root directory inside the current working directory
				return nameDeletionDirectory == null
					? null
					: this.fileSystem.ensureRoot(nameDeletionDirectory)
				;
			}
			
			protected ADirectory getTruncationDirectory()
			{
				if(this.truncationDirectory != null)
				{
					return this.truncationDirectory;
				}
				
				// Defaults method is the single location to control behavior.
				final String nameTruncationDirectory = Defaults.defaultTruncationDirectory();

				// note: relative root directory inside the current working directory
				return nameTruncationDirectory == null
					? null
					: this.fileSystem.ensureRoot(nameTruncationDirectory)
				;
			}
			
			protected StorageDirectoryStructureProvider getDirectoryStructureProvider()
			{
				if(this.structureProvider != null)
				{
					return this.structureProvider;
				}
				
				return StorageDirectoryStructureProvider.New();
			}
			
			@Override
			public StorageBackupFileProvider createBackupFileProvider()
			{
				return StorageBackupFileProvider.New(
					this.getBaseDirectory(),
					this.getDeletionDirectory(),
					this.getTruncationDirectory(),
					this.getDirectoryStructureProvider()
				);
			}
			
		}
		
	}
}
