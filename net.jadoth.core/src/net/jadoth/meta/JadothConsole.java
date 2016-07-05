package net.jadoth.meta;

import static net.jadoth.Jadoth.BREAK;
import static net.jadoth.util.time.JadothTime.now;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.concurrent.JadothThreads;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;



/**
 * @author Thomas Muenz
 *
 */
public final class JadothConsole
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final transient int              LINE_BUFFER_INITIAL_SIZE = 256                                 ;
	private static final transient int              SOURCE_POSITION_PADDING  = 64                                  ;
	private static final transient DecimalFormat    DECIMAL_FORMAT_NANOS     = new DecimalFormat("00,000,000,000") ;
	private static final transient char[]           TIME_SEPERATOR           = {'>', ' '}                          ;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////


	static String formatTimestamp(final Date timestamp)
	{
		// JDK geniuses can't write immutable types properly immutable.
		return new SimpleDateFormat("HH:mm:ss.SSS").format(timestamp);
	}

	public static final void debugln(final String s)
	{
		debugln(s, 1);
	}

	public static final void debugln(final String s, final int stackTraceCut)
	{
		// index 1 is always safely this method call itself, index 2 is always safely the calling context
		final String e = JadothThreads.getStackTraceElement(2 + stackTraceCut).toString();

		// every StackTraceElement string is guaranteed to be in the pattern [class].[method]([class].java:[line])
		System.out.println(
			VarString.New(LINE_BUFFER_INITIAL_SIZE)
			.padRight(e.substring(e.lastIndexOf('.', e.lastIndexOf('.') - 1)), SOURCE_POSITION_PADDING, ' ')
			.add(formatTimestamp(now()))
			.add(TIME_SEPERATOR)
			.add(s))
		;
	}

	public static final void printlnElapsedNanos(final long elapsedTime)
	{
		System.out.println("Elapsed Time: " + DECIMAL_FORMAT_NANOS.format(elapsedTime));
	}

	public static final void printCollection(
		final XGettingCollection<?> collection,
		final String                start     ,
		final String                separator ,
		final String                end       ,
		final Integer               limit
	)
	{
		final char[] sepp = separator != null ? separator.toCharArray() : null;

		final VarString vc = VarString.New();
		if(start != null)
		{
			vc.add(start);
		}
		final int vcOldLength = vc.length();
		if(limit == null)
		{
			collection.iterate(new Consumer<Object>()
			{
				@Override
				public void accept(final Object e)
				{
					vc.add(e);
					if(sepp != null)
					{
						vc.add(sepp);
					}
				}
			});
		}
		else
		{
			collection.iterate(new Consumer<Object>()
			{
				private int lim = limit;
				@Override
				public void accept(final Object e)
				{
					if(--this.lim <= 0)
					{
						throw BREAK;
					}
					vc.add(e);
					if(sepp != null)
					{
						vc.add(sepp);
					}
				}
			});
		}
		if(sepp != null && vc.length() > vcOldLength)
		{
			vc.deleteLast(sepp.length);
		}

		if(end != null)
		{
			vc.add(end);
		}

		System.out.println(vc.toString());
		System.out.flush();
	}

	public static final VarString assembleTable(
		final VarString           vs        ,
		final XGettingTable<?, ?> collection,
		final String              start     ,
		final String              mapper    ,
		final String              separator ,
		final String              end       ,
		final Integer             limit
	)
	{
		final char[] sepp = separator != null ? separator.toCharArray() : null;
		if(start != null)
		{
			vs.add(start);
		}
		final int vcOldLength = vs.length();
		if(limit == null)
		{
			collection.iterate(new Consumer<KeyValue<?, ?>>()
			{
				@Override
				public void accept(final KeyValue<?, ?> e)
				{
					vs.add(e.key());
					if(mapper != null)
					{
						vs.add(mapper);
					}
					vs.add(e.value());
					if(sepp != null)
					{
						vs.add(sepp);
					}
				}
			});
		}
		else
		{
			collection.iterate(new Consumer<KeyValue<?, ?>>()
			{
				private int lim = limit;
				@Override
				public void accept(final KeyValue<?, ?> e)
				{
					if(--this.lim <= 0)
					{
						throw BREAK;
					}
					vs.add(e.key());
					if(mapper != null)
					{
						vs.add(mapper);
					}
					vs.add(e.value());
					if(sepp != null)
					{
						vs.add(sepp);
					}
				}
			});
		}
		if(sepp != null && vs.length() > vcOldLength)
		{
			vs.deleteLast(sepp.length);
		}
		if(end != null)
		{
			vs.add(end);
		}
		return vs;
	}

	public static final void printTable(
		final XGettingTable<?, ?> collection,
		final String              start     ,
		final String              mapper    ,
		final String              separator ,
		final String              end       ,
		final Integer             limit
	)
	{
		System.out.println(assembleTable(VarString.New(), collection, start, mapper, separator, end, limit));
	}

	public static final void printArray(
		final Object[] array,
		final String start,
		final String separator,
		final String end,
		final Integer limit
	)
	{
		final char[] sepp = separator != null ? separator.toCharArray() : null;

		final VarString vc = VarString.New();
		if(start != null)
		{
			vc.add(start);
		}
		final int size = limit == null ? array.length : Math.min(array.length, limit);
		for(int i = 0; i < size; i++)
		{
			vc.add(array[i]);
			if(sepp != null)
			{
				vc.add(sepp);
			}
		}
		if(size > 1 && sepp != null)
		{
			vc.deleteLast(sepp.length);
		}
		if(end != null)
		{
			vc.add(end);
		}

		System.out.println(vc.toString());
		System.out.flush();
	}



	private JadothConsole()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
