
package one.microstream.storage.configuration;

import static one.microstream.chars.XChars.isEmpty;

import java.nio.file.Path;

import one.microstream.io.XPaths;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageHousekeepingController;


@FunctionalInterface
public interface EmbeddedStorageFoundationCreator
{
	public EmbeddedStorageFoundation<?> createFoundation(Configuration configuration);
	
	public static EmbeddedStorageFoundationCreator New()
	{
		return new Default();
	}
	
	public static class Default implements EmbeddedStorageFoundationCreator
	{
		protected Default()
		{
			super();
		}
		
		@Override
		public EmbeddedStorageFoundation<?> createFoundation(final Configuration configuration)
		{
			final Path                          baseDirectory          = XPaths.ensureDirectoryUnchecked(XPaths.Path(configuration.getBaseDirectory()));
			final StorageFileProvider           fileProvider           = this.createFileProvider(configuration, baseDirectory);
			final StorageChannelCountProvider   channelCountProvider   = this.createChannelCountProvider(configuration);
			final StorageHousekeepingController housekeepingController = this.createHousekeepingController(configuration);
			final StorageDataFileEvaluator      dataFileEvaluator      = this.createDataFileEvaluator(configuration);
			final StorageEntityCacheEvaluator   entityCacheEvaluator   = this.createEntityCacheEvaluator(configuration);
			
			final StorageConfiguration.Builder<?> configBuilder = Storage.ConfigurationBuilder()
				.setStorageFileProvider   (fileProvider          )
				.setChannelCountProvider  (channelCountProvider  )
				.setHousekeepingController(housekeepingController)
				.setDataFileEvaluator     (dataFileEvaluator     )
				.setEntityCacheEvaluator  (entityCacheEvaluator  );
			
			String backupDirectory;
			if(!isEmpty(backupDirectory = configuration.getBackupDirectory()))
			{
				configBuilder.setBackupSetup(Storage.BackupSetup(backupDirectory));
			}
			
			final EmbeddedStorageFoundation<?> storageFoundation = EmbeddedStorage.Foundation(configBuilder.createConfiguration());

			final String typeDictionaryFilename = configuration.getTypeDictionaryFilename();
			if(typeDictionaryFilename != null)
			{
				storageFoundation.getConnectionFoundation().setTypeDictionaryIoHandler(
					PersistenceTypeDictionaryFileHandler.New(
						XPaths.Path(baseDirectory, typeDictionaryFilename)
					)
				);
			}
			
			return storageFoundation;
		}
		
		protected StorageFileProvider createFileProvider(
			final Configuration configuration,
			final Path          baseDirectory
		)
		{
			return Storage.FileProviderBuilder()
				.setBaseDirectory         (baseDirectory.getAbsolutePath()          )
				.setDeletionDirectory     (configuration.getDeletionDirectory()     )
				.setTruncationDirectory   (configuration.getTruncationDirectory()   )
				.setChannelDirectoryPrefix(configuration.getChannelDirectoryPrefix())
				.setStorageFilePrefix     (configuration.getDataFilePrefix()        )
				.setStorageFileSuffix     (configuration.getDataFileSuffix()        )
				.setTransactionsFilePrefix(configuration.getTransactionFilePrefix() )
				.setTransactionsFileSuffix(configuration.getTransactionFileSuffix() )
				.setTypeDictionaryFileName(configuration.getTypeDictionaryFilename())
				.createFileProvider();
		}
		
		protected StorageChannelCountProvider createChannelCountProvider(final Configuration configuration)
		{
			return Storage.ChannelCountProvider(configuration.getChannelCount());
		}
		
		protected StorageHousekeepingController createHousekeepingController(final Configuration configuration)
		{
			return Storage.HousekeepingController(
				configuration.getHouseKeepingInterval      (),
				configuration.getHouseKeepingNanoTimeBudget()
			);
		}
		
		protected StorageDataFileEvaluator createDataFileEvaluator(final Configuration configuration)
		{
			return Storage.DataFileEvaluator(
				configuration.getDataFileMinSize      (),
				configuration.getDataFileMaxSize      (),
				configuration.getDataFileDissolveRatio()
			);
		}
		
		protected StorageEntityCacheEvaluator createEntityCacheEvaluator(final Configuration configuration)
		{
			return Storage.EntityCacheEvaluator(
				configuration.getEntityCacheTimeout  (),
				configuration.getEntityCacheThreshold()
			);
		}

		protected PersistenceTypeDictionaryFileHandler createTypeDictionaryFileHandler(
			final Configuration configuration,
			final Path          baseDirectory
		)
		{
			final String typeDictionaryFilename = configuration.getTypeDictionaryFilename();
			return typeDictionaryFilename == null
				? null
				: PersistenceTypeDictionaryFileHandler.New(
					XPaths.Path(baseDirectory, typeDictionaryFilename)
				);
		}
	}
}
