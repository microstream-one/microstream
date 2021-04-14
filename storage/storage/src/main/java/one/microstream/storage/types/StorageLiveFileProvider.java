package one.microstream.storage.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AFileSystem;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;

public interface StorageLiveFileProvider extends StorageFileProvider
{
	/**
	 * Returns a String that uniquely identifies the storage location.
	 * 
	 * @return a String that uniquely identifies the storage location.
	 */
	public String getStorageLocationIdentifier();
		
	public AFile provideDataFile(int channelIndex, long fileNumber);

	public AFile provideTransactionsFile(int channelIndex);
	
	public AFile provideLockFile();
	
	
	
	public interface Defaults
	{
		public static String defaultStorageDirectory()
		{
			return "storage";
		}
		
	}

	
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageLiveFileProvider} instance with default values
	 * provided by {@link StorageLiveFileProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLiveFileProvider.Builder}.
	 * 
	 * @return {@linkDoc StorageLiveFileProvider#New(ADirectory)@return}
	 * 
	 * @see StorageLiveFileProvider#New(ADirectory)
	 * @see StorageLiveFileProvider.Builder
	 * @see StorageLiveFileProvider.Defaults
	 */
	public static StorageLiveFileProvider New()
	{
		return Storage.FileProviderBuilder()
			.createFileProvider()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageLiveFileProvider} instance with the passed file
	 * as the storage directory and defaults provided by {@link StorageLiveFileProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLiveFileProvider.Builder}.
	 * 
	 * @param storageDirectory the directory where the storage will be located.
	 * 
	 * @return a new {@link StorageLiveFileProvider} instance.
	 * 
	 * @see StorageLiveFileProvider#New()
	 * @see StorageLiveFileProvider.Builder
	 * @see StorageLiveFileProvider.Defaults
	 */
	public static StorageLiveFileProvider New(final ADirectory storageDirectory)
	{
		return Storage.FileProviderBuilder(storageDirectory.fileSystem())
			.setDirectory(storageDirectory)
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
	public static StorageLiveFileProvider.Default New(
		final ADirectory                                   baseDirectory      ,
		final ADirectory                                   deletionDirectory  ,
		final ADirectory                                   truncationDirectory,
		final StorageDirectoryStructureProvider            structureProvider  ,
		final StorageFileNameProvider                      fileNameProvider   ,
		final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator
	)
	{
		return new StorageLiveFileProvider.Default(
			notNull(baseDirectory)      , // base directory must at least be a relative directory name.
			mayNull(deletionDirectory)  ,
			mayNull(truncationDirectory),
			notNull(structureProvider)  ,
			notNull(fileNameProvider)   ,
			notNull(fileHandlerCreator)
		);
	}
	
	public final class Default
	extends StorageFileProvider.Abstract
	implements StorageLiveFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ADirectory                                   baseDirectory      ,
			final ADirectory                                   deletionDirectory  ,
			final ADirectory                                   truncationDirectory,
			final StorageDirectoryStructureProvider            structureProvider  ,
			final StorageFileNameProvider                      fileNameProvider   ,
			final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator
		)
		{
			super(
				baseDirectory,
				deletionDirectory,
				truncationDirectory,
				structureProvider,
				fileNameProvider,
				fileHandlerCreator
			);
		}
		


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public String getStorageLocationIdentifier()
		{
			return this.baseDirectory().toPathString();
		}
					
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageLiveFileProvider.Builder} instance
	 * with the default file system.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLiveFileProvider.Builder}.
	 * 
	 * @see Storage#DefaultFileSystem()
	 * @return a new {@link StorageLiveFileProvider.Builder} instance.
	 */
	public static StorageLiveFileProvider.Builder<?> Builder()
	{
		return Builder(Storage.DefaultFileSystem());
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageLiveFileProvider.Builder} instance.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLiveFileProvider.Builder}.
	 * 
	 * @return a new {@link StorageLiveFileProvider.Builder} instance.
	 */
	public static StorageLiveFileProvider.Builder<?> Builder(final AFileSystem fileSystem)
	{
		return new StorageLiveFileProvider.Builder.Default(
			notNull(fileSystem)
		);
	}
	
	public interface Builder<B extends Builder<?>> extends StorageFileProvider.Builder<B>
	{
		@Override
		public StorageLiveFileProvider createFileProvider();
		
		
		
		public class Default
		extends StorageFileProvider.Builder.Abstract<StorageLiveFileProvider.Builder.Default>
		implements StorageLiveFileProvider.Builder<StorageLiveFileProvider.Builder.Default>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			Default(final AFileSystem fileSystem)
			{
				super(fileSystem);
			}
			
			
			@Override
			protected ADirectory getBaseDirectory()
			{
				if(this.directory() != null)
				{
					return this.directory();
				}
				
				// note: relative root directory inside the current working directory
				return this.fileSystem().ensureRoot(Defaults.defaultStorageDirectory());
			}
		
			@Override
			public StorageLiveFileProvider createFileProvider()
			{
				return StorageLiveFileProvider.New(
					this.getBaseDirectory(),
					this.getDeletionDirectory(),
					this.getTruncationDirectory(),
					this.getDirectoryStructureProvider(),
					this.getFileNameProvider(),
					this.getTypeDictionaryFileHandler()
				);
			}
			
		}
		
	}

}
