package one.microstream.storage.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.file.Path;
import java.util.function.Consumer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.chars.VarString;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;

public interface StorageFileProvider extends PersistenceTypeDictionaryIoHandler.Provider
{
	/* (03.03.2019 TM)TODO: proper file abstraction
	 * An abstraction on the persistence layer is required with types like
	 * - PersistenceDataItem (Folder or File)
	 * - PersistenceDataLocation extends PersistenceDataItem (Folder, has n PersistenceDataItems, each with unique name)
	 * - PersistenceDataFile extends PersistenceDataItem (File, must always be Folder + String name)
	 * 
	 * Then this type here will no longer extend PersistenceTypeDictionaryIoHandler.Provider,
	 * but just a PersistenceTypeDictionaryDataFileProvider
	 */
	
	/**
	 * Returns a String that uniquely identifies the storage location.
	 * 
	 * @return a String that uniquely identifies the storage location.
	 */
	public String getStorageLocationIdentifier();
	
	@Override
	public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
		PersistenceTypeDictionaryStorer writeListener
	);
	
	public <F extends StorageDataFile> F provideDataFile(
		StorageDataFile.Creator<F> creator     ,
		int                        channelIndex,
		long                       fileNumber
	);

	public <F extends StorageTransactionsFile> F provideTransactionsFile(
		StorageTransactionsFile.Creator<F> creator     ,
		int                                channelIndex
	);
	
	public StorageLockFile provideLockFile();
	
	public StorageBackupDataFile provideDeletionTargetFile(StorageLiveDataFile fileToBeDeleted);
	
	public StorageBackupDataFile provideTruncationBackupTargetFile(StorageLiveDataFile fileToBeTruncated, long newLength);

	
	
	public <F extends StorageDataFile, P extends Consumer<F>> P collectDataFiles(
		StorageDataFile.Creator<F> creator     ,
		P                          collector   ,
		int                        channelIndex
	);
	
	
	public interface Defaults
	{
		public static String defaultStorageDirectory()
		{
			return "storage";
		}
		
		public static String defaultDeletionDirectory()
		{
			return null;
		}
		
		public static String defaultTruncationDirectory()
		{
			return null;
		}
		
		public static String defaultChannelDirectoryPrefix()
		{
			return "channel_";
		}
		
		public static String defaultStorageFilePrefix()
		{
			return "channel_";
		}
		
		public static String defaultStorageFileSuffix()
		{
			return "dat";
		}

		public static String defaultTransactionFilePrefix()
		{
			return "transactions_";
		}
		
		public static String defaultTransactionFileSuffix()
		{
			return "sft"; // "storage file transactions"
		}

		public static String defaultTypeDictionaryFileName()
		{
			return Persistence.defaultFilenameTypeDictionary();
		}
		
		public static String defaultLockFileName()
		{
			return "used.lock";
		}

		public static PersistenceTypeDictionaryFileHandler.Creator defaultTypeDictionaryFileHandlerCreator()
		{
			return PersistenceTypeDictionaryFileHandler::New;
		}
		
	}


	public final class Static
	{
		public static final <F extends StorageDataFile, C extends Consumer<? super F>>
		C collectFile(
			final StorageDataFile.Creator<F> fileCreator     ,
			final C                          collector       ,
			final int                        channelIndex    ,
			final ADirectory                 storageDirectory,
			final String                     fileBaseName    ,
			final String                     suffix
		)
		{
			storageDirectory.iterateFiles(f ->
			{
				internalCollectDataFile(fileCreator, collector, channelIndex, f, fileBaseName, suffix);
			});
			
			return collector;
		}

		private static final <F extends StorageDataFile> void internalCollectDataFile(
			final StorageDataFile.Creator<F> fileCreator ,
			final Consumer<? super F>        collector   ,
			final int                        hashIndex   ,
			final AFile                      file        ,
			final String                     fileBaseName,
			final String                     suffix
		)
		{
			final String filename = file.name();
			if(!filename.startsWith(fileBaseName))
			{
				return;
			}
			if(!suffix.equals(file.type()))
			{
				return;
			}

			final String middlePart = filename.substring(fileBaseName.length(), filename.length() - suffix.length());
			final int separatorIndex = middlePart.indexOf('_');
			if(separatorIndex < 0)
			{
				return;
			}
			
			final String hashIndexString = middlePart.substring(0, separatorIndex);
			try
			{
				if(Integer.parseInt(hashIndexString) != hashIndex)
				{
					return;
				}
			}
			catch(final NumberFormatException e)
			{
				return;
			}

			final String fileNumberString = middlePart.substring(separatorIndex + 1);
			final long fileNumber;
			try
			{
				fileNumber = Long.parseLong(fileNumberString);
			}
			catch(final NumberFormatException e)
			{
				return; // not a strictly validly named file, ignore intentionally despite all previous matches.
			}

			// strictly validly named file, collect.
			collector.accept(fileCreator.createDataFile(file, hashIndex, fileNumber));
		}

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}
	
	
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider.Builder} instance.
	 * <p>
	 * For explanations and customizing values, see {@link StorageFileProvider.Builder}.
	 * 
	 * @return a new {@link StorageFileProvider.Builder} instance.
	 */
	public static Builder<?> Builder()
	{
		return Builder(NioFileSystem.New());
	}
	
	public static Builder<?> Builder(final AFileSystem fileSystem)
	{
		return new StorageFileProvider.Builder.Default<>(
			notNull(fileSystem)
		);
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public AFileSystem fileSystem();
		
		public ADirectory baseDirectory();

		public B setBaseDirectory(ADirectory baseDirectory);

		public ADirectory deletionDirectory();

		public B setDeletionDirectory(ADirectory deletionDirectory);

		public ADirectory truncationDirectory();

		public B setTruncationDirectory(ADirectory truncationDirectory);

		public String channelDirectoryPrefix();

		public B setChannelDirectoryPrefix(String channelDirectoryPrefix);

		public String storageFilePrefix();

		public B setStorageFilePrefix(String storageFilePrefix);

		public String storageFileSuffix();

		public B setStorageFileSuffix(String storageFileSuffix);

		public String transactionsFilePrefix();

		public B setTransactionsFilePrefix(String transactionsFilePrefix);

		public String transactionsFileSuffix();

		public B setTransactionsFileSuffix(String transactionsFileSuffix);

		public String typeDictionaryFileName();

		public B setTypeDictionaryFileName(String typeDictionaryFileName);

		public String lockFileName();

		public B setLockFileName(String lockFileName);
		
		public PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator();
		
		public B setFileHandlerCreator(PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator);
		
		public StorageFileProvider createFileProvider();
		
		
		
		public class Default<B extends Builder.Default<?>> implements StorageFileProvider.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFileSystem fileSystem;
			
			private ADirectory
				baseDirectory      ,
				deletionDirectory  ,
				truncationDirectory
			;
			
			private String
				channelDirectoryPrefix,
				storageFilePrefix     ,
				storageFileSuffix     ,
				transactionsFilePrefix,
				transactionsFileSuffix,
				typeDictionaryFileName,
				lockFileName
			;
			
			private PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;
			
			

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
			public ADirectory baseDirectory()
			{
				return this.baseDirectory;
			}

			@Override
			public B setBaseDirectory(final ADirectory baseDirectory)
			{
				this.baseDirectory = this.fileSystem.validateMember(baseDirectory);
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
			public String channelDirectoryPrefix()
			{
				return this.channelDirectoryPrefix;
			}

			@Override
			public B setChannelDirectoryPrefix(final String channelDirectoryPrefix)
			{
				this.channelDirectoryPrefix = channelDirectoryPrefix;
				return this.$();
			}

			@Override
			public String storageFilePrefix()
			{
				return this.storageFilePrefix;
			}

			@Override
			public B setStorageFilePrefix(final String storageFilePrefix)
			{
				this.storageFilePrefix = storageFilePrefix;
				return this.$();
			}

			@Override
			public String storageFileSuffix()
			{
				return this.storageFileSuffix;
			}

			@Override
			public B setStorageFileSuffix(final String storageFileSuffix)
			{
				this.storageFileSuffix = storageFileSuffix;
				return this.$();
			}

			@Override
			public String transactionsFilePrefix()
			{
				return this.transactionsFilePrefix;
			}

			@Override
			public B setTransactionsFilePrefix(final String transactionsFilePrefix)
			{
				this.transactionsFilePrefix = transactionsFilePrefix;
				return this.$();
			}

			@Override
			public String transactionsFileSuffix()
			{
				return this.transactionsFileSuffix;
			}

			@Override
			public B setTransactionsFileSuffix(final String transactionsFileSuffix)
			{
				this.transactionsFileSuffix = transactionsFileSuffix;
				return this.$();
			}

			@Override
			public String typeDictionaryFileName()
			{
				return this.typeDictionaryFileName;
			}

			@Override
			public B setTypeDictionaryFileName(final String typeDictionaryFileName)
			{
				this.typeDictionaryFileName = typeDictionaryFileName;
				return this.$();
			}

			@Override
			public String lockFileName()
			{
				return this.lockFileName;
			}

			@Override
			public B setLockFileName(final String lockFileName)
			{
				this.lockFileName = lockFileName;
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
			
			protected ADirectory getBaseDirectory()
			{
				if(this.baseDirectory != null)
				{
					return this.baseDirectory;
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
				
				// Defaults method is the single location to controls behavior.
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
				
				// Defaults method is the single location to controls behavior.
				final String nameTruncationDirectory = Defaults.defaultTruncationDirectory();

				// note: relative root directory inside the current working directory
				return nameTruncationDirectory == null
					? null
					: this.fileSystem.ensureRoot(nameTruncationDirectory)
				;
			}
			
			@Override
			public StorageFileProvider createFileProvider()
			{
				return StorageFileProvider.New(
					this.getBaseDirectory(),
					this.getDeletionDirectory(),
					this.getTruncationDirectory(),
					coalesce(this.channelDirectoryPrefix, Defaults.defaultChannelDirectoryPrefix()          ),
					coalesce(this.storageFilePrefix     , Defaults.defaultStorageFilePrefix()               ),
					coalesce(this.storageFileSuffix     , Defaults.defaultStorageFileSuffix()               ),
					coalesce(this.transactionsFilePrefix, Defaults.defaultTransactionFilePrefix()           ),
					coalesce(this.transactionsFileSuffix, Defaults.defaultTransactionFileSuffix()           ),
					coalesce(this.typeDictionaryFileName, Defaults.defaultTypeDictionaryFileName()          ),
					coalesce(this.lockFileName          , Defaults.defaultLockFileName()                    ),
					coalesce(this.fileHandlerCreator    , Defaults.defaultTypeDictionaryFileHandlerCreator())
				);
			}
			
		}
		
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider} instance with default values
	 * provided by {@link StorageFileProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageFileProvider.Builder}.
	 * 
	 * @return {@linkDoc StorageFileProvider#New(Path)@return}
	 * 
	 * @see StorageFileProvider#New(Path)
	 * @see StorageFileProvider.Builder
	 * @see StorageFileProvider.Defaults
	 */
	public static StorageFileProvider New()
	{
		return Storage.FileProviderBuilder()
			.createFileProvider()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider} instance with the passed file
	 * as the storage directory and defaults provided by {@link StorageFileProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageFileProvider.Builder}.
	 * 
	 * @param storageDirectory the directory where the storage will be located.
	 * 
	 * @return a new {@link StorageFileProvider} instance.
	 * 
	 * @see StorageFileProvider#New()
	 * @see StorageFileProvider.Builder
	 * @see StorageFileProvider.Defaults
	 */
	public static StorageFileProvider New(final ADirectory storageDirectory)
	{
		return Storage.FileProviderBuilder(storageDirectory.fileSystem())
			.setBaseDirectory(storageDirectory)
			.createFileProvider()
		;
	}
	
	/**
	 * 
	 * @param baseDirectory may <b>not</b> be null.
	 * @param channelDirectoryPrefix may <b>not</b> be null.
	 * @param storageFilePrefix may <b>not</b> be null.
	 * @param storageFileSuffix may <b>not</b> be null.
	 * @param transactionsFilePrefix may <b>not</b> be null.
	 * @param transactionsFileSuffix may <b>not</b> be null.
	 * @param typeDictionaryFileName may <b>not</b> be null.
	 * @param lockFileName may <b>not</b> be null.
	 * @param fileHandlerCreator may <b>not</b> be null.
	 * @param deletionDirectory may be null.
	 * @param truncationDirectory may be null.
	 */
	public static StorageFileProvider.Default New(
		final ADirectory                                   baseDirectory         ,
		final ADirectory                                   deletionDirectory     ,
		final ADirectory                                   truncationDirectory   ,
		final String                                       channelDirectoryPrefix,
		final String                                       storageFilePrefix     ,
		final String                                       storageFileSuffix     ,
		final String                                       transactionsFilePrefix,
		final String                                       transactionsFileSuffix,
		final String                                       typeDictionaryFileName,
		final String                                       lockFileName          ,
		final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator
	)
	{
		return new StorageFileProvider.Default(
			notNull(baseDirectory)         , // base directory must at least be a relative directory name.
			notNull(channelDirectoryPrefix),
			notNull(storageFilePrefix)     ,
			notNull(storageFileSuffix)     ,
			notNull(transactionsFilePrefix),
			notNull(transactionsFileSuffix),
			notNull(typeDictionaryFileName),
			notNull(lockFileName)          ,
			notNull(fileHandlerCreator)    ,
			mayNull(deletionDirectory)     , // null (no directory) means actually delete retired files
			mayNull(truncationDirectory)     // null (no directory) means actually delete truncated files
		);
	}


	
	public final class Default implements StorageFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ADirectory
			baseDirectory      ,
			deletionDirectory  ,
			truncationDirectory
		;

		private final String
			channelDirectoryPrefix,
			dataFilePrefix        ,
			dataFileSuffix        ,
			transactionsFilePrefix,
			transactionsFileSuffix,
			typeDictionaryFileName,
			lockFileName
		;
		
		private final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ADirectory                                   baseDirectory         ,
			final String                                       channelDirectoryPrefix,
			final String                                       dataFilePrefix        ,
			final String                                       dataFileSuffix        ,
			final String                                       transactionsFilePrefix,
			final String                                       transactionsFileSuffix,
			final String                                       typeDictionaryFileName,
			final String                                       lockFileName          ,
			final PersistenceTypeDictionaryFileHandler.Creator dictFileHandlerCreator,
			final ADirectory                                   deletionDirectory     ,
			final ADirectory                                   truncationDirectory
		)
		{
			super();
			this.baseDirectory          = baseDirectory         ;
			this.channelDirectoryPrefix = channelDirectoryPrefix;
			this.dataFilePrefix         = dataFilePrefix        ;
			this.dataFileSuffix         = dataFileSuffix        ;
			this.transactionsFilePrefix = transactionsFilePrefix;
			this.transactionsFileSuffix = transactionsFileSuffix;
			this.typeDictionaryFileName = typeDictionaryFileName;
			this.lockFileName           = lockFileName          ;
			this.fileHandlerCreator     = dictFileHandlerCreator;
			this.deletionDirectory      = deletionDirectory     ;
			this.truncationDirectory    = truncationDirectory   ;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String getStorageLocationIdentifier()
		{
			return this.baseDirectory().toPathString();
		}

		public ADirectory baseDirectory()
		{
			return this.baseDirectory;
		}

		public ADirectory deletionDirectory()
		{
			return this.deletionDirectory;
		}

		public ADirectory truncationDirectory()
		{
			return this.truncationDirectory;
		}
		
		public String channelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}
		
		public String storageFileSuffix()
		{
			return this.dataFileSuffix;
		}
		
		public String typeDictionaryFileName()
		{
			return this.typeDictionaryFileName;
		}

		public String lockFileName()
		{
			return this.lockFileName;
		}

		public final String provideStorageFileName(final int channelIndex, final long fileNumber)
		{
			return this.dataFilePrefix + channelIndex + '_' + fileNumber;
		}

		public final String provideTransactionFileName(final int channelIndex)
		{
			return this.transactionsFilePrefix + channelIndex;
		}
		
		@Override
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			/*
			 * (04.03.2019 TM)TODO: forced delegating API is not a clean solution.
			 * This is only a temporary solution. See the task containing "PersistenceDataFile".
			 */
			final ADirectory directory = this.baseDirectory();
			directory.ensure();
			
			final AFile file = directory.ensureFile(this.typeDictionaryFileName());
			
			return this.fileHandlerCreator.createTypeDictionaryIoHandler(file, writeListener);
		}

		public final ADirectory provideChannelDirectory(
			final ADirectory parentDirectory,
			final int        hashIndex
		)
		{
			final String channelDirectoryName = this.channelDirectoryPrefix() + hashIndex;
			final ADirectory channelDirectory = parentDirectory.ensureDirectory(channelDirectoryName);
			
			channelDirectory.ensure();
			
			return channelDirectory;
		}

		public ADirectory provideChannelDirectory(final int channelIndex)
		{
			return this.provideChannelDirectory(this.baseDirectory(), channelIndex);
		}
		
		@Override
		public <F extends StorageDataFile> F provideDataFile(
			final StorageDataFile.Creator<F> creator     ,
			final int                        channelIndex,
			final long                       fileNumber
		)
		{
			final ADirectory channelDirectory = this.provideChannelDirectory(channelIndex);
			final String     dataFileName     = this.provideStorageFileName(channelIndex, fileNumber);
			final String     dataFileType     = this.dataFileSuffix;
			final AFile      file             = channelDirectory.ensureFile(dataFileName, dataFileType);
			
			return creator.createDataFile(file, channelIndex, fileNumber);
		}

		@Override
		public <F extends StorageTransactionsFile> F provideTransactionsFile(
			final StorageTransactionsFile.Creator<F> creator     ,
			final int                                channelIndex
		)
		{
			final ADirectory channelDirectory = this.provideChannelDirectory(channelIndex);
			final String     dataFileName     = this.provideTransactionFileName(channelIndex);
			final String     dataFileType     = this.transactionsFileSuffix;
			final AFile      file             = channelDirectory.ensureFile(dataFileName, dataFileType);
			
			return creator.createTransactionsFile(file, channelIndex);
		}
		
		@Override
		public StorageLockFile provideLockFile()
		{
			final AFile lockFile = this.baseDirectory().ensureFile(this.lockFileName());
			
			return StorageLockFile.New(lockFile);
		}

		@Override
		public StorageBackupDataFile provideDeletionTargetFile(final StorageLiveDataFile fileToBeDeleted)
		{
			final ADirectory deletionDirectory = this.deletionDirectory();
			if(deletionDirectory == null)
			{
				return null;
			}
			
			final int  channelIndex = fileToBeDeleted.channelIndex();
			final long fileNumber   = fileToBeDeleted.number();
			
			final ADirectory deletionChannelDir = this.provideChannelDirectory(deletionDirectory, channelIndex);
			final String     deletionFileName   = this.provideStorageFileName(channelIndex, fileNumber);
			final AFile      backupFile         = deletionChannelDir.ensureFile(deletionFileName);
						
			return StorageBackupDataFile.New(backupFile, channelIndex, fileNumber);
		}
		
		@Override
		public StorageBackupDataFile provideTruncationBackupTargetFile(
			final StorageLiveDataFile fileToBeTruncated,
			final long                newLength
		)
		{
			final ADirectory truncationDirectory = this.truncationDirectory();
			if(truncationDirectory == null)
			{
				return null;
			}
			
			final int  channelIndex = fileToBeTruncated.channelIndex();
			final long fileNumber   = fileToBeTruncated.number();
			
			final ADirectory truncationChannelDir = this.provideChannelDirectory(truncationDirectory, channelIndex);
			final String     truncationFileName   = this.provideStorageFileName(channelIndex, fileNumber)
				+ "_truncated_from_" + fileToBeTruncated.size() + "_to_" + newLength
				+ "_@" + System.currentTimeMillis() + ".bak"
			;
			final AFile truncationBackupFile = truncationChannelDir.ensureFile(truncationFileName);
			
			return StorageBackupDataFile.New(truncationBackupFile, channelIndex, fileNumber);
		}

		@Override

		public <F extends StorageDataFile, P extends Consumer<F>> P collectDataFiles(
			final StorageDataFile.Creator<F> creator     ,
			final P                          collector   ,
			final int                        channelIndex
		)
		{
			return Static.collectFile(
				creator,
				collector,
				channelIndex,
				this.provideChannelDirectory(channelIndex),
				this.channelDirectoryPrefix(),
				this.storageFileSuffix()
			);
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("base directory"          ).tab().add('=').blank().add(this.baseDirectory         ).lf()
				.blank().add("deletion directory"      ).tab().add('=').blank().add(this.deletionDirectory     ).lf()
				.blank().add("channel directory prefix").tab().add('=').blank().add(this.channelDirectoryPrefix).lf()
				.blank().add("storage file prefix"     ).tab().add('=').blank().add(this.dataFilePrefix     ).lf()
				.blank().add("file suffix"             ).tab().add('=').blank().add(this.dataFileSuffix     ).lf()
				.blank().add("lockFileName"            ).tab().add('=').blank().add(this.lockFileName          )
				.toString()
			;
		}
			
	}

}
