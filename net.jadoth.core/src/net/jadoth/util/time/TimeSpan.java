package net.jadoth.util.time;

import net.jadoth.util.chars.VarString;

/**
 * @author Thomas Muenz
 *
 */
public interface TimeSpan
{
	public static final long MS_PER_SECOND = 1000;
	public static final long MS_PER_MINUTE = 60 * MS_PER_SECOND;
	public static final long MS_PER_HOUR   = 60 * MS_PER_MINUTE;
	public static final long MS_PER_DAY    = 24 * MS_PER_HOUR;
	public static final long MS_PER_YEAR   = (long)(365.2425 * MS_PER_DAY);

	public static final int INDEX_MILLIS = 5;
	public static final int INDEX_SECOND = 4;
	public static final int INDEX_MINUTE = 3;
	public static final int INDEX_HOUR   = 2;
	public static final int INDEX_DAY    = 1;
	public static final int INDEX_YEAR   = 0;



	public int getYears();

	public int getDays();

	public int getHours();

	public int getMinutes();

	public int getSeconds();

	public int getMilliseconds();

	public long getTime();

	public int[] toArray();

	@Override
	public String toString();

	public String toString(final Format format);

	public VarString assemble(final VarString vc, final Format format);



	public class Format
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final Integer INDEX_MILLIS = TimeSpan.INDEX_MILLIS;
		private static final Integer INDEX_SECOND = TimeSpan.INDEX_SECOND;
		private static final Integer INDEX_MINUTE = TimeSpan.INDEX_MINUTE;
		private static final Integer INDEX_HOUR   = TimeSpan.INDEX_HOUR;
		private static final Integer INDEX_DAY    = TimeSpan.INDEX_DAY;
		private static final Integer INDEX_YEAR   = TimeSpan.INDEX_YEAR;

		public static final String TOKEN_YEAR   = "y";
		public static final String TOKEN_DAY    = "d";
		public static final String TOKEN_HOUR   = "h";
		public static final String TOKEN_MINUTE = "m";
		public static final String TOKEN_SECOND = "s";
		public static final String TOKEN_MILLIS = "S";

		public static final String INDICATOR_START = "%";
		public static final String INDICATOR_END   = "%";

		public static final String MARKER_YEAR   = INDICATOR_START + TOKEN_YEAR   + INDICATOR_END;
		public static final String MARKER_DAY    = INDICATOR_START + TOKEN_DAY    + INDICATOR_END;
		public static final String MARKER_HOUR   = INDICATOR_START + TOKEN_HOUR   + INDICATOR_END;
		public static final String MARKER_MINUTE = INDICATOR_START + TOKEN_MINUTE + INDICATOR_END;
		public static final String MARKER_SECOND = INDICATOR_START + TOKEN_SECOND + INDICATOR_END;
		public static final String MARKER_MILLIS = INDICATOR_START + TOKEN_MILLIS + INDICATOR_END;



		private static Object parse(final String s)
		{
			// (27.11.2010)TODO use String-switch
			if(MARKER_YEAR  .equals(s))
			{
				return INDEX_YEAR;
			}
			if(MARKER_DAY   .equals(s))
			{
				return INDEX_DAY;
			}
			if(MARKER_HOUR  .equals(s))
			{
				return INDEX_HOUR;
			}
			if(MARKER_MINUTE.equals(s))
			{
				return INDEX_MINUTE;
			}
			if(MARKER_SECOND.equals(s))
			{
				return INDEX_SECOND;
			}
			if(MARKER_MILLIS.equals(s))
			{
				return INDEX_MILLIS;
			}
			return s;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final String formatString;
		private final Object[] parsedFormatString;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Format(final String formatString)
		{
			super();
			this.formatString = formatString;

			final String[] strings = formatString.split("(?=%.%)|(?<=%.%)");
			final Object[] parsedElements = new Object[strings.length];

			for(int i = 0, size = strings.length; i < size; i++)
			{
				parsedElements[i] = parse(strings[i]);
			}
			this.parsedFormatString = parsedElements;
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		public String getFormatString()
		{
			return this.formatString;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		public VarString assembleFormat(final VarString vc, final TimeSpan timeSpan)
		{
			final int[] array = timeSpan.toArray();
			for(final Object o : this.parsedFormatString)
			{
				vc.add(o instanceof Integer ? array[(Integer)o] : (String)o);
			}
			return vc;
		}

		public String format(final TimeSpan timeSpan)
		{
			return this.assembleFormat(VarString.New(this.formatString.length()), timeSpan).toString();
		}

	}

	public final class Implementation implements TimeSpan
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		private static final int LITERAL_LENGTH_DAYS    = 3;
		private static final int LITERAL_LENGTH_HOUR    = 2;
		private static final int LITERAL_LENGTH_MINUTES = 2;
		private static final int LITERAL_LENGTH_SECONDS = 2;
		private static final int LITERAL_LENGTH_MILLIS  = 3;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long time   ;

		private final int  years  ;
		private final int  days   ;
		private final int  hours  ;
		private final int  minutes;
		private final int  seconds;
		private final int  millis ;

		// effectively threads-safe, even if two threads calculate concurrently, because the result is always the same
		private transient int[] array;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final long time)
		{
			super();
			this.time = time;
			this.millis = (int)(time % MS_PER_SECOND);
			if(time < MS_PER_SECOND)
			{
				this.years   = 0;
				this.days    = 0;
				this.hours   = 0;
				this.minutes = 0;
				this.seconds = 0;
				return;
			}

			this.seconds = (int)(time % MS_PER_MINUTE / MS_PER_SECOND);
			if(time < MS_PER_SECOND)
			{
				this.years   = 0;
				this.days    = 0;
				this.hours   = 0;
				this.minutes = 0;
				return;
			}

			this.minutes = (int)(time % MS_PER_HOUR   / MS_PER_MINUTE);
			if(time < MS_PER_SECOND)
			{
				this.years   = 0;
				this.days    = 0;
				this.hours   = 0;
				return;
			}

			this.hours   = (int)(time % MS_PER_DAY    / MS_PER_HOUR);
			if(time < MS_PER_SECOND)
			{
				this.years   = 0;
				this.days    = 0;
				return;
			}

			this.days    = (int)(time % MS_PER_YEAR   / MS_PER_DAY);
			if(time < MS_PER_SECOND)
			{
				this.years   = 0;
				return;
			}

			this.years   = (int)(time / MS_PER_YEAR);
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public int getYears()
		{
			return this.years;
		}

		@Override
		public int getDays()
		{
			return this.days;
		}

		@Override
		public int getHours()
		{
			return this.hours;
		}

		@Override
		public int getMinutes()
		{
			return this.minutes;
		}

		@Override
		public int getSeconds()
		{
			return this.seconds;
		}

		@Override
		public int getMilliseconds()
		{
			return this.millis;
		}

		@Override
		public long getTime()
		{
			return this.time;
		}



		@Override
		public int[] toArray()
		{
			if(this.array == null)
			{
				this.array = new int[]
				{
					this.years, this.days, this.hours, this.minutes, this.seconds, this.millis
				};
			}
			return this.array;
		}

		@Override
		public String toString()
		{
			final VarString vc = VarString.New();

			boolean valueFound = false;

			int value = 0;
			if((value = this.years) != 0)
			{
				vc.add(value).add('y', ' ');
				valueFound = true;
			}
			if((value = this.days) != 0 || valueFound)
			{
				vc.padLeft(Integer.toString(value), LITERAL_LENGTH_DAYS, ' ').add('d', ' ');
				valueFound = true;
			}
			if((value = this.hours) != 0 || valueFound)
			{
//				padLeft(vc, Integer.toString(value), 2)
				vc.padLeft(Integer.toString(value), LITERAL_LENGTH_HOUR, ' ').add('h', ' ');
				valueFound = true;
			}
			if((value = this.minutes) != 0 || valueFound)
			{
//				padLeft(vc, Integer.toString(value), 2)
				vc.padLeft(Integer.toString(value), LITERAL_LENGTH_MINUTES, ' ').add('m', ' ');
				valueFound = true;
			}
			if((value = this.seconds) != 0 || valueFound)
			{
				vc.padLeft(Integer.toString(value), LITERAL_LENGTH_SECONDS, ' ').add('s', ' ');
				valueFound = true;
			}
			vc.padLeft(Integer.toString(this.millis), LITERAL_LENGTH_MILLIS, ' ').add('m', 's', ' ');

			return vc.toString();
		}

		@Override
		public String toString(final Format format)
		{
			return format.format(this);
		}

		@Override
		public VarString assemble(final VarString vc, final Format format)
		{
			return format.assembleFormat(vc, this);
		}

	}

}
