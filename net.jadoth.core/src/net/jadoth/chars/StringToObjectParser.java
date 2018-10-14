package net.jadoth.chars;

import java.io.File;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Thomas Muenz
 *
 */
public interface StringToObjectParser<T> extends Function<String, T>
{
	/**
	 * Tries to parse a given String to an object of type T.
	 * If the attempt fails, a {@link net.jadoth.chars.StringToObjectParser.ParseException} will be thrown.
	 *
	 * @param s the string to be parsed to an object.
	 * @return the object successfully created by parsing the passed string.
	 * @throws StringToObjectParser.ParseException
	 * @see {@link Function}
	 */
	@Override
	public T apply(String s) throws ParseException;




	public final class Constants
	{
		// pattern that recognizes all escaped newlines (\n) but not the escaped escaped ones (\\n).
//		private static final String REGEXP_PATTERN_TRUE_NEWLINE = "([^\\\\])\\\\n";
//		private static final String REGEXP_REPLACEMENT_NEWLINE  = "$1\n";
		/* (26.09.2012 TM)NOTE: The pattern above does not recognize double line breaks.
		 * No time for research, switching to simpler variant. Must be reviewed on demand.
		 */
		private static final String REGEXP_PATTERN_TRUE_NEWLINE = "\\\\n";
		private static final String REGEXP_REPLACEMENT_NEWLINE  = "\n";

		private static final Pattern P = Pattern.compile(REGEXP_PATTERN_TRUE_NEWLINE);

		public static final String parseBackslashNToNewLine(final String s)
		{
			return P.matcher(s).replaceAll(REGEXP_REPLACEMENT_NEWLINE);
		}

		/**
		 * Returns the passed string itself without further actions.<br>
		 * This function is merely for architectural compatability reasons.
		 */
		public static final StringToObjectParser<String> STRING = new StringToObjectParser<String>()
		{
			@Override
			public String apply(final String s) throws ParseException
			{
				return parseBackslashNToNewLine(s);
			}
		};

		public static final StringToObjectParser<Boolean> BOOLEAN = new StringToObjectParser<Boolean>()
		{
			@Override
			public Boolean apply(final String s) throws ParseException
			{
				return Boolean.valueOf(s);
			}
		};

		public static final StringToObjectParser<Integer> INTEGER = new StringToObjectParser<Integer>()
		{
			@Override
			public Integer apply(final String s) throws ParseException
			{
				try
				{
					return Integer.valueOf(s);
				}
				catch(final NumberFormatException e)
				{
					throw new ParseException(e);
				}
			}
		};

		public static final StringToObjectParser<Double> DOUBLE = new StringToObjectParser<Double>()
		{
			@Override
			public Double apply(final String s) throws ParseException
			{
				try
				{
					return Double.valueOf(s);
				}
				catch(final NumberFormatException e)
				{
					throw new ParseException(e);
				}
			}
		};

		public static final StringToObjectParser<Long> LONG = new StringToObjectParser<Long>()
		{
			@Override
			public Long apply(final String s) throws ParseException
			{
				try
				{
					return Long.valueOf(s);
				}
				catch(final NumberFormatException e)
				{
					throw new ParseException(e);
				}
			}
		};

		public static final StringToObjectParser<File> FILE = new StringToObjectParser<File>()
		{
			@Override
			public File apply(final String s) throws ParseException
			{
				try
				{
					return new File(s);
				}
				catch(final NumberFormatException e)
				{
					throw new ParseException(e);
				}
			}
		};

		private Constants()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * An {@link RuntimeException} specific for {@link StringToObjectParser} indicating that the parsing attempt of a
	 * {@link StringToObjectParser} instance failed.
	 *
	 * @author Thomas Muenz
	 */
	public class ParseException extends RuntimeException
	{
		public ParseException()
		{
			super();
		}

		public ParseException(final String message, final Throwable cause)
		{
			super(message, cause);
		}

		public ParseException(final String message)
		{
			super(message);
		}

		public ParseException(final Throwable cause)
		{
			super(cause);
		}



		}
}
