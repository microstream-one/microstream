package one.microstream.util.cql;

import java.util.Comparator;
import java.util.function.BiConsumer;

import one.microstream.collections.sorting.Sortable;
import one.microstream.functional.Aggregator;

public final class CqlWrapperCollectorLinkingSorting<O, R extends Sortable<O>> implements Aggregator<O, R>
{
	final R                     target;
	final BiConsumer<O, R>     linker;
	final Comparator<? super O> order ;

	CqlWrapperCollectorLinkingSorting(final R target, final BiConsumer<O, R> linker, final Comparator<? super O> order)
	{
		super();
		this.target = target;
		this.linker = linker;
		this.order  = order ;
	}

	@Override
	public final void accept(final O element)
	{
		this.linker.accept(element, this.target);
	}

	@Override
	public final R yield()
	{
		this.target.sort(this.order);
		return this.target;
	}

}
