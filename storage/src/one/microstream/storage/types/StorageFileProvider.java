package one.microstream.storage.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.File;
import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.files.XFiles;
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
	
	@Override
	public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
		PersistenceTypeDictionaryStorer writeListener
	);
	
	public StorageNumberedFile provideDataFile(int channelIndex, long fileNumber);

	public StorageNumberedFile provideTransactionsFile(int channelIndex);
	
	public StorageLockedFile provideLockFile();
	
	public StorageNumberedFile provideDeletionTargetFile(StorageNumberedFile fileToBeDeleted);
	
	public StorageNumberedFile provideTruncationBackupTargetFile(StorageNumberedFile fileToBeTruncated, long newLength);

	
	
	public <P extends Consumer<StorageNumberedFile>> P collectDataFiles(P collector, int channelIndex);
	
	
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
			return ".dat";
		}

		public static String defaultTransactionFilePrefix()
		{
			return "transactions_";
		}
		
		public static String defaultTransactionFileSuffix()
		{
			return ".sft"; // "storage file transactions"
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
		public static final <C extends Consumer<? super StorageNumberedFile>>
		C collectFile(
			final C      collector       ,
			final int    channelIndex    ,
			final File   storageDirectory,
			final String fileBaseName    ,
			final String suffix
		)
		{
			final File[] files = storageDirectory.listFiles();
			if(files != null)
			{
				for(final File file : files)
				{
					internalCollectFile(collector, channelIndex, file, fileBaseName, suffix);
				}
			}

			return collector;
		}

		private static final void internalCollectFile(
			final Consumer<? super StorageNumberedFile> collector   ,
			final int                                   hashIndex   ,
			final File                                  file        ,
			final String                                fileBaseName,
			final String                                suffix
		)
		{
			if(file.isDirectory())
			{
				return;
			}

			final String filename = file.getName();
			if(!filename.startsWith(fileBaseName))
			{
				return;
			}
			if(!filename.endsWith(suffix))
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
			collector.accept(StorageNumberedFile.New(hashIndex, fileNumber, file));
		}



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
		return new StorageFileProvider.Builder.Default<>();
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public String baseDirectory();

		public B setBaseDirectory(String baseDirectory);

		public String deletionDirectory();

		public B setDeletionDirectory(String deletionDirectory);

		public String truncationDirectory();

		public B setTruncationDirectory(String truncationDirectory);

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
			
			private String
				baseDirectory         ,
				deletionDirectory     ,
				truncationDirectory   ,
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
			
			Default()
			{
				super();
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
			public String baseDirectory()
			{
				return this.baseDirectory;
			}

			@Override
			public B setBaseDirectory(final String baseDirectory)
			{
				this.baseDirectory = baseDirectory;
				return this.$();
			}

			@Override
			public String deletionDirectory()
			{
				return this.deletionDirectory;
			}

			@Override
			public B setDeletionDirectory(final String deletionDirectory)
			{
				this.deletionDirectory = deletionDirectory;
				return this.$();
			}

			@Override
			public String truncationDirectory()
			{
				return this.truncationDirectory;
			}

			@Override
			public B setTruncationDirectory(final String truncationDirectory)
			{
				this.truncationDirectory = truncationDirectory;
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
			
			@Override
			public StorageFileProvider createFileProvider()
			{
				return StorageFileProvider.New(
					coalesce(this.baseDirectory         , Defaults.defaultStorageDirectory()                ),
					coalesce(this.deletionDirectory     , Defaults.defaultDeletionDirectory()               ),
					coalesce(this.truncationDirectory   , Defaults.defaultTruncationDirectory()             ),
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
	 * 
	 * @param baseDirectory
	 * @param deletionDirectory
	 * @param truncationDirectory
	 * @param channelDirectoryPrefix
	 * @param storageFilePrefix
	 * @param storageFileSuffix
	 * @param transactionsFilePrefix
	 * @param transactionsFileSuffix
	 * @param typeDictionaryFileName
	 * @param lockFileName
	 * @param fileHandlerCreator
	 * @return
	 */
	public static StorageFileProvider.Default New(
		final String                                       baseDirectory         ,
		final String                                       deletionDirectory     ,
		final String                                       truncationDirectory   ,
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
			notNull(baseDirectory)         , // base directory must at least a relative directory name.
			mayNull(deletionDirectory)     , // null (no directory) means actually delete files
			mayNull(truncationDirectory)   , // null (no directory) means actually delete files
			notNull(channelDirectoryPrefix),
			notNull(storageFilePrefix)     ,
			notNull(storageFileSuffix)     ,
			notNull(transactionsFilePrefix),
			notNull(transactionsFileSuffix),
			notNull(typeDictionaryFileName),
			notNull(lockFileName)          ,
			notNull(fileHandlerCreator)
		);
	}


	
	public final class Default implements StorageFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String
			baseDirectory         ,
			deletionDirectory     ,
			truncationDirectory   ,
			channelDirectoryPrefix,
			storageFilePrefix     ,
			storageFileSuffix     ,
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
			final String                                       baseDirectory         ,
			final String                                       deletionDirectory     ,
			final String                                       truncationDirectory   ,
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
			super();
			this.baseDirectory          = baseDirectory         ;
			this.deletionDirectory      = deletionDirectory     ;
			this.truncationDirectory    = truncationDirectory   ;
			this.channelDirectoryPrefix = channelDirectoryPrefix;
			this.storageFilePrefix      = storageFilePrefix     ;
			this.storageFileSuffix      = storageFileSuffix     ;
			this.transactionsFilePrefix = transactionsFilePrefix;
			this.transactionsFileSuffix = transactionsFileSuffix;
			this.typeDictionaryFileName = typeDictionaryFileName;
			this.lockFileName           = lockFileName          ;
			this.fileHandlerCreator     = fileHandlerCreator;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public String baseDirectory()
		{
			return this.baseDirectory;
		}

		public String deletionDirectory()
		{
			return this.deletionDirectory;
		}

		public String truncationDirectory()
		{
			return this.truncationDirectory;
		}
		
		public String channelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}
		
		public String storageFileSuffix()
		{
			return this.storageFileSuffix;
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
			return this.storageFilePrefix + channelIndex + '_' + fileNumber + this.storageFileSuffix;
		}

		public final String provideTransactionFileName(final int channelIndex)
		{
			return this.transactionsFilePrefix + channelIndex + this.transactionsFileSuffix;
		}
		
		@Override
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			/* (04.03.2019 TM)TODO: forced delegating API is not a clean solution.
			 * This is only a temporary solution. See the task containing "PersistenceDataFile".
			 */
			final File directory = new File(this.baseDirectory());
			XFiles.ensureDirectory(directory);
			
			final File file = new File(directory, this.typeDictionaryFileName());
			return this.fileHandlerCreator.createTypeDictionaryIoHandler(file, writeListener);
		}

		public final File provideChannelDirectory(final String parentDirectory, final int hashIndex)
		{
			return XFiles.ensureDirectory(
				new File(parentDirectory, this.channelDirectoryPrefix() + hashIndex)
			);
		}

		public File provideChannelDirectory(final int channelIndex)
		{
			return this.provideChannelDirectory(this.baseDirectory(), channelIndex);
		}
		
		@Override
		public final StorageNumberedFile provideDataFile(final int channelIndex, final long fileNumber)
		{
			final File file = new File(
				this.provideChannelDirectory(channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber)
			);
			
			return StorageNumberedFile.New(channelIndex, fileNumber, file);
		}

		@Override
		public StorageNumberedFile provideTransactionsFile(final int channelIndex)
		{
			final File file = new File(
				this.provideChannelDirectory(channelIndex),
				this.provideTransactionFileName(channelIndex)
			);

			return StorageNumberedFile.New(channelIndex, Storage.transactionsFileNumber(), file);
		}
		
		@Override
		public StorageLockedFile provideLockFile()
		{
			final File lockFile = new File(this.baseDirectory(), this.lockFileName());
			
			return StorageLockedFile.openLockedFile(lockFile);
		}

		@Override
		public StorageNumberedFile provideDeletionTargetFile(final StorageNumberedFile fileToBeDeleted)
		{
			final String deletionDirectory = this.deletionDirectory();
			if(deletionDirectory == null)
			{
				return null;
			}
			
			final int  channelIndex = fileToBeDeleted.channelIndex();
			final long fileNumber   = fileToBeDeleted.number();
			
			final File file = new File(
				this.provideChannelDirectory(deletionDirectory, channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber)
			);
			
			return StorageNumberedFile.New(channelIndex, fileNumber, file);
		}
		
		@Override
		public StorageNumberedFile provideTruncationBackupTargetFile(
			final StorageNumberedFile fileToBeTruncated,
			final long                newLength
		)
		{
			final String truncationDirectory = this.truncationDirectory();
			if(truncationDirectory == null)
			{
				return null;
			}
			
			final int  channelIndex = fileToBeTruncated.channelIndex();
			final long fileNumber   = fileToBeTruncated.number();
			
			final File file = new File(
				this.provideChannelDirectory(truncationDirectory, channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber)
				+ "_truncated_from_" + fileToBeTruncated.length() + "_to_" + newLength
				+ "_@" + System.currentTimeMillis() + ".bak"
			);
			
			return StorageNumberedFile.New(channelIndex, fileNumber, file);
		}

		@Override
		public <P extends Consumer<StorageNumberedFile>> P collectDataFiles(
			final P   collector   ,
			final int channelIndex
		)
		{
			return Static.collectFile(
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
				.blank().add("storage file prefix"     ).tab().add('=').blank().add(this.storageFilePrefix     ).lf()
				.blank().add("file suffix"             ).tab().add('=').blank().add(this.storageFileSuffix     ).lf()
				.blank().add("lockFileName"            ).tab().add('=').blank().add(this.lockFileName          )
				.toString()
			;
		}
			
	}

}
