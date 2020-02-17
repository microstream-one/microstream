package one.microstream.storage.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.bytes.ByteMultiple;
import one.microstream.storage.exceptions.StorageExceptionInvalidConfiguration;

@FunctionalInterface
public interface FileSizeParser
{
	public long parseFileSize(String text);
	
	
	public static FileSizeParser SuffixBasedParser()
	{
		return new SuffixBasedParser();
	}
	
	public static FileSizeParser Default()
	{
		return SuffixBasedParser();
	}
	
	
	public static class SuffixBasedParser implements FileSizeParser
	{
		private final Pattern pattern = Pattern.compile(
			"([\\d.,]+)\\s*(\\w+)",
			Pattern.CASE_INSENSITIVE
		);
		
		SuffixBasedParser()
		{
			super();
		}
		
		@Override
		public long parseFileSize(final String text)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.find())
			{
				final String amountGroup = matcher.group(1);
				final String unitGroup   = matcher.group(2);
				
				double amount;
				try
				{
					amount = Double.parseDouble(amountGroup);
				}
				catch(final NumberFormatException nfe)
				{
					throw new StorageExceptionInvalidConfiguration("Invalid file size: " + text, nfe);
				}
				
				final ByteMultiple byteMultiple = ByteMultiple.ofName(unitGroup);
				if(byteMultiple != null)
				{
					return byteMultiple.toBytes(amount);
				}
			}
			else // missing suffix is interpreted as size in bytes
			{
				try
				{
					return Long.parseLong(text);
				}
				catch(final NumberFormatException nfe)
				{
					throw new StorageExceptionInvalidConfiguration("Invalid file size: " + text, nfe);
				}
			}
			
			throw new StorageExceptionInvalidConfiguration("Invalid file size: " + text);
		}
		
	}
	
}
