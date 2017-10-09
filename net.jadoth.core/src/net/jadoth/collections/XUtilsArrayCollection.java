package net.jadoth.collections;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.functions.AggregateMax;
import net.jadoth.collections.functions.IsCustomEqual;
import net.jadoth.collections.functions.IsGreater;
import net.jadoth.collections.functions.IsSmaller;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XMap;
import net.jadoth.collections.types.XSettingList;
import net.jadoth.functional.Aggregator;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.util.Equalator;
import net.jadoth.util.chars.VarString;

public final class XUtilsArrayCollection
{
	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingCollection<E>> A iterate(
		final A a,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		AbstractArrayStorage.forwardConditionalIterate(
			a.internalGetStorageArray(),
			0,
			Jadoth.to_int(a.size()),
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, element, comparator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngHasUniqueValues(final A a, final int offset, final int length)
	{
		return AbstractArrayStorage.rangedHasUniqueValues(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, equalator
		);
	}

//	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
//	boolean rngContainsAll(
//		final A a,
//		final int offset,
//		final int length,
//		final XGettingCollection<? extends E> samples,
//		final Equalator<? super E> equalator
//	)
//	{
//		return AbstractArrayStorage.rangedContainsAll(
//			a.internalGetStorageArray(), a.size(), offset, length, a, equalator
//		);
//	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContainsAll(
		final A a,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> elements
	)
	{
		return AbstractArrayStorage.rangedContainsAll(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, elements
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngMax(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedAggregate(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new AggregateMax<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngMin(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedAggregate(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new AggregateMax<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngIndexOf(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngIndexOF(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedIndexOF(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngCount(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedConditionalCount(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngCount(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedCount(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngIsSorted(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedIsSorted(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, comparator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>,
	C extends Consumer<? super E>> C rngCopyTo(final A a, final int offset, final int length, final C target)
	{
		return AbstractArrayStorage.rangedCopyTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, target
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	A rngIterate(final A a, final int offset, final int length, final Consumer<? super E> procedure)
	{
		AbstractArrayStorage.rangedIterate(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, procedure
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	A rngIterate(final A a, final int offset, final int length, final IndexProcedure<? super E> procedure)
	{
		AbstractArrayStorage.rangedIterate(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, procedure
		);
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate, procedure
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngFind(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedQueryElement(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample), null
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContains(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedContainsSame(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContainsId(final A a, final int offset, final int length, final E element)
	{
		return AbstractArrayStorage.rangedContainsSame(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, element
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContains(final A a, final int offset, final int length, final E sample, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedContains(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new IsCustomEqual<>(equalator, sample)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContains(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedContains(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngContainsNull(final A a, final int offset, final int length)
	{
		return AbstractArrayStorage.rangedContainsNull(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	boolean rngApplies(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedApplies(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngCount(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedConditionalCount(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngIndexOf(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedConditionalIndexOf(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngScan(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E rngSearch(final A a, final int offset, final int length, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.rangedQueryElement(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate, null
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	E[] rngToArray(final A a, final int offset, final int length, final Class<E> type)
	{
		return AbstractArrayStorage.rangedToArray(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, type
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	Object[] rngToArray(final A a, final int offset, final int length)
	{
		return AbstractArrayStorage.rangedToArray(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length
		);
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
		return AbstractArrayStorage.rangedEqualsContent(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, list, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, R>
	R rngAggregate(final A a, final int offset, final int length, final Aggregator<? super E, R> aggregate)
	{
		AbstractArrayStorage.rangedIterate(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, aggregate
		);
		return aggregate.yield();
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(final A a, final int offset, final int length, final VarString vc)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, vc
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(final A a, final int offset, final int length, final VarString vc, final String separator)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, vc, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(final A a, final int offset, final int length, final VarString vc, final char separator)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, vc, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(
		final A a,
		final int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender
	)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, vc, appender
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(
		final A a,
		final int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender,
		final char separator
	)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, vc, appender, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	VarString rngAppendTo(
		final A a,
		final int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender,
		final String separator
	)
	{
		return AbstractArrayStorage.rangedAppendTo(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, vc, appender, separator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngMaxIndex(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new IsGreater<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>>
	int rngMinIndex(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.rangedScan(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, new IsSmaller<>(comparator)
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngDistinct(final A a, final int offset, final int length, final C target)
	{
		return AbstractArrayStorage.rangedDistinct(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, target
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XGettingSequence<E>, C extends Consumer<? super E>>
	C rngDistinct(final A a, final int offset, final int length, final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.rangedDistinct(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, target, equalator
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, samples, equalator, target
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, samples, equalator, target
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, samples, equalator, target
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, oldElement, newElement, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceOne(final A a, final int offset, final int length, final E oldElement, final E newElement)
	{
		return AbstractArrayStorage.rangedReplaceOne(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, oldElement, newElement
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, oldElement, newElement, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplace(final A a, final int offset, final int length, final E oldElement, final E newElement)
	{
		return AbstractArrayStorage.rangedReplace(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, oldElement, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceAll(final A a, final int offset, final int length, final XMap<E, E> replacementMapping)
	{
		return AbstractArrayStorage.rangedReplaceAll(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, replacementMapping
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, (XGettingCollection<E>)oldElements, newElement
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
			a.internalGetStorageArray(), Jadoth.to_int(a.size()),
			offset, length, (XGettingCollection<E>)oldElements, newElement, equalator
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplaceOne(final A a, final int offset, final int length, final Predicate<? super E> predicate, final E newElement)
	{
		return AbstractArrayStorage.rangedReplaceOne(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>>
	int rngReplace(final A a, final int offset, final int length, final Predicate<? super E> predicate, final E newElement)
	{
		return AbstractArrayStorage.rangedReplace(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, predicate, newElement
		);
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSortQuick(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSortQuick(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, comparator
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngShuffle(final A a, final int offset, final int length)
	{
		AbstractArrayStorage.rangedShuffle(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSortMerge(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSortMerge(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, comparator
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSort(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSort(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, comparator
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngSortInsertion(final A a, final int offset, final int length, final Comparator<? super E> comparator)
	{
		AbstractArrayStorage.rangedSortInsertion(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length, comparator
		);
		return a;
	}

	public static final <E, A extends AbstractSimpleArrayCollection<E> & XSettingList<E>> XSettingList<E>
	rngReverse(final A a, final int offset, final int length)
	{
		AbstractArrayStorage.rangedReverse(
			a.internalGetStorageArray(), Jadoth.to_int(a.size()), offset, length
		);
		return a;
	}



	// TODO additional AbstractArraySequence-specific methods, maybe simple copy whole XUtilsCollection when done.



	private XUtilsArrayCollection()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
