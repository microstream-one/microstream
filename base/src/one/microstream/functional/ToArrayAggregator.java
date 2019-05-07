package one.microstream.functional;

import static one.microstream.X.notNull;

public final class ToArrayAggregator<E> implements Aggregator<E, E[]>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final E[] array;

	int i = -1;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ToArrayAggregator(final E[] array)
	{
		super();
		this.array = notNull(array);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void accept(final E element)
	{
		this.array[++this.i] = element;
	}

	@Override
	public final E[] yield()
	{
		return this.array;
	}

}
