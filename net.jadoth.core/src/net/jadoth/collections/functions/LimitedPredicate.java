package net.jadoth.collections.functions;

import static net.jadoth.Jadoth.BREAK;
import static net.jadoth.Jadoth.notNull;

import java.util.function.Predicate;


public final class LimitedPredicate<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final Predicate<? super E> predicate;
	private int skip;
	private int limit;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
		throw BREAK;
	}

}
