package net.jadoth.cql;

import static net.jadoth.X.notNull;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.XIterable;
import net.jadoth.functional.Aggregator;

public interface CqlAggregation<I, R> extends CqlQuery<I, I, R>
{
	// fluent API //

	@Override
	public default CqlAggregation<I, R> skip(final Number count) // Number to allow fluent API use of int values
	{
		return CqlAggregation.New(
			this.getSource()   ,
			CQL.asLong(count) ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlAggregation<I, R> limit(final Number count) // Number to allow fluent API use of int values
	{
		return CqlAggregation.New(
			this.getSource()   ,
			this.getSkip()     ,
			CQL.asLong(count) ,
			this.getSelector() ,
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlAggregation<I, R> select(final Predicate<? super I> selector)
	{
		return CqlAggregation.New(
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			selector           ,
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlAggregation<I, R> orderBy(final Comparator<? super I> order)
	{
		return CqlAggregation.New(
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			order              ,
			this.getResultor()
		);
	}

	@Override
	public default CqlAggregation<I, R> from(final XIterable<? extends I> source)
	{
		return CqlAggregation.New(
			source             ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default <P> CqlProjection<I, P> project(final Function<? super I, P> projector)
	{
		return CqlProjection.New(
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			projector          ,
			null
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
			target
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
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getOrder()    ,
			notNull(resultor)
		);
	}

	@Override
	public default <R1> CqlAggregation<I, R1> targeting(final Aggregator<I, R1> collector)
	{
		return CqlAggregation.New(
			this.getSource()   ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getOrder()    ,
			CqlResultor.NewFromAggregator(collector)
		);
	}


	@Override
	public default R execute()
	{
		return this.executeOn(CQL.prepareSource(this.getSource()));
	}

	@Override
	public default R executeOn(final XIterable<? extends I> source)
	{
		return CQL.executeQuery(
			source             ,
			this.getSkip()     ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getResultor() ,
			this.getOrder()
		);
	}

	public static <I, R> CqlAggregation<I, R> New()
	{
		return new Implementation<>(null, null, null, null, null, null);
	}

	public static <I, R> CqlAggregation<I, R> New(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final Comparator<? super I>  order   ,
		final CqlResultor<I, R>      resultor
	)
	{
		return new Implementation<>(source, skip, limit, selector, order, resultor);
	}

	final class Implementation<I, R> extends Abstract<I, I, R> implements CqlAggregation<I, R>
	{
		Implementation(
			final XIterable<? extends I> source  ,
			final Long                   skip    ,
			final Long                   limit   ,
			final Predicate<? super I>   selector,
			final Comparator<? super I>  order   ,
			final CqlResultor<I, R>      resultor
		)
		{
			super(source, skip, limit, selector, null, order, resultor);
		}

	}

}
