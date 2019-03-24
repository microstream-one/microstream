package one.microstream.time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public final class XTime
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final int
		MIN_MONTH        =   1,
		MIN_DAY_IN_MONTH =   1,
		MIN_HOUR         =   0,
		MIN_MINUTE       =   0,
		MIN_SECOND       =   0,
		MIN_MILLI        =   0,
		MAX_MONTH        =  12,
		MAX_DAY_IN_MONTH =  31,
		MAX_HOUR         =  23,
		MAX_MINUTE       =  59,
		MAX_SECOND       =  59,
		MAX_MILLI        = 999
	;
	
	
	
	/**
	 * Short cut for <code>new Date(System.currentTimeMillis())</code>.
	 * Returns a new {@link Date} instance representing the current time in the current {@link TimeZone}
	 * and for the current {@link Locale}.
	 * @return right now!
	 */
	public static final Date now()
	{
		return new Date();
	}

	public static final Date timestamp(
		final int year,
		final int month,
		final int day,
		final int hour,
		final int minute,
		final int second,
		final int milliseconds
	)
	{
		if(month < MIN_MONTH || month > MAX_MONTH)
		{
			throw new IllegalArgumentException("Invalid month: " + month);
		}
		if(day < MIN_DAY_IN_MONTH || day > MAX_DAY_IN_MONTH)
		{
			throw new IllegalArgumentException("Invalid day: " + day);
		}
		if(hour < MIN_HOUR || hour > MAX_HOUR)
		{
			throw new IllegalArgumentException("Invalid hour: " + hour);
		}
		if(minute < MIN_MINUTE || minute > MAX_MINUTE)
		{
			throw new IllegalArgumentException("Invalid minute: " + minute);
		}
		if(second < MIN_SECOND || second > MAX_SECOND)
		{
			throw new IllegalArgumentException("Invalid second: " + second);
		}
		if(milliseconds < MIN_MILLI || milliseconds > MAX_MILLI)
		{
			throw new IllegalArgumentException("Invalid milliseconds: " + milliseconds);
		}

		final Calendar c = Calendar.getInstance();
		c.clear();
		c.set(year, month - 1, day, hour, minute, second);
		c.set(Calendar.MILLISECOND, milliseconds);
		return c.getTime();
	}

	public static final Date timestamp(
		final int year,
		final int month,
		final int day,
		final int hour,
		final int minute,
		final int second
	)
	{
		return timestamp(year, month, day, hour, minute, second, 0);
	}

	public static final Date date(
		final int year,
		final int month,
		final int day
	)
	{
		return timestamp(year, month, day);
	}

	public static final Date timestamp(
		final int year,
		final int month,
		final int day
	)
	{
		return timestamp(year, month, day, 0, 0, 0, 0);
	}

	public static final TimeSpan TimeSpan(final long time)
	{
		return new TimeSpan.Implementation(time);
	}


	public static final GregorianCalendar asGregCal(final Date date)
	{
		// stupid old JDK stuff. Everything has to be done manually -.-. Boiler plate code to the max.
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		return gc;
	}

	public static final GregorianCalendar asGregCal(final long timestamp)
	{
		// stupid old JDK stuff. Everything has to be done manually -.-. Boiler plate code to the max.
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp);
		return gc;
	}


	public static final int currentYear()
	{
		/* I wonder if any of those JDK hackers ever use the stuff they're knitting in real world projects ^^
		 * Not only is "reading more important than writing", but this is even a mess to type, too. o_0
		 */
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	private XTime()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
