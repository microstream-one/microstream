package net.jadoth.cql;

import java.util.Comparator;

import net.jadoth.collections.XIterable;
import net.jadoth.collections.sorting.SortableProcedure;
import net.jadoth.functional.Aggregator;
import java.util.function.Consumer;

public final class CqlWrapperCollectorSequenceSorting<O, R extends Consumer<O> & XIterable<O>>
implements Aggregator<O, R>
{
	final R target;
	final Comparator<? super O> order;

	CqlWrapperCollectorSequenceSorting(final R target, final Comparator<? super O> order)
	{
		super();
		this.target = target;
		this.order  = order ;
	}

	@Override
	public final void accept(final O element)
	{
		this.target.accept(element);
	}

	@Override
	public final R yield()
	{
		SortableProcedure.sortIfApplicable(this.target, this.order);
		return this.target;
	}

}
