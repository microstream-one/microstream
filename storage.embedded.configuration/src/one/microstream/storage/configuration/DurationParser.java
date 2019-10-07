package one.microstream.storage.configuration;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.storage.exceptions.StorageExceptionInvalidConfiguration;

@FunctionalInterface
public interface DurationParser
{
	public Duration parse(String text);
	
	
	/**
	 * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> duration parser.
	 * 
	 * @see Duration#parse(CharSequence)
	 */
	public static DurationParser IsoParser()
	{
		return Duration::parse;
	}
	
	/**
	 * Case insensitive suffix based parser, supported units:
	 * <ul>
	 * <li>ns</li>
	 * <li>ms</li>
	 * <li>s</li>
	 * <li>m</li>
	 * <li>h</li>
	 * <li>d</li>
	 * </ul>
	 */
	public static DurationParser SuffixBasedParser()
	{
		return new SuffixBasedParser();
	}
	
	public static DurationParser Default()
	{
		return SuffixBasedParser();
	}
	
	
	public static class SuffixBasedParser implements DurationParser
	{
		private final Pattern pattern = Pattern.compile("([\\d]+)\\s*(\\w+)",Pattern.CASE_INSENSITIVE);
		
		protected SuffixBasedParser()
		{
			super();
		}
		
		@Override
		public Duration parse(final String text)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.find())
			{
				final String amountGroup = matcher.group(1);
				final String unitGroup   = matcher.group(2);
				
				long amount;
				try
				{
					amount = Long.parseLong(amountGroup);
				}
				catch(final NumberFormatException nfe)
				{
					throw new StorageExceptionInvalidConfiguration("Invalid Duration: " + text, nfe);
				}
				
				switch(unitGroup.toLowerCase())
				{
					case "ns": return Duration.ofNanos(amount);
					case "ms": return Duration.ofMillis(amount);
					case "s" : return Duration.ofSeconds(amount);
					case "m" : return Duration.ofMinutes(amount);
					case "h" : return Duration.ofHours(amount);
					case "d" : return Duration.ofDays(amount);
				}
			}
			
			throw new StorageExceptionInvalidConfiguration("Invalid Duration: "+text);
		}
	}
}
