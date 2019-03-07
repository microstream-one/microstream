package net.jadoth.functional;

import java.util.Comparator;

import net.jadoth.collections.sorting.SortableProcedure;

public interface SortingAggregator<E, R> extends Aggregator<E, R>, SortableProcedure<E>
{
	@Override
	public SortingAggregator<E, R> sort(Comparator<? super E> order);
}
