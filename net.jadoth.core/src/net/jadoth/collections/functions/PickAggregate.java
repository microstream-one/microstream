package net.jadoth.collections.functions;

import static net.jadoth.Jadoth.BREAK;
import net.jadoth.functional.Aggregator;

public final class PickAggregate<E> implements Aggregator<E, E>
{
	private E found;

	@Override
	public final void accept(final E element)
	{
		this.found = element;
		throw BREAK;
	}

	@Override
	public final E yield()
	{
		return this.found;
	}

}
