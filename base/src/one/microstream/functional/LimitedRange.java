package one.microstream.functional;

import java.util.function.Predicate;

import one.microstream.X;


public final class LimitedRange<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int skip;
	private int limit;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedRange(final int skip, final int limit)
	{
		super();
		this.skip  = skip ;
		this.limit = limit;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final boolean test(final E e)
	{
		if(this.skip > 0)
		{
			this.skip--;
			return false;
		}
		if(this.limit > 0)
		{
			this.limit--;
			return true;
		}
		throw X.BREAK();
	}

}
