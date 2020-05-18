
package one.microstream.storage.configuration;

import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageHousekeepingController;

/**
 * Function to create an {@link Configuration} based on a {@link EmbeddedStorageFoundation}.
 *
 * @since 3.1
 */
@FunctionalInterface
public interface ConfigurationCreator
{
	public Configuration createConfiguration(EmbeddedStorageFoundation<?> foundation);


	public static ConfigurationCreator New()
	{
		return new ConfigurationCreator.Default();
	}


	public static class Default implements ConfigurationCreator
	{
		Default()
		{
			super();
		}

		@Override
		public Configuration createConfiguration(final EmbeddedStorageFoundation<?> foundation)
		{
			final Configuration        configuration        = Configuration.Default();
			final StorageConfiguration storageConfiguration = foundation.getConfiguration();

			this.updateConfiguration(configuration, storageConfiguration.fileProvider          ());
			this.updateConfiguration(configuration, storageConfiguration.channelCountProvider  ());
			this.updateConfiguration(configuration, storageConfiguration.housekeepingController());
			this.updateConfiguration(configuration, storageConfiguration.dataFileEvaluator     ());
			this.updateConfiguration(configuration, storageConfiguration.entityCacheEvaluator  ());
			this.updateConfiguration(configuration, storageConfiguration.backupSetup           ());

			return configuration;
		}

		protected void updateConfiguration(
			final Configuration       configuration,
			final StorageFileProvider fileProvider
		)
		{
			if(fileProvider instanceof StorageFileProvider.Default)
			{
				final StorageFileProvider.Default defaultFileProvider =
					(StorageFileProvider.Default)fileProvider;
				configuration
					.setBaseDirectory         (defaultFileProvider.baseDirectory         ())
					.setDeletionDirectory     (defaultFileProvider.deletionDirectory     ())
					.setTruncationDirectory   (defaultFileProvider.truncationDirectory   ())
					.setChannelDirectoryPrefix(defaultFileProvider.channelDirectoryPrefix())
					.setDataFilePrefix        (defaultFileProvider.storageFilePrefix     ())
					.setDataFileSuffix        (defaultFileProvider.storageFileSuffix     ())
					.setTransactionFilePrefix (defaultFileProvider.transactionsFilePrefix())
					.setTransactionFileSuffix (defaultFileProvider.transactionsFileSuffix())
					.setTypeDictionaryFilename(defaultFileProvider.typeDictionaryFileName())
				;
			}
		}

		protected void updateConfiguration(
			final Configuration               configuration,
			final StorageChannelCountProvider channelCountProvider
		)
		{
			configuration.setChannelCount(channelCountProvider.getChannelCount());
		}

		protected void updateConfiguration(
			final Configuration                 configuration,
			final StorageHousekeepingController housekeepingController
		)
		{
			configuration
				.setHousekeepingIntervalMs  (housekeepingController.housekeepingIntervalMs  ())
				.setHousekeepingTimeBudgetNs(housekeepingController.housekeepingTimeBudgetNs())
			;
		}

		protected void updateConfiguration(
			final Configuration            configuration,
			final StorageDataFileEvaluator dataFileEvaluator
		)
		{
			configuration
				.setDataFileMinimumSize(dataFileEvaluator.fileMinimumSize())
				.setDataFileMaximumSize(dataFileEvaluator.fileMaximumSize())
			;
			if(dataFileEvaluator instanceof StorageDataFileEvaluator.Default)
			{
				final StorageDataFileEvaluator.Default defaultDataFileEvaluator =
					(StorageDataFileEvaluator.Default)dataFileEvaluator;
				configuration
					.setDataFileMinimumUseRatio(defaultDataFileEvaluator.minimumUseRatio())
					.setDataFileCleanupHeadFile(defaultDataFileEvaluator.cleanupHeadFile())
				;
			}
		}

		protected void updateConfiguration(
			final Configuration               configuration,
			final StorageEntityCacheEvaluator entityCacheEvaluator
		)
		{
			if(entityCacheEvaluator instanceof StorageEntityCacheEvaluator.Default)
			{
				final StorageEntityCacheEvaluator.Default defaultEntityCacheEvaluator =
					(StorageEntityCacheEvaluator.Default)entityCacheEvaluator;
				configuration
					.setEntityCacheTimeoutMs(defaultEntityCacheEvaluator.timeoutMs())
					.setEntityCacheThreshold(defaultEntityCacheEvaluator.threshold())
				;
			}
		}

		protected void updateConfiguration(
			final Configuration      configuration,
			final StorageBackupSetup backupSetup
		)
		{
			StorageFileProvider fileProvider;
			if(backupSetup != null && (fileProvider = backupSetup.backupFileProvider()) != null)
			{
				configuration.setBackupDirectory(fileProvider.getStorageLocationIdentifier());
			}
		}

	}

}
