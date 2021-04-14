package one.microstream.functional;

import one.microstream.collections.types.XAddingCollection;

public class AggregateCountingAdd<E> implements Aggregator<E, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XAddingCollection<? super E> target;
	private int addCount;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateCountingAdd(final XAddingCollection<? super E> target)
	{
		super();
		this.target = target;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(this.target.add(element))
		{
			this.addCount++;
		}
	}

	@Override
	public final Integer yield()
	{
		return this.addCount;
	}

}
