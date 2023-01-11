
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
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.old.AbstractOldGettingList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XImmutableList;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;

public class SubListView<E> implements XGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static final <E> IndexedAcceptor<E> offset(
		final IndexedAcceptor<? super E> procedure,
		final long startIndex,
		final int d
	)
	{
		// tricky 8-)
		return new IndexedAcceptor<E>()
		{
			@Override
			public void accept(final E e, final long index)
			{
				procedure.accept(e, (index - startIndex) * d);
			}
		};
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XGettingList<E> list;
	long startIndex;
	long size;
	long length;
	int d;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SubListView(final XGettingList<E> list, final long fromIndex, final long toIndex)
	{
		final long length, size;
		if(fromIndex <= toIndex)
		{
			size = toIndex - fromIndex + 1;
			length = size;
			if(fromIndex < 0 || toIndex >= XTypes.to_int(list.size()))
			{
				throw new IndexOutOfBoundsException(this.exceptionStringRange(fromIndex, size));
			}
		}
		else
		{
			size = fromIndex - toIndex + 1;
			length = -size;
			if(toIndex < 0 || fromIndex >= XTypes.to_int(list.size()))
			{
				throw new IndexOutOfBoundsException(this.exceptionStringRange(toIndex, size));
			}
		}

		this.list = list;
		this.startIndex = fromIndex;
		this.size = size;
		this.length = length;
		this.d = length < 0 ? -1 : 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	String exceptionStringRange(final long startIndex, final long length)
	{
		return "Range [" + startIndex + ';' + (startIndex + length - 1) + "] not in [0;" + (this.size - 1) + "]";
	}

	String exceptionStringRange2(final long startIndex, final long endIndex)
	{
		return "Range [" + startIndex + ';' + endIndex + "] not in [0;" + (this.size - 1) + "]";
	}

	void checkIndex(final long index)
	{
		if(index < 0 || index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
	}

	void checkVector(final long startIndex, final long length)
	{
		if(length >= 0)
		{
			if(startIndex < 0 || startIndex + length > this.size)
			{
				throw new IndexOutOfBoundsException(this.exceptionStringRange(startIndex, length));
			}
		}
		else
		{
			if(startIndex + length < -1 || startIndex >= this.size)
			{
				throw new IndexOutOfBoundsException(this.exceptionStringRange(startIndex, length));
			}
		}
	}

	void checkRange(final long startIndex, final long endIndex)
	{
		if(startIndex < 0 || endIndex >= this.size)
		{
			throw new IndexOutOfBoundsException(this.exceptionStringRange2(startIndex, endIndex));
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Equalator<? super E> equality()
	{
		return this.list.equality();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return  XUtilsCollection.rngApplies(this.list, this.startIndex, this.length, predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return  XUtilsCollection.rngAppliesAll(this.list, this.startIndex, this.length, predicate);
	}

	@Override
	public boolean nullAllowed()
	{
		return this.list.nullAllowed();
	}

	@Override
	public boolean nullContained()
	{
		return  XUtilsCollection.rngContainsNull(this.list, this.startIndex, this.length);
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return  XUtilsCollection.rngCount(this.list, this.startIndex, this.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || XTypes.to_int(samples.size()) != this.size || !(samples instanceof SubList<?>))
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return  XUtilsCollection.rngEqualsContent(
			this.list          ,
			this.startIndex    ,
			this.length        ,
			(SubList<E>)samples,
			equalator
		);
	}

	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return XUtilsCollection.rngExcept(this.list, this.startIndex, this.length, other, equalator, target);
	}

	@Override
	public boolean contains(final E element)
	{
		return  XUtilsCollection.rngContains(this.list, this.startIndex, this.length, element);
	}

	@Override
	public long count(final E element)
	{
		return  XUtilsCollection.rngCount(this.list, this.startIndex, this.length, element);
	}

	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return XUtilsCollection.rngIntersect(this.list, this.startIndex, this.length, other, equalator, target);
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return  XUtilsCollection.rngMax(this.list, this.startIndex, this.length, comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return  XUtilsCollection.rngMin(this.list, this.startIndex, this.length, comparator);
	}

	@Override
	public SubListView<E> copy()
	{
		return new SubListView<>(this.list, this.startIndex, this.getEndIndex());
	}

	protected long[] shiftIndices(final long[] indices)
	{
		// shift indices, determine min and max, check range
		final int len;
		final long startIndex = this.startIndex;
		final long[] shifted = new long[len = indices.length];
		long min = Long.MAX_VALUE, max = 0;

		long idx;
		for(int i = 0; i < len; i++)
		{
			idx = indices[i];
			if(idx < min)
			{
				min = idx;
			}
			if(idx > max)
			{
				max = idx;
			}
			shifted[i] = idx + startIndex;
		}
		this.checkRange(min, max);
		return shifted;
	}

	public long getEndIndex()
	{
		return this.startIndex + this.length - this.d;
	}

	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return this.list.copySelection(target, this.shiftIndices(indices));
	}

	@Override
	public E at(final long index)
	{
		this.checkIndex(index);
		return this.list.at(this.startIndex + index - this.d);
	}

	@Override
	public E get()
	{
		return this.list.at(this.startIndex);
	}

	@Override
	public E first()
	{
		return this.list.at(this.startIndex);
	}

	@Override
	public E last()
	{
		return this.list.at(this.getEndIndex());
	}

	@Override
	public E poll()
	{
		return this.size == 0 ? null : this.list.at(this.startIndex);
	}

	@Override
	public E peek()
	{
		return this.size == 0 ? null : this.list.at(this.getEndIndex());
	}

	@Override
	public long indexOf(final E element)
	{
		return XUtilsCollection.rngIndexOF(this.list, this.startIndex, this.length, element);
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return XUtilsCollection.rngIndexOf(this.list, this.startIndex, this.length, predicate);
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return XUtilsCollection.rngIsSorted(this.list, this.startIndex, this.length, comparator);
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return XUtilsCollection.rngIndexOF(this.list, this.getEndIndex(), -this.length, element);
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return XUtilsCollection.rngIndexOf(this.list, this.getEndIndex(), -this.length, predicate);
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		AbstractExtendedCollection.validateIndex(this.list.size(), index);
		return new ReadOnlyListIterator<>(this, (int)index);
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return XUtilsCollection.rngMaxIndex(this.list, this.startIndex, this.length, comparator);
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return XUtilsCollection.rngMinIndex(this.list, this.startIndex, this.length, comparator);
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return XUtilsCollection.rngScan(this.list, this.startIndex, this.length, predicate);
	}

	@Override
	public long size()
	{
		return this.size;
	}

	@Override
	public long maximumCapacity()
	{
		return this.list.maximumCapacity() - XTypes.to_int(this.list.size()) + this.size; // complicated ^^
	}

	@Override
	public boolean isFull()
	{
		return this.list.isFull();
	}

	@Override
	public long remainingCapacity()
	{
		return this.list.remainingCapacity();
	}

	@Override
	public SubListView<E> view()
	{
		return this;
	}

	@Override
	public SubListView<E> view(final long lowIndex, final long highIndex)
	{
		this.checkRange(lowIndex, highIndex);
		return new SubListView<>(this.list, this.startIndex + lowIndex * this.d, this.startIndex + highIndex * this.d);
	}

	@Override
	public SubListView<E> range(final long lowIndex, final long highIndex)
	{
		this.checkRange(lowIndex, highIndex);
		return new SubListView<>(this.list, this.startIndex + lowIndex * this.d, this.startIndex + highIndex * this.d);
	}

	@Override
	public Object[] toArray()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME SubListView#toArray()
	}

	@Override
	public XImmutableList<E> immure()
	{
		return ConstList.New(this);
	}

	@Override
	public SubListView<E> toReversed()
	{
		return new SubListView<>(this.list, this.getEndIndex(), this.startIndex);
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return XUtilsCollection.rngContainsAll(this.list, this.startIndex, this.length, elements);
	}

	@Override
	public boolean containsId(final E element)
	{
		return XUtilsCollection.rngContainsId(this.list, this.startIndex, this.length, element);
	}

	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return XUtilsCollection.rngCopyTo(this.list, this.startIndex, this.length, target);
	}

	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return XUtilsCollection.rngCopyTo(this.list, this.startIndex, this.length, target, predicate);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return XUtilsCollection.rngDistinct(this.list, this.startIndex, this.length, target);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return XUtilsCollection.rngDistinct(this.list, this.startIndex, this.length, target, equalator);
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.list.hasVolatileElements();
	}

	@Override
	public E seek(final E sample)
	{
		return XUtilsCollection.rngGet(this.list, this.startIndex, this.length, sample, this.list.equality());
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return XUtilsCollection.rngSearch(this.list, this.startIndex, this.length, predicate);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME SubListView#toArray()
	}

	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return XUtilsCollection.rngUnion(this.list, this.startIndex, this.length, other, equalator, target);
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		XUtilsCollection.rngIterate(this.list, this.startIndex, this.length, procedure);
		return procedure;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		XUtilsCollection.rngIterate(this.list, this.startIndex, this.length, offset(procedure, this.startIndex, this.d));
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		XUtilsCollection.rngJoin(this.list, this.startIndex, this.length, joiner, aggregate);
		return aggregate;
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public OldSubListView<E> old()
	{
		return new OldSubListView<>(this);
	}

	static class OldSubListView<E> extends AbstractOldGettingList<E>
	{
		OldSubListView(final SubListView<E> list)
		{
			super(list);
		}

		@Override
		public SubListView<E> parent()
		{
			return (SubListView<E>)super.parent();
		}

	}

}
