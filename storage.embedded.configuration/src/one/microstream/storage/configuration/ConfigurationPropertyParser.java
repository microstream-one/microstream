
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.util.Map;
import java.util.function.Consumer;

import one.microstream.bytes.ByteMultiple;
import one.microstream.storage.exceptions.StorageExceptionInvalidConfiguration;


@FunctionalInterface
public interface ConfigurationPropertyParser
{
	public void parseProperties(
		Map<String, String> properties,
		Configuration       configuration
	);
	
	
	public static ConfigurationPropertyParser New()
	{
		return new ConfigurationPropertyParser.Default(
			DurationParser.Default(),
			FileSizeParser.Default()
		);
	}
		
	public static ConfigurationPropertyParser New(
		final DurationParser durationParser,
		final FileSizeParser fileSizeParser
	)
	{
		return new ConfigurationPropertyParser.Default(
			notNull(durationParser),
			notNull(fileSizeParser)
		);
	}
	
	
	public static class Default implements ConfigurationPropertyParser, ConfigurationPropertyNames
	{
		private final DurationParser durationParser;
		private final FileSizeParser fileSizeParser;
		
		Default(
			final DurationParser durationParser,
			final FileSizeParser fileSizeParser
		)
		{
			super();
			
			this.durationParser = durationParser;
			this.fileSizeParser = fileSizeParser;
		}
		
		@Override
		public void parseProperties(
			final Map<String, String> properties, 
			final Configuration       configuration
		)
		{
			properties.entrySet().forEach(kv ->
				this.parseProperty(kv.getKey(), kv.getValue(), configuration)
			);
		}
		
		@SuppressWarnings("deprecation") // keeps parsing deprecated properties
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
				
					case HOUSEKEEPING_INTERVAL:
					case HOUSEKEEPING_INTERVAL_MS:
					{
						configuration.setHousekeepingIntervalMs(
							this.durationParser.parse(value, DurationUnit.MS).toMillis()
						);
					}
					break;
				
					case HOUSEKEEPING_NANO_TIME_BUDGET:
					case HOUSEKEEPING_TIME_BUDGET_NS:
					{
						configuration.setHousekeepingTimeBudgetNs(
							this.durationParser.parse(value, DurationUnit.NS).toNanos()
						);
					}
					break;
				
					case ENTITY_CACHE_THRESHOLD:
					{
						configuration.setEntityCacheThreshold(
							this.fileSizeParser.parseFileSize(value, ByteMultiple.B)
						);
					}
					break;
				
					case ENTITY_CACHE_TIMEOUT:
					case ENTITY_CACHE_TIMEOUT_MS:
					{
						configuration.setEntityCacheTimeoutMs(
							this.durationParser.parse(value, DurationUnit.MS).toMillis()
						);
					}
					break;
				
					case DATA_FILE_MIN_SIZE:
					case DATA_FILE_MINIMUM_SIZE:
					{
						configuration.setDataFileMinimumSize(
							this.parseFileSize_int(value, ByteMultiple.B)
						);
					}
					break;
				
					case DATA_FILE_MAX_SIZE:
					case DATA_FILE_MAXIMUM_SIZE:
					{
						configuration.setDataFileMaximumSize(
							this.parseFileSize_int(value, ByteMultiple.B)
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
						throw new StorageExceptionInvalidConfiguration("Unsupported property: " + name);
				}
			}
			catch(final NumberFormatException nfe)
			{
				throw new StorageExceptionInvalidConfiguration(
						"Invalid value for property " + name + ": " + value,nfe);
			}
		}
		
		protected void parseDirectoryPath(
			final String           value,
			final Consumer<String> defaultPathConsumer,
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

		protected int parseFileSize_int(final String value, final ByteMultiple defaultByteMultiple)
		{
			final long fileSize = this.fileSizeParser.parseFileSize(value, defaultByteMultiple);
			if(fileSize > Integer.MAX_VALUE)
			{
				throw new StorageExceptionInvalidConfiguration("Invalid file size: " + value);
			}
			
			return (int)fileSize;
		}
		
	}
	
}
