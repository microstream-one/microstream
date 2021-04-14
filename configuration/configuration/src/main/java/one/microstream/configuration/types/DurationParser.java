package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parser for {@link Duration}s out of textual representation.
 * 
 */
@FunctionalInterface
public interface DurationParser
{
	/**
	 * Tries to parse a {@link Duration} out of <code>text</code>.
	 * It usually consists of an amount and an unit, e.g. <code>"1S"</code>
	 * or the ISO format, as described here {@link Duration#parse(CharSequence)}.
	 * 
	 * @param text the textual input
	 * @return the parsed {@link Duration}
	 * @throws IllegalArgumentException if the text couldn't be parsed to a {@link Duration}
	 * @see DurationUnit
	 */
	public Duration parse(String text);
	
	/**
	 * Pseudo-constructor method to create a new {@link DurationParser}, with {@link DurationUnit#MS} as default unit.
	 */
	public static DurationParser New()
	{
		return new DurationParser.Default(DurationUnit.MS);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link DurationParser}.
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
		private final static Pattern ISO_PATTERN = Pattern.compile(
			"([-+]?)P(?:([-+]?[0-9]+)D)?" +
				"(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?",
			Pattern.CASE_INSENSITIVE
		);
		
		private final static Pattern AMOUNT_UNIT_PATTERN = Pattern.compile(
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
			if(ISO_PATTERN.matcher(text).matches())
			{
				// ISO format
				
				try
				{
					return Duration.parse(text);
				}
				catch(final DateTimeParseException e)
				{
					throw new IllegalArgumentException(
						"Invalid duration: " + text,
						e
					);
				}
			}
			
			final Matcher matcher = AMOUNT_UNIT_PATTERN.matcher(text);
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
