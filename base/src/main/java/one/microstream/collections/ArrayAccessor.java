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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.old.AbstractOldSettingList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XSettingList;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;


/**
 * Full scale general purpose implementation of extended collection type {@link XList}.
 * <p>
 * This array-backed implementation is optimal for all needs of a list that do not require frequent structural
 * modification (insert or remove) of single elements before the end of the list.<br>
 * It is recommended to use this implementation as default list type until concrete performance deficiencies are
 * identified. If used properly, this implementation has equal or
 * massively superior performance to linked-list implementation in most cases.
 * <p>
 * This implementation is NOT synchronized and thus should only be used by a
 * single thread or in a thread-safe manner (i.e. read-only as soon as multiple threads access it).<br>
 * See {@link SynchList} wrapper class to use a list in a synchronized manner.
 * <p>
 * Note that this List implementation does NOT keep track of modification count as JDK's collection implementations do
 * (and thus never throws a {@link ConcurrentModificationException}), for two reasons:<br>
 * 1.) It is already explicitly declared thread-unsafe and for single-thread (or thread-safe)
 * use only.<br>
 * 2.) The common modCount-concurrency exception behavior ("failfast") has buggy and inconsistent behavior by
 * throwing {@link ConcurrentModificationException} even in single thread use, i.e. when iterating over a collection
 * and removing more than one element of it without using the iterator's method.<br>
 * <br>
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 *
 * @version 0.9, 2011-02-06
 */
public final class ArrayAccessor<E> extends AbstractSimpleArrayCollection<E> implements XSettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final Object   MARKER = new Object() ;
	private static final Object[] DUMMY  = new Object[0];



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static String exceptionStringRange(final long size, final long startIndex, final long length)
	{
		return "Range [" + (length < 0 ? startIndex + length + 1 + ";" + startIndex
			: startIndex + ";" + (startIndex + length - 1)) + "] not in [0;" + (size - 1) + "]";
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private E[] data; // the storage array containing the elements
	private int size; // the current element count (logical size)



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public ArrayAccessor()
	{
		super();
		this.data = (E[])DUMMY;
		this.size = 0;
	}

	public ArrayAccessor(final ArrayAccessor<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data;
		this.size = original.size;
	}

	@SafeVarargs
	public ArrayAccessor(final E... elements) throws NullPointerException
	{
		super();
		this.data = elements;
		this.size = elements.length;
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
	public ArrayAccessor<E> setArray(final E[] array)
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
	public ArrayAccessor<E> copy()
	{
		return new ArrayAccessor<>(this);
	}

	@Override
	public XImmutableList<E> immure()
	{
		return ConstList.New(this);
	}

	@Override
	public ArrayAccessor<E> toReversed()
	{
		final E[] rData = X.ArrayOfSameType(this.data, this.size);
		final E[] data = this.data;
		for(int i = this.size, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new ArrayAccessor<>(rData);
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
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardQueryElement(this.data, 0, this.size, predicate, null);
	}

	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.forwardContainsSame(this.data, 0, this.size, sample) ? sample : null;
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
		if(samples == null || !(samples instanceof ArrayAccessor<?>) || XTypes.to_int(samples.size()) != this.size)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}

		// equivalent to equalsContent()
		return XArrays.equals(
			this.data                       ,
			0                               ,
			((ArrayAccessor<?>)samples).data,
			0                               ,
			this.size                       ,
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
		return AbstractArrayStorage.rangedCopyTo(
			this.data, this.size, startIndex, length,  target, offset
		);
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
	// setting methods //
	////////////////////

	@Override
	public ListView<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex); // range check is done in constructor
	}

	@Override
	public ArrayAccessor<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		if(sourceIndex >= this.size)
		{
			throw new IndexExceededException(this.size, sourceIndex);
		}
		if(targetIndex >= this.size)
		{
			throw new IndexExceededException(this.size, targetIndex);
		}
		if(sourceIndex == targetIndex)
		{
			if(sourceIndex < 0)
			{
				throw new IndexExceededException(this.size, sourceIndex);
			}
			return this;
		}

		final E shiftling = this.data[(int)sourceIndex];
		if(sourceIndex < targetIndex)
		{
			System.arraycopy(this.data, (int)sourceIndex + 1, this.data, (int)sourceIndex, (int)targetIndex - (int)sourceIndex);
		}
		else
		{
			System.arraycopy(this.data, (int)targetIndex, this.data, (int)targetIndex + 1, (int)sourceIndex - (int)targetIndex);
		}

		this.data[(int)targetIndex] = shiftling;
		return this;
	}

	@Override
	public ArrayAccessor<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		if(sourceIndex + length >= this.size)
		{
			throw new IndexBoundsException(this.size, sourceIndex);
		}
		if(targetIndex + length >= this.size)
		{
			throw new IndexBoundsException(this.size, targetIndex);
		}
		if(sourceIndex == targetIndex)
		{
			if(sourceIndex < 0)
			{
				throw new IndexBoundsException(this.size, sourceIndex);
			}
			return this;
		}

		final Object[] shiftlings;
		System.arraycopy(this.data, (int)sourceIndex, shiftlings = newArray(X.checkArrayRange(length)), 0, (int)length);
		if(sourceIndex < targetIndex)
		{
			System.arraycopy(
				this.data,
				X.checkArrayRange(sourceIndex + length),
				this.data,
				(int)sourceIndex,
				X.checkArrayRange(targetIndex - sourceIndex)
			);
		}
		else
		{
			System.arraycopy(
				this.data,
				(int)targetIndex,
				this.data,
				X.checkArrayRange(targetIndex + length),
				X.checkArrayRange(sourceIndex - targetIndex)
			);
		}

		System.arraycopy(shiftlings, 0, this.data, (int)targetIndex, (int)length);
		return this;
	}

	@Override
	public ArrayAccessor<E> shiftBy(final long sourceIndex, final long distance)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance);
	}

	@Override
	public ArrayAccessor<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance, length);
	}

	@Override
	public ArrayAccessor<E> swap(final long indexA, final long indexB)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(indexA >= this.size)
		{
			throw new IndexBoundsException(this.size, indexA);
		}
		if(indexB >= this.size)
		{
			throw new IndexBoundsException(this.size, indexB);
		}

		final E t = this.data[(int)indexA];
		this.data[(int)indexA] = this.data[(int)indexB];
		this.data[(int)indexB] = t;

		return this;
	}

	@Override
	public ArrayAccessor<E> swap(final long indexA, final long indexB, final long length)
	{
		AbstractArrayStorage.swap(
			this.data                     ,
			this.size                     ,
			X.checkArrayRange(indexA),
			X.checkArrayRange(indexB),
			X.checkArrayRange(length)
		);
		return this;
	}

	@Override
	public ArrayAccessor<E> reverse()
	{
		AbstractArrayStorage.reverse(this.data, this.size);
		return this;
	}

	// direct setting //

	@Override
	public void setFirst(final E element)
	{
		this.data[0] = element;
	}

	@Override
	public void setLast(final E element)
	{
		this.data[this.size - 1] = element;
	}

	@SafeVarargs
	@Override
	public final ArrayAccessor<E> setAll(final long offset, final E... elements)
	{
		if(offset < 0 || offset + elements.length > this.size)
		{
			throw new IndexOutOfBoundsException(exceptionStringRange(this.size, offset, offset + elements.length - 1));
		}
		System.arraycopy(elements, 0, this.data, X.checkArrayRange(offset), elements.length);
		return this;
	}

	@Override
	public ArrayAccessor<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		AbstractArrayStorage.set(this.data, this.size, X.checkArrayRange(offset), src, srcIndex, srcLength);
		return this;
	}

	@Override
	public ArrayAccessor<E> set(
		final long                          offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                          elementsOffset,
		final long                          elementsLength
	)
	{
		AbstractArrayStorage.set(this.data, this.size, X.checkArrayRange(offset), elements, elementsOffset, elementsLength);
		return this;
	}

	@Override
	public ArrayAccessor<E> fill(final long offset, final long length, final E element)
	{
		AbstractArrayStorage.fill(
			this.data                     ,
			this.size                     ,
			X.checkArrayRange(offset),
			X.checkArrayRange(length),
			element
		);
		return this;
	}

	// sorting //

	@Override
	public ArrayAccessor<E> sort(final Comparator<? super E> comparator)
	{
		XSort.mergesort(this.data, 0, this.size, comparator);
		return this;
	}

	// replacing - single //

	@Override
	public boolean replaceOne(final E element, final E replacement)
	{
		return AbstractArrayStorage.replaceOne(this.data, this.size, element, replacement);
	}

	@Override
	public boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		return AbstractArrayStorage.substituteOne(this.data, this.size, predicate, substitute);
	}

	// replacing - multiple //

	@Override
	public long replace(final E element, final E replacement)
	{
		return AbstractArrayStorage.replace(this.data, this.size, element, replacement);
	}

	@Override
	public long replace(final Predicate<? super E> predicate, final E substitute)
	{
		return AbstractArrayStorage.substitute(this.data, this.size, predicate, substitute);
	}

	// replacing - multiple all //

	@Override
	public long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		return AbstractArrayStorage.replaceAll(this.data, this.size, elements, replacement, MARKER);
	}

	@Override
	public long substitute(final Function<? super E, ? extends E> mapper)
	{
		return AbstractArrayStorage.substitute(this.data, this.size, mapper);
	}

	@Override
	public long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		return AbstractArrayStorage.substitute(this.data, this.size, predicate, mapper);
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
	public boolean set(final long index, final E element)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}

		this.data[(int)index] = element;

		return false;
	}

	@Override
	public E setGet(final long index, final E element) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}

		final E old = this.data[(int)index];
		this.data[(int)index] = element;

		return old;
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
	public SubListAccessor<E> range(final long fromIndex, final long toIndex)
	{
		// range check is done in constructor
		return new SubListAccessor<>(this, fromIndex, toIndex);
	}

	@Override
	public String toString()
	{
		return AbstractArrayStorage.toString(this.data, this.size);
	}

	@Override
	public Object[] toArray()
	{
		final Object[] array;
		System.arraycopy(this.data, 0, array = new Object[this.size], 0, this.size);
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
		// (09.04.2012 TM)FIXME: what's List supposed to do here? (check all other occurrences as well)
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
	public OldArrayAccessor<E> old()
	{
		return new OldArrayAccessor<>(this);
	}

	public static final class OldArrayAccessor<E> extends AbstractOldSettingList<E>
	{
		OldArrayAccessor(final ArrayAccessor<E> list)
		{
			super(list);
		}

		@Override
		public ArrayAccessor<E> parent()
		{
			return (ArrayAccessor<E>)super.parent();
		}

	}



}
