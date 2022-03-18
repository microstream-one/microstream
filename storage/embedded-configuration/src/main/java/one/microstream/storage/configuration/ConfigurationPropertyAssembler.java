package one.microstream.storage.configuration;

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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
public interface ConfigurationPropertyAssembler
{
	public Map<String, String> assemble(Configuration configuration);


	public static ConfigurationPropertyAssembler New()
	{
		return new ConfigurationPropertyAssembler.Default(
			DurationAssembler.Default(),
			ByteSizeAssembler.Default()
		);
	}

	public static ConfigurationPropertyAssembler New(
		final DurationAssembler durationParser,
		final ByteSizeAssembler fileSizeParser
	)
	{
		return new ConfigurationPropertyAssembler.Default(
			notNull(durationParser),
			notNull(fileSizeParser)
		);
	}


	public static class Default implements ConfigurationPropertyAssembler, ConfigurationPropertyNames
	{
		private final DurationAssembler durationAssembler;
		private final ByteSizeAssembler fileSizeAssembler;

		Default(
			final DurationAssembler durationAssembler,
			final ByteSizeAssembler fileSizeAssembler
		)
		{
			super();
			this.durationAssembler = durationAssembler;
			this.fileSizeAssembler = fileSizeAssembler;
		}

		@Override
		public Map<String, String> assemble(
			final Configuration configuration
		)
		{
			final Map<String, String> map      = new HashMap<>();
			final Configuration       defaults = Configuration.Default();

			this.optPutProperty(
				map,
				BASE_DIRECTORY,
				configuration.getBaseDirectory(),
				defaults.getBaseDirectory()
			);
			this.optPutProperty(
				map,
				DELETION_DIRECTORY,
				configuration.getDeletionDirectory(),
				defaults.getDeletionDirectory()
			);
			this.optPutProperty(
				map,
				TRUNCATION_DIRECTORY,
				configuration.getTruncationDirectory(),
				defaults.getTruncationDirectory()
			);
			this.optPutProperty(
				map,
				BACKUP_DIRECTORY,
				configuration.getBackupDirectory(),
				defaults.getBackupDirectory()
			);
			this.optPutProperty(
				map,
				CHANNEL_DIRECTORY_PREFIX,
				configuration.getChannelDirectoryPrefix(),
				defaults.getChannelDirectoryPrefix()
			);
			this.optPutProperty(
				map,
				DATA_FILE_PREFIX,
				configuration.getDataFilePrefix(),
				defaults.getDataFilePrefix()
			);
			this.optPutProperty(
				map,
				DATA_FILE_SUFFIX,
				configuration.getDataFileSuffix(),
				defaults.getDataFileSuffix()
			);
			this.optPutProperty(
				map,
				TRANSACTION_FILE_PREFIX,
				configuration.getTransactionFilePrefix(),
				defaults.getTransactionFilePrefix()
			);
			this.optPutProperty(
				map,
				TRANSACTION_FILE_SUFFIX,
				configuration.getTransactionFileSuffix(),
				defaults.getTransactionFileSuffix()
			);
			this.optPutProperty(
				map,
				TYPE_DICTIONARY_FILENAME,
				configuration.getTypeDictionaryFilename(),
				defaults.getTypeDictionaryFilename()
			);
			this.optPutProperty(
				map,
				CHANNEL_COUNT,
				configuration.getChannelCount(),
				defaults.getChannelCount()
			);
			this.optPutProperty(
				map,
				HOUSEKEEPING_INTERVAL_MS,
				configuration.getHousekeepingIntervalMs(),
				defaults.getHousekeepingIntervalMs()
			);
			this.optPutProperty(
				map,
				HOUSEKEEPING_TIME_BUDGET_NS,
				configuration.getHousekeepingTimeBudgetNs(),
				defaults.getHousekeepingTimeBudgetNs()
			);
			this.optPutProperty(
				map,
				ENTITY_CACHE_TIMEOUT_MS,
				configuration.getEntityCacheTimeoutMs(),
				defaults.getEntityCacheTimeoutMs()
			);
			this.optPutProperty(
				map,
				ENTITY_CACHE_THRESHOLD,
				configuration.getEntityCacheThreshold(),
				defaults.getEntityCacheThreshold()
			);
			this.optPutProperty(
				map,
				DATA_FILE_MINIMUM_SIZE,
				configuration.getDataFileMinimumSize(),
				defaults.getDataFileMinimumSize()
			);
			this.optPutProperty(
				map,
				DATA_FILE_MAXIMUM_SIZE,
				configuration.getDataFileMaximumSize(),
				defaults.getDataFileMaximumSize()
			);
			this.optPutProperty(
				map,
				DATA_FILE_MINIMUM_USE_RATIO,
				configuration.getDataFileMinimumUseRatio(),
				defaults.getDataFileMinimumUseRatio()
			);
			this.optPutProperty(
				map,
				DATA_FILE_CLEANUP_HEAD_FILE,
				configuration.getDataFileCleanupHeadFile(),
				defaults.getDataFileCleanupHeadFile()
			);

			return map;
		}

		protected <T> void optPutProperty(
			final Map<String, String> map         ,
			final String              key         ,
			final T                   value       ,
			final T                   defaultValue
		)
		{
			if(value != null && !value.equals(defaultValue))
			{
				map.put(key, this.toStringValue(key, value));
			}
		}

		protected String toStringValue(
			final String key  ,
			final Object value
		)
		{
			switch(key)
			{
				case BASE_DIRECTORY:
				case DELETION_DIRECTORY:
				case TRUNCATION_DIRECTORY:
				case BACKUP_DIRECTORY:
				case CHANNEL_DIRECTORY_PREFIX:
				case DATA_FILE_PREFIX:
				case DATA_FILE_SUFFIX:
				case TRANSACTION_FILE_PREFIX:
				case TRANSACTION_FILE_SUFFIX:
				case TYPE_DICTIONARY_FILENAME:
				case CHANNEL_COUNT:
				case DATA_FILE_MINIMUM_USE_RATIO:
				case DATA_FILE_CLEANUP_HEAD_FILE:
				{
					return value.toString();
				}

				case HOUSEKEEPING_INTERVAL_MS:
				case ENTITY_CACHE_TIMEOUT_MS:
				{
					return this.durationAssembler.assemble(Duration.ofMillis(((Number)value).longValue()));
				}

				case HOUSEKEEPING_TIME_BUDGET_NS:
				{
					return this.durationAssembler.assemble(Duration.ofNanos(((Number)value).longValue()));
				}

				case ENTITY_CACHE_THRESHOLD:
				case DATA_FILE_MINIMUM_SIZE:
				case DATA_FILE_MAXIMUM_SIZE:
				{
					return this.fileSizeAssembler.assemble(((Number)value).longValue());
				}

				default:
					throw new InvalidStorageConfigurationException("Unsupported property: " + key);
			}
		}

	}

}
