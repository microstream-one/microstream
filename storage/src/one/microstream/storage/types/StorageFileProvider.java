package one.microstream.storage.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.chars.VarString;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;


public interface StorageFileProvider extends PersistenceTypeDictionaryIoHandler.Provider
{
	public AFileSystem fileSystem();
	
	public AFile provideDeletionTargetFile(
		StorageChannelFile fileToBeDeleted
	);
	
	public AFile provideTruncationTargetFile(
		StorageChannelFile fileToBeTruncated,
		long               newLength
	);
	
	public <F extends StorageDataFile, C extends Consumer<F>> C collectDataFiles(
		StorageDataFile.Creator<F> creator     ,
		C                          collector   ,
		int                        channelIndex
	);
	
	
	public interface Builder<B extends Builder<?>>
	{
		public AFileSystem fileSystem();
		
		public ADirectory directory();

		public B setDirectory(ADirectory directory);
		
		public ADirectory deletionDirectory();

		public B setDeletionDirectory(ADirectory deletionDirectory);

		public ADirectory truncationDirectory();

		public B setTruncationDirectory(ADirectory truncationDirectory);

		public StorageDirectoryStructureProvider directoryStructureProvider();

		public B setDirectoryStructureProvider(StorageDirectoryStructureProvider directoryStructureProvider);
		
		public StorageFileNameProvider fileNameProvider();
		
		public B setFileNameProvider(StorageFileNameProvider fileNameProvider);
		
		public PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator();
		
		public B setFileHandlerCreator(PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator);
		
		public StorageFileProvider createFileProvider();
		
		
		
		public abstract class Abstract<B extends Builder.Abstract<?>>
		implements StorageFileProvider.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFileSystem fileSystem;
			
			private ADirectory
				directory      ,
				deletionDirectory  ,
				truncationDirectory
			;
			
			private StorageDirectoryStructureProvider structureProvider;
			
			private StorageFileNameProvider fileNameProvider;
			
			private PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;
			
			

			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			Abstract(final AFileSystem fileSystem)
			{
				super();
				this.fileSystem = notNull(fileSystem);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final AFileSystem fileSystem()
			{
				return this.fileSystem;
			}
			
			@SuppressWarnings("unchecked")
			protected final B $()
			{
				return (B)this;
			}

			@Override
			public ADirectory directory()
			{
				return this.directory;
			}

			@Override
			public B setDirectory(final ADirectory baseDirectory)
			{
				this.directory = this.fileSystem.validateMember(baseDirectory);
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
			
			@Override
			public StorageFileNameProvider fileNameProvider()
			{
				return this.fileNameProvider;
			}
			
			@Override
			public B setFileNameProvider(final StorageFileNameProvider fileNameProvider)
			{
				this.fileNameProvider = fileNameProvider;
				
				return this.$();
			}
			
			@Override
			public PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator()
			{
				return this.fileHandlerCreator;
			}
			
			@Override
			public B setFileHandlerCreator(final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator)
			{
				this.fileHandlerCreator = fileHandlerCreator;
				return this.$();
			}
			
			protected abstract ADirectory getBaseDirectory();
			
			protected ADirectory getDeletionDirectory()
			{
				// no default / default is null
				return this.deletionDirectory;
			}
			
			protected ADirectory getTruncationDirectory()
			{
				// no default / default is null
				return this.truncationDirectory;
			}
			
			protected StorageDirectoryStructureProvider getDirectoryStructureProvider()
			{
				return this.structureProvider != null
					? this.structureProvider
					: StorageDirectoryStructureProvider.Defaults.defaultDirectoryStructureProvider()
				;
			}
			
			protected StorageFileNameProvider getFileNameProvider()
			{
				return this.fileNameProvider != null
					? this.fileNameProvider
					: StorageFileNameProvider.Defaults.defaultFileNameProvider()
				;
			}
			
			protected PersistenceTypeDictionaryFileHandler.Creator getTypeDictionaryFileHandler()
			{
				return this.fileHandlerCreator != null
					? this.fileHandlerCreator
					: PersistenceTypeDictionaryFileHandler::New
				;
			}
			
		}
		
	}
	
	
	public abstract class Abstract
	extends PersistenceTypeDictionaryIoHandler.Provider.Abstract
	implements StorageFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem fileSystem;
		
		private final ADirectory
			baseDirectory      ,
			deletionDirectory  ,
			truncationDirectory
		;
		
		private final StorageDirectoryStructureProvider structureProvider;
		
		private final StorageFileNameProvider fileNameProvider;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Abstract(
			final ADirectory                                   baseDirectory      ,
			final ADirectory                                   deletionDirectory  ,
			final ADirectory                                   truncationDirectory,
			final StorageDirectoryStructureProvider            structureProvider  ,
			final StorageFileNameProvider                      fileNameProvider   ,
			final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator
		)
		{
			super(fileHandlerCreator);
			this.fileSystem          = baseDirectory.fileSystem();
			this.baseDirectory       = baseDirectory      ;
			this.deletionDirectory   = deletionDirectory  ;
			this.truncationDirectory = truncationDirectory;
			this.structureProvider   = structureProvider  ;
			this.fileNameProvider    = fileNameProvider   ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public AFileSystem fileSystem()
		{
			return this.fileSystem;
		}

		public ADirectory baseDirectory()
		{
			return this.baseDirectory;
		}

		@Override
		protected AFile defineTypeDictionaryFile()
		{
			final ADirectory directory = this.baseDirectory;
			directory.ensureExists();
			
			final AFile file = directory.ensureFile(this.fileNameProvider.typeDictionaryFileName());
			
			return file;
		}
		
		private ADirectory provideChannelDirectory(
			final ADirectory baseDirectory,
			final int        channelIndex
		)
		{
			final ADirectory channelDirectory = this.structureProvider.provideChannelDirectory(
				baseDirectory,
				channelIndex,
				this.fileNameProvider
			);
			channelDirectory.ensureExists();
			
			return channelDirectory;
		}
		
		private AFile provideChannelFile(
			final ADirectory         baseDirectory,
			final StorageChannelFile channelFile  ,
			final String             newName      ,
			final String             newType
		)
		{
			final String effectiveName = coalesce(newName, channelFile.file().name());
			final String effectiveType = coalesce(newType, channelFile.file().type());
			
			return this.provideChannelFile(baseDirectory, channelFile.channelIndex(), effectiveName, effectiveType);
		}
		
		private AFile provideChannelFile(
			final ADirectory baseDirectory,
			final int        channelIndex ,
			final String     newName      ,
			final String     newType
		)
		{
			final ADirectory channelDirectory = this.provideChannelDirectory(baseDirectory, channelIndex);
			final AFile      channelFile      = channelDirectory.ensureFile(newName, newType);
			
			return channelFile;
		}
		
		@Override
		public AFile provideDeletionTargetFile(final StorageChannelFile fileToBeDeleted)
		{
			// maybe null, indicating to not backup the file
			if(this.deletionDirectory == null)
			{
				return null;
			}
			
			final String baseFileName = fileToBeDeleted.file().name();
			final String fileName     = addDeletionFileNameTag(baseFileName);
			final String fileType     = this.fileNameProvider.rescuedFileSuffix();
			
			return this.provideChannelFile(this.deletionDirectory, fileToBeDeleted, fileName, fileType);
		}
		
		protected static String addDeletionFileNameTag(final String currentName)
		{
			final SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss_SSS");
			final String newFileName = currentName + sdf.format(System.currentTimeMillis());
			
			return newFileName;
		}

		@Override
		public AFile provideTruncationTargetFile(final StorageChannelFile fileToBeTruncated, final long newLength)
		{
			// maybe null, indicating to not backup the file
			if(this.truncationDirectory == null)
			{
				return null;
			}

			final String baseFileName = fileToBeTruncated.file().name();
			final String fileType     = this.fileNameProvider.rescuedFileSuffix();
			final String fileName     = addTruncationFileNameTag(
				baseFileName,
				fileToBeTruncated.size(),
				newLength
			);

			return this.provideChannelFile(this.truncationDirectory, fileToBeTruncated, fileName, fileType);
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

		@Override
		public <F extends StorageDataFile, C extends Consumer<F>> C collectDataFiles(
			final StorageDataFile.Creator<F> creator     ,
			final C                          collector   ,
			final int                        channelIndex
		)
		{
			final ADirectory directory = this.structureProvider.provideChannelDirectory(
				this.baseDirectory,
				channelIndex,
				this.fileNameProvider
			);
			
			/*
			 * Only actually existing files are relevant.
			 * ...
			 * Hm. But does that justify a library deleting file entries from some potentially passed AFS instance?
			 * Maybe the exist check in the iteration is the better option, yet.
			 */
//			directory.consolidateFiles();
			
			directory.iterateFiles(f ->
			{
				// collecting files refers only to those that physically exist. Residual AFS entries don't count.
				if(!f.exists())
				{
					return;
				}
				this.fileNameProvider.parseDataInventoryFile(creator, collector, channelIndex, f);
			});
			
			return collector;
		}

		public ADirectory provideChannelDirectory(final int channelIndex)
		{
			return this.provideChannelDirectory(this.baseDirectory, channelIndex);
		}
		
		public AFile provideDataFile(final int channelIndex, final long fileNumber)
		{
			final ADirectory channelDirectory = this.provideChannelDirectory(channelIndex);
			final String     dataFileName     = this.fileNameProvider.provideDataFileName(channelIndex, fileNumber);
			final String     dataFileType     = this.fileNameProvider.dataFileSuffix();
			final AFile      file             = channelDirectory.ensureFile(dataFileName, dataFileType);
			
			return file;
		}

		public AFile provideTransactionsFile(final int channelIndex)
		{
			final ADirectory channelDirectory = this.provideChannelDirectory(channelIndex);
			final String     dataFileName     = this.fileNameProvider.provideTransactionsFileName(channelIndex);
			final String     dataFileType     = this.fileNameProvider.transactionsFileSuffix();
			final AFile      file             = channelDirectory.ensureFile(dataFileName, dataFileType);
			
			return file;
		}

		public AFile provideLockFile()
		{
			final AFile file = this.baseDirectory.ensureFile(this.fileNameProvider.lockFileName());
			
			return file;
		}
		
		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("base directory").tab().add('=').blank().add(this.baseDirectory   ).lf()
				.blank().add("file names"    ).tab().add('=').blank().add(this.fileNameProvider).lf()
				.toString()
			;
		}
		
	}
	
}
