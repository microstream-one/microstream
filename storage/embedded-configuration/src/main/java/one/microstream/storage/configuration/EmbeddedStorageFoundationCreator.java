
package one.microstream.storage.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageFoundationCreatorConfigurationBased;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.storage.types.StorageHousekeepingController;
import one.microstream.storage.types.StorageLiveFileProvider;

/**
 * 
 * @deprecated replaced by {@link EmbeddedStorageFoundationCreatorConfigurationBased}, will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
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

			Optional.ofNullable(configuration.getBackupDirectory())
				.filter(backupDirectory -> !XChars.isEmpty(backupDirectory))
				.ifPresent(backupDirectory -> configBuilder.setBackupSetup(
					Storage.BackupSetup(backupDirectory)
				));

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
			
			final StorageLiveFileProvider.Builder<?> builder = Storage.FileProviderBuilder(fileSystem)
				.setDirectory(fileSystem.ensureDirectory(baseDirectory))
				.setFileNameProvider(fileNameProvider)
			;
			
			Optional.ofNullable(configuration.getDeletionDirectory())
				.filter(deletionDirectory -> !XChars.isEmpty(deletionDirectory))
				.ifPresent(deletionDirectory -> builder.setDeletionDirectory(
					fileSystem.ensureDirectory(Paths.get(deletionDirectory))
				));
			
			Optional.ofNullable(configuration.getTruncationDirectory())
				.filter(truncationDirectory -> !XChars.isEmpty(truncationDirectory))
				.ifPresent(truncationDirectory -> builder.setTruncationDirectory(
					fileSystem.ensureDirectory(Paths.get(truncationDirectory))
				));
			
			return builder.createFileProvider();
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
