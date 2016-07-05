package net.jadoth.collections.functions;

import java.util.Comparator;

import net.jadoth.functional.Aggregator;

public final class AggregateMin<E> implements Aggregator<E, E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final Comparator<? super E> comparator;
	private       E                     currentMin;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AggregateMin(final Comparator<? super E> comparator)
	{
		super();
		this.comparator = comparator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(this.comparator.compare(element, this.currentMin) < 0)
		{
			this.currentMin = element;
		}
	}

	@Override
	public final E yield()
	{
		return this.currentMin;
	}

}
