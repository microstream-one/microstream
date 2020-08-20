package one.microstream.storage.configuration;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.storage.exceptions.InvalidStorageConfigurationException;

@FunctionalInterface
public interface DurationParser
{
	public Duration parse(String text, DurationUnit defaultUnit);
	
	
	/**
	 * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> duration parser.
	 * 
	 * @see Duration#parse(CharSequence)
	 */
	public static DurationParser IsoParser()
	{
		return (text, defaultUnit) -> Duration.parse(text);
	}
	
	/**
	 * Case insensitive, suffix based parser, for supported units see {@link DurationUnit}.
	 */
	public static DurationParser Default()
	{
		return new DurationParser.Default();
	}
	
	
	public static class Default implements DurationParser
	{
		private final Pattern pattern = Pattern.compile(
			"(?<amount>[0-9]*)(?:\\s*)(?<unit>[a-z]+)",
			Pattern.CASE_INSENSITIVE
		);
		
		Default()
		{
			super();
		}
		
		@Override
		public Duration parse(
			final String       text       ,
			final DurationUnit defaultUnit
		)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.matches())
			{
				return this.parseDurationWithUnit(
					matcher.group("amount"),
					matcher.group("unit")
				);
			}

			try
			{
				return defaultUnit.create(Long.parseLong(text));
			}
			catch(final NumberFormatException nfe)
			{
				throw new InvalidStorageConfigurationException(
					"Invalid duration: " + text,
					nfe
				);
			}
		}
		
		private Duration parseDurationWithUnit(
			final String amountText,
			final String unitText
		)
		{
			long amount;
			try
			{
				amount = Long.parseLong(amountText);
			}
			catch(final NumberFormatException nfe)
			{
				throw new InvalidStorageConfigurationException(
					"Invalid duration: " + amountText + unitText,
					nfe
				);
			}
			
			try
			{
				return DurationUnit.valueOf(unitText.toUpperCase()).create(amount);
			}
			catch(final IllegalArgumentException e)
			{
				throw new InvalidStorageConfigurationException(
					"Invalid duration: " + amountText + unitText +
					", unknown unit: " + unitText
				);
			}
		}
		
	}
	
}
