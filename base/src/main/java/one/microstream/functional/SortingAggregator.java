package one.microstream.functional;

import java.util.Comparator;

import one.microstream.collections.sorting.SortableProcedure;

public interface SortingAggregator<E, R> extends Aggregator<E, R>, SortableProcedure<E>
{
	@Override
	public SortingAggregator<E, R> sort(Comparator<? super E> order);
}
