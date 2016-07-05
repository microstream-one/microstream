package net.jadoth.collections.functions;

import java.util.function.Predicate;

import net.jadoth.functional.Aggregator;

public final class AggregateArrayAdder<E> implements Aggregator<E, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final Predicate<? super E> predicate;
	private final E[] array;
	private int i;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AggregateArrayAdder(final Predicate<? super E> predicate, final E[] array, final int i)
	{
		super();
		this.predicate = predicate;
		this.array = array;
		this.i = i;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		if(!this.predicate.test(e))
		{
			return;
		}
		this.array[this.i++] = e;
	}

	@Override
	public final Integer yield()
	{
		return this.i;
	}

}
