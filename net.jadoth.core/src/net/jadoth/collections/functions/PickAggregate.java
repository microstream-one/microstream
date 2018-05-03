package net.jadoth.collections.functions;

import net.jadoth.X;
import net.jadoth.functional.Aggregator;

public final class PickAggregate<E> implements Aggregator<E, E>
{
	private E found;

	@Override
	public final void accept(final E element)
	{
		this.found = element;
		throw X.BREAK();
	}

	@Override
	public final E yield()
	{
		return this.found;
	}

}
