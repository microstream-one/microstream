package one.microstream.storage.configuration;

import static one.microstream.chars.XChars.isEmpty;
import static one.microstream.files.XFiles.ensureDirectory;

import java.io.File;
import java.util.function.Consumer;

import one.microstream.persistence.internal.FileObjectIdStrategy;
import one.microstream.persistence.internal.FileTypeIdStrategy;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceTypeIdProvider;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageHousekeepingController;

public interface ConfigurationConsumer extends Consumer<Configuration>
{
	@Override
	public void accept(Configuration configuration);
	
	
	
	public static ConfigurationConsumer FoundationUpdater(final EmbeddedStorageFoundation<?> storageFoundation)
	{
		return new FoundationUpdater(storageFoundation);
	}
	
	
	public static class FoundationUpdater implements ConfigurationConsumer
	{
		private final EmbeddedStorageFoundation<?> storageFoundation;

		public FoundationUpdater(final EmbeddedStorageFoundation<?> storageFoundation)
		{
			super();
			this.storageFoundation = storageFoundation;
		}
		
		@Override
		public void accept(final Configuration configuration)
		{
			final File baseDir = ensureDirectory(new File(configuration.getBaseDirectory()));
									
			final StorageFileProvider fileProvider = Storage.FileProviderBuilder()
					.setBaseDirectory(baseDir.getAbsolutePath())
					.setDeletionDirectory(configuration.getDeletionDirectory())
					.setTruncationDirectory(configuration.getTruncationDirectory())
					.setChannelDirectoryPrefix(configuration.getChannelDirectoryPrefix())
					.setStorageFilePrefix(configuration.getDataFilePrefix())
					.setStorageFileSuffix(configuration.getDataFileSuffix())
					.setTransactionsFilePrefix(configuration.getTransactionFilePrefix())
					.setTransactionsFileSuffix(configuration.getTransactionFileSuffix())
					.setTypeDictionaryFileName(configuration.getTypeDictionaryFilename())
					.createFileProvider();
			
			final StorageChannelCountProvider channelCountProvider = Storage.ChannelCountProvider(
					configuration.getChannelCount());
			
			final StorageHousekeepingController housekeepingController = Storage.HousekeepingController(
					configuration.getHouseKeepingInterval(),
					configuration.getHouseKeepingNanoTimeBudget());
			
			final StorageDataFileEvaluator dataFileEvaluator = Storage.DataFileEvaluator(
					configuration.getDataFileMinSize(),
					configuration.getDataFileMaxSize(),
					configuration.getDataFileDissolveRatio());
			
			final StorageEntityCacheEvaluator entityCacheEvaluator = Storage.EntityCacheEvaluator(
					configuration.getEntityCacheTimeout(),
					configuration.getEntityCacheThreshold());
			
			final StorageConfiguration.Builder<?> storageConfigurationBuilder = Storage.ConfigurationBuilder()
					.setStorageFileProvider(fileProvider)
					.setChannelCountProvider(channelCountProvider)
					.setHousekeepingController(housekeepingController)
					.setFileEvaluator(dataFileEvaluator)
					.setEntityCacheEvaluator(entityCacheEvaluator);
			
			String backupDirectory;
			if(!isEmpty(backupDirectory = configuration.getBackupDirectory()))
			{
				storageConfigurationBuilder.setBackupSetup(Storage.BackupSetup(backupDirectory));
			}
			
			final StorageConfiguration storageConfiguration = storageConfigurationBuilder.createConfiguration();
			
			final PersistenceTypeDictionaryFileHandler typeDictionaryFileHandler = PersistenceTypeDictionaryFileHandler
					.New(new File(baseDir,configuration.getTypeDictionaryFilename()));
			
			final PersistenceTypeIdProvider typeIdProvider = FileTypeIdStrategy
					.New(baseDir, configuration.getTypeIdFilename())
					.createTypeIdProvider();
						
			final PersistenceObjectIdProvider objectIdProvider = FileObjectIdStrategy
					.New(baseDir, configuration.getObjectIdFilename())
					.createObjectIdProvider();
			
			this.storageFoundation.setConfiguration(storageConfiguration)
				.getConnectionFoundation()
					.setTypeDictionaryIoHandler(typeDictionaryFileHandler)
					.setTypeIdProvider(typeIdProvider)
					.setObjectIdProvider(objectIdProvider);
		}
	}
}
