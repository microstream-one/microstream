package one.microstream.util.cql;

import java.util.Comparator;
import java.util.function.Consumer;

import one.microstream.collections.sorting.SortableProcedure;
import one.microstream.functional.SortingAggregator;

public final class CqlWrapperCollectorProcedure<O, T extends Consumer<O>> implements SortingAggregator<O, T>
{
	final T target;

	CqlWrapperCollectorProcedure(final T target)
	{
		super();
		this.target = target;
	}

	@Override
	public final void accept(final O element)
	{
		this.target.accept(element);
	}

	@Override
	public final T yield()
	{
		return this.target;
	}

	@Override
	public CqlWrapperCollectorProcedure<O, T> sort(final Comparator<? super O> order)
	{
		SortableProcedure.<O>sortIfApplicable(this.target, order);
		return this;
	}

}
