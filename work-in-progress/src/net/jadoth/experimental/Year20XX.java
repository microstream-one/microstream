package net.jadoth.experimental;

import java.util.Comparator;

import net.jadoth.hashing.HashEqualator;
import net.jadoth.typing.ValueType;

/**
 * Sample / template value type of questionable practical use on its own.
 *
 * @author Thomas Muenz
 */
public final class Year20XX extends Number implements ValueType
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	public static final int MIN_VALUE = 2000;
	public static final int MAX_VALUE = 2099;

	private static final Year20XX[] cache = new Year20XX[MAX_VALUE - MIN_VALUE + 1];



	///////////////////////////////////////////////////////////////////////////
	// functions        //
	/////////////////////

	public static final HashEqualator<Year20XX> equality = new HashEqualator<Year20XX>(){
		@Override
		public boolean equal(final Year20XX kj1, final Year20XX kj2)
		{
			return Year20XX.equal(kj1, kj2);
		}
		@Override
		public int hash(final Year20XX kj)
		{
			return Year20XX.hash(kj);
		}
	};

	public static final Comparator<Year20XX> comparator = new Comparator<Year20XX>(){
		@Override
		public int compare(final Year20XX kj1, final Year20XX kj2)
		{
			if(kj1 == null)
			{
				return kj2 == null ?0 :1;
			}
			return kj2 == null ?-1 :kj1.value - kj2.value; // overflow-save, due to limitation of [2000;2100[
		}
	};

	public static final Comparator<Year20XX> nonNullComparator = new Comparator<Year20XX>(){
		@Override
		public int compare(final Year20XX kj1, final Year20XX kj2)
		{
			return kj1.value - kj2.value; // overflow-save, due to limitation of [2000;2100[
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static boolean equal(final Year20XX kj1, final Year20XX kj2)
	{
		return kj1 == null ?kj2 == null :kj2 != null && kj1.value == kj2.value;
	}

	public static int hash(final Year20XX kj)
	{
		return kj == null ?0 :kj.value;
	}



	///////////////////////////////////////////////////////////////////////////
	// factory methods //
	////////////////////

	public static Year20XX get(final int year) throws IllegalArgumentException
	{
		if(year < MIN_VALUE || year > MAX_VALUE)
		{
			throw new IllegalArgumentException();
		}
		if(cache[year - MIN_VALUE] == null)
		{
			cache[year - MIN_VALUE] = new Year20XX(year);
		}
		return cache[year - MIN_VALUE];
	}

	public static Year20XX create(final Integer year) throws IllegalArgumentException
	{
		// interesting: is null supposed to be an error or to cause returning of null
		return year == null ?null :get(year.intValue());
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final int value;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	Year20XX(final int value)
	{
		super();
		this.value = value;
	}


	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public Year20XX prev()
	{
		return Year20XX.get(this.value - 1);
	}

	public Year20XX next()
	{
		return Year20XX.get(this.value + 1);
	}

	public boolean isBefore(final Year20XX other)
	{
		return this.value < other.value;
	}

	public boolean isAfter(final Year20XX other)
	{
		return this.value > other.value;
	}

	public boolean isSame(final Year20XX other)
	{
		return this.value == other.value;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean equals(final Object obj)
	{
		// true ValueType (value as primary identity, immutable), thus equals() and hashCode() applicable
		if(obj == this)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(obj instanceof Year20XX)
		{
			return this.value == ((Year20XX)obj).value;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		// true ValueType (value as primary identity, immutable), thus equals() and hashCode() applicable
		return this.value;
	}

	@Override
	public String toString()
	{
		return Integer.toString(this.value);
	}

	@Override
	public int intValue()
	{
		return this.value;
	}

	@Override
	public long longValue()
	{
		return this.value;
	}

	@Override
	public float floatValue()
	{
		return this.value;
	}

	@Override
	public double doubleValue()
	{
		return this.value;
	}



}
