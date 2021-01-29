package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@FunctionalInterface
public interface DurationParser
{
	public Duration parse(String text);
	
	
	/**
	 * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> duration parser.
	 * 
	 * @see Duration#parse(CharSequence)
	 */
	public static DurationParser Iso()
	{
		return Duration::parse;
	}
	
	/**
	 * Case insensitive, suffix based parser, with {@link DurationUnit#MS} as default unit.
	 */
	public static DurationParser New()
	{
		return new DurationParser.Default(DurationUnit.MS);
	}
	
	/**
	 * Case insensitive, suffix based parser.
	 */
	public static DurationParser New(
		final DurationUnit defaultUnit
	)
	{
		return new DurationParser.Default(
			notNull(defaultUnit)
		);
	}
	
	
	public static class Default implements DurationParser
	{
		private final Pattern pattern = Pattern.compile(
			"(?<amount>[0-9]*)(?:\\s*)(?<unit>[a-z]+)",
			Pattern.CASE_INSENSITIVE
		);
		
		private final DurationUnit defaultUnit;
		
		Default(
			final DurationUnit defaultUnit
		)
		{
			super();
			this.defaultUnit = defaultUnit;
		}
		
		@Override
		public Duration parse(
			final String text
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
				return this.defaultUnit.create(Long.parseLong(text));
			}
			catch(final NumberFormatException nfe)
			{
				throw new IllegalArgumentException(
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
				throw new IllegalArgumentException(
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
				throw new IllegalArgumentException(
					"Invalid duration: " + amountText + unitText +
					", unknown unit: " + unitText
				);
			}
		}
		
	}
	
}
