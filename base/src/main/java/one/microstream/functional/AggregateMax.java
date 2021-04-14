package one.microstream.functional;

import java.util.Comparator;

public final class AggregateMax<E> implements Aggregator<E, E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super E> comparator;
	private       E                     currentMax;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateMax(final Comparator<? super E> comparator)
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
		if(this.comparator.compare(element, this.currentMax) >= 0)
		{
			this.currentMax = element;
		}
	}

	@Override
	public final E yield()
	{
		return this.currentMax;
	}

}
