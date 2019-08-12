
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.bytes.ByteMultiple;
import one.microstream.storage.exceptions.StorageExceptionInvalidConfiguration;


@FunctionalInterface
public interface ConfigurationPropertyParser
{
	public void parseProperty(String name, String value, Configuration configuration);
			
	public static ConfigurationPropertyParser New()
	{
		return new Default();
	}
	
	public static class Default implements ConfigurationPropertyParser, ConfigurationPropertyNames
	{
		protected Default()
		{
			super();
		}
		
		@Override
		public void parseProperty(final String name, final String value, final Configuration configuration)
		{
			notNull(value);

			switch(notEmpty(name))
			{
				case BASE_DIRECTORY:
				{
					if(value.startsWith("~/") || value.startsWith("~\\"))
					{
						final String directoryInUserHome = value.substring(2);
						configuration.setBaseDirectoryInUserHome(directoryInUserHome);
					}
					else
					{
						configuration.setBaseDirectory(notEmpty(value));
					}
				}
				break;
				
				case DELETION_DIRECTORY:
				{
					configuration.setDeletionDirectory(value);
				}
				break;
				
				case TRUNCATION_DIRECTORY:
				{
					configuration.setTruncationDirectory(value);
				}
				break;
			
				case CHANNEL_COUNT:
				{
					configuration.setChannelCount(Integer.parseInt(value));
				}
				break;
			
				case CHANNEL_DIRECTORY_PREFIX:
				{
					configuration.setChannelDirectoryPrefix(notEmpty(value));
				}
				break;
			
				case DATA_FILE_PREFIX:
				{
					configuration.setDataFilePrefix(notEmpty(value));
				}
				break;
			
				case DATA_FILE_SUFFIX:
				{
					configuration.setDataFileSuffix(notEmpty(value));
				}
				break;
			
				case TRANSACTION_FILE_PREFIX:
				{
					configuration.setTransactionFilePrefix(notEmpty(value));
				}
				break;
			
				case TRANSACTION_FILE_SUFFIX:
				{
					configuration.setTransactionFileSuffix(notEmpty(value));
				}
				break;
			
				case TYPE_DICTIONARY_FILENAME:
				{
					configuration.setTypeDictionaryFilename(notEmpty(value));
				}
				break;
			
				case HOUSEKEEPING_INTERVAL:
				{
					configuration.setHouseKeepingInterval(this.parseDurationMillies(value).toMillis());
				}
				break;
			
				case HOUSEKEEPING_NANO_TIME_BUDGET:
				{
					configuration.setHouseKeepingNanoTimeBudget(this.parseDurationNanos(value).toNanos());
				}
				break;
			
				case ENTITY_CACHE_THRESHOLD:
				{
					configuration.setEntityCacheThreshold(this.parseFileSize_long(value));
				}
				break;
			
				case ENTITY_CACHE_TIMEOUT:
				{
					configuration.setEntityCacheTimeout(this.parseDurationMillies(value).toMillis());
				}
				break;
			
				case DATA_FILE_MIN_SIZE:
				{
					configuration.setDataFileMinSize(this.parseFileSize_int(value));
				}
				break;
			
				case DATA_FILE_MAX_SIZE:
				{
					configuration.setDataFileMaxSize(this.parseFileSize_int(value));
				}
				break;
			
				case DATA_FILE_DISSOLVE_RATIO:
				{
					configuration.setDataFileDissolveRatio(Double.parseDouble(value));
				}
				break;
			
				default:
					throw new StorageExceptionInvalidConfiguration("Unknown property: " + name);
			}
		}
		
		protected Duration parseDurationMillies(final String value)
		{
			try
			{
				return Duration.parse(value);
			}
			catch(final DateTimeParseException e)
			{
				try
				{
					return Duration.ofMillis(Long.parseLong(value));
				}
				catch(final NumberFormatException e1)
				{
					// fall through to exception
				}
			}
			
			throw new StorageExceptionInvalidConfiguration("Invalid duration: " + value);
		}
		
		protected Duration parseDurationNanos(final String value)
		{
			try
			{
				return Duration.parse(value);
			}
			catch(final DateTimeParseException e)
			{
				try
				{
					return Duration.ofNanos(Long.parseLong(value));
				}
				catch(final NumberFormatException e1)
				{
					// fall through to exception
				}
			}
			
			throw new StorageExceptionInvalidConfiguration("Invalid duration: " + value);
		}
		
		protected int parseFileSize_int(final String value)
		{
			final long fileSize = this.parseFileSize_long(value);
			return fileSize >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)fileSize;
		}
		
		protected long parseFileSize_long(final String value)
		{
			final Matcher m = Pattern.compile("([\\d.,]+)\\s*(\\w+)",Pattern.CASE_INSENSITIVE)
					.matcher(value);
			if(!m.find())
			{
				return Long.parseLong(value);
			}
			
			final String amount = m.group(1);
			final ByteMultiple byteMultiple = ByteMultiple.ofName(m.group(2));
			if(byteMultiple == null)
			{
				throw new StorageExceptionInvalidConfiguration("Invalid file size: " + value);
			}
			
			return byteMultiple.toBytes(Double.parseDouble(amount));
		}
	}
}
