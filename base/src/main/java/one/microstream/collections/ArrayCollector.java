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

import static one.microstream.collections.XArrays.removeAllFromArray;

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
import one.microstream.collections.old.AbstractBridgeXList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.GenericListIterator;


/**
 * Full scale general purpose implementation of extended collection type {@link XList}.
 * <p>
 * This array-backed implementation is optimal for all needs of a list that do not require frequent structural
 * modification (insert or remove) of single elements before the end of the list.<br>
 * It is recommended to use this implementation as default list type until concrete performance deficiencies are
 * identified. If used properly (e.g. always ensure enough capacity, make use of batch procedures like
 * {@link #inputAll(long, Object...)}, {@link #removeRange(long, long)}, etc.), this implementation has equal or
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
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 *
 * @version 0.9, 2011-02-06
 */
public final class ArrayCollector<E> extends AbstractSimpleArrayCollection<E> implements XList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// internal marker object for marking to be removed buckets for batch removal and null ambiguity resolution
	private static final Object   MARKER = new Object() ;
	private static final Object[] DUMMY  = new Object[0];



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static String exceptionStringRange(final long size, final long startIndex, final long length)
	{
		return "Range [" + (length < 0
			? startIndex + length + 1 + ";" + startIndex
			: startIndex + ";" + (startIndex + length - 1)) + "] not in [0;" + (size - 1) + "]";
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private E[] data;      // the storage array containing the elements
	private int size;      // the current element count (logical size)



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public ArrayCollector()
	{
		super();
		this.data = (E[])DUMMY;
		this.size = 0;
	}

	public ArrayCollector(final ArrayCollector<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data;
		this.size = original.size;
	}

	@SafeVarargs
	public ArrayCollector(final E... elements) throws NullPointerException
	{
		super();
		this.data = elements;
		this.size = elements.length;
	}

	public ArrayCollector(final E[] elements, final int size)
	{
		super();
		this.setArray(elements, size);
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
		// (28.06.2011)FIXME: fix ArrayCollector internalCountingAddAll
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return elements.length;
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		if(length >= 0)
		{
			this.ensureFreeCapacity(length); // increaseCapacity
			System.arraycopy(elements, offset, this.data, this.size, length); // automatic bounds checks
			this.size += length;
			return length;
		}

		final int bound;
		if((bound = offset + length) < -1)
		{
			throw new ArrayIndexOutOfBoundsException(bound + 1);
		}
		this.ensureFreeCapacity(-length); // increaseCapacity
		final Object[] data = this.data;
		int size = this.size;
		for(int i = offset; i > bound; i--)
		{
			data[size++] = elements[i];
		}
		this.size = size;
		return -length;
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.internalCountingAddAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				XTypes.to_int(elements.size())
			);
		}
		final int oldSize = this.size;
		elements.iterate(this);
		return this.size - oldSize;
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return elements.length;
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		if(length >= 0)
		{
			this.ensureFreeCapacity(length); // increaseCapacity
			System.arraycopy(elements, offset, this.data, this.size, length); // automatic bounds checks
			this.size += length;
			return length;
		}

		final int bound;
		if((bound = offset + length) < -1)
		{
			throw new ArrayIndexOutOfBoundsException(bound + 1);
		}
		this.ensureFreeCapacity(-length); // increaseCapacity
		final Object[] data = this.data;
		int size = this.size;
		for(int i = offset; i > bound; i--)
		{
			data[size++] = elements[i];
		}
		this.size = size;
		return -length;
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.internalCountingAddAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				XTypes.to_int(elements.size())
			);
		}

		final int oldSize = this.size;
		elements.iterate(this);
		return this.size - oldSize;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters //
	////////////

	@SuppressWarnings("unchecked")
	public ArrayCollector<E> setArray(final E[] array, final int size)
	{
		if(array == null)
		{
			this.data = (E[])DUMMY;
			this.size = 0;
			return this;
		}

		if(size < 0 || size > array.length)
		{
			throw new ArrayIndexOutOfBoundsException(size);
		}
		this.data = array;
		this.size = size;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private int internalInsertArray(final int index, final Object[] elements, final int elementsSize)
	{
		// check for necessary capacity increase
		if(this.data.length - this.size < elementsSize)
		{
			throw new ArrayCapacityException((long)elementsSize + this.size);
		}

		// simply free up enough space at index and slide in new elements
		System.arraycopy(this.data, index, this.data, index + elementsSize, this.size - index);
		System.arraycopy(elements, 0, this.data, index, elementsSize);
		this.size += elementsSize;
		return elementsSize;
	}

	private int internalInsertArray(final int index, final Object[] elements, final int offset, final int length)
	{
		if(length < 0)
		{
			// check for necessary capacity increase
			if(this.data.length - this.size < -length)
			{
				throw new ArrayCapacityException((long)-length + this.size);
			}

			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index - length, this.size - index);
			XArrays.reverseArraycopy(elements, offset, elements , index         , -length          );
			this.size -= length;
			return -length;
		}

		// check for necessary capacity increase
		if(this.data.length - this.size < length)
		{
			throw new ArrayCapacityException((long)length + this.size);
		}

		// simply free up enough space at index and slide in new elements
		System.arraycopy(this.data, index, this.data, index + length, this.size - index);
		System.arraycopy(elements ,     0, this.data, index         , length           );
		this.size += length;
		return length;
	}

	private int internalInputArray(final int index, final Object[] elements, final int elementsSize)
	{
		// check for necessary capacity increase
		if(this.data.length - this.size < elementsSize)
		{
			throw new ArrayCapacityException((long)elementsSize + this.size);
		}

		// simply free up enough space at index and slide in new elements
		System.arraycopy(this.data, index, this.data, index + elementsSize, this.size - index);
		System.arraycopy(elements, 0, this.data, index, elementsSize);
		this.size += elementsSize;
		return elementsSize;
	}

	private int internalInputArray(final int index, final Object[] elements, final int offset, final int length)
	{
		if(length < 0)
		{
			// check for necessary capacity increase
			if(this.data.length - this.size < -length)
			{
				throw new ArrayCapacityException((long)-length + this.size);
			}

			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index - length, this.size - index);
			XArrays.reverseArraycopy(elements, offset, elements , index         , -length          );
			this.size -= length;
			return -length;
		}

		// check for necessary capacity increase
		if(this.data.length - this.size < length)
		{
			throw new ArrayCapacityException((long)length + this.size);
		}

		// simply free up enough space at index and slide in new elements
		System.arraycopy(this.data, index, this.data, index + length, this.size - index);
		System.arraycopy(elements ,     0, this.data, index         , length           );
		this.size += length;
		return length;
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
	public ArrayCollector<E> copy()
	{
		return new ArrayCollector<>(this);
	}

	@Override
	public XImmutableList<E> immure()
	{
		return ConstList.New(this);
	}

	@Override
	public ArrayCollector<E> toReversed()
	{
		final E[] rData = X.ArrayOfSameType(this.data, this.data.length);
		final E[] data = this.data;
		for(int i = this.size, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new ArrayCollector<>(rData, this.size);
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
		if(samples == null || !(samples instanceof ArrayCollector<?>) || XTypes.to_int(samples.size()) != this.size)
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
			((ArrayCollector<?>)samples).data,
			0,
			this.size, (Equalator<Object>)equalator
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
	public ArrayCollector<E> shiftTo(final long sourceIndex, final long targetIndex)
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
	public ArrayCollector<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		if(sourceIndex + length >= this.size)
		{
			throw new IndexExceededException(this.size, sourceIndex);
		}
		if(targetIndex + length >= this.size)
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
	public ArrayCollector<E> shiftBy(final long sourceIndex, final long distance)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance);
	}

	@Override
	public ArrayCollector<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance, length);
	}

	@Override
	public ArrayCollector<E> swap(final long indexA, final long indexB)
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
	public ArrayCollector<E> swap(final long indexA, final long indexB, final long length)
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
	public ArrayCollector<E> reverse()
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
	public final ArrayCollector<E> setAll(final long offset, final E... elements)
	{
		if(offset < 0 || offset + elements.length > this.size)
		{
			throw new IndexOutOfBoundsException(exceptionStringRange(this.size, offset, offset + elements.length - 1));
		}
		System.arraycopy(elements, 0, this.data, X.checkArrayRange(offset), elements.length);
		return this;
	}

	@Override
	public ArrayCollector<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		AbstractArrayStorage.set(this.data, this.size, X.checkArrayRange(offset), src, srcIndex, srcLength);
		return this;
	}

	@Override
	public ArrayCollector<E> set(
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
	public ArrayCollector<E> fill(final long offset, final long length, final E element)
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
	public ArrayCollector<E> sort(final Comparator<? super E> comparator)
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
	// adding //
	///////////

	@Override
	public long currentCapacity()
	{
		return this.data.length;
	}

	@Override
	public long maximumCapacity()
	{
		return this.data.length;
	}

	@Override
	public boolean isFull()
	{
		return this.size >= this.data.length;
	}

	@Override
	public long remainingCapacity()
	{
		return this.data.length - this.size;
	}

	@Override
	public long optimize()
	{
		return this.data.length;
	}

	@Override
	public ArrayCollector<E> ensureFreeCapacity(final long requiredFreeCapacity)
	{
		// as opposed to ensureCapacity(size + requiredFreeCapacity), this subtraction is overflow-safe
		if(this.data.length - this.size >= requiredFreeCapacity)
		{
			return this; // enough free capacity
		}
		throw new IndexBoundsException(this.data.length);
	}

	@Override
	public ArrayCollector<E> ensureCapacity(final long minCapacity)
	{
		if(minCapacity > this.data.length)
		{
			throw new IndexBoundsException(this.data.length);
		}
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	@Override
	public void accept(final E element)
	{
		this.add(element);
	}

	@Override
	public boolean add(final E element)
	{
		if(this.size >= this.data.length)
		{
			throw new IndexOutOfBoundsException("Reached maximum capacity");
		}
		this.data[this.size++] = element;
		return true;
	}

	@SafeVarargs
	@Override
	public final ArrayCollector<E> addAll(final E... elements)
	{
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return this;
	}

	@Override
	public ArrayCollector<E> addAll(final E[] elements, final int offset, final int length)
	{
		if(length == 0)
		{
			return this; // skip sanity checks
		}
		else if(length > 0)
		{
			this.ensureFreeCapacity(length); // increaseCapacity
			System.arraycopy(elements, offset, this.data, this.size, length); // automatic bounds checks
			this.size += length;
		}
		else
		{
			final int bound;
			if((bound = offset + length) < -1)
			{
				throw new ArrayIndexOutOfBoundsException(bound + 1);
			}
			this.ensureFreeCapacity(-length); // increaseCapacity
			final Object[] data = this.data;
			int size = this.size;
			for(int i = offset; i > bound; i--)
			{
				data[size++] = elements[i];
			}
			this.size = size;
		}
		return this;
	}

	@Override
	public ArrayCollector<E> addAll(final XGettingCollection<? extends E> elements)
	{
		return elements.iterate(this);
	}

	@Override
	public boolean nullAdd()
	{
		if(this.size >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length);
		}
		this.size++; // as overhang array elements are guaranteed to be null, the array setting can be spared
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	@Override
	public boolean put(final E element)
	{
		return this.add(element);
	}

	@SafeVarargs
	@Override
	public final ArrayCollector<E> putAll(final E... elements)
	{
		return this.addAll(elements);
	}

	@Override
	public ArrayCollector<E> putAll(final E[] elements, final int offset, final int length)
	{
		return this.addAll(elements, offset, length);
	}

	@Override
	public ArrayCollector<E> putAll(final XGettingCollection<? extends E> elements)
	{
		return elements.iterate(this);
	}

	@Override
	public boolean nullPut()
	{
		return this.nullAdd();
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	@Override
	public boolean prepend(final E element)
	{
		if(this.size >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length);
		}
		System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		this.data[0] = element;
		this.size++;
		return true;
	}

	@SafeVarargs
	@Override
	public final ArrayCollector<E> prependAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public ArrayCollector<E> prependAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public ArrayCollector<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public boolean nullPrepend()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	@Override
	public boolean preput(final E element)
	{
		if(this.size >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length);
		}
		System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		this.data[0] = element;
		this.size++;
		return true;
	}

	@SafeVarargs
	@Override
	public final ArrayCollector<E> preputAll(final E... elements)
	{
		if(this.data.length - elements.length < this.size)
		{
			throw new IndexBoundsException(this.data.length);
		}

		System.arraycopy(this.data, 0, this.data, elements.length, this.size); // ignore size == 0 corner case
		System.arraycopy(elements , 0, this.data, 0, elements.length);
		this.size += elements.length;
		return this;
	}

	@Override
	public ArrayCollector<E> preputAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public ArrayCollector<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public boolean nullPreput()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	@Override
	public boolean insert(final long index, final E element)
	{
		if(this.size >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length);
		}
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				this.data[this.size++] = element;
				return true;
			}
			throw new IndexBoundsException(this.size, index);
		}

		System.arraycopy(this.data, (int)index, this.data, (int)index + 1, this.size - (int)index);
		this.data[(int)index] = element;
		this.size++;

		return true;
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final E... elements) throws IndexOutOfBoundsException
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingAddAll(elements);
			}
			throw new IndexBoundsException(this.size, index);
		}

		return this.internalInsertArray((int)index, elements, elements.length);
	}

	@Override
	public long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingAddAll(elements, offset, length);
			}
			throw new IndexBoundsException(this.size, index);
		}

		return this.internalInsertArray((int)index, elements, offset, length);
	}

	@Override
	public long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingAddAll(elements);
			}
			throw new IndexBoundsException(this.size, index);
		}

		final Object[] elementsToAdd = elements instanceof AbstractSimpleArrayCollection<?>
			? ((AbstractSimpleArrayCollection<?>)elements).internalGetStorageArray()
			: elements.toArray() // anything else is probably not worth the hassle
		;

		return this.internalInsertArray((int)index, elementsToAdd, elementsToAdd.length);
	}

	@Override
	public boolean nullInsert(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	@Override
	public boolean input(final long index, final E element)
	{
		if(this.size >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length);
		}
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				this.data[this.size++] = element;
				return true;
			}
			throw new IndexBoundsException(this.size, index);
		}

		System.arraycopy(this.data, (int)index, this.data, (int)index + 1, this.size - (int)index);
		this.data[(int)index] = element;
		this.size++;
		return true;
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements) throws IndexOutOfBoundsException
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingPutAll(elements);
			}
			throw new IndexBoundsException(this.size, index);
		}

		return this.internalInputArray((int)index, elements, elements.length);
	}

	@Override
	public long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingPutAll(elements, offset, length);
			}
			throw new IndexBoundsException(this.size, index);
		}

		return this.internalInputArray((int)index, elements, offset, length);
	}

	@Override
	public long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingPutAll(elements);
			}
			throw new IndexBoundsException(this.size, index);
		}

		final Object[] elementsToAdd = elements instanceof AbstractSimpleArrayCollection<?>
			? ((AbstractSimpleArrayCollection<?>)elements).internalGetStorageArray()
			: elements.toArray() // anything else is probably not worth the hassle
		;

		return this.internalInputArray((int)index, elementsToAdd, elementsToAdd.length);
	}

	@Override
	public boolean nullInput(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ArrayCollector#nullInput()
	}



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	@SuppressWarnings("unchecked")
	@Override
	public void truncate()
	{
		this.data = (E[])DUMMY;
		this.size = 0;
	}

	@Override
	public long consolidate()
	{
		return 0; // nothing to do here
	}

	// removing - single //

	@SuppressWarnings("unchecked")
	@Override
	public E retrieve(final E element)
	{
		final E removedElement;
		if((removedElement = AbstractArrayStorage.retrieve(this.data, this.size, element, (E)MARKER)) != MARKER)
		{
			this.size--;
			return removedElement;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		final E e;
		if((e = AbstractArrayStorage.retrieve(this.data, this.size, predicate, (E)MARKER)) != MARKER)
		{
			this.size--;
			return e;
		}
		return null;
	}

	@Override
	public boolean removeOne(final E element)
	{
		if(AbstractArrayStorage.removeOne(this.data, this.size, element))
		{
			this.size--;
			return true;
		}
		return false;
	}

	// removing - multiple //

	@Override
	public long remove(final E element)
	{
		int removeCount;
		this.size -= removeCount = removeAllFromArray(this.data, 0, this.size, element);
		return removeCount;
	}

	@Override
	public long nullRemove()
	{
		final int removeCount;
		this.size -= removeCount = XArrays.removeAllFromArray(this.data, 0, this.size, null);
		return removeCount;
	}

	// reducing //

	@SuppressWarnings("unchecked")
	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.reduce(this.data, this.size, predicate, (E)MARKER);
		return removeCount;
	}

	// retaining //

	@SuppressWarnings("unchecked")
	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.retainAll(
			this.data, this.size, (XGettingCollection<E>)elements, (E)MARKER
		);
		return removeCount;
	}

	// processing //

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		this.size -= AbstractArrayStorage.process(this.data, this.size, procedure, (E)MARKER);
		return procedure;
	}

	// moving //

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		this.size -= AbstractArrayStorage.moveTo(this.data, this.size, target, predicate, (E)MARKER);
		return target;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		this.size -= AbstractArrayStorage.moveSelection(this.data, this.size, indices, target, (E)MARKER);
		return target;
	}

	// removing - multiple all //

	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		final int removed = removeAllFromArray(elements, this.data, 0, this.size);
		this.size -= removed;
		return removed;
	}

	// removing - duplicates //

	@SuppressWarnings("unchecked")
	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.removeDuplicates(
			this.data, this.size, equalator, (E)MARKER
		);
		return removeCount;
	}

	@Override
	public long removeDuplicates()
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.removeDuplicates(this.data, this.size, MARKER);
		return removeCount;
	}

	// removing - indexed //

	@SuppressWarnings("unchecked")
	@Override
	public E fetch()
	{
		final Object element = this.data[0];
		System.arraycopy(this.data, 1, this.data, 0, --this.size);
		this.data[this.size] = null;
		return (E)element;
	}

	@Override
	public E pop()
	{
		final E element = this.data[this.size - 1]; // get element and provoke index exception
		this.data[--this.size] = null; // update state
		return element;
	}

	@Override
	public E pinch()
	{
		if(this.size == 0)
		{
			return null;
		}
		final E element = this.data[0];
		System.arraycopy(this.data, 1, this.data, 0, --this.size);
		this.data[this.size] = null;
		return element;
	}

	@Override
	public E pick()
	{
		if(this.size == 0)
		{
			return null;
		}
		final E element = this.data[--this.size];
		this.data[this.size] = null;
		return element;
	}

	@Override
	public long removeSelection(final long[] indices)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.removeSelection(this.data, this.size, indices, MARKER);
		return removeCount;
	}

	@Override
	public ArrayCollector<E> removeRange(final long startIndex, final long length)
	{
		this.size -= AbstractArrayStorage.removeRange(
			this.data                         ,
			this.size                         ,
			X.checkArrayRange(startIndex),
			X.checkArrayRange(length)
		);
		return this;
	}

	@Override
	public ArrayCollector<E> retainRange(final long startIndex, final long length)
	{
		AbstractArrayStorage.retainRange(
			this.data                         ,
			this.size                         ,
			X.checkArrayRange(startIndex),
			X.checkArrayRange(length)
		);
		this.size = (int)length;

		return this;
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
		return new GenericListIterator<>(this);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new GenericListIterator<>(this);
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		validateIndex(this.size, index);
		return new GenericListIterator<>(this, (int)index);
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
	public SubList<E> range(final long fromIndex, final long toIndex)
	{
		// range check is done in constructor
		return new SubList<>(this, fromIndex, toIndex);
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

	@Override
	public void clear()
	{
		final Object[] data = this.data;
		for(int i = this.size; i-- > 0;)
		{
			data[i] = null;
		}
		this.size = 0;
	}

	@Override
	public E removeAt(final long index) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		final E oldValue = this.data[(int)index];

		final int moveCount;
		if((moveCount = this.size - 1 - (int)index) > 0)
		{
			System.arraycopy(this.data, (int)index + 1, this.data, (int)index, moveCount);
		}
		this.data[--this.size] = null;

		return oldValue;
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
		// (09.04.2012 TM)FIXME: what's List supposed to do here?
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
	public OldArrayCollector<E> old()
	{
		return new OldArrayCollector<>(this);
	}

	public static final class OldArrayCollector<E> extends AbstractBridgeXList<E>
	{
		OldArrayCollector(final ArrayCollector<E> list)
		{
			super(list);
		}

		@Override
		public ArrayCollector<E> parent()
		{
			return (ArrayCollector<E>)super.parent();
		}

	}

}
