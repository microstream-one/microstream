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
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XInsertingList;
import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XProcessingCollection;
import one.microstream.collections.types.XProcessingSequence;
import one.microstream.collections.types.XPuttingCollection;
import one.microstream.collections.types.XSettingList;
import one.microstream.collections.types.XSortableSequence;
import one.microstream.collections.types.XTable;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.AggregateCountingAdd;
import one.microstream.functional.AggregateCountingPut;
import one.microstream.functional.AggregateMax;
import one.microstream.functional.AggregateMin;
import one.microstream.functional.AggregateOffsetLength;
import one.microstream.functional.Aggregator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.IsCustomEqual;
import one.microstream.functional.IsGreater;
import one.microstream.functional.IsSmaller;
import one.microstream.functional.XFunc;
import one.microstream.math.FastRandom;
import one.microstream.typing.XTypes;

public final class XUtilsCollection
{
	private static <T> LimitList<T> buffer(final int srcSize, final long length)
	{
		int temp, minimum = srcSize;
		if((temp = X.checkArrayRange(length < 0 ? -length : length)) < minimum)
		{
			minimum = temp;
		}
		return new LimitList<>(minimum);
	}



	private static final Object MARKER = new Object();


	@SuppressWarnings("unchecked")
	public static final <E, C extends XIterable<? extends E>> C iterate(
		final C collection,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.forwardConditionalIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				0,
				((AbstractSimpleArrayCollection<?>)collection).internalSize(),
				predicate, procedure
			);
		}
		else if(collection instanceof AbstractChainCollection<?, ?, ?, ?>)
		{
			((AbstractChainCollection<E, ?, ?, ?>)collection).getInternalStorageChain().iterate(predicate, procedure);
		}
		else
		{
			collection.iterate(XFunc.wrapWithPredicate(procedure, predicate));
		}
		return collection;
	}


	public static <K, E, C extends XTable<K, E>> C valueSortByValues(
		final C                     collection,
		final Comparator<? super E> order
	)
	{
		valueSort(collection.values(), order);
		return collection;
	}

	public static <V, E, C extends XTable<E, V>> C valueSortByKeys(
		final C                     collection,
		final Comparator<? super E> order
	)
	{
		valueSort(collection.keys(), order);
		return collection;
	}


	public static <E, C extends XSortableSequence<E>> C valueSort(
		final C                     collection,
		final Comparator<? super E> order
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			XSort.valueSort(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				0,
				XTypes.to_int(collection.size()),
				order
			);
		}
		else
		{
			collection.sort(order); // sorting a non-array collection (i.e. chain) by value yields no advantage
		}
		return collection;
	}


	public static <E> void shuffle(final XSortableSequence<E> collection)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.shuffle(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection), XTypes.to_int(collection.size())
			);
		}
		else
		{
			final FastRandom random = new FastRandom();
			for(int i = XTypes.to_int(collection.size()); i > 1; i--)
			{
				collection.swap(i - 1, random.nextInt(i));
			}
		}
	}

	public static <E> void rngShuffle(final XSortableSequence<E> collection, final long offset, final long length)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedShuffle(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				XTypes.to_int(collection.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length)
			);
		}
		else
		{
			final FastRandom random = new FastRandom();
			for(int i = XTypes.to_int(collection.size()); i > 1; i--)
			{
				collection.swap(i - 1, random.nextInt(i));
			}
		}
	}

	public static <E, C extends XGettingCollection<E>> C diverge(
		final C elements,
		final Consumer<? super E> positives,
		final Consumer<? super E> negatives,
		final Predicate<? super E> predicate
	)
	{
		elements.iterate(e ->
		{
			if(predicate.test(e))
			{
				positives.accept(e);
			}
			else
			{
				negatives.accept(e);
			}
		});
		return elements;
	}

	public static <E, C extends XProcessingCollection<E>> C partition(
		final C collection,
		final Predicate<? super E> predicate,
		final Consumer<? super E> positiveTarget,
		final Consumer<? super E> negativeTarget
	)
	{
		collection.process(e ->
		{
			if(predicate.test(e))
			{
				positiveTarget.accept(e);
			}
			else
			{
				negativeTarget.accept(e);
			}
		});
		return collection;
	}

	public static <E, C extends XGettingCollection<E>> C decide(
		final C collection,
		final Predicate<? super E> predicate,
		final Consumer<? super E> positiveOperation,
		final Consumer<? super E> negativeOperation
	)
	{
		collection.iterate(e ->
		{
			if(predicate.test(e))
			{
				positiveOperation.accept(e);
			}
			else
			{
				negativeOperation.accept(e);
			}
		});
		return collection;
	}

	
	public static <E, C extends XCollection<E>> C subtract(
		final C                               elements,
		final XGettingCollection<? extends E> other
	)
	{
		elements.removeAll(other);
		return elements;
	}


	private static String exceptionStringOffset(final int size, final long offset)
	{
		return "Invalid offset of " + offset + " for size " + size;
	}

	private static String exceptionStringRange(final int size, final long offset, final long length)
	{
		return "Invalid range (" + offset + ", " + length + " for size " + size;
	}

	public static <E> int rngBinarySearch(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final E element,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedBinarySearch(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element,
				comparator
			);
		}
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngHasUniqueValues(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedHasUniqueValues(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length)
			);
		}
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngHasUniqueValues(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedHasUniqueValues(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				equalator
			);
		}
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContainsAll(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> elements
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				elements
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngMax(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAggregate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new AggregateMax<>(comparator)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngMin(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAggregate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new AggregateMin<>(comparator)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngIndexOf(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsCustomEqual<>(equalator, sample)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngIndexOF(final XGettingSequence<E> sequence, final long offset, final long length, final E element)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedIndexOF(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngCount(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedConditionalCount(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsCustomEqual<>(equalator, sample)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngCount(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedCount(AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngIsSorted(
		final XGettingSequence<E>   sequence  ,
		final long                  offset    ,
		final long                  length    ,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedIsSorted(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngCopyTo(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedCopyTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target
			);
		}

		AbstractArrayStorage.validateRange0toUpperBound(
			XTypes.to_int(sequence.size()),
			XTypes.to_int(offset),
			XTypes.to_int(length)
		);
		sequence.iterate(
			new Consumer<E>()
			{
				long ofs = offset, len = length;

				@Override
				public void accept(final E e)
				{
					if(this.ofs != 0)
					{
						this.ofs--;
						return;
					}
					if(this.len-- == 0)
					{
						throw X.BREAK();
					}
					target.accept(e);
				}
			}
		);
		return target;
	}

	public static <E, C extends Consumer<? super E>>
	C rngCopyTo(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedCopyTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target,
				predicate
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XGettingSequence<E>>
	C rngIterate(final C sequence, final long offset, final long length, final Consumer<? super E> procedure)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				procedure
			);
			return sequence;
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XGettingSequence<E>> C rngIterate(
		final C                         sequence ,
		final long                      offset   ,
		final long                      length   ,
		final IndexedAcceptor<? super E> procedure
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				procedure
			);
			return sequence;
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XGettingSequence<E>> C rngIterate(
		final C                    sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate,
		final Consumer<? super E>  procedure
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedConditionalIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate,
				procedure
			);
			return sequence;
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, A> A rngJoin(
		final XGettingSequence<E>               sequence ,
		final long                              offset   ,
		final long                              length   ,
		final BiConsumer<? super E, ? super A> joiner   ,
		final A                                 aggregate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedJoin(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				joiner,
				aggregate
			);
			return aggregate;
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngFind(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final E                    sample   ,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedQueryElement(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsCustomEqual<>(equalator, sample),
				null
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContains(
		final XGettingSequence<E> sequence,
		final long                offset  ,
		final long                length  ,
		final E                   element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsSame(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContainsId(
		final XGettingSequence<E> sequence,
		final long                offset  ,
		final long                length  ,
		final E                   element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsSame(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContains(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final E                    sample   ,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContains(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsCustomEqual<>(equalator, sample)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContains(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContains(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContainsNull(final XGettingSequence<E> sequence, final long offset, final long length)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsNull(
				AbstractSimpleArrayCollection.internalGetStorageArray(
					(AbstractSimpleArrayCollection<?>)sequence),
					XTypes.to_int(sequence.size()),
					XTypes.to_int(offset),
					XTypes.to_int(length)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngApplies(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngAppliesAll(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedApplies(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngCount(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedConditionalCount(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngIndexOf(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedConditionalIndexOf(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngScan(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngGet(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final E                    sample   ,
		final Equalator<? super E> equalator
	)
	{
		// implementation-specific optimized alternatives
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedQueryElement(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsCustomEqual<>(equalator, sample), null
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngSearch(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		// implementation-specific optimized alternatives
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedQueryElement(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate, null
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngEqualsContent(
		final XGettingSequence<E>           sequence ,
		final long                          offset   ,
		final long                          length   ,
		final XGettingSequence<? extends E> other    ,
		final Equalator<? super E>          equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedEqualsContent(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				other, equalator
			);
		}

		// (13.03.2011)TODO: rngEqualsContent() ... tricky
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, R> R rngAggregate(
		final XGettingSequence<E>      sequence ,
		final long                     offset   ,
		final long                     length   ,
		final Aggregator<? super E, R> aggregate
	)
	{
		// implementation-specific optimized alternatives
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				aggregate
			);
			return aggregate.yield();
		}


		// sanity checks
		final int size = XTypes.to_int(sequence.size());
		if(offset < 0 || offset >= size)
		{
			throw new IllegalArgumentException(exceptionStringOffset(size, offset));
		}
		if(length == 0)
		{
			return aggregate.yield(); // length 0, return default value of aggregate
		}

		final long bound = offset + length;
		if(bound < -1 || bound > size)
		{
			throw new IllegalArgumentException(exceptionStringRange(size, offset, length));
		}

		// execute via stateful wrapper aggregate
		final AggregateOffsetLength<E, R> wrappedAggregate;
		sequence.iterate(wrappedAggregate = new AggregateOffsetLength<>(offset, length, aggregate));
		return wrappedAggregate.yield();
	}

	public static <E> VarString rngAppendTo(
		final XGettingSequence<E> sequence,
		final long                offset  ,
		final long                length  ,
		final VarString           vs
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAppendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				vs
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString rngAppendTo(
		final XGettingSequence<E> sequence ,
		final long                offset   ,
		final long                length   ,
		final VarString           vs       ,
		final String              separator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAppendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				vs,
				separator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString rngAppendTo(
		final XGettingSequence<E> sequence ,
		final long                offset   ,
		final long                length   ,
		final VarString           vs       ,
		final char                separator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAppendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				vs,
				separator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString rngAppendTo(
		final XGettingSequence<E>               sequence,
		final long                              offset  ,
		final long                              length  ,
		final VarString                         vs      ,
		final BiConsumer<VarString, ? super E> appender
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAppendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				vs,
				appender
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString rngAppendTo(
		final XGettingSequence<E>               sequence ,
		final long                              offset   ,
		final long                              length   ,
		final VarString                         vc       ,
		final BiConsumer<VarString, ? super E> appender ,
		final char                              separator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAppendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				vc,
				appender,
				separator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString rngAppendTo(
		final XGettingSequence<E>               sequence ,
		final long                              offset   ,
		final long                              length   ,
		final VarString                         vs       ,
		final BiConsumer<VarString, ? super E> appender ,
		final String                            separator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAppendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				vs,
				appender,
				separator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngMaxIndex(
		final XGettingSequence<E>   sequence  ,
		final long                  offset    ,
		final long                  length    ,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsGreater<>(comparator)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngMinIndex(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsSmaller<>(comparator)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>>
	C rngDistinct(final XGettingSequence<E> sequence, final long offset, final long length, final C target)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedDistinct(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngDistinct(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final C target,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedDistinct(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target,
				equalator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngIntersect(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedIntersect(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				target
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngUnion(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedUnion(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				target
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet

	}

	public static <E, C extends Consumer<? super E>> C rngExcept(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedExcept(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				target
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	///////////////////////////////////////////////////////////////////////////
	// adding / putting //
	/////////////////////

	public static <E, C extends XAddingCollection<? super E>> C addAll(
		final C target,
		final E[] elements,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}
	public static <E, C extends XAddingCollection<? super E>> C addAll(
		final C target,
		final XGettingCollection<? extends E> elements,
		final Predicate<? super E> predicate
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XAddingCollection<? super E>> C putAll(
		final C target,
		final E[] elements,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}
	public static <E, C extends XAddingCollection<? super E>> C putAll(
		final C target,
		final XGettingCollection<? extends E> elements,
		final Predicate<? super E> predicate
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XInsertingList<? super E>> C insert(
		final C target,
		final int index,
		final E[] elements,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		final int size;
		if(index == (size = XTypes.to_int(target.size())))
		{
			XUtilsCollection.addAll(target, elements, offset, length, predicate);
			return target;
		}
		if(index < 0 || index > size)
		{
			throw new IndexBoundsException(size, index);
		}

		// select elements into buffer
		final LimitList<E> buffer = XUtilsCollection.addAll(
			XUtilsCollection.<E>buffer(elements.length, length), elements, offset, length, predicate
		);

		// internal copying of selected elements
		if(XTypes.to_int(buffer.size()) > 0)
		{
			target.insertAll(index, buffer.internalGetStorageArray(), 0, XTypes.to_int(buffer.size()));
		}
		return target;
	}

	public static <E, C extends XInsertingList<? super E>> C insert(
		final C target,
		final long index,
		final XGettingCollection<? extends E> elements,
		final Predicate<? super E> predicate
	)
	{
		final long size;
		if(index == (size = target.size()))
		{
			XUtilsCollection.addAll(target, elements, predicate);
			return target;
		}
		if(index < 0 || index > size)
		{
			throw new IndexBoundsException(size, index);
		}

		// select elements into buffer
		final LimitList<E> buffer = XUtilsCollection.addAll(new LimitList<E>(XTypes.to_int(elements.size())), elements, predicate);

		// internal copying of selected elements
		if(buffer.size() > 0)
		{
			target.insertAll(index, buffer.internalGetStorageArray(), 0, XTypes.to_int(buffer.size()));
		}
		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	@SuppressWarnings("unchecked")
	public static <E> E rngRetrieve(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetrieve(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> E rngRetrieve(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetrieve(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				sample,
				equalator,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> E rngRetrieve(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetrieve(
				AbstractSimpleArrayCollection.<E>internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngRemoveOne(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveOne(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngRemoveOne(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveOne(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				sample,
				equalator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemoveNull(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveNull(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemove(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemove(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemove(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemove(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				sample,
				equalator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRemoveAll(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemoveAll(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> elements
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				elements
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRetainAll(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				(XGettingCollection<E>)samples,
				equalator,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRetainAll(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> elements
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				(XGettingCollection<E>)elements,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRemoveDuplicates(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveDuplicates(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				equalator,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRemoveDuplicates(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveDuplicates(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngReduce(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedReduce(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate,
				(E)MARKER
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E, C extends Consumer<? super E>> C rngMoveTo(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedMoveTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target,
				predicate,
				(E)MARKER
			);
			return target;
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> XProcessingSequence<E> rngProcess(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Consumer<? super E> procedure
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedProcess(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				procedure,
				(E)MARKER
			);
			return sequence;
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> void rngSort(
		final XSettingList<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedSort(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> void rngShuffle(
		final XSettingList<E> sequence,
		final long offset,
		final long length
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedShuffle(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length)
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> void rngSortMerge(
		final XSettingList<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedSortMerge(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> void rngSortInsertion(
		final XSettingList<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedSortInsertion(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> void rngSortQuick(
		final XSettingList<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedSortQuick(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString appendTo(final XGettingCollection<E> collection, final VarString vc)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection), XTypes.to_int(collection.size()), vc
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		collection.iterate(e -> vc.add(e).append(','));
		return vc.deleteLast();
	}

	public static <E> VarString appendTo(final XGettingCollection<E> collection, final VarString vc, final char separator)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection), XTypes.to_int(collection.size()), vc, separator
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		collection.iterate(e -> vc.add(e).append(separator));

		return vc.deleteLast();
	}

	public static <E> VarString appendTo(final XGettingCollection<E> collection, final VarString vc, final String separator)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection), XTypes.to_int(collection.size()), vc, separator
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		final char[] sepp = XChars.readChars(separator);
		collection.iterate(e -> vc.add(e).add(sepp));
		return vc.deleteLast(sepp.length);
	}

	public static <E> VarString appendTo(
		final XGettingCollection<E> collection,
		final VarString vc,
		final BiConsumer<VarString, ? super E> appender
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection), XTypes.to_int(collection.size()), vc, appender
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		collection.iterate(e ->
		{
			appender.accept(vc, e);
			vc.append(',');
		});
		return vc.deleteLast();
	}

	public static <E> VarString appendTo(
		final XGettingCollection<E> collection,
		final VarString vc, final BiConsumer<VarString, ? super E> appender,
		final char separator
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				XTypes.to_int(collection.size()), vc, appender, separator
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		collection.iterate(e ->
		{
			appender.accept(vc, e);
			vc.append(separator);
		});
		return vc.deleteLast();
	}

	public static <E> VarString appendTo(
		final XGettingCollection<E> collection,
		final VarString vc, final BiConsumer<VarString, ? super E> appender,
		final String separator
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				XTypes.to_int(collection.size()), vc, appender, separator
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		final char[] sepp = XChars.readChars(separator);
		collection.iterate(e ->
		{
			appender.accept(vc, e);
			vc.add(sepp);
		});
		return vc.deleteLast(sepp.length);
	}

	// counting add and put //

	@SuppressWarnings("unchecked")
	public static <E> int addAll(final XAddingCollection<E> target, final E... elements)
	{
		// parameter type ensures target really implements the internal adding methods
		if(target instanceof AbstractExtendedCollection<?>)
		{
			return ((AbstractExtendedCollection<E>)target).internalCountingAddAll(elements);
		}

		int addCount = 0;
		for(int i = 0; i < elements.length; i++)
		{
			if(target.add(elements[i]))
			{
				addCount++;
			}
		}
		return addCount;
	}
	@SuppressWarnings("unchecked")
	public static <E> int addAll(final XAddingCollection<E> target, final E[] elements, final int offset, final int length)
	{
		// parameter type ensures target really implements the internal adding methods
		if(target instanceof AbstractExtendedCollection<?>)
		{
			return ((AbstractExtendedCollection<E>)target).internalCountingAddAll(
				elements,
				XTypes.to_int(offset),
				XTypes.to_int(length)
			);
		}

		final int d;
		if((d = XArrays.validateArrayRange(elements, offset, length)) == 0)
		{
			return 0;
		}

		final int bound = XTypes.to_int(offset + length);
		int addCount = 0;
		for(int i = XTypes.to_int(offset); i != bound; i += d)
		{
			if(target.add(elements[i]))
			{
				addCount++;
			}
		}
		return addCount;
	}
	@SuppressWarnings("unchecked")
	public static <E> int addAll(final XAddingCollection<E> target, final XGettingCollection<? extends E> elements)
	{
		// parameter type ensures target really implements the internal adding methods
		if(target instanceof AbstractExtendedCollection<?>)
		{
			return ((AbstractExtendedCollection<E>)target).internalCountingAddAll(elements);
		}

		return elements.iterate(new AggregateCountingAdd<>(target)).yield();
	}

	@SuppressWarnings("unchecked")
	public static <E> int putAll(final XPuttingCollection<E> target, final E... elements)
	{
		// parameter type ensures target really implements the internal putting methods
		if(target instanceof AbstractExtendedCollection<?>)
		{
			return ((AbstractExtendedCollection<E>)target).internalCountingPutAll(elements);
		}

		int addCount = 0;
		for(int i = 0; i < elements.length; i++)
		{
			if(target.put(elements[i]))
			{
				addCount++;
			}
		}
		return addCount;
	}
	@SuppressWarnings("unchecked")
	public static <E> int putAll(
		final XPuttingCollection<E> target  ,
		final E[]                   elements,
		final int                   offset  ,
		final int                   length
	)
	{
		// parameter type ensures target really implements the internal putting methods
		if(target instanceof AbstractExtendedCollection<?>)
		{
			return ((AbstractExtendedCollection<E>)target).internalCountingPutAll(elements, offset, length);
		}

		final int d;
		if((d = XArrays.validateArrayRange(elements, offset, length)) == 0)
		{
			return 0;
		}

		final int bound = XTypes.to_int(offset + length);
		int addCount = 0;
		for(int i = XTypes.to_int(offset); i != bound; i += d)
		{
			if(target.put(elements[i]))
			{
				addCount++;
			}
		}
		return addCount;
	}

	@SuppressWarnings("unchecked")
	public static <E> int putAll(final XPuttingCollection<E> target, final XGettingCollection<? extends E> elements)
	{
		// parameter type ensures target really implements the internal putting methods
		if(target instanceof AbstractExtendedCollection<?>)
		{
			return ((AbstractExtendedCollection<E>)target).internalCountingPutAll(elements);
		}

		return elements.iterate(new AggregateCountingPut<>(target)).yield();
	}

	public static <E, S extends E> E[] toArray(
		final XGettingCollection<S> collection        ,
		final Class<E>              arrayComponentType
	)
	{
		final E[] array = X.Array(arrayComponentType, X.checkArrayRange(collection.size()));
		XArrays.copyTo(collection, array);

		return array;
	}


	@SafeVarargs
	public static final <V> EqHashTable<Integer, V> toTable(final V... values)
	{
		final EqHashTable<Integer, V> table = EqHashTable.New();

		for(int i = 0; i < values.length; i++)
		{
			table.add(i, values[i]);
		}

		return table;
	}


	/* (07.07.2011 TM)TODO: binarySearch()
	 * Note: binarySearch must be a util method and can't be a sequence instance method
	 * as unsorted sequence can't be (efficiently) guaranteed to be sorted according to
	 * the passed comparator. Having to ensure "manually" to sort the sequence beforehand
	 * would be an unnatural contract.
	 * Furthermore, binarySearch can be inefficient for chain storage implementations,
	 * further discouraging the declaration of binarySearch() in an interface type.
	 *
	 * And SortedSequences can't be explicitly binarySearch-ed either, as they use an
	 * internal comparator. BinarySearch can be used internally when searching an element.
	 *
	 * Hence, the binarySearch() is best defined as an independent util method taking advantage
	 * of proper delegation architecture.
	 *
	 */


	/**
	 * Creates an array containing the indices of all elements the passed predicate applies to.
	 *
	 * @param <E> the element type of the passed sequence.
	 * @param sequence the sequence to be indexed.
	 * @param predicate the predicate to be used to create the index.
	 * @return the index describing the passed predicate for the passed sequence.
	 */
	public static <E> int[] index(final XGettingSequence<E> sequence, final Predicate<? super E> predicate)
	{
		final Indexer<E> indexer;
		sequence.iterateIndexed(indexer = new Indexer<>(predicate));
		return indexer.yield();
	}

	public static <E> int[] orderedIndex(final XGettingSequence<E> sequence, final Predicate<? super E> predicate)
	{
		final Indexer<E> indexer;
		sequence.iterateIndexed(indexer = new Indexer<>(predicate));
		return indexer.sortAndYield();
	}
	
	public static <I, O, C extends XAddingCollection<? super O>> C projectInto(
		final Iterable<? extends I> elements ,
		final Function<I, O>        projector,
		final C                     target
	)
	{
		for(final I e : elements)
		{
			target.add(projector.apply(e));
		}
		return target;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XUtilsCollection()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
