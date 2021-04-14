package one.microstream.functional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.function.Predicate;

import one.microstream.X;

public class AggregateApplies<E> implements Aggregator<E, Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Boolean applies = TRUE;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateApplies(final Predicate<? super E> predicate)
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
		if(!this.predicate.test(element))
		{
			this.applies = FALSE;
			throw X.BREAK();
		}
	}

	@Override
	public final Boolean yield()
	{
		return this.applies;
	}

}
