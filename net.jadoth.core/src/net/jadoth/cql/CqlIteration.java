package net.jadoth.cql;

import static net.jadoth.X.notNull;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.XIterable;
import net.jadoth.collections.types.XSequence;
import net.jadoth.functional.Aggregator;

public interface CqlIteration<I, O, R extends XIterable<O>> extends CqlQuery<I, O, R>, XIterable<O>
{
	// fluent API //

	@Override
	public default CqlIteration<I, O, R> skip(final Number count) // Number to allow fluent API use of int values
	{
		return CqlIteration.New(
			this.getSource()   ,
			CQL.asLong(count) ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getProjector(),
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlIteration<I, O, R> limit(final Number count) // Number to allow fluent API use of int values
	{
		return CqlIteration.New(
			this.getSource()   ,
			this.getSkip()     ,
			CQL.asLong(count) ,
			this.getSelector() ,
			this.getProjector(),
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlIteration<I, O, R> select(final Predicate<? super I> selector)
	{
		return CqlIteration.New(
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			selector           ,
			this.getProjector(),
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlIteration<I, O, R> orderBy(final Comparator<? super O> order)
	{
		return CqlIteration.New(
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getProjector(),
			order              ,
			this.getResultor()
		);
	}

	@Override
	public default CqlIteration<I, O, R> from(final XIterable<? extends I> source)
	{
		return CqlIteration.New(
			source             ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getProjector(),
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default <P extends Consumer<? super O>> P iterate(final P procedure)
	{
		this.execute().iterate(procedure);
		return procedure;
	}

	public static <I, O, R extends XIterable<O>> CqlIteration<I, O, R> New()
	{
		return new Implementation<>(null, null, null, null, null, null, null);
	}

	public static <I, O> CqlQuery<I, O, XSequence<O>> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Function<? super I, O> projector ,
		final Comparator<? super O>  comparator
	)
	{
		return new Implementation<>(
			source            ,
			skip              ,
			limit             ,
			selector          ,
			notNull(projector),
			comparator        ,
			CqlResultor.New()
		);
	}

	public static <I, O, R extends XIterable<O>> CqlQuery<I, O, R> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Function<? super I, O> projector ,
		final Comparator<? super O>  comparator,
		final Aggregator<O, R>       aggregator
	)
	{
		return new Implementation<>(
			source                  ,
			skip                    ,
			limit                   ,
			selector                ,
			projector               ,
			comparator              ,
			CqlResultor.NewFromAggregator(aggregator)
		);
	}

	public static <I, O, R extends Consumer<O> & XIterable<O>> CqlQuery<I, O, R> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Function<? super I, O> projector ,
		final Comparator<? super O>  comparator,
		final R                      target
	)
	{
		return new Implementation<>(
			source              ,
			skip                ,
			limit               ,
			selector            ,
			projector           ,
			comparator          ,
			CqlResultor.New(target)
		);
	}

	public static <I, O, R extends XIterable<O>> CqlIteration<I, O, R> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Function<? super I, O> projector ,
		final Comparator<? super O>  comparator,
		final CqlResultor<O, R>      resultor
	)
	{
		return new Implementation<>(source, skip, limit, selector, projector, comparator, resultor);
	}


	final class Implementation<I, O, R extends XIterable<O>> extends Abstract<I, O, R> implements CqlIteration<I, O, R>
	{
		Implementation(
			final XIterable<? extends I> source    ,
			final Long                   skip      ,
			final Long                   limit     ,
			final Predicate<? super I>   selector  ,
			final Function<? super I, O> projector ,
			final Comparator<? super O>  comparator,
			final CqlResultor<O, R>         resultor
		)
		{
			super(source, skip, limit, selector, projector, comparator, resultor);
		}

	}

}
