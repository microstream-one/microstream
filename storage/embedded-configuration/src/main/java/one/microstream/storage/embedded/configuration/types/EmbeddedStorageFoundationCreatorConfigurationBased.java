package one.microstream.storage.embedded.configuration.types;

/*-
 * #%L
 * microstream-storage-embedded-configuration
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Supplier;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.ByteSize;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;
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
 * Creator for a storage foundation, based on a configuration.
 * 
 * @since 05.00.00
 *
 */
public interface EmbeddedStorageFoundationCreatorConfigurationBased extends EmbeddedStorageFoundation.Creator
{
	/**
	 * Pseudo-constructor method to create a new foundation creator.
	 * @param configuration the configuration the foundation will be based on
	 * @return a new foundation creator
	 */
	public static EmbeddedStorageFoundationCreatorConfigurationBased New(
		final Configuration configuration
	)
	{
		return new EmbeddedStorageFoundationCreatorConfigurationBased.Default(
			notNull(configuration)
		);
	}
	
	public static class Default implements
	EmbeddedStorageFoundationCreatorConfigurationBased,
	EmbeddedStorageConfigurationPropertyNames
	{
		private final Configuration configuration;

		Default(
			final Configuration configuration
		)
		{
			super();
			this.configuration = configuration;
		}

		@Override
		public EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation()
		{
			try
			{
				return this.internalCreateEmbeddedStorageFoundation();
			}
			catch(final ConfigurationException e)
			{
				throw e;
			}
			catch(final Exception e)
			{
				throw new ConfigurationException(this.configuration, e);
			}
		}
		
		
		private EmbeddedStorageFoundation<?> internalCreateEmbeddedStorageFoundation()
		{
			final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
			
			final AFileSystem fileSystem = this.createFileSystem(
				STORAGE_FILESYSTEM,
				NioFileSystem::New
			);
			
			final StorageConfiguration.Builder<?> configBuilder = Storage.ConfigurationBuilder()
				.setStorageFileProvider   (this.createFileProvider(fileSystem)          )
				.setChannelCountProvider  (this.createChannelCountProvider()            )
				.setHousekeepingController(this.createHousekeepingController(foundation))
				.setDataFileEvaluator     (this.createDataFileEvaluator()               )
				.setEntityCacheEvaluator  (this.createEntityCacheEvaluator()            )
			;

			this.configuration.opt(BACKUP_DIRECTORY)
				.filter(backupDirectory -> !XChars.isEmpty(backupDirectory))
				.map(this::createDirectoryPath)
				.ifPresent(backupDirectory ->
				{
					final AFileSystem backupFileSystem = this.createFileSystem(
						BACKUP_FILESYSTEM,
						() -> fileSystem
					);
					configBuilder.setBackupSetup(Storage.BackupSetup(
						backupFileSystem.ensureDirectoryPath(backupDirectory)
					));
				})
			;

			foundation.setConfiguration(configBuilder.createConfiguration());
			
			return foundation;
		}
		
		private AFileSystem createFileSystem(
			final String                configurationKey         ,
			final Supplier<AFileSystem> defaultFileSystemSupplier
		)
		{
			final Configuration configuration = this.configuration.child(configurationKey);
			if(configuration != null)
			{
				for(final ConfigurationBasedCreator<AFileSystem> creator :
					ConfigurationBasedCreator.registeredCreators(AFileSystem.class))
				{
					final AFileSystem fileSystem = creator.create(configuration);
					if(fileSystem != null)
					{
						return fileSystem;
					}
				}
			}
			
			return defaultFileSystemSupplier.get();
		}

		private StorageLiveFileProvider createFileProvider(final AFileSystem fileSystem)
		{
			final ADirectory baseDirectory = fileSystem.ensureDirectoryPath(
				this.createDirectoryPath(
					this.configuration.opt(STORAGE_DIRECTORY)
						.orElse(StorageLiveFileProvider.Defaults.defaultStorageDirectory())
				)
			);
			
			final StorageFileNameProvider fileNameProvider = StorageFileNameProvider.New(
				this.configuration.opt(CHANNEL_DIRECTORY_PREFIX)
					.orElse(StorageFileNameProvider.Defaults.defaultChannelDirectoryPrefix()),
				this.configuration.opt(DATA_FILE_PREFIX)
					.orElse(StorageFileNameProvider.Defaults.defaultDataFilePrefix()),
				this.configuration.opt(DATA_FILE_SUFFIX)
					.orElse(StorageFileNameProvider.Defaults.defaultDataFileSuffix()),
				this.configuration.opt(TRANSACTION_FILE_PREFIX)
					.orElse(StorageFileNameProvider.Defaults.defaultTransactionsFilePrefix()),
				this.configuration.opt(TRANSACTION_FILE_SUFFIX)
					.orElse(StorageFileNameProvider.Defaults.defaultTransactionsFileSuffix()),
				this.configuration.opt(RESCUED_FILE_SUFFIX)
					.orElse(StorageFileNameProvider.Defaults.defaultRescuedFileSuffix()),
				this.configuration.opt(TYPE_DICTIONARY_FILE_NAME)
					.orElse(StorageFileNameProvider.Defaults.defaultTypeDictionaryFileName()),
				this.configuration.opt(LOCK_FILE_NAME)
					.orElse(StorageFileNameProvider.Defaults.defaultLockFileName())
			);
			
			final StorageLiveFileProvider.Builder<?> builder = Storage.FileProviderBuilder(fileSystem)
				.setDirectory(baseDirectory)
				.setFileNameProvider(fileNameProvider)
			;
			
			this.configuration.opt(DELETION_DIRECTORY)
				.filter(deletionDirectory -> !XChars.isEmpty(deletionDirectory))
				.ifPresent(deletionDirectory -> builder.setDeletionDirectory(
					fileSystem.ensureDirectoryPath(deletionDirectory)
				))
			;
			
			this.configuration.opt(TRUNCATION_DIRECTORY)
				.filter(truncationDirectory -> !XChars.isEmpty(truncationDirectory))
				.ifPresent(truncationDirectory -> builder.setTruncationDirectory(
					fileSystem.ensureDirectoryPath(truncationDirectory)
				))
			;
			
			return builder.createFileProvider();
		}

		private StorageChannelCountProvider createChannelCountProvider()
		{
			return Storage.ChannelCountProvider(
				this.configuration.optInteger(CHANNEL_COUNT)
					.orElse(StorageChannelCountProvider.Defaults.defaultChannelCount())
			);
		}

		private StorageHousekeepingController createHousekeepingController(final EmbeddedStorageFoundation<?> foundation)
		{
			StorageHousekeepingController controller = Storage.HousekeepingController(
				this.configuration.opt(HOUSEKEEPING_INTERVAL, Duration.class)
					.map(Duration::toMillis)
					.orElse(StorageHousekeepingController.Defaults.defaultHousekeepingIntervalMs()),
				this.configuration.opt(HOUSEKEEPING_TIME_BUDGET, Duration.class)
					.map(Duration::toNanos)
					.orElse(StorageHousekeepingController.Defaults.defaultHousekeepingTimeBudgetNs())
			);
			
			if(this.configuration.optBoolean(HOUSEKEEPING_ADAPTIVE).orElse(false))
			{
				controller = StorageHousekeepingController.Adaptive(
					controller,
					this.configuration.opt(HOUSEKEEPING_INCREASE_THRESHOLD, Duration.class)
						.map(Duration::toMillis)
						.orElse(StorageHousekeepingController.Adaptive.Defaults.defaultAdaptiveHousekeepingIncreaseThresholdMs()),
					this.configuration.opt(HOUSEKEEPING_INCREASE_AMOUNT, Duration.class)
						.map(Duration::toNanos)
						.orElse(StorageHousekeepingController.Adaptive.Defaults.defaultAdaptiveHousekeepingIncreaseAmountNs()),
					this.configuration.opt(HOUSEKEEPING_MAXIMUM_TIME_BUDGET, Duration.class)
						.map(Duration::toNanos)
						.orElse(StorageHousekeepingController.Adaptive.Defaults.defaultAdaptiveHousekeepingMaximumTimeBudgetNs()),
					foundation
				);
			}
			
			return controller;
		}

		private StorageDataFileEvaluator createDataFileEvaluator()
		{
			return Storage.DataFileEvaluator(
				this.configuration.opt(DATA_FILE_MINIMUM_SIZE, ByteSize.class)
					.map(byteSize -> (int)byteSize.bytes())
					.orElse(StorageDataFileEvaluator.Defaults.defaultFileMinimumSize()),
				this.configuration.opt(DATA_FILE_MAXIMUM_SIZE, ByteSize.class)
					.map(byteSize -> (int)byteSize.bytes())
					.orElse(StorageDataFileEvaluator.Defaults.defaultFileMaximumSize()),
				this.configuration.optDouble(DATA_FILE_MINIMUM_USE_RATIO)
					.orElse(StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio()),
				this.configuration.optBoolean(DATA_FILE_CLEANUP_HEAD_FILE)
					.orElse(StorageDataFileEvaluator.Defaults.defaultResolveHeadfile())
			);
		}

		private StorageEntityCacheEvaluator createEntityCacheEvaluator()
		{
			return Storage.EntityCacheEvaluator(
				this.configuration.opt(ENTITY_CACHE_TIMEOUT, Duration.class)
					.map(Duration::toMillis)
					.orElse(StorageEntityCacheEvaluator.Defaults.defaultTimeoutMs()),
				this.configuration.optLong(ENTITY_CACHE_THRESHOLD)
					.orElse(StorageEntityCacheEvaluator.Defaults.defaultCacheThreshold())
			);
		}
		
		private String createDirectoryPath(
			final String path
		)
		{
			return path.startsWith("~/") || path.startsWith("~\\")
				? Paths.get(System.getProperty("user.home"), path.substring(2)).toString()
				: path
			;
		}
		
	}
	
}
