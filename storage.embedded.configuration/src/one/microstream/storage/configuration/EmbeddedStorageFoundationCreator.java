
package one.microstream.storage.configuration;

import static one.microstream.chars.XChars.isEmpty;

import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.storage.types.StorageHousekeepingController;
import one.microstream.storage.types.StorageLiveFileProvider;

/**
 * Function to create an {@link EmbeddedStorageFoundation} based on a {@link Configuration}.
 */
@FunctionalInterface
public interface EmbeddedStorageFoundationCreator
{
	public EmbeddedStorageFoundation<?> createFoundation(Configuration configuration);


	public static EmbeddedStorageFoundationCreator New()
	{
		return new EmbeddedStorageFoundationCreator.Default();
	}


	public static class Default implements EmbeddedStorageFoundationCreator
	{
		Default()
		{
			super();
		}

		@Override
		public EmbeddedStorageFoundation<?> createFoundation(
			final Configuration configuration
		)
		{
			final Path baseDirectory = XIO.unchecked.ensureDirectory(
				XIO.Path(configuration.getBaseDirectory())
			);

			final StorageConfiguration.Builder<?> configBuilder = Storage.ConfigurationBuilder()
				.setStorageFileProvider   (this.createFileProvider(configuration, baseDirectory))
				.setChannelCountProvider  (this.createChannelCountProvider(configuration)       )
				.setHousekeepingController(this.createHousekeepingController(configuration)     )
				.setDataFileEvaluator     (this.createDataFileEvaluator(configuration)          )
				.setEntityCacheEvaluator  (this.createEntityCacheEvaluator(configuration)       )
			;

			String backupDirectory;
			if(!isEmpty(backupDirectory = configuration.getBackupDirectory()))
			{
				configBuilder.setBackupSetup(Storage.BackupSetup(backupDirectory));
			}

			return EmbeddedStorage.Foundation(
				configBuilder.createConfiguration()
			);
		}

		protected StorageLiveFileProvider createFileProvider(
			final Configuration configuration,
			final Path          baseDirectory
		)
		{
			final StorageFileNameProvider fileNameProvider = StorageFileNameProvider.New(
				configuration.getChannelDirectoryPrefix(),
				configuration.getDataFilePrefix        (),
				configuration.getDataFileSuffix        (),
				configuration.getTransactionFilePrefix (),
				configuration.getTransactionFileSuffix (),
				configuration.getRescuedFileSuffix     (),
				configuration.getTypeDictionaryFilename(),
				configuration.getLockFileName          ()
			);

			final NioFileSystem fileSystem = NioFileSystem.New();
			return Storage
				.FileProviderBuilder   (fileSystem                                                                   )
				.setDirectory          (fileSystem.ensureDirectory(baseDirectory)                                    )
				.setDeletionDirectory  (fileSystem.ensureDirectory(Paths.get(configuration.getDeletionDirectory()))  )
				.setTruncationDirectory(fileSystem.ensureDirectory(Paths.get(configuration.getTruncationDirectory())))
				.setFileNameProvider   (fileNameProvider                                                             )
				.createFileProvider    ()
			;
		}

		protected StorageChannelCountProvider createChannelCountProvider(
			final Configuration configuration
		)
		{
			return Storage.ChannelCountProvider(
				configuration.getChannelCount()
			);
		}

		protected StorageHousekeepingController createHousekeepingController(
			final Configuration configuration
		)
		{
			return Storage.HousekeepingController(
				configuration.getHousekeepingIntervalMs  (),
				configuration.getHousekeepingTimeBudgetNs()
			);
		}

		protected StorageDataFileEvaluator createDataFileEvaluator(
			final Configuration configuration
		)
		{
			return Storage.DataFileEvaluator(
				configuration.getDataFileMinimumSize    (),
				configuration.getDataFileMaximumSize    (),
				configuration.getDataFileMinimumUseRatio(),
				configuration.getDataFileCleanupHeadFile()
			);
		}

		protected StorageEntityCacheEvaluator createEntityCacheEvaluator(
			final Configuration configuration
		)
		{
			return Storage.EntityCacheEvaluator(
				configuration.getEntityCacheTimeoutMs(),
				configuration.getEntityCacheThreshold()
			);
		}

	}

}
