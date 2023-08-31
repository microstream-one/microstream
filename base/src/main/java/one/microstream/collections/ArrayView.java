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
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.old.AbstractOldGettingList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XSettingList;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;


/**
 * Immutable implementation of extended collection type {@link XGettingList}.
 * <p>
 * For mutable extended lists (implementors of {@link XSettingList}, {@link XList}), see {@link FixedList},
 * {@link LimitList}, {@link BulkList}.
 * <p>
 * As instances of this class are completely immutable after creation, this list is automatically thread-safe.
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 *
 * @version 0.91, 2011-02-28
 */
public final class ArrayView<E> extends AbstractSimpleArrayCollection<E> implements XGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final Object[] DUMMY = new Object[0];



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	E[] data; // the storage array containing the elements
	int size; // the current element count (logical size)



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public ArrayView()
	{
		super();
		this.data = (E[])DUMMY;
		this.size = 0;
	}

	public ArrayView(final ArrayView<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data;
		this.size = original.size;
	}

	@SuppressWarnings("unchecked")
	public ArrayView(final E... elements) throws NullPointerException
	{
		super();
		this.size = (this.data = elements != null ? elements : (E[])DUMMY).length;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public E[] getArray()
	{
		return this.data == DUMMY ? null : (E[])this.data;
	}

	@Override
	public Equalator<? super E> equality()
	{
		return Equalator.identity();
	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}



	///////////////////////////////////////////////////////////////////////////
	// setters //
	////////////

	@SuppressWarnings("unchecked")
	public ArrayView<E> setArray(final E[] array)
	{
		if(array == null)
		{
			this.data = (E[])DUMMY;
			this.size = 0;
			return this;
		}

		if(this.size < 0 || this.size > array.length)
		{
			throw new ArrayIndexOutOfBoundsException(this.size);
		}
		this.data = array;
		this.size = array.length;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected E[] internalGetStorageArray()
	{
		return this.data;
	}

	@Override
	protected int internalSize()
	{
		return this.size;
	}

	@Override
	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.size}; // trivial section
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public ArrayView<E> copy()
	{
		return new ArrayView<>(this);
	}

	@Override
	public XImmutableList<E> immure()
	{
		return ConstList.New(this);
	}

	@Override
	public ArrayView<E> toReversed()
	{
		final E[] rData = X.ArrayOfSameType(this.data, this.size);
		final E[] data = this.data;
		for(int i = this.size, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new ArrayView<>(rData);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = X.Array(type, this.size);
		System.arraycopy(this.data, 0, array, 0, this.size);
		return array;
	}

	// executing //

	@Override
	public <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate(this.data, this.size, procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join(this.data, this.size, joiner, aggregate);
		return aggregate;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		AbstractArrayStorage.iterate(this.data, this.size, procedure);
		return procedure;
	}

	// aggregating //

	// count querying //

	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.forwardCount(this.data, 0, this.size, element);
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalCount(this.data, 0, this.size, predicate);
	}

	// index querying //

	@Override
	public long indexOf(final E element)
	{
		return AbstractArrayStorage.forwardIndexOf(this.data, 0, this.size, element);
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalIndexOf(this.data, 0, this.size, predicate);
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return AbstractArrayStorage.rangedIndexOF(this.data, this.size, this.size - 1, -this.size, element);
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.lastIndexOf(this.data, this.size, predicate);
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.maxIndex(this.data, this.size, comparator);
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.minIndex(this.data, this.size, comparator);
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardScan(this.data, 0, this.size, predicate);
	}

	// element querying //

	@Override
	public E get()
	{
		return this.data[0];
	}

	@Override
	public E first()
	{
		return this.data[0];
	}

	@Override
	public E last()
	{
		return this.data[this.size - 1];
	}

	@Override
	public E poll()
	{
		return this.size == 0 ? null : (E)this.data[0];
	}

	@Override
	public E peek()
	{
		return this.size == 0 ? null : (E)this.data[this.size - 1];
	}

	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.forwardContainsSame(this.data, 0, this.size, sample) ? sample : null;
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardQueryElement(this.data, 0, this.size, predicate, null);
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.max(this.data, this.size, comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.min(this.data, this.size, comparator);
	}

	// boolean querying //

	@Override
	public boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public boolean nullAllowed()
	{
		return true;
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.isSorted(this.data, this.size, comparator);
	}

	// boolean querying - applies //

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardContains(this.data, 0, this.size, predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardApplies(this.data, 0, this.size, predicate);
	}

	// boolean querying - contains //

	@Override
	public boolean nullContained()
	{
		return AbstractArrayStorage.forwardNullContained(this.data, 0, this.size);
	}

	@Override
	public boolean containsId(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame(this.data, 0, this.size, element);
	}

	@Override
	public boolean contains(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame(this.data, 0, this.size, element);
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll(this.data, this.size, elements);
	}

	// boolean querying - equality //

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof ArrayView<?>) || XTypes.to_int(samples.size()) != this.size)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}

		// equivalent to equalsContent()
		return XArrays.equals(
			this.data,
			0,
			((ArrayView<?>)samples).data,
			0,
			this.size,
			(Equalator<Object>)equalator
		);
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || XTypes.to_int(samples.size()) != this.size)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return AbstractArrayStorage.equalsContent(this.data, this.size, samples, equalator);
	}

	// data set procedures //

	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.intersect(this.data, this.size, samples, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.except(this.data, this.size, samples, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.union(this.data, this.size, samples, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return AbstractArrayStorage.forwardCopyTo(this.data, 0, this.size, target);
	}

	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardCopyTo(this.data, 0, this.size, target, predicate);
	}

	public <T> T[] rngCopyTo(final int startIndex, final int length, final T[] target, final int offset)
	{
		return AbstractArrayStorage.rangedCopyTo(this.data, this.size, startIndex, length,  target, offset);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return AbstractArrayStorage.distinct(this.data, this.size, target);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.distinct(this.data, this.size, target, equalator);
	}

	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return AbstractArrayStorage.copySelection(this.data, this.size, indices, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives //
	///////////////////////////////////

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
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
		validateIndex(this.size, index);
		return new ReadOnlyListIterator<>(this, (int)index);
	}

	@Override
	public long size()
	{
		return this.size;
	}

	@Override
	public long maximumCapacity()
	{
		return this.size; // size is always array length
	}

	@Override
	public boolean isFull()
	{
		return true; // array is always "full"
	}

	@Override
	public long remainingCapacity()
	{
		return 0;
	}

	@Override
	public ArrayView<E> view()
	{
		return this;
	}

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex); // range check is done in constructor
	}

	@Override
	public SubListView<E> range(final long fromIndex, final long toIndex)
	{
		// range check is done in constructor
		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public String toString()
	{
		return AbstractArrayStorage.toString(this.data, this.size);
	}

	@Override
	public Object[] toArray()
	{
		final Object[] array = new Object[this.size];
		System.arraycopy(this.data, 0, array, 0, this.size);
		return array;
	}

	@Override
	public E at(final long index) throws ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		return this.data[(int)index];
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		//trivial escape conditions
		if(o == this)
		{
			return true;
		}
		if(o == null || !(o instanceof List<?>))
		{
			return false;
		}

		final List<?> list = (List<?>)o;
		if(this.size != list.size())
		{
			return false; //lists can only be equal if they have the same length
		}

		final Object[] data = this.data;
		int i = 0;
		for(final Object e2 : list)
		{
			// use iterator for passed list as it could be a non-random-access list
			final Object e1 = data[i++];
			if(e1 == null)
			{
				// null-handling escape conditions
				if(e2 != null)
				{
					return false;
				}
				continue;
			}
			if(!e1.equals(e2))
			{
				return false;
			}
		}
		return true; //no un-equal element found, so lists must be equal
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return XArrays.arrayHashCode(this.data, this.size);
	}

	@Override
	public OldArrayView<E> old()
	{
		return new OldArrayView<>(this);
	}

	public static final class OldArrayView<E> extends AbstractOldGettingList<E>
	{
		OldArrayView(final ArrayView<E> list)
		{
			super(list);
		}

		@Override
		public ArrayView<E> parent()
		{
			return (ArrayView<E>)super.parent();
		}

	}

}
