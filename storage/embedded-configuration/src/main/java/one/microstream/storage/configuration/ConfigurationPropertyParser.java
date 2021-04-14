
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.util.Map;
import java.util.function.Consumer;

import one.microstream.configuration.types.ByteSizeParser;
import one.microstream.configuration.types.DurationParser;
import one.microstream.configuration.types.DurationUnit;

/**
 * 
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
@FunctionalInterface
public interface ConfigurationPropertyParser
{
	public void parseProperties(
		Map<String, String> properties   ,
		Configuration       configuration
	);


	public static ConfigurationPropertyParser New()
	{
		return new ConfigurationPropertyParser.Default(
			DurationParser.New(DurationUnit.MS),
			DurationParser.New(DurationUnit.NS),
			ByteSizeParser.New()
		);
	}

	public static ConfigurationPropertyParser New(
		final DurationParser durationParserMs,
		final DurationParser durationParserNs,
		final ByteSizeParser byteSizeParser
	)
	{
		return new ConfigurationPropertyParser.Default(
			notNull(durationParserMs),
			notNull(durationParserNs),
			notNull(byteSizeParser)
		);
	}


	public static class Default implements ConfigurationPropertyParser, ConfigurationPropertyNames
	{
		private final DurationParser durationParserMs;
		private final DurationParser durationParserNs;
		private final ByteSizeParser byteSizeParser;

		Default(
			final DurationParser durationParserMs,
			final DurationParser durationParserNs,
			final ByteSizeParser byteSizeParser
		)
		{
			super();

			this.durationParserMs = durationParserMs;
			this.durationParserNs = durationParserNs;
			this.byteSizeParser = byteSizeParser;
		}

		@Override
		public void parseProperties(
			final Map<String, String> properties   ,
			final Configuration       configuration
		)
		{
			properties.entrySet().forEach(kv ->
				this.parseProperty(kv.getKey(), kv.getValue(), configuration)
			);
		}

		protected void parseProperty(
			final String name                ,
			final String value               ,
			final Configuration configuration
		)
		{
			notNull(value);

			try
			{
				switch(notEmpty(name))
				{
					case BASE_DIRECTORY:
					{
						this.parseDirectoryPath(
							value,
							configuration::setBaseDirectory,
							configuration::setBaseDirectoryInUserHome
						);
					}
					break;

					case DELETION_DIRECTORY:
					{
						configuration.setDeletionDirectory(
							notEmpty(value)
						);
					}
					break;

					case TRUNCATION_DIRECTORY:
					{
						configuration.setTruncationDirectory(
							notEmpty(value)
						);
					}
					break;

					case BACKUP_DIRECTORY:
					{
						this.parseDirectoryPath(
							value,
							configuration::setBackupDirectory,
							configuration::setBackupDirectoryInUserHome
						);
					}
					break;

					case CHANNEL_COUNT:
					{
						configuration.setChannelCount(
							Integer.parseInt(value)
						);
					}
					break;

					case CHANNEL_DIRECTORY_PREFIX:
					{
						configuration.setChannelDirectoryPrefix(
							notEmpty(value)
						);
					}
					break;

					case DATA_FILE_PREFIX:
					{
						configuration.setDataFilePrefix(
							notEmpty(value)
						);
					}
					break;

					case DATA_FILE_SUFFIX:
					{
						configuration.setDataFileSuffix(
							notEmpty(value)
						);
					}
					break;

					case TRANSACTION_FILE_PREFIX:
					{
						configuration.setTransactionFilePrefix(
							notEmpty(value)
						);
					}
					break;

					case TRANSACTION_FILE_SUFFIX:
					{
						configuration.setTransactionFileSuffix(
							notEmpty(value)
						);
					}
					break;

					case TYPE_DICTIONARY_FILENAME:
					{
						configuration.setTypeDictionaryFilename(
							notEmpty(value)
						);
					}
					break;

					case RESCUED_FILE_SUFFIX:
					{
						configuration.setRescuedFileSuffix(
							notEmpty(value)
						);
					}
					break;

					case LOCK_FILE_NAME:
					{
						configuration.setLockFileName(
							notEmpty(value)
						);
					}
					break;

					case HOUSEKEEPING_INTERVAL:
					case HOUSEKEEPING_INTERVAL_MS:
					{
						configuration.setHousekeepingIntervalMs(
							this.durationParserMs.parse(value).toMillis()
						);
					}
					break;

					case HOUSEKEEPING_NANO_TIME_BUDGET:
					case HOUSEKEEPING_TIME_BUDGET_NS:
					{
						configuration.setHousekeepingTimeBudgetNs(
							this.durationParserNs.parse(value).toNanos()
						);
					}
					break;

					case ENTITY_CACHE_THRESHOLD:
					{
						configuration.setEntityCacheThreshold(
							this.byteSizeParser.parse(value).bytes()
						);
					}
					break;

					case ENTITY_CACHE_TIMEOUT:
					case ENTITY_CACHE_TIMEOUT_MS:
					{
						configuration.setEntityCacheTimeoutMs(
							this.durationParserMs.parse(value).toMillis()
						);
					}
					break;

					case DATA_FILE_MIN_SIZE:
					case DATA_FILE_MINIMUM_SIZE:
					{
						configuration.setDataFileMinimumSize(
							this.parseFileSize_int(value)
						);
					}
					break;

					case DATA_FILE_MAX_SIZE:
					case DATA_FILE_MAXIMUM_SIZE:
					{
						configuration.setDataFileMaximumSize(
							this.parseFileSize_int(value)
						);
					}
					break;

					case DATA_FILE_DISSOLVE_RATIO:
					case DATA_FILE_MINIMUM_USE_RATIO:
					{
						configuration.setDataFileMinimumUseRatio(
							Double.parseDouble(value)
						);
					}
					break;

					case DATA_FILE_CLEANUP_HEAD_FILE:
					{
						configuration.setDataFileCleanupHeadFile(
							Boolean.parseBoolean(value)
						);
					}
					break;

					default:
						throw new InvalidStorageConfigurationException("Unsupported property: " + name);
				}
			}
			catch(final NumberFormatException nfe)
			{
				throw new InvalidStorageConfigurationException(
						"Invalid value for property " + name + ": " + value,nfe);
			}
		}

		protected void parseDirectoryPath(
			final String           value               ,
			final Consumer<String> defaultPathConsumer ,
			final Consumer<String> userHomePathConsumer
		)
		{
			if(value.startsWith("~/") || value.startsWith("~\\"))
			{
				final String directoryInUserHome = value.substring(2);
				userHomePathConsumer.accept(directoryInUserHome);
			}
			else
			{
				defaultPathConsumer.accept(notEmpty(value));
			}
		}

		protected int parseFileSize_int(
			final String value
		)
		{
			final long fileSize = this.byteSizeParser.parse(value).bytes();
			if(fileSize > Integer.MAX_VALUE)
			{
				throw new InvalidStorageConfigurationException("Invalid file size: " + value);
			}

			return (int)fileSize;
		}

	}

}
