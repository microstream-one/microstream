package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
public interface ByteSizeParser
{
	public ByteSize parse(String text);
	
	
	public static ByteSizeParser New()
	{
		return new ByteSizeParser.Default(ByteUnit.B);
	}
		
	public static ByteSizeParser New(
		final ByteUnit defaultUnit
	)
	{
		return new ByteSizeParser.Default(
			notNull(defaultUnit)
		);
	}
	
	
	public static class Default implements ByteSizeParser
	{
		private final Pattern pattern = Pattern.compile(
			"(?<amount>[0-9]*\\.?[0-9]*([eE][-+]?[0-9]+)?)(?:\\s*)(?<unit>[a-z]+)",
			Pattern.CASE_INSENSITIVE
		);
		
		private final ByteUnit defaultUnit;
		
		Default(
			final ByteUnit defaultUnit
		)
		{
			super();
			this.defaultUnit = defaultUnit;
		}
		
		@Override
		public ByteSize parse(
			final String text
		)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.find())
			{
				return this.parseWithUnit(
					matcher.group("amount"),
					matcher.group("unit")
				);
			}

			try
			{
				return ByteSize.New(
					Double.parseDouble(text),
					this.defaultUnit
				);
			}
			catch(final NumberFormatException nfe)
			{
				throw new IllegalArgumentException(
					"Invalid byte size: " + text,
					nfe
				);
			}
		}

		private ByteSize parseWithUnit(
			final String amountText,
			final String unitText
		)
		{
			final double amount;
			try
			{
				amount = Double.parseDouble(amountText);
			}
			catch(final NumberFormatException nfe)
			{
				throw new IllegalArgumentException(
					"Invalid byte size: " + amountText + unitText,
					nfe
				);
			}

			final ByteUnit byteMultiple = ByteUnit.ofName(unitText);
			if(byteMultiple == null)
			{
				throw new IllegalArgumentException(
					"Invalid byte size: " + amountText + unitText +
					", unknown unit: " + unitText
				);
			}

			return ByteSize.New(amount, byteMultiple);
		}
		
	}
	
}
