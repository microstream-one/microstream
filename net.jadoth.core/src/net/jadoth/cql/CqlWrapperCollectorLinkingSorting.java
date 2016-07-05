package net.jadoth.cql;

import java.util.Comparator;

import net.jadoth.collections.sorting.Sortable;
import net.jadoth.functional.Aggregator;
import net.jadoth.functional.BiProcedure;

public final class CqlWrapperCollectorLinkingSorting<O, R extends Sortable<O>> implements Aggregator<O, R>
{
	final R                     target;
	final BiProcedure<O, R>     linker;
	final Comparator<? super O> order ;

	CqlWrapperCollectorLinkingSorting(final R target, final BiProcedure<O, R> linker, final Comparator<? super O> order)
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
