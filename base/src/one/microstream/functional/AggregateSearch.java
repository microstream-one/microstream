package one.microstream.functional;

import java.util.function.Predicate;

import one.microstream.X;

public class AggregateSearch<E> implements Aggregator<E, E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private E found;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateSearch(final Predicate<? super E> predicate)
	{
		super();
		this.predicate = predicate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void accept(final E element)
	{
		if(this.predicate.test(element))
		{
			this.found = element;
			throw X.BREAK();
		}
	}

	@Override
	public E yield()
	{
		return this.found;
	}

}
