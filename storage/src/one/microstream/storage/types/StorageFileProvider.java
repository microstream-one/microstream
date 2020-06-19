package one.microstream.storage.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import java.nio.file.Path;
import java.util.function.Consumer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.chars.VarString;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;

public interface StorageFileProvider extends PersistenceTypeDictionaryIoHandler.Provider
{
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
			final StorageFileNameProvider    parser
		)
		{
			storageDirectory.iterateFiles(f ->
			{
				parser.parseDataInventoryFile(fileCreator, collector, channelIndex, f);
			});
			
			return collector;
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
		
		public StorageFileNameProvider fileNameProvider();
		
		public B setFileNameProvider(StorageFileNameProvider fileNameProvider);
		
		public PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator();
		
		public B setFileHandlerCreator(PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator);
		
		public StorageFileProvider createFileProvider();
		
		
		
		public class Default<B extends Builder.Default<?>> implements StorageFileProvider.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFileSystem fileSystem;
			
			private ADirectory baseDirectory;
			
			private StorageDirectoryStructureProvider structureProvider;
			
			private StorageFileNameProvider fileNameProvider;
			
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
			
			protected ADirectory getBaseDirectory()
			{
				if(this.baseDirectory != null)
				{
					return this.baseDirectory;
				}
				
				// note: relative root directory inside the current working directory
				return this.fileSystem.ensureRoot(Defaults.defaultStorageDirectory());
			}
			
			protected StorageDirectoryStructureProvider getDirectoryStructureProvider()
			{
				// (18.06.2020 TM)TODO: priv#49: StorageDirectoryStructureProvider DEFAULT instance.
				return this.structureProvider != null
					? this.structureProvider
					: StorageDirectoryStructureProvider.New()
				;
			}
			
			protected StorageFileNameProvider getFileNameProvider()
			{
				return this.fileNameProvider != null
					? this.fileNameProvider
					: StorageFileNameProvider.Defaults.defaultFileNameProvider()
				;
			}
						
			@Override
			public StorageFileProvider createFileProvider()
			{
				return StorageFileProvider.New(
					this.getBaseDirectory(),
					coalesce(this.fileHandlerCreator, Defaults.defaultTypeDictionaryFileHandlerCreator()),
					this.getDirectoryStructureProvider(),
					this.getFileNameProvider()
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
	 * @param fileHandlerCreator may <b>not</b> be null.
	 * @param deletionDirectory may be null.
	 * @param truncationDirectory may be null.
	 */
	public static StorageFileProvider.Default New(
		final ADirectory                                   baseDirectory     ,
		final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator,
		final StorageDirectoryStructureProvider            structureProvider ,
		final StorageFileNameProvider                      fileNameProvider
	)
	{
		return new StorageFileProvider.Default(
			notNull(baseDirectory)     , // base directory must at least be a relative directory name.
			notNull(fileHandlerCreator),
			notNull(structureProvider) ,
			notNull(fileNameProvider)
		);
	}


	
	public final class Default implements StorageFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// (18.06.2020 TM)FIXME: priv#49: set structureProvider
		private final ADirectory                                   baseDirectory     ;
		private final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;
		private final StorageDirectoryStructureProvider            structureProvider ;
		private final StorageFileNameProvider                      fileNameProvider  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ADirectory                                   baseDirectory         ,
			final PersistenceTypeDictionaryFileHandler.Creator dictFileHandlerCreator,
			final StorageDirectoryStructureProvider            structureProvider     ,
			final StorageFileNameProvider                      fileNameProvider
		)
		{
			super();
			this.baseDirectory      = baseDirectory         ;
			this.fileHandlerCreator = dictFileHandlerCreator;
			this.structureProvider  = structureProvider     ;
			this.fileNameProvider   = fileNameProvider      ;
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
			directory.ensureExists();
			
			final AFile file = directory.ensureFile(this.fileNameProvider.typeDictionaryFileName());
			
			return this.fileHandlerCreator.createTypeDictionaryIoHandler(file, writeListener);
		}

		public final ADirectory provideChannelDirectory(
			final ADirectory parentDirectory,
			final int        hashIndex
		)
		{
			final String channelDirectoryName = this.fileNameProvider.provideChannelDirectoryName(hashIndex);
			final ADirectory channelDirectory = parentDirectory.ensureDirectory(channelDirectoryName);
			
			channelDirectory.ensureExists();
			
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
			final String     dataFileName     = this.fileNameProvider.provideDataFileName(channelIndex, fileNumber);
			final String     dataFileType     = this.fileNameProvider.dataFileSuffix();
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
			final String     dataFileName     = this.fileNameProvider.provideTransactionsFileName(channelIndex);
			final String     dataFileType     = this.fileNameProvider.transactionsFileSuffix();
			final AFile      file             = channelDirectory.ensureFile(dataFileName, dataFileType);
			
			return creator.createTransactionsFile(file, channelIndex);
		}
		
		@Override
		public StorageLockFile provideLockFile()
		{
			final AFile lockFile = this.baseDirectory().ensureFile(this.fileNameProvider.lockFileName());
			
			return StorageLockFile.New(lockFile);
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
				this.fileNameProvider
			);
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
