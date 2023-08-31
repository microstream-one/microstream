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

import static one.microstream.X.coalesce;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.LimitList;
import one.microstream.collections.XSort;
import one.microstream.collections.interfaces.Sized;
import one.microstream.collections.sorting.Sortable;
import one.microstream.collections.sorting.SortableProcedure;
import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XSequence;
import one.microstream.functional.Aggregate_doubleMin;
import one.microstream.functional.Aggregate_doubleSum;
import one.microstream.functional.Aggregator;
import one.microstream.functional.To_double;
import one.microstream.functional.XFunc;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


/**
 * Collection Query Library
 * <p>
 * API for fluent query syntax and query instance definition.
 *
 */
public final class CQL
{
	///////////////////////////////////////////////////////////////////////////
	// query stub constructors //
	////////////////////////////

	public static <I> CqlSelection<I> Selection()
	{
		return CqlSelection.New();
	}

	public static <I> CqlTransfer<I, XSequence<I>> Transfer()
	{
		return CqlTransfer.New();
	}

	public static <I, R> CqlAggregation<I, R> Aggregation()
	{
		return CqlAggregation.New();
	}

	public static <I, O> CqlProjection<I, O> Projection()
	{
		return CqlProjection.New();
	}

	public static <I, O, R extends XIterable<O>> CqlIteration<I, O, R> Iteration()
	{
		return CqlIteration.New();
	}

	public static <I, O, R> CqlQuery<I, O, R> Query()
	{
		return CqlQuery.New();
	}

	public static <T> boolean isNotNull(final T instance)
	{
		return instance != null;
	}

	public static <T> boolean isNull(final T instance)
	{
		return instance == null;
	}



	///////////////////////////////////////////////////////////////////////////
	// fluent API methods //
	///////////////////////

	public static <I> CqlSelection<I> select(final Predicate<? super I> predicate)
	{
		return CqlSelection.New(null, null, null, predicate, null);
	}

	public static <I, R> CqlAggregation<I, R> aggregate(final CqlResultor<I, R> aggregator)
	{
		return CqlAggregation.New(null, null, null, null, null, aggregator);
	}

	public static <I, R> CqlAggregation<I, R> aggregate(
		final Supplier<R>      supplier,
		final BiConsumer<I, R> linker
	)
	{
		return aggregate(CqlResultor.NewFromSupplier(supplier, linker));
	}

	public static <I, R extends Sortable<I>> CqlAggregation<I, R> aggregate(
		final Supplier<R>           supplier,
		final BiConsumer<I, R>      linker  ,
		final Comparator<? super I> order
	)
	{
		return aggregate(CqlResultor.NewFromSupplier(supplier, linker, order));
	}

	public static <I, R> CqlAggregation<I, R> aggregate(
		final Supplier<R>         supplier ,
		final BiConsumer<I, R>    linker   ,
		final Consumer<? super R> finalizer
	)
	{
		return aggregate(CqlResultor.NewFromSupplier(supplier, linker, finalizer));
	}

	public static <I> CqlSelection<I> from(final XIterable<? extends I> source)
	{
		return CqlSelection.New(source, null, null, null, null);
	}
	
	public static <I, O> CqlProjection<I, O> project(final Function<? super I, O> projector)
	{
		return CqlProjection.New(null, null, null, null, projector, null);
	}
	
	@SafeVarargs
	public static <I> CqlProjection<I, Object[]> project(final Function<? super I, Object>... projectors)
	{
		return CqlProjection.New(null, null, null, null, ArrayProjector.New(projectors), null);
	}

	/**
	 * Fluent alias for {@code predicate.negate()}.
	 *
	 * @param <T> the type of the input to the predicate
	 * @param predicate the predicate to negate
	 * @return the negated predicate
	 */
	public static <T> Predicate<T> not(final Predicate<T> predicate)
	{
		return predicate.negate();
	}

	/**
	 * Helper method to make a lambda or method reference expression chainable.
	 * They somehow forgot that in the lambda language extension, so it has to be worked around, sadly.
	 *
	 * @param <T> the type of the input to the predicate
	 * @param predicate the condiation
	 * @return the given predicate
	 */
	public static <T> Predicate<T> where(final Predicate<T> predicate)
	{
		return predicate;
	}

	/**
	 * Fluent alias for {@code Comparator#reversed()}
	 * 
	 * @param <T> the type of objects that may be compared by this comparator
	 * @param order the comparator the be reversed
	 * @return the reversed comparator
	 */
	public static <T> Comparator<T> reversed(final Comparator<T> order)
	{
		return order.reversed();
	}

	/**
	 * Helper method to make a lambda or method reference expression chainable.
	 * They somehow forgot that in the lambda language extension, so it has to be worked around, sadly.
	 *
	 * @param <T> the type of objects that may be compared by this comparator
	 * @param order the comparator
	 * @return the given comparator
	 */
	public static <T> Comparator<T> comparing(final Comparator<T> order)
	{
		return order;
	}

	public static <E, T> Comparator<E> comparing(final Function<E, T> getter, final Comparator<T> order)
	{
		return (e1, e2) -> order.compare(getter.apply(e1), getter.apply(e2));
	}

	public static <I> Function<I, I> identity()
	{
		return Function.identity();
	}



	///////////////////////////////////////////////////////////////////////////
	// preparing //
	//////////////

	public static <I> XIterable<? extends I> prepareSource(final XIterable<? extends I> source)
	{
		return coalesce(source, X.empty());
	}

	public static <E> XSequence<E> prepareTargetCollection(final XIterable<?> source)
	{
		// best effort to choose a suitable generic buffer type
		return source instanceof Sized
			? new LimitList<>(XTypes.to_int(((Sized)source).size()))
			: new BulkList<>()
		;
	}

	public static <I> Consumer<I> prepareSourceIterator(
		final Long                 skip     ,
		final Long                 limit    ,
		final Predicate<? super I> selector ,
		final Consumer<? super I>  target
	)
	{
		if(selector == null)
		{
			return prepareSourceIterator(skip, limit, target);
		}

		if(isSkip(skip))
		{
			return isLimit(limit)
				? XFunc.wrapWithPredicateSkipLimit(target, selector, skip, limit)
				: XFunc.wrapWithPredicateSkip     (target, selector, skip       )
			;
		}
		return isLimit(limit)
			? XFunc.wrapWithPredicateLimit(target, selector, limit)
			: XFunc.wrapWithPredicate     (target, selector       )
		;
	}

	public static <I> Consumer<I> prepareSourceIterator(
		final Long                 skip  ,
		final Long                 limit ,
		final Consumer<? super I> target
	)
	{
		if(isSkip(skip))
		{
			return isLimit(limit)
				? XFunc.wrapWithSkipLimit(target, skip, limit)
				: XFunc.wrapWithSkip     (target, skip)
			;
		}

		return isLimit(limit)
			? XFunc.wrapWithLimit(target, limit)
			: i -> target.accept(i)
		;
	}

	public static <I, O> Consumer<I> prepareSourceIterator(
		final Long                   skip     ,
		final Long                   limit    ,
		final Predicate<? super I>   selector ,
		final Function<? super I, O> projector,
		final Consumer<? super O>    target
	)
	{
		if(selector == null)
		{
			return prepareSourceIterator(skip, limit, projector, target);
		}
		isNotNull(projector);

		if(isSkip(skip))
		{
			return isLimit(limit)
				? XFunc.wrapWithPredicateFunctionSkipLimit(target, selector, projector, skip, limit)
				: XFunc.wrapWithPredicateFunctionSkip     (target, selector, projector, skip       )
			;
		}
		return isLimit(limit)
			? XFunc.wrapWithPredicateFunctionLimit(target, selector, projector, limit)
			: XFunc.wrapWithPredicateFunction     (target, selector, projector       )
		;
	}

	public static <I, O> Consumer<I> prepareSourceIterator(
		final Long                   skip     ,
		final Long                   limit    ,
		final Function<? super I, O> projector,
		final Consumer<? super O>    target
	)
	{
		isNotNull(projector);

		if(isSkip(skip))
		{
			return isLimit(limit)
				? XFunc.wrapWithFunctionSkipLimit(target, projector, skip, limit)
				: XFunc.wrapWithFunctionSkip     (target, projector, skip       )
			;
		}

		return isLimit(limit)
			? XFunc.wrapWithFunctionLimit(target, projector, limit)
			: XFunc.wrapWithFunction     (target, projector       )
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// executing //
	//////////////

	public static <E> XSequence<E> executeQuery(final XIterable<? extends E> source)
	{
		return executeQuery(source, prepareTargetCollection(source));
	}

	public static <E, T extends Consumer<? super E>> T executeQuery(final XIterable<? extends E> source, final T target)
	{
		return source.iterate(target);
	}

	public static <I, P extends Consumer<? super I>> P executeQuery(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final P                      target
	)
	{
		executeQuery(
			source,
			prepareSourceIterator(skip, limit, selector, target)
		);
		return target;
	}

	public static <I, P extends Consumer<I>> P executeQuery(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final P                      target  ,
		final Comparator<? super I>  order
	)
	{
		executeQuery(source, skip, limit, selector, target);
		SortableProcedure.sortIfApplicable(target, order);
		return target;
	}

	public static <I, R> R executeQuery(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final CqlResultor<I, R>      resultor,
		final Comparator<? super I>  order
	)
	{
		final Aggregator<I, R> collector = resultor.prepareCollector(source);
		executeQuery(source, skip, limit, selector, collector, order);
		return collector.yield();
	}

	public static <I, O, R> R executeQuery(
		final XIterable<? extends I> source   ,
		final Long                   skip     ,
		final Long                   limit    ,
		final Predicate<? super I>   selector ,
		final Function<? super I, O> projector,
		final CqlResultor<O, R>      resultor ,
		final Comparator<? super O>  order
	)
	{
		final Aggregator<O, R> collector = resultor.prepareCollector(source);
		executeQuery(source, skip, limit, selector, projector, collector, order);
		return collector.yield();
	}

	public static <I, O, P extends Consumer<O>> P executeQuery(
		final XIterable<? extends I> source   ,
		final Long                   skip     ,
		final Long                   limit    ,
		final Predicate<? super I>   selector ,
		final Function<? super I, O> projector,
		final P                      target   ,
		final Comparator<? super O>  order
	)
	{
		executeQuery(
			source,
			prepareSourceIterator(skip, limit, selector , projector, target)
		);
		SortableProcedure.sortIfApplicable(target, order);
		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	// Resultor constructors //
	//////////////////////////

	public static <O, T extends Consumer<O>> CqlResultor<O, T> resulting(final Supplier<T> supplier)
	{
		return CqlResultor.NewFromSupplier(supplier);
	}

	public static <O, T extends Consumer<O> & XIterable<O>> CqlResultor<O, T> resultingIterable(
		final Supplier<T> supplier
	)
	{
		return CqlResultor.NewFromSupplier(supplier);
	}

	public static <O> CqlResultor<O, BulkList<O>> resultingBulkList()
	{
		return CqlResultor.NewFromSupplier((Supplier<BulkList<O>>)BulkList::New);
	}

	public static <O> CqlResultor<O, BulkList<O>> resultingBulkList(final int initialCapacity)
	{
		return CqlResultor.NewFromSupplier(() -> BulkList.<O>New(initialCapacity));
	}

	public static <O> CqlResultor<O, LimitList<O>> resultingLimitList(final int initialCapacity)
	{
		return CqlResultor.NewFromSupplier(() -> LimitList.<O>New(initialCapacity));
	}

	public static <O> CqlResultor<O, HashEnum<O>> resultingHashEnum()
	{
		return CqlResultor.NewFromSupplier(() -> HashEnum.<O>New());
	}

	public static <O> CqlResultor<O, EqHashEnum<O>> resultingEqHashEnum()
	{
		return CqlResultor.NewFromSupplier(() -> EqHashEnum.<O>New());
	}

	public static <O> CqlResultor<O, EqHashEnum<O>> resultingEqHashEnum(final HashEqualator<? super O> hashEqualator)
	{
		return CqlResultor.NewFromSupplier(() -> EqHashEnum.<O>New(hashEqualator));
	}

	public static <K, V> CqlResultor<KeyValue<K, V>, HashTable<K, V>> resultingHashTable()
	{
		return CqlResultor.NewFromSupplier(() -> HashTable.<K, V>New());
	}

	public static <K, V> CqlResultor<KeyValue<K, V>, EqHashTable<K, V>> resultingEqHashTable()
	{
		return CqlResultor.NewFromSupplier(() -> EqHashTable.<K, V>New());
	}

	public static <K, V> CqlResultor<KeyValue<K, V>, EqHashTable<K, V>> resultingEqHashTable(
		final HashEqualator<? super K> hashEqualator
	)
	{
		return CqlResultor.NewFromSupplier(() -> EqHashTable.<K, V>New(hashEqualator));
	}

	// (25.03.2014 Tm)TODO: CQL: more resulting~


	public static <O> CqlResultor<O, Double> sum(final To_double<? super O> getter)
	{
		return e -> new Aggregate_doubleSum<>(getter);
	}
	
	public static <O> CqlResultor<O, Double> min(final To_double<? super O> getter)
	{
		return e -> new Aggregate_doubleMin<>(getter);
	}

	
	@SafeVarargs
	public static final <E> Comparator<? super E> chain(final Comparator<? super E>... comparators)
	{
		return XSort.chain(comparators);
	}
	

	///////////////////////////////////////////////////////////////////////////
	// internal helper methods //
	////////////////////////////

	static Long asLong(final Number number)
	{
		return number instanceof Long ? (Long)number : number.longValue();
	}

	static boolean isSkip(final Long skip)
	{
		return skip != null && skip != 0;
	}

	static boolean isLimit(final Long limit)
	{
		return limit != null && limit != 0;
	}

	private CQL()
	{
		throw new java.lang.UnsupportedOperationException(); // static class only
	}

}
