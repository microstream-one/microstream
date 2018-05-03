package net.jadoth.collections.functions;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.function.Predicate;

import net.jadoth.X;
import net.jadoth.functional.Aggregator;

public final class AggregateContains<E> implements Aggregator<E, Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private Boolean contains = FALSE;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AggregateContains(final Predicate<? super E> predicate)
	{
		super();
		this.predicate = predicate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(this.predicate.test(element))
		{
			this.contains = TRUE;
			throw X.BREAK();
		}
	}

	@Override
	public final Boolean yield()
	{
		return this.contains;
	}

}
