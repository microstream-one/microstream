package net.jadoth.collections.functions;

import net.jadoth.collections.types.XPuttingCollection;
import net.jadoth.functional.Aggregator;

public class AggregateCountingPut<E> implements Aggregator<E, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XPuttingCollection<? super E> target;
	private int putCount;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateCountingPut(final XPuttingCollection<? super E> target)
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
		if(this.target.put(element))
		{
			this.putCount++;
		}
	}

	@Override
	public final Integer yield()
	{
		return this.putCount;
	}

}
