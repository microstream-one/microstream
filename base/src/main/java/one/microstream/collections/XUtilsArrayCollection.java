package one.microstream.collections;

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

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XSettingList;
import one.microstream.equality.Equalator;
import one.microstream.functional.AggregateMax;
import one.microstream.functional.Aggregator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.IsCustomEqual;
import one.microstream.functional.IsGreater;
import one.microstream.functional.IsSmaller;
import one.microstream.typing.XTypes;

public final class XUtilsArrayCollection
{
	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingCollection<E>> A iterate(
		final A a,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		AbstractArrayStorage.forwardConditionalIterate(
			a.internalGetStorageArray(), XTypes.to_int(a.size()),
			0,
			predicate,
			procedure
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngBinarySearch(
		final A a,
		final int offset,
		final int length,
		final E element,
		final Comparator<? super E> comparator
	)
	{
		return AbstractArrayStorage.rangedBinarySearch(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, element, comparator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngHasUniqueValues(final A a, final int offset, final int length)
	{
		return AbstractArrayStorage.rangedHasUniqueValues(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngHasUniqueValues(
		final A a,
		final int offset,
		final int length,
		final Equalator<? super E> equalator
	)
	{
		return AbstractArrayStorage.rangedHasUniqueValues(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContainsAll(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> elements
	)
	{
		return AbstractArrayStorage.rangedContainsAll(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, elements
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngMax(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedAggregate(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new AggregateMax<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngMin(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedAggregate(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new AggregateMax<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngIndexOf(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngIndexOF(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedIndexOF(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngCount(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedConditionalCount(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngCount(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedCount(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, element);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngIsSorted(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedIsSorted(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, comparator);
	}

	public static final <
		E,
		A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>,
		C extends Consumer<? super E>
	>
	C rngCopyTo(final A a, final int offset, final int length, final C target)
	{
		return AbstractArrayStorage.rangedCopyTo(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, target);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	A rngIterate(final A a, final int offset, final int length, final Consumer<? super E> procedure)
	{
		AbstractArrayStorage.rangedIterate(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, procedure);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	A rngIterate(final A a, final int offset, final int length, final IndexedAcceptor<? super E> procedure)
	{
		AbstractArrayStorage.rangedIterate(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, procedure);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	A rngIterate(
		final A a,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		AbstractArrayStorage.rangedConditionalIterate(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate, procedure
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngFind(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedQueryElement(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample), null
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContains(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedContainsSame(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContainsId(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedContainsSame(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContains(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedContains(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContains(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedContains(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContainsNull(final A a, final int offset, final int length)
	{
		return AbstractArrayStorage.rangedContainsNull(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngApplies(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedApplies(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngCount(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedConditionalCount(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngIndexOf(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedConditionalIndexOf(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngScan(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngSearch(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedQueryElement(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate, null
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E[] rngToArray(final A a, final int offset, final int length, final Class<E> type)
	{
		return AbstractArrayStorage.rangedToArray(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, type);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	Object[] rngToArray(final A a, final int offset, final int length)
	{
		return AbstractArrayStorage.rangedToArray(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngEqualsContent(
		final A a,
		final int offset,
		final int length,
		final XGettingList<? extends E> list,
		final Equalator<? super E> equalator
	)
	{
		return AbstractArrayStorage.rangedEqualsContent(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, list, equalator);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, R>
	R rngAggregate(final A a, final int offset, final int length, final Aggregator<? super E, R> aggregate)
	{
		AbstractArrayStorage.rangedIterate(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, aggregate
		);
		return aggregate.yield();
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(final A a, final int offset, final int length, final VarString vc)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, vc
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(final A a, final int offset, final int length, final VarString vc, final String separator)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, vc, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(final A a, final int offset, final int length, final VarString vc, final char separator)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, vc, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(
		final A a,
		final int offset,
		final int length,
		final VarString vc,
		final BiConsumer<VarString, ? super E> appender
	)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, vc, appender
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(
		final A a,
		final int offset,
		final int length,
		final VarString vc,
		final BiConsumer<VarString, ? super E> appender,
		final char separator
	)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, vc, appender, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(
		final A a,
		final int offset,
		final int length,
		final VarString vc,
		final BiConsumer<VarString, ? super E> appender,
		final String separator
	)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, vc, appender, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngMaxIndex(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new IsGreater<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngMinIndex(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, new IsSmaller<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngDistinct(final A a, final int offset, final int length, final C target)
	{
		return AbstractArrayStorage.rangedDistinct(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, target
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngDistinct(final A a, final int offset, final int length, final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedDistinct(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, target, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngIntersect(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.rangedIntersect(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, samples, equalator, target
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngUnion(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.rangedUnion(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, samples, equalator, target
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngExcept(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.rangedExcept(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, samples, equalator, target
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceOne(
		final A a,
		final int offset,
		final int length,
		final E oldElement,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		return AbstractArrayStorage.rangedReplaceOne(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, oldElement, newElement, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceOne(final A a, final int offset, final int length, final E oldElement, final E newElement)
	{
		return AbstractArrayStorage.rangedReplaceOne(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, oldElement, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	long rngReplace(
		final A a,
		final int offset,
		final int length,
		final E oldElement,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		return AbstractArrayStorage.rangedReplace(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, oldElement, newElement, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplace(final A a, final int offset, final int length, final E oldElement, final E newElement)
	{
		return AbstractArrayStorage.rangedReplace(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, oldElement, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceAll(final A a, final int offset, final int length, final XMap<E, E> replacementMapping)
	{
		return AbstractArrayStorage.rangedReplaceAll(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, replacementMapping
		);
	}

	@SuppressWarnings("unchecked")
	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceAll(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> oldElements,
		final E newElement
	)
	{
		return AbstractArrayStorage.rangedReplaceAll(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, (XGettingCollection<E>)oldElements, newElement
		);
	}

	@SuppressWarnings("unchecked")
	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceAll(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> oldElements,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		return AbstractArrayStorage.rangedReplaceAll(
			a.internalGetStorageArray(), XTypes.to_int(a.size()),
			offset, length, (XGettingCollection<E>)oldElements, newElement, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceOne(final A a, final int offset, final int length, final Predicate<? super E> predicate, final E newElement)
	{
		return AbstractArrayStorage.rangedReplaceOne(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplace(final A a, final int offset, final int length, final Predicate<? super E> predicate, final E newElement)
	{
		return AbstractArrayStorage.rangedReplace(
			a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, predicate, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSortQuick(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSortQuick(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, comparator);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngShuffle(final A a, final int offset, final int length)
	{
		AbstractArrayStorage.rangedShuffle(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSortMerge(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSortMerge(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, comparator);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSort(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSort(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, comparator);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSortInsertion(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSortInsertion(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length, comparator);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngReverse(final A a, final int offset, final int length)
	{
		AbstractArrayStorage.rangedReverse(a.internalGetStorageArray(), XTypes.to_int(a.size()), offset, length);
		return a;
	}



	// TODO additional AbstractArraySequence-specific methods, maybe simple copy whole XUtilsCollection when done.

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XUtilsArrayCollection()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
