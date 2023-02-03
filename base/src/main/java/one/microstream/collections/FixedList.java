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

import java.util.Collection;
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
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XSettingList;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;


/**
 * Full scale general purpose implementation of extended collection type {@link XList}.
 * <p>
 * This array-backed implementation is optimal for all needs of a list that do not require frequent structural
 * modification (insert or remove) of single elements before the end of the list.<br>
 * It is recommended to use this implementation as default list type until concrete performance deficiencies are
 * identified. If used properly, this implementation has equal or
 * massively superior performance to linked-list implementation is most cases.
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
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 *
 * @version 0.9, 2011-02-06
 */
public final class FixedList<E> extends AbstractSimpleArrayCollection<E> implements XSettingList<E>, Composition
{
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

	final Object[] data; // the storage array containing the elements



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public FixedList(final int initialCapacity)
	{
		super();
		this.data = new Object[initialCapacity];
	}

	public FixedList(final FixedList<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data.clone();
	}

	public FixedList(final Collection<? extends E> elements) throws NullPointerException
	{
		super();
		this.data = elements.toArray();
	}

	public FixedList(final XGettingCollection<? extends E> elements) throws NullPointerException
	{
		super();
		this.data = elements.toArray();
	}

	@SafeVarargs
	public FixedList(final E... elements) throws NullPointerException
	{
		super();
		this.data = elements.clone();
	}

	public FixedList(final E[] src, final int srcStart, final int srcLength)
	{
		super();
		// automatically check arguments 8-)
		System.arraycopy(src, srcStart, this.data = new Object[srcLength], 0, srcLength);
	}

	FixedList(final Object[] internalData, final int size)
	{
		super();
		this.data = internalData;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public static class Creator<E> implements XSettingList.Creator<E>
	{
		private final int initialCapacity;

		public Creator(final int initialCapacity)
		{
			super();
			this.initialCapacity = XMath.pow2BoundMaxed(initialCapacity);
		}

		public int getInitialCapacity()
		{
			return this.initialCapacity;
		}

		@Override
		public FixedList<E> newInstance()
		{
			return new FixedList<>(new Object[this.initialCapacity], this.initialCapacity);
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	@Override
	protected E[] internalGetStorageArray()
	{
		return (E[])this.data;
	}

	@Override
	protected int internalSize()
	{
		return this.data.length;
	}

	@Override
	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.data.length}; // trivial section
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
	// getting methods //
	////////////////////

	@Override
	public FixedList<E> copy()
	{
		return new FixedList<>(this);
	}

	@Override
	public ConstList<E> immure()
	{
		return ConstList.New(this);
	}

	@Override
	public FixedList<E> toReversed()
	{
		final Object[] rData = new Object[this.data.length];
		final Object[] data = this.data;
		for(int i = data.length, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new FixedList<>(rData, data.length);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = X.Array(type, this.data.length);
		System.arraycopy(this.data, 0, array, 0, this.data.length);
		return array;
	}

	// executing //

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate((E[])this.data, this.data.length, procedure);
		return procedure;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		AbstractArrayStorage.iterate((E[])this.data, this.data.length, procedure);
		return procedure;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join((E[])this.data, this.data.length, joiner, aggregate);
		return aggregate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.forwardCount((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalCount((E[])this.data, 0, this.data.length, predicate);
	}

	// index querying //

	@SuppressWarnings("unchecked")
	@Override
	public long indexOf(final E element)
	{
		return AbstractArrayStorage.forwardIndexOf((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalIndexOf((E[])this.data, 0, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexOf(final E element)
	{
		return AbstractArrayStorage.rangedIndexOF((E[])this.data, this.data.length, this.data.length - 1, -this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.lastIndexOf((E[])this.data, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.maxIndex((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.minIndex((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardScan((E[])this.data, 0, this.data.length, predicate);
	}

	// element querying //

	@SuppressWarnings("unchecked")
	@Override
	public E get()
	{
		return (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E first()
	{
		return (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E last()
	{
		return (E)this.data[this.data.length - 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		return this.data.length == 0 ? null : (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		return this.data.length == 0 ? null : (E)this.data[this.data.length - 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.forwardContainsSame((E[])this.data, 0, this.data.length, sample) ? sample : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardQueryElement((E[])this.data, 0, this.data.length, predicate, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.max((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.min((E[])this.data, this.data.length, comparator);
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.isSorted((E[])this.data, this.data.length, comparator);
	}

	// boolean querying - applies //

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardContains((E[])this.data, 0, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardApplies((E[])this.data, 0, this.data.length, predicate);
	}

	// boolean querying - contains //

	@SuppressWarnings("unchecked")
	@Override
	public boolean nullContained()
	{
		return AbstractArrayStorage.forwardNullContained((E[])this.data, 0, this.data.length);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsId(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame((E[])this.data, 0, this.data.length, element);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll((E[])this.data, this.data.length, elements);
	}

	// boolean querying - equality //

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof FixedList<?>) || XTypes.to_int(samples.size()) != this.data.length)
		{
			return false;
		}

		if(samples == this)
		{
			return true;
		}

		// equivalent to equalsContent()
		return XArrays.equals(this.data, 0, ((FixedList<?>)samples).data, 0, this.data.length, (Equalator<Object>)equalator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || XTypes.to_int(samples.size()) != this.data.length)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return AbstractArrayStorage.equalsContent((E[])this.data, this.data.length, samples, equalator);
	}

	// data set procedures //

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.intersect((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.except((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.union((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return AbstractArrayStorage.forwardCopyTo((E[])this.data, 0, this.data.length, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardCopyTo((E[])this.data, 0, this.data.length, target, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return AbstractArrayStorage.distinct((E[])this.data, this.data.length, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.distinct((E[])this.data, this.data.length, target, equalator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return AbstractArrayStorage.copySelection((E[])this.data, this.data.length, indices, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// setting methods //
	////////////////////

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex); // range check is done in constructor
	}

	@Override
	public FixedList<E> swap(final long indexA, final long indexB)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(indexA >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, indexA);
		}
		if(indexB >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, indexB);
		}

		final Object t = this.data[(int)indexA];
		this.data[(int)indexA] = this.data[(int)indexB];
		this.data[(int)indexB] = t;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FixedList<E> swap(final long indexA, final long indexB, final long length)
	{
		AbstractArrayStorage.swap(
			(E[])this.data                ,
			this.data.length              ,
			X.checkArrayRange(indexA),
			X.checkArrayRange(indexB),
			X.checkArrayRange(length)
		);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FixedList<E> reverse()
	{
		AbstractArrayStorage.reverse((E[])this.data, this.data.length);
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
		this.data[this.data.length - 1] = element;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FixedList<E> setAll(final long offset, final E... elements)
	{
		if(offset < 0 || offset + elements.length > this.data.length)
		{
			throw new IndexOutOfBoundsException(exceptionStringRange(this.data.length, offset, offset + elements.length - 1));
		}

		System.arraycopy(elements, 0, this.data, X.checkArrayRange(offset), elements.length);

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FixedList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		AbstractArrayStorage.set((E[])this.data, this.data.length, X.checkArrayRange(offset), src, srcIndex, srcLength);
		return this;
	}

	@Override
	public FixedList<E> set(final long offset, final XGettingSequence<? extends E> elements, final long elementsOffset, final long elementsLength)
	{
		AbstractArrayStorage.set(this.data, this.data.length, X.checkArrayRange(offset), elements, elementsOffset, elementsLength);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FixedList<E> fill(final long offset, final long length, final E element)
	{
		AbstractArrayStorage.fill((E[])this.data, this.data.length, X.checkArrayRange(offset), X.checkArrayRange(length), element);
		return this;
	}

	// sorting //

	@SuppressWarnings("unchecked")
	@Override
	public FixedList<E> sort(final Comparator<? super E> comparator)
	{
		XSort.mergesort(this.data, 0, this.data.length, (Comparator<Object>)comparator);
		return this;
	}

	// replacing - single //

	@SuppressWarnings("unchecked")
	@Override
	public boolean replaceOne(final E element, final E replacement)
	{
		return AbstractArrayStorage.replaceOne((E[])this.data, this.data.length, element, replacement);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		return AbstractArrayStorage.substituteOne((E[])this.data, this.data.length, predicate, substitute);
	}

	// replacing - multiple //

	@SuppressWarnings("unchecked")
	@Override
	public long replace(final E element, final E replacement)
	{
		return AbstractArrayStorage.replace((E[])this.data, this.data.length, element, replacement);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long replace(final Predicate<? super E> predicate, final E substitute)
	{
		return AbstractArrayStorage.substitute((E[])this.data, this.data.length, predicate, substitute);
	}

	@Override
	public long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		return AbstractArrayStorage.replaceAll(this.data, this.data.length, elements, replacement, marker());
	}

	@SuppressWarnings("unchecked")
	@Override
	public long substitute(final Function<? super E, ? extends E> mapper)
	{
		return AbstractArrayStorage.substitute((E[])this.data, this.data.length, mapper);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		return AbstractArrayStorage.substitute((E[])this.data, this.data.length, predicate, mapper);
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives //
	///////////////////////////////////

	@Override
	public boolean isEmpty()
	{
		return this.data.length == 0;
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
		validateIndex(this.data.length, index);
		return new ReadOnlyListIterator<>(this, (int)index);
	}

	@Override
	public boolean set(final long index, final E element) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, index);
		}

		this.data[(int)index] = element;

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E setGet(final long index, final E element) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, index);
		}

		final E old = (E)this.data[(int)index];
		this.data[(int)index] = element;

		return old;
	}

	@Override
	public long size()
	{
		return this.data.length;
	}

	@Override
	public long maximumCapacity()
	{
		return this.data.length; // max capacity is always array length
	}

	@Override
	public boolean isFull()
	{
		return true; // fixed list is always "full"
	}

	@Override
	public long remainingCapacity()
	{
		return 0;
	}

	@Override
	public ListView<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public FixedList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		if(sourceIndex >= this.data.length)
		{
			throw new IndexExceededException(this.data.length, sourceIndex);
		}
		if(targetIndex >= this.data.length)
		{
			throw new IndexExceededException(this.data.length, targetIndex);
		}
		if(sourceIndex == targetIndex)
		{
			if(sourceIndex < 0)
			{
				throw new IndexExceededException(this.data.length, sourceIndex);
			}
			return this;
		}

		final Object shiftling = this.data[(int)sourceIndex];
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
	public FixedList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		if(sourceIndex + length >= this.data.length)
		{
			throw new IndexExceededException(this.data.length, sourceIndex);
		}
		if(targetIndex + length >= this.data.length)
		{
			throw new IndexExceededException(this.data.length, targetIndex);
		}
		if(sourceIndex == targetIndex)
		{
			if(sourceIndex < 0)
			{
				throw new IndexExceededException(this.data.length, sourceIndex);
			}

			return this;
		}

		final E[] shiftlings;
		System.arraycopy(this.data, (int)sourceIndex, shiftlings = newArray((int)length), 0, (int)length);
		if(sourceIndex < targetIndex)
		{
			System.arraycopy(this.data, (int)(sourceIndex + length), this.data, (int)sourceIndex, (int)(targetIndex - sourceIndex));
		}
		else
		{
			System.arraycopy(this.data, (int)targetIndex, this.data, (int)(targetIndex + length), (int)(sourceIndex - targetIndex));
		}

		System.arraycopy(shiftlings, 0, this.data, (int)targetIndex, (int)length);

		return this;
	}

	@Override
	public FixedList<E> shiftBy(final long sourceIndex, final long distance)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance);
	}

	@Override
	public FixedList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance, length);
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
		return AbstractArrayStorage.toString(this.data, this.data.length);
	}

	@Override
	public Object[] toArray()
	{
		return this.data.clone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E at(final long index) throws ArrayIndexOutOfBoundsException
	{
		if(index >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, index);
		}
		return (E)this.data[(int)index];
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
		if(this.data.length != list.size())
		{
			return false; // lists can only be equal if they have the same length
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
		return XArrays.arrayHashCode(this.data, this.data.length);
	}



	@Override
	public OldFixedList<E> old()
	{
		return new OldFixedList<>(this);
	}

	public static final class OldFixedList<E> extends AbstractOldSettingList<E>
	{
		OldFixedList(final FixedList<E> list)
		{
			super(list);
		}

		@Override
		public FixedList<E> parent()
		{
			return (FixedList<E>)super.parent();
		}

	}

}
