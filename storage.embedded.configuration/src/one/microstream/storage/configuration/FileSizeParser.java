package one.microstream.storage.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.bytes.ByteMultiple;
import one.microstream.storage.exceptions.StorageExceptionInvalidConfiguration;

@FunctionalInterface
public interface FileSizeParser
{
	public long parseFileSize(String text);

	
	public static FileSizeParser Default()
	{
		return new FileSizeParser.Default();
	}
	
	
	public static class Default implements FileSizeParser
	{
		private final Pattern pattern = Pattern.compile(
			"([\\d.,]+)\\s*(\\w+)",
			Pattern.CASE_INSENSITIVE
		);
		
		Default()
		{
			super();
		}
		
		@Override
		public long parseFileSize(final String text)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.find())
			{
				return this.parseFileSizeWithUnit(
					matcher.group(1),
					matcher.group(2)
				);
			}
			
			// missing unit is interpreted as size in bytes
			try
			{
				return Long.parseLong(text);
			}
			catch(final NumberFormatException nfe)
			{
				throw new StorageExceptionInvalidConfiguration(
					"Invalid file size: " + text,
					nfe
				);
			}
		}

		private long parseFileSizeWithUnit(
			final String amountText,
			final String unitText
		)
		{
			double amount;
			try
			{
				amount = Double.parseDouble(amountText);
			}
			catch(final NumberFormatException nfe)
			{
				throw new StorageExceptionInvalidConfiguration(
					"Invalid file size: " + amountText + unitText,
					nfe
				);
			}
			
			final ByteMultiple byteMultiple = ByteMultiple.ofName(unitText);
			if(byteMultiple == null)
			{
				throw new StorageExceptionInvalidConfiguration(
					"Invalid file size: " + amountText + unitText +
					", unknown unit: " + unitText
				);
			}
			
			return byteMultiple.toBytes(amount);
		}
		
	}
	
}
