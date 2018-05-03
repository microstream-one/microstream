package net.jadoth.cql;

import static net.jadoth.X.notNull;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.XIterable;
import net.jadoth.collections.types.XSequence;

public interface CqlSelection<I> extends CqlProjection<I, I>, CqlTransfer<I, XSequence<I>>
{
	@Override
	public default CqlSelection<I> skip(final Number count)
	{
		return CqlSelection.New(
			this.getSource()  ,
			CQL.asLong(count),
			this.getLimit()   ,
			this.getSelector(),
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlSelection<I> limit(final Number count)
	{
		return CqlSelection.New(
			this.getSource()  ,
			this.getSkip()    ,
			CQL.asLong(count),
			this.getSelector(),
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlSelection<I> select(final Predicate<? super I> selector)
	{
		return CqlSelection.New(
			this.getSource()  ,
			this.getSkip()    ,
			this.getLimit()   ,
			selector          ,
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlSelection<I> from(final XIterable<? extends I> source)
	{
		return CqlSelection.New(
			source            ,
			this.getSkip()    ,
			this.getLimit()   ,
			this.getSelector(),
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlSelection<I> orderBy(final Comparator<? super I> order)
	{
		return CqlSelection.New(
			this.getSource()  ,
			this.getSkip()    ,
			this.getLimit()   ,
			this.getSelector(),
			order             ,
			this.getResultor()
		);
	}

	@Override
	public default <P extends Consumer<I> & XIterable<I>> CqlTransfer<I, P> into(final P target)
	{
		return CqlTransfer.New(
			this.getSource()    ,
			this.getSkip()      ,
			this.getLimit()     ,
			this.getSelector()  ,
			this.getOrder()     ,
			CqlResultor.New(target)
		);
	}

	@Override
	public default <X extends XIterable<I>> CqlTransfer<I, X> into(final CqlResultor<I, X> resultor)
	{
		return CqlTransfer.New(
			this.getSource()    ,
			this.getSkip()      ,
			this.getLimit()     ,
			this.getSelector()  ,
			this.getOrder()     ,
			notNull(resultor)
		);
	}

	@Override
	public default <R1> CqlAggregation<I, R1> over(final CqlResultor<I, R1> resultor)
	{
		return CqlAggregation.New(
			this.getSource()    ,
			this.getSkip()      ,
			this.getLimit()     ,
			this.getSelector()  ,
			this.getOrder()     ,
			notNull(resultor)
		);
	}

	@Override
	public default <P extends Consumer<? super I>> P iterate(final P procedure)
	{
		this.execute().iterate(procedure);
		return procedure;
	}

	public static <I> CqlSelection<I> New()
	{
		return new Implementation<>(null, null, null, null, null, null);
	}

	public static <I> CqlSelection<I> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Comparator<? super I>  comparator
	)
	{
		return new Implementation<>(source, skip, limit, selector, comparator, CqlResultor.New());
	}

	public static <I> CqlSelection<I> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Comparator<? super I>  comparator,
		final XSequence<I>           target
	)
	{
		return new Implementation<>(source, skip, limit, selector, comparator, CqlResultor.New(target));
	}

	public static <I> CqlSelection<I> New(
		final XIterable<? extends I>    source    ,
		final Long                      skip      ,
		final Long                      limit     ,
		final Predicate<? super I>      selector  ,
		final Comparator<? super I>     comparator,
		final CqlResultor<I, XSequence<I>> resultor
	)
	{
		return new Implementation<>(source, skip, limit, selector, comparator, resultor);
	}


	final class Implementation<I> extends CqlQuery.Abstract<I, I, XSequence<I>> implements CqlSelection<I>
	{
		Implementation(
			final XIterable<? extends I>    source    ,
			final Long                      skip      ,
			final Long                      limit     ,
			final Predicate<? super I>      selector  ,
			final Comparator<? super I>     comparator,
			final CqlResultor<I, XSequence<I>> resultor
		)
		{
			super(source, skip, limit, selector, null, comparator, resultor);
		}

	}

}
