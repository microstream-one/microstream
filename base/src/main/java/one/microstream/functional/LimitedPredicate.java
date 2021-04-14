package one.microstream.functional;

import static one.microstream.X.notNull;

import java.util.function.Predicate;

import one.microstream.X;


public final class LimitedPredicate<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<? super E> predicate;
	private int skip;
	private int limit;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedPredicate(final Predicate<? super E> predicate, final int skip, final int limit)
	{
		super();
		this.predicate = notNull(predicate);
		this.skip = skip;
		this.limit = limit;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final boolean test(final E e)
	{
		if(!this.predicate.test(e))
		{
			return false;
		}
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
