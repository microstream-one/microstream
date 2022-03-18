package one.microstream.util.cql;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XSequence;
import one.microstream.functional.Aggregator;


public interface CqlProjection<I, O> extends CqlIteration<I, O, XSequence<O>>
{
	// fluent API //

	@Override
	public default CqlProjection<I, O> skip(final Number count) // Number to allow fluent API use of int values
	{
		return CqlProjection.New(
			this.getSource()   ,
			CQL.asLong(count)  ,
			this.getLimit()    ,
			this.getSelector() ,
			this.getProjector(),
			this.getOrder()    ,
			this.getResultor()
		);
	}

	@Override
	public default CqlProjection<I, O> limit(final Number count) // Number to allow fluent API use of int values
	{
		return CqlProjection.New(
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
	public default CqlProjection<I, O> select(final Predicate<? super I> selector)
	{
		return CqlProjection.New(
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
	public default CqlProjection<I, O> orderBy(final Comparator<? super O> order)
	{
		return CqlProjection.New(
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
	public default CqlProjection<I, O> from(final XIterable<? extends I> source)
	{
		return CqlProjection.New(
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


	// constructors //

	public static <I, O> CqlProjection<I, O> New()
	{
		return new Default<>(null, null, null, null, null, null, null);
	}

	public static <I, O> CqlProjection<I, O> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Function<? super I, O> projector ,
		final Comparator<? super O>  comparator
	)
	{
		return new Default<>(
			source            ,
			skip              ,
			limit             ,
			selector          ,
			notNull(projector),
			comparator        ,
			CqlResultor.New()
		);
	}

	public static <I, O> CqlProjection<I, O> New(
		final XIterable<? extends I>        source    ,
		final Long                          skip      ,
		final Long                          limit     ,
		final Predicate<? super I>          selector  ,
		final Function<? super I, O>        projector ,
		final Comparator<? super O>         comparator,
		final Aggregator<O, XSequence<O>> aggregator
	)
	{
		return new Default<>(
			source                  ,
			skip                    ,
			limit                   ,
			selector                ,
			projector               ,
			comparator              ,
			CqlResultor.NewFromAggregator(aggregator)
		);
	}

	public static <I, O> CqlProjection<I, O> New(
		final XIterable<? extends I> source    ,
		final Long                   skip      ,
		final Long                   limit     ,
		final Predicate<? super I>   selector  ,
		final Function<? super I, O> projector ,
		final Comparator<? super O>  comparator,
		final XSequence<O>           target
	)
	{
		return new Default<>(
			source              ,
			skip                ,
			limit               ,
			selector            ,
			projector           ,
			comparator          ,
			CqlResultor.New(target)
		);
	}

	public static <I, O> CqlProjection<I, O> New(
		final XIterable<? extends I>       source    ,
		final Long                         skip      ,
		final Long                         limit     ,
		final Predicate<? super I>         selector  ,
		final Function<? super I, O>       projector ,
		final Comparator<? super O>        comparator,
		final CqlResultor<O, XSequence<O>> resultor
	)
	{
		return new Default<>(source, skip, limit, selector, projector, comparator, resultor);
	}

	final class Default<I, O> extends CqlQuery.Abstract<I, O, XSequence<O>> implements CqlProjection<I, O>
	{
		Default(
			final XIterable<? extends I>    source    ,
			final Long                      skip      ,
			final Long                      limit     ,
			final Predicate<? super I>      selector  ,
			final Function<? super I, O>    projector ,
			final Comparator<? super O>     comparator,
			final CqlResultor<O, XSequence<O>> resultor
		)
		{
			super(source, skip, limit, selector, projector, comparator, resultor);
		}

	}

}
