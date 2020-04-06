package one.microstream.storage.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.bytes.ByteMultiple;
import one.microstream.storage.exceptions.InvalidStorageConfigurationException;

@FunctionalInterface
public interface FileSizeParser
{
	public long parseFileSize(String text, ByteMultiple defaultByteMultiple);

	
	public static FileSizeParser Default()
	{
		return new FileSizeParser.Default();
	}
	
	
	public static class Default implements FileSizeParser
	{
		private final Pattern pattern = Pattern.compile(
			"(?<amount>[0-9]*\\.?[0-9]*([eE][-+]?[0-9]+)?)(?:\\s*)(?<unit>[a-z]+)",
			Pattern.CASE_INSENSITIVE
		);
		
		Default()
		{
			super();
		}
		
		@Override
		public long parseFileSize(final String text, final ByteMultiple defaultByteMultiple)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.find())
			{
				return this.parseFileSizeWithUnit(
					matcher.group("amount"),
					matcher.group("unit")
				);
			}
			
			try
			{
				return defaultByteMultiple.toBytes(Double.parseDouble(text));
			}
			catch(final NumberFormatException nfe)
			{
				throw new InvalidStorageConfigurationException(
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
				throw new InvalidStorageConfigurationException(
					"Invalid file size: " + amountText + unitText,
					nfe
				);
			}
			
			final ByteMultiple byteMultiple = ByteMultiple.ofName(unitText);
			if(byteMultiple == null)
			{
				throw new InvalidStorageConfigurationException(
					"Invalid file size: " + amountText + unitText +
					", unknown unit: " + unitText
				);
			}
			
			return byteMultiple.toBytes(amount);
		}
		
	}
	
}
