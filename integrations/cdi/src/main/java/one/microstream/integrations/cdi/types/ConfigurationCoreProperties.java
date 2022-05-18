
package one.microstream.integrations.cdi.types;

/*-
 * #%L
 * microstream-integrations-cdi
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.Config;

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationPropertyNames;


/**
 * The relation with the properties from MicroStream docs:
 * https://docs.microstream.one/manual/storage/configuration/properties.html
 */
public enum ConfigurationCoreProperties
{
	/**
	 * The base directory of the storage in the file system. Default is "storage" in the working directory.
	 */
	STORAGE_DIRECTORY(
		Constants.PREFIX + "storage.directory",
		EmbeddedStorageConfigurationPropertyNames.STORAGE_DIRECTORY
	),
	
	/**
	 * The live file system configuration. See storage targets configuration.
	 */
	STORAGE_FILESYSTEM(
			Constants.PREFIX + "storage.filesystem",
		EmbeddedStorageConfigurationPropertyNames.STORAGE_FILESYSTEM
	),
	
	/**
	 * If configured, the storage will not delete files. Instead of deleting a file it will be moved to this directory.
	 */
	DELETION_DIRECTORY(
			Constants.PREFIX + "deletion.directory",
		EmbeddedStorageConfigurationPropertyNames.DELETION_DIRECTORY
	),
	
	/**
	 * If configured, files that will get truncated are copied into this directory.
	 */
	TRUNCATION_DIRECTORY(
			Constants.PREFIX + "truncation.directory",
		EmbeddedStorageConfigurationPropertyNames.TRUNCATION_DIRECTORY
	),
	
	/**
	 * The backup directory.
	 */
	BACKUP_DIRECTORY(
			Constants.PREFIX + "backup.directory",
		EmbeddedStorageConfigurationPropertyNames.BACKUP_DIRECTORY
	),
	
	/**
	 * The backup file system configuration. See storage targets configuration.
	 */
	BACKUP_FILESYSTEM(
			Constants.PREFIX + "backup.filesystem",
		EmbeddedStorageConfigurationPropertyNames.BACKUP_FILESYSTEM
	),
	
	/**
	 * The number of threads and number of directories used by the storage engine. Every thread has exclusive access
	 * to its directory. Default is 1.
	 */
	CHANNEL_COUNT(
			Constants.PREFIX + "channel.count",
		EmbeddedStorageConfigurationPropertyNames.CHANNEL_COUNT
	),
	
	/**
	 * Name prefix of the subdirectories used by the channel threads. Default is "channel_".
	 */
	CHANNEL_DIRECTORY_PREFIX(
			Constants.PREFIX + "channel.directory.prefix",
		EmbeddedStorageConfigurationPropertyNames.CHANNEL_DIRECTORY_PREFIX
	),
	
	/**
	 * Name prefix of the storage files. Default is "channel_".
	 */
	DATA_FILE_PREFIX(
			Constants.PREFIX + "data.file.prefix",
		EmbeddedStorageConfigurationPropertyNames.DATA_FILE_PREFIX
	),
	
	/**
	 * Name suffix of the storage files. Default is ".dat".
	 */
	DATA_FILE_SUFFIX(
			Constants.PREFIX + "data.file.suffix",
		EmbeddedStorageConfigurationPropertyNames.DATA_FILE_SUFFIX
	),
	
	/**
	 * Name prefix of the storage transaction file. Default is "transactions_".
	 */
	TRANSACTION_FILE_PREFIX(
			Constants.PREFIX + "transaction.file.prefix",
		EmbeddedStorageConfigurationPropertyNames.TRANSACTION_FILE_PREFIX
	),
	
	/**
	 * Name suffix of the storage transaction file. Default is ".sft".
	 */
	TRANSACTION_FILE_SUFFIX(
			Constants.PREFIX + "transaction.file.suffix",
		EmbeddedStorageConfigurationPropertyNames.TRANSACTION_FILE_SUFFIX
	),
	
	/**
	 * The name of the dictionary file. Default is "PersistenceTypeDictionary.ptd".
	 */
	TYPE_DICTIONARY_FILE_NAME(
			Constants.PREFIX + "type.dictionary.file.name",
		EmbeddedStorageConfigurationPropertyNames.TYPE_DICTIONARY_FILE_NAME
	),
	
	/**
	 * Name suffix of the storage rescue files. Default is ".bak".
	 */
	RESCUED_FILE_SUFFIX(
			Constants.PREFIX + "rescued.file.suffix",
		EmbeddedStorageConfigurationPropertyNames.RESCUED_FILE_SUFFIX
	),
	
	/**
	 * Name of the lock file. Default is "used.lock".
	 */
	LOCK_FILE_NAME(
			Constants.PREFIX + "lock.file.name",
		EmbeddedStorageConfigurationPropertyNames.LOCK_FILE_NAME
	),
	
	/**
	 * Interval for the housekeeping. This is work like garbage collection or cache checking. In combination with
	 * houseKeepingNanoTimeBudget the maximum processor time for housekeeping work can be set. Default is 1 second.
	 */
	HOUSEKEEPING_INTERVAL(
			Constants.PREFIX + "housekeeping.interval",
		EmbeddedStorageConfigurationPropertyNames.HOUSEKEEPING_INTERVAL
	),
	
	/**
	 * Number of nanoseconds used for each housekeeping cycle. Default is 10 milliseconds = 0.01 seconds.
	 */
	HOUSEKEEPING_TIME_BUDGET(
			Constants.PREFIX + "housekeeping.time.budget",
		EmbeddedStorageConfigurationPropertyNames.HOUSEKEEPING_TIME_BUDGET
	),
	
	/**
	 * Abstract threshold value for the lifetime of entities in the cache. Default is 1000000000.
	 */
	ENTITY_CACHE_THRESHOLD(
			Constants.PREFIX + "entity.cache.threshold",
		EmbeddedStorageConfigurationPropertyNames.ENTITY_CACHE_THRESHOLD
	),
	
	/**
	 * Timeout in milliseconds for the entity cache evaluator. If an entity wasn’t
	 * accessed in this timespan it will be removed from the cache. Default is 1 day.
	 */
	ENTITY_CACHE_TIMEOUT(
			Constants.PREFIX + "entity.cache.timeout",
		EmbeddedStorageConfigurationPropertyNames.ENTITY_CACHE_TIMEOUT
	),
	
	/**
	 * Minimum file size for a data file to avoid cleaning it up. Default is 1024^2 = 1 MiB.
	 */
	DATA_FILE_MINIMUM_SIZE(
			Constants.PREFIX + "data.file.minimum.size",
		EmbeddedStorageConfigurationPropertyNames.DATA_FILE_MINIMUM_SIZE
	),
	
	/**
	 * Maximum file size for a data file to avoid cleaning it up. Default is 1024^2*8 = 8 MiB.
	 */
	DATA_FILE_MAXIMUM_SIZE(
			Constants.PREFIX + "data.file.maximum.size",
		EmbeddedStorageConfigurationPropertyNames.DATA_FILE_MAXIMUM_SIZE
	),
	
	/**
	 * The ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent the file from being
	 * dissolved. Default is 0.75 (75%).
	 */
	DATA_FILE_MINIMUM_USE_RATIO(
			Constants.PREFIX + "data.file.minimum.use.ratio",
		EmbeddedStorageConfigurationPropertyNames.DATA_FILE_MINIMUM_USE_RATIO
	),
	
	/**
	 * A flag defining whether the current head file (the only file actively written to)
	 * shall be subjected to file cleanups as well.
	 */
	DATA_FILE_CLEANUP_HEAD_FILE(
			Constants.PREFIX + "data.file.cleanup.head.file",
		EmbeddedStorageConfigurationPropertyNames.DATA_FILE_CLEANUP_HEAD_FILE
	),
	
	/**
	 * Allow custom properties in through Microprofile, using this prefix. E.g.:
	 * If you want to include the "custom.test" property, you will set it as "one.microstream.property.custom.test"
	 */
	CUSTOM(
			Constants.PREFIX + "property",
		""
	);

	private final String microprofile;
	private final String microstream ;
	
	ConfigurationCoreProperties(final String microprofile, final String microstream)
	{
		this.microprofile = microprofile;
		this.microstream  = microstream ;
	}
	
	public String getMicroprofile()
	{
		return this.microprofile;
	}
	
	public String getMicrostream()
	{
		return this.microstream;
	}
	
	/**
	 * Check if there is a relation between the Microstream and Microstream properties.
	 * If not, it is because it is a custom property.
	 *
	 * @return true if miscrostream is {@link String#isBlank()}
	 */
	public boolean isCustom()
	{
		return this.microstream.isBlank();
	}
	
	/**
	 * Return true if there is a relation between the Microstream and Microstream properties.
	 *
	 * @return the positive of {@link ConfigurationCoreProperties#isCustom()}
	 */
	boolean isMapped()
	{
		return !this.isCustom();
	}
	
	public static Map<String, String> getProperties(final Config config)
	{
		final Map<String, String> properties = new HashMap<>();
		
		Arrays.stream(ConfigurationCoreProperties.values())
			.filter(ConfigurationCoreProperties::isMapped)
			.forEach(p -> addProperty(config, properties, p))
		;
		
		StreamSupport.stream(config.getPropertyNames().spliterator(), false)
			.filter(k -> k.contains(CUSTOM.getMicroprofile()))
			.forEach(k ->
			{
				final String key   = k.split(CUSTOM.getMicroprofile() + ".")[1];
				final String value = config.getValue(k, String.class);
				properties.put(key, value);
			})
		;
		
		return properties;
	}
	
	private static void addProperty(
		final Config                      config    ,
		final Map<String, String>         properties,
		final ConfigurationCoreProperties property
	)
	{
		config.getOptionalValue(property.getMicroprofile(), String.class)
			.ifPresent(v -> properties.put(property.getMicrostream(), v))
		;
	}

	private static class Constants
	{
		static final String PREFIX = "one.microstream.";
	}
}
