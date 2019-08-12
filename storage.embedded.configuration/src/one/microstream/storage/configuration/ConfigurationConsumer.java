
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
import one.microstream.storage.types.EmbeddedStorageConnectionFoundation;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageHousekeepingController;


@FunctionalInterface
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
		
		protected FoundationUpdater(final EmbeddedStorageFoundation<?> storageFoundation)
		{
			super();
			
			this.storageFoundation = storageFoundation;
		}
		
		@Override
		public void accept(final Configuration configuration)
		{
			final File                            baseDir                = ensureDirectory(new File(configuration.getBaseDirectory()));
			final StorageFileProvider             fileProvider           = this.createFileProvider(configuration, baseDir);
			final StorageChannelCountProvider     channelCountProvider   = this.createChannelCountProvider(configuration);
			final StorageHousekeepingController   housekeepingController = this.createHousekeepingController(configuration);
			final StorageDataFileEvaluator        dataFileEvaluator      = this.createDataFileEvaluator(configuration);
			final StorageEntityCacheEvaluator     entityCacheEvaluator   = this.createEntityCacheEvaluator(configuration);
			
			final StorageConfiguration.Builder<?> configBuilder = Storage.ConfigurationBuilder()
				.setStorageFileProvider   (fileProvider          )
				.setChannelCountProvider  (channelCountProvider  )
				.setHousekeepingController(housekeepingController)
				.setFileEvaluator         (dataFileEvaluator     )
				.setEntityCacheEvaluator  (entityCacheEvaluator  );
			
			String backupDirectory;
			if(!isEmpty(backupDirectory = configuration.getBackupDirectory()))
			{
				configBuilder.setBackupSetup(Storage.BackupSetup(backupDirectory));
			}
			
			this.storageFoundation.setConfiguration(configBuilder.createConfiguration());

			final EmbeddedStorageConnectionFoundation<?> connectionFoundation      = this.storageFoundation.getConnectionFoundation();
			final PersistenceTypeDictionaryFileHandler   typeDictionaryFileHandler = this.createTypeDictionaryFileHandler(configuration, baseDir);
			final PersistenceTypeIdProvider              typeIdProvider            = this.createTypeIdProvider(configuration, baseDir);
			final PersistenceObjectIdProvider            objectIdProvider          = this.createObjectIdProvider(configuration, baseDir);
			
			if(typeDictionaryFileHandler != null)
			{
				connectionFoundation.setTypeDictionaryIoHandler(typeDictionaryFileHandler);
			}
			if(typeIdProvider != null)
			{
				connectionFoundation.setTypeIdProvider(typeIdProvider);
			}
			if(objectIdProvider != null)
			{
				connectionFoundation.setObjectIdProvider(objectIdProvider);
			}
		}
		
		protected StorageFileProvider createFileProvider(final Configuration configuration, final File baseDir)
		{
			return Storage.FileProviderBuilder()
				.setBaseDirectory         (baseDir      .getAbsolutePath()          )
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

		protected PersistenceTypeDictionaryFileHandler createTypeDictionaryFileHandler(final Configuration configuration, final File baseDir)
		{
			final String typeDictionaryFilename = configuration.getTypeDictionaryFilename();
			return typeDictionaryFilename == null
				? null
				: PersistenceTypeDictionaryFileHandler.New(
					new File(baseDir, typeDictionaryFilename)
				);
		}

		protected PersistenceTypeIdProvider createTypeIdProvider(final Configuration configuration, final File baseDir)
		{
			final String typeIdFilename = configuration.getTypeIdFilename();
			return typeIdFilename == null
				? null
				: FileTypeIdStrategy
					.New(baseDir, typeIdFilename)
					.createTypeIdProvider();
		}

		protected PersistenceObjectIdProvider createObjectIdProvider(final Configuration configuration, final File baseDir)
		{
			final String objectIdFilename = configuration.getObjectIdFilename();
			return objectIdFilename == null
				? null
				: FileObjectIdStrategy
					.New(baseDir, objectIdFilename)
					.createObjectIdProvider();
		}
	}
}
