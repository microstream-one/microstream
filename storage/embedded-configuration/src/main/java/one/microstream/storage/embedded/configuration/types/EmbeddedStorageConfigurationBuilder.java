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

import java.io.File;
import java.time.Duration;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.configuration.types.ByteSize;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationValueMapperProvider;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.typing.KeyValue;

/**
 * A specialized {@link Configuration.Builder}, containing setter methods for
 * properties used in storage configurations.
 * <p>
 * Use {@link #createEmbeddedStorageFoundation()} as a shortcut to create a
 * storage foundation and finally a storage manager:
 * <pre>
 * EmbeddedStorageManager storage = EmbeddedStorageConfigurationBuilder.New()
 * 	.setChannelCount(4)
 * 	.setStorageDirectory("/path/to/storage/")
 * 	.createEmbeddedStorageFoundation()
 * 	.start();
 * </pre>
 * Or load a configuration from an external source:
 * <pre>
 * EmbeddedStorageManager storage = EmbeddedStorageConfiguration.load()
 * 	.createEmbeddedStorageFoundation()
 * 	.start();
 * </pre>
 *
 * @see EmbeddedStorageConfiguration
 * @see EmbeddedStorageConfigurationPropertyNames
 * @since 05.00.00
 *
 */
public interface EmbeddedStorageConfigurationBuilder extends Configuration.Builder
{
	/**
	 * The base directory of the storage in the file system.
	 * @param storageDirectory the storage directory
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setStorageDirectory(String storageDirectory);

	/**
	 * The base directory of the storage in the file system.
	 *
	 * @param storageDirectoryInUserHome relative location in the user home directory
	 * @return this
	 */
	public default EmbeddedStorageConfigurationBuilder setStorageDirectoryInUserHome(
		final String storageDirectoryInUserHome
	)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setStorageDirectory(new File(userHomeDir, storageDirectoryInUserHome).getAbsolutePath());
		return this;
	}

	/**
	 * The deletion directory.
	 * @param deletionDirectory the deletion directory
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setDeletionDirectory(String deletionDirectory);

	/**
	 * The truncation directory.
	 * @param truncationDirectory the trunctation directory
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setTruncationDirectory(String truncationDirectory);

	/**
	 * The backup directory.
	 * @param backupDirectory the backup directory
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setBackupDirectory(String backupDirectory);

	/**
	 * The backup directory.
	 *
	 * @param backupDirectoryInUserHome relative location in the user home directory
	 * @return this
	 */
	public default EmbeddedStorageConfigurationBuilder setBackupDirectoryInUserHome(
		final String backupDirectoryInUserHome
	)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setBackupDirectory(new File(userHomeDir, backupDirectoryInUserHome).getAbsolutePath());
		return this;
	}

	/**
	 * The number of threads and number of directories used by the storage
	 * engine. Every thread has exclusive access to its directory. Default is
	 * <code>1</code>.
	 *
	 * @param channelCount the new channel count, must be a power of 2
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setChannelCount(int channelCount);

	/**
	 * Name prefix of the subdirectories used by the channel threads. Default is
	 * <code>"channel_"</code>.
	 *
	 * @param channelDirectoryPrefix new prefix
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setChannelDirectoryPrefix(String channelDirectoryPrefix);

	/**
	 * Name prefix of the storage files. Default is <code>"channel_"</code>.
	 *
	 * @param dataFilePrefix new prefix
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setDataFilePrefix(String dataFilePrefix);

	/**
	 * Name suffix of the storage files. Default is <code>".dat"</code>.
	 *
	 * @param dataFileSuffix new suffix
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileSuffix(String dataFileSuffix);

	/**
	 * Name prefix of the storage transaction file. Default is <code>"transactions_"</code>.
	 *
	 * @param transactionFilePrefix new prefix
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setTransactionFilePrefix(String transactionFilePrefix);

	/**
	 * Name suffix of the storage transaction file. Default is <code>".sft"</code>.
	 *
	 * @param transactionFileSuffix new suffix
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setTransactionFileSuffix(String transactionFileSuffix);

	/**
	 * The name of the dictionary file. Default is
	 * <code>"PersistenceTypeDictionary.ptd"</code>.
	 *
	 * @param typeDictionaryFileName new name
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setTypeDictionaryFileName(String typeDictionaryFileName);

	public EmbeddedStorageConfigurationBuilder setRescuedFileSuffix(String rescuedFileSuffix);

	public EmbeddedStorageConfigurationBuilder setLockFileName(String lockFileName);

	/**
	 * Interval for the housekeeping. This is work like garbage
	 * collection or cache checking. In combination with
	 * {@link #setHousekeepingTimeBudget(Duration)} the maximum processor
	 * time for housekeeping work can be set. Default is one second.
	 *
	 * @param housekeepingInterval the new interval
	 * @return this
	 *
	 * @see #setHousekeepingTimeBudget(Duration)
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingInterval(Duration housekeepingInterval);

	/**
	 * Duration used for each housekeeping cycle. However, no
	 * matter how low the number is, one item of work will always be completed.
	 * But if there is nothing to clean up, no processor time will be wasted.
	 * Default is 10 milliseconds = 0.01 seconds.
	 *
	 * @param housekeepingTimeBudget the new time budget
	 * @return this
	 *
	 * @see #setHousekeepingInterval(Duration)
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingTimeBudget(Duration housekeepingTimeBudget);

	/**
	 * Usage of an adaptive housekeeping controller, which will increase the time budgets on demand,
	 * if the garbage collector needs more time to reach the sweeping phase.
	 * 
	 * @param adaptive <code>true</code> if an adaptive controller should be used
	 * @return this
	 * 
	 * @see #setHousekeepingIncreaseThreshold(Duration)
	 * @see #setHousekeepingIncreaseAmount(Duration)
	 * @see #setHousekeepingMaximumTimeBudget(Duration)
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingAdaptive(boolean adaptive);

	/**
	 * The threshold of the adaption cycle to calculate new budgets for the housekeeping process.
	 * <p>
	 * Only used when {@link #setHousekeepingAdaptive(boolean)} is <code>true</code>.
	 * 
	 * @param housekeepingIncreaseThreshold the new increase threshold
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingIncreaseThreshold(Duration housekeepingIncreaseThreshold);

	/**
	 * The amount the housekeeping budgets will be increased each cycle.
	 * <p>
	 * Only used when {@link #setHousekeepingAdaptive(boolean)} is <code>true</code>.
	 * 
	 * @param housekeepingIncreaseAmount the new increase amount
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingIncreaseAmount(Duration housekeepingIncreaseAmount);

	/**
	 * The upper limit of the housekeeping time budgets.
	 * <p>
	 * Only used when {@link #setHousekeepingAdaptive(boolean)} is <code>true</code>.
	 * 
	 * @param housekeepingMaximumTimeBudget the new maximum time budget
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingMaximumTimeBudget(Duration housekeepingMaximumTimeBudget);

	/**
	 * Abstract threshold value for the lifetime of entities in the cache. See
	 * {@link StorageEntityCacheEvaluator#New(long, long)}. Default is <code>1.000.000.000</code>.
	 *
	 * @param entityCacheThreshold the new threshold
	 * @return this
	 *
	 * @see #setEntityCacheTimeout(Duration)
	 */
	public EmbeddedStorageConfigurationBuilder setEntityCacheThreshold(long entityCacheThreshold);

	/**
	 * Timeout for the entity cache evaluator. If an entity
	 * wasn't accessed in this timespan it will be removed from the cache.
	 * Default is one day.
	 * See {@link StorageEntityCacheEvaluator#New(long, long)}.
	 *
	 * @param entityCacheTimeout the new timeout
	 * @return this
	 *
	 * @see Duration
	 * @see #setEntityCacheThreshold(long)
	 */
	public EmbeddedStorageConfigurationBuilder setEntityCacheTimeout(Duration entityCacheTimeout);

	/**
	 * Minimum file size for a data file to avoid cleaning it up. Default is 1 MiB.
	 *
	 * @param dataFileMinimumSize the new minimum file size
	 * @return this
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileMinimumSize(ByteSize dataFileMinimumSize);

	/**
	 * Maximum file size for a data file to avoid cleaning it up. Default is 8 MiB.
	 *
	 * @param dataFileMaximumSize the new maximum file size
	 * @return this
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileMaximumSize(ByteSize dataFileMaximumSize);

	/**
	 * The ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent
	 * the file from being dissolved. "Gap" data is anything that is not the latest version of an entity's data,
	 * including older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 * as a negative value length header).<br>
	 * The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 * file dissolving (data transfers to new files) is required and vice versa.
	 *
	 * @param dataFileMinimumUseRatio the new minimum use ratio
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileMinimumUseRatio(double dataFileMinimumUseRatio);

	/**
	 * A flag defining whether the current head file (the only file actively written to)
	 * shall be subjected to file cleanups as well.
	 *
	 * @param dataFileCleanupHeadFile the new clean head file
	 * @return this
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileCleanupHeadFile(boolean dataFileCleanupHeadFile);

	/**
	 * Maximum file size for a transaction file to avoid cleaning it up. Default is 1 GiB.
	 *
	 * @param transactionFileMaximumSize the new maximum file size
	 * @return this
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public EmbeddedStorageConfigurationBuilder setTransactionFileMaximumSize(ByteSize transactionFileMaximumSize);
	
	/**
	 * Creates an {@link EmbeddedStorageFoundation} based on the settings of this builder.
	 *
	 * @return an {@link EmbeddedStorageFoundation}
	 *
	 * @see EmbeddedStorageFoundationCreatorConfigurationBased
	 */
	public default EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation()
	{
		return EmbeddedStorageFoundationCreatorConfigurationBased.New(
			this.buildConfiguration()
		)
		.createEmbeddedStorageFoundation()
		;
	}



	/**
	 * Pseudo-constructor method to create a new builder.
	 *
	 * @return a new {@link EmbeddedStorageConfigurationBuilder}
	 */
	public static EmbeddedStorageConfigurationBuilder New()
	{
		return new Default(Configuration.Builder());
	}

	/**
	 * Pseudo-constructor method to create a new builder, wrapping an existing one.
	 *
	 * @param delegate the delegate to wrap
	 * @return a new {@link EmbeddedStorageConfigurationBuilder}
	 */
	public static EmbeddedStorageConfigurationBuilder New(
		final Configuration.Builder delegate
	)
	{
		return new Default(
			notNull(delegate)
		);
	}


	public static class Default implements EmbeddedStorageConfigurationBuilder, EmbeddedStorageConfigurationPropertyNames
	{
		private final Configuration.Builder delegate;

		Default(
			final Configuration.Builder delegate
		)
		{
			super();
			this.delegate = delegate;
		}

		// ############################
		// Delegate methods
		// ############################

		@Override
		public EmbeddedStorageConfigurationBuilder valueMapperProvider(
			final ConfigurationValueMapperProvider valueMapperProvider
		)
		{
			this.delegate.valueMapperProvider(valueMapperProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder set(
			final String key  ,
			final String value
		)
		{
			this.delegate.set(key, value);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setAll(
			final XGettingCollection<KeyValue<String, String>> properties
		)
		{
			this.delegate.setAll(properties);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EmbeddedStorageConfigurationBuilder setAll(
			final KeyValue<String, String>... properties
		)
		{
			this.delegate.setAll(properties);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder child(
			final String key
		)
		{
			this.delegate.child(key);
			return this;
		}

		@Override
		public Configuration buildConfiguration()
		{
			return this.delegate.buildConfiguration();
		}

		// ############################
		// Builder methods
		// ############################

		@Override
		public EmbeddedStorageConfigurationBuilder setStorageDirectory(
			final String storageDirectory
		)
		{
			return this.set(STORAGE_DIRECTORY, storageDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDeletionDirectory(
			final String deletionDirectory
		)
		{
			return this.set(DELETION_DIRECTORY, deletionDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTruncationDirectory(
			final String truncationDirectory
		)
		{
			return this.set(TRUNCATION_DIRECTORY, truncationDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setBackupDirectory(
			final String backupDirectory
		)
		{
			return this.set(BACKUP_DIRECTORY, backupDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setChannelCount(
			final int channelCount
		)
		{
			return this.set(CHANNEL_COUNT, Integer.toString(channelCount));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setChannelDirectoryPrefix(
			final String channelDirectoryPrefix
		)
		{
			return this.set(CHANNEL_DIRECTORY_PREFIX, channelDirectoryPrefix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFilePrefix(
			final String dataFilePrefix
		)
		{
			return this.set(DATA_FILE_PREFIX, dataFilePrefix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileSuffix(
			final String dataFileSuffix
		)
		{
			return this.set(DATA_FILE_SUFFIX, dataFileSuffix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTransactionFilePrefix(
			final String transactionFilePrefix
		)
		{
			return this.set(TRANSACTION_FILE_PREFIX, transactionFilePrefix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTransactionFileSuffix(
			final String transactionFileSuffix
		)
		{
			return this.set(TRANSACTION_FILE_SUFFIX, transactionFileSuffix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTypeDictionaryFileName(
			final String typeDictionaryFileName
		)
		{
			return this.set(TYPE_DICTIONARY_FILE_NAME, typeDictionaryFileName);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setRescuedFileSuffix(
			final String rescuedFileSuffix
		)
		{
			this.set(RESCUED_FILE_SUFFIX, rescuedFileSuffix);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setLockFileName(
			final String lockFileName
		)
		{
			return this.set(LOCK_FILE_NAME, lockFileName);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingInterval(
			final Duration houseKeepingInterval
		)
		{
			return this.set(HOUSEKEEPING_INTERVAL, houseKeepingInterval.toString());
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingTimeBudget(
			final Duration housekeepingTimeBudget
		)
		{
			return this.set(HOUSEKEEPING_TIME_BUDGET, housekeepingTimeBudget.toString());
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingAdaptive(
			final boolean adaptive
		)
		{
			return this.set(HOUSEKEEPING_ADAPTIVE, Boolean.toString(adaptive));
		}
		
		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingIncreaseThreshold(
			final Duration housekeepingIncreaseThreshold
		)
		{
			return this.set(HOUSEKEEPING_INCREASE_THRESHOLD, housekeepingIncreaseThreshold.toString());
		}
		
		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingIncreaseAmount(
			final Duration housekeepingIncreaseAmount
		)
		{
			return this.set(HOUSEKEEPING_INCREASE_AMOUNT, housekeepingIncreaseAmount.toString());
		}
		
		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingMaximumTimeBudget(
			final Duration housekeepingMaximumTimeBudget
		)
		{
			return this.set(HOUSEKEEPING_MAXIMUM_TIME_BUDGET, housekeepingMaximumTimeBudget.toString());
		}
		
		@Override
		public EmbeddedStorageConfigurationBuilder setEntityCacheThreshold(
			final long entityCacheThreshold
		)
		{
			return this.set(ENTITY_CACHE_THRESHOLD, Long.toString(entityCacheThreshold));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setEntityCacheTimeout(
			final Duration entityCacheTimeout
		)
		{
			return this.set(ENTITY_CACHE_TIMEOUT, entityCacheTimeout.toString());
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileMinimumSize(
			final ByteSize dataFileMinimumSize
		)
		{
			return this.set(DATA_FILE_MINIMUM_SIZE, dataFileMinimumSize.toString());
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileMaximumSize(
			final ByteSize dataFileMaximumSize
		)
		{
			return this.set(DATA_FILE_MAXIMUM_SIZE, dataFileMaximumSize.toString());
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileMinimumUseRatio(
			final double dataFileMinimumUseRatio
		)
		{
			return this.set(DATA_FILE_MINIMUM_USE_RATIO, Double.toString(dataFileMinimumUseRatio));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileCleanupHeadFile(
			final boolean dataFileCleanupHeadFile
		)
		{
			return this.set(DATA_FILE_CLEANUP_HEAD_FILE, Boolean.toString(dataFileCleanupHeadFile));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTransactionFileMaximumSize(
			final ByteSize transactionFileMaximumSize
		)
		{
			return this.set(TRANSACTION_FILE_MAXIMUM_SIZE, transactionFileMaximumSize.toString());
		}

	}

}
