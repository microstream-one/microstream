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
import one.microstream.collections.types.XList;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.IsCustomEqual;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.GenericListIterator;


/**
 * Full scale general purpose implementation of extended collection type {@link XList}.
 * <p>
 * Additional to the {@link BulkList}, this implementation needs an {@link Equalator}
 * to define equality between elements.
 * <p>
 * This array-backed implementation is optimal for all needs of a list that do not require frequent structural
 * modification (insert or remove) of single elements before the end of the list.<br>
 * It is recommended to use this implementation as default list type until concrete performance deficiencies are
 * identified. If used properly (e.g. always ensure enough capacity, make use of batch procedures like
 * {@link #inputAll(long, Object...)}, {@link #removeRange(long, long)}, etc.), this implementation has equal or
 * massively superior performance to linked-list implementation in most cases.
 * <p>
 * This implementation is <b>not</b> synchronized and thus should only be used by a
 * single thread or in a thread-safe manner (i.e. read-only as soon as multiple threads access it).<br>
 * See {@link SynchList} wrapper class to use a list in a synchronized manner.
 * <p>
 * Note that this List implementation does <b>not</b> keep track of modification count as JDK's collection implementations do
 * (and thus never throws a {@link ConcurrentModificationException}), for two reasons:<br>
 * 1.) It is already explicitly declared thread-unsafe and for single-thread (or thread-safe)
 * use only.<br>
 * 2.) The common modCount-concurrency exception behavior ("failfast") has inconsistent behavior by
 * throwing {@link ConcurrentModificationException} even in single thread use, i.e. when iterating over a collection
 * and removing more than one element of it without using the iterator's method.
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 *
 * @param <E> type of contained elements
 * 
 */
public final class EqBulkList<E> extends AbstractSimpleArrayCollection<E> implements XList<E>, Composition
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

	E[]   data; // the storage array containing the elements
	int   size; // the current element count (logical size)
	final Equalator<? super E> equalator;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Default constructor instantiating an empty instance with default (minimum) capacity.
	 * 
	 * @param equalator the equality logic
	 */
	public EqBulkList(final Equalator<? super E> equalator)
	{
		super();
		this.size = 0;
		this.data = newArray(1);
		this.equalator = equalator;
	}

	/**
	 * Initial capacity constructor instantiating an empty instance with a given initial capacity.
	 * <p>
	 * The actual initial capacity is the highest of the following three values:
	 * <ul>
	 * <li>{@link Integer} MAX_VALUE, if the given initial capacity is greater than 2^30.</li>
	 * <li>The lowest power of two value that is equal to or greater than the given initial capacity.</li>
	 * <li>The default (minimum) capacity.</li>
	 * </ul>
	 * 
	 * @param equalator the equality logic
	 * @param initialCapacity the desired custom initial capacity.
	 */
	public EqBulkList(final Equalator<? super E> equalator, final int initialCapacity)
	{
		super();
		this.size = 0;
		this.data = newArray(XMath.pow2BoundMaxed(initialCapacity));
		this.equalator = equalator;
	}

	/**
	 * Copy constructor that instantiates a new instance with a copy of the passed original instance's data and same
	 * size.
	 *
	 * @param original the instance to be copied.
	 * @throws NullPointerException if {@code null} was passed.
	 *
	 * @see #copy()
	 */
	public EqBulkList(final EqBulkList<E> original) throws NullPointerException
	{
		super();
		this.size = original.size;
		this.data = original.data.clone();
		this.equalator = original.equalator;
	}

	/**
	 * Convenience initial data constructor, instantiating a new instance containing all elements of the passed
	 * array. The element size of the new instance will be equal to the passed array's length.
	 * <p>
	 * Note that providing no element at all in the VarArgs parameter will automatically cause the
	 * default constructor {@link #EqBulkList(Equalator)} to be used instead. Explicitely providing an {@code null} array
	 * reference will cause a {@link NullPointerException}.
	 * 
	 * @param equalator the equality logic
	 * @param elements the initial elements for the new instance.
	 * @throws NullPointerException if an explicit {@code null} array reference was passed.
	 *
	 * @see #EqBulkList(Equalator)
	 */
	@SafeVarargs
	public EqBulkList(final Equalator<? super E> equalator, final E... elements) throws NullPointerException
	{
		super();
		System.arraycopy(
			elements,
			0,
			this.data = newArray(XMath.pow2BoundMaxed(this.size = elements.length)),
			0,
			this.size
		);
		this.equalator = equalator;
	}

	/**
	 * Detailed initializing constructor allowing to specify initial capacity and a custom array range of initial data.
	 * <p>
	 * The actual initial capacity will be calculated based on the higher of the two values {@code initialCapacity}
	 * and {@code srcLength} as described in {@link #EqBulkList(Equalator, int)}.
	 * <p>
	 * The specified initial elements array range is copied via {@link System#arraycopy(Object, int, Object, int, int)}.
	 * 
	 * @param equalator the equality logic
	 * @param initialCapacity the desired initial capacity for the new instance.
	 * @param src the source array containg the desired range of initial elements.
	 * @param srcStart the start index of the desired range of initial elements in the source array.
	 * @param srcLength the length of the desired range of initial elements in the source array.
	 */
	public EqBulkList(final Equalator<? super E> equalator, final int initialCapacity, final E[] src, final int srcStart, final int srcLength)
	{
		super();
		System.arraycopy(
			src,
			srcStart,
			this.data = newArray(XMath.pow2BoundMaxed(initialCapacity >= srcLength ? initialCapacity : srcLength)),
			0,
			this.size = srcLength
		);
		this.equalator = equalator;
	}

	/**
	 * Internal constructor to directly supply the storage array instance and size.
	 * <p>
	 * The passed storage array must comply to the power of two aligned size rules as specified in
	 * {@link #BulkList(int)} and the size must be consistent to the storage array.<br>
	 * Calling this constructor without complying to these rules will result in a broken instance.
	 * <p>
	 * It is recommended to NOT use this constructor outside collections-framework-internal implementations.
	 * 
	 * @param equalator the equality logic
	 * @param storageArray the array to be used as the storage for the new instance.
	 * @param size the element size of the new instance.
	 */
	EqBulkList(final Equalator<? super E> equalator, final E[] storageArray, final int size)
	{
		super();
		this.size = size;
		this.data = storageArray;
		this.equalator = equalator;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/* this method is highly optimized for performance, yielding up to around 300% the speed of
	 * java.util.ArrayList.add() when adding elements to an already big enough storage.
	 * Moving the storage increase part to a private increaseStorage() would make it faster when
	 * regular increasing is needed, but puzzlingly then the already-big-enough performance
	 * advantage drops to around 110% faster instead of 300% faster (even though the single not called
	 * increase method should oblige to HotSpot compiling. Seems there is a bug or at least
	 * some heavy confusion going on there.
	 * As a consequence, storage increasing has NOT been moved to a private method, thus maintaining
	 * the huge already-big-enough performance advantage, but making it slower in regular-growth-cases
	 * (also very strange).
	 * Maybe one of the two HotSpot compiling problems improves in the future, so that both cases
	 * of advanced performance are reachable by optimization.
	 */
	void internalAdd(final E element)
	{
		/* notes on algorithm:
		 * - " >= " is significantly faster than " == ", probably due to simple sign bit checking?
		 * - string for IOOB Exception is omitted intentionally for performance, as this exception is rather academic
		 * - assignment inlining increases normal case performance by >10% ^^, maybe due to shorter bytecode
		 * - float conversion is automatically capped at MAX_VALUE, whereas "<<= 1" can only reach 2^30 and then crash
		 * - "<<= 1" would speed up normal case by ~5%, but would limit list size to 2^30 instead of MAX_VALUE
		 * - " + +this.lastIndex" would be ~5% faster than "this.size+ + ", but would complicate every use of list's size
		 */
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
		}
		this.data[this.size++] = element;
	}

	private int internalInputArray(final int index, final Object[] elements, final int elementsSize)
	{
		// check for simple case without a required capacity increase
		if(this.data.length - this.size >= elementsSize)
		{
			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index + elementsSize, elementsSize);
			System.arraycopy(elements ,     0, this.data, index               , elementsSize);
			this.size += elementsSize;
			return elementsSize;
		}

		// overflow-safe check for unreachable capacity
		if(Integer.MAX_VALUE - this.size < elementsSize)
		{
			// unreachable capacity
			throw new ArrayCapacityException((long)elementsSize + this.size);
		}

		// required and reachable capacity increase
		final int newSize = this.size + elementsSize;
		int newCapacity;
		if(XMath.isGreaterThanHighestPowerOf2(newSize))
		{
			// JVM technical limit
			newCapacity = Integer.MAX_VALUE;
		}
		else
		{
			newCapacity = this.data.length;
			while(newCapacity < newSize)
			{
				newCapacity <<= 1;
			}
		}

		/* copy elements in two steps:
		 *        old array             new array
		 * 1.) [    0; index] -> [        0;    index]
		 * 2.) [index;  size] -> [index+gap; size+gap]
		 *
		 * So it looks like this:
		 * --- - 1.)----       ----2.)----
		 * |||||||||||_______|||||||||||
		 * where this ^^^^^^^ is exactly enough space (the gap) for inserting "elements"
		 *
		 * this way, all elements are only copied once
		 */
		final E[] data;
		System.arraycopy(this.data,     0, data = newArray(newCapacity), 0, index);
		System.arraycopy(this.data, index, data, index + elementsSize, elementsSize);
		System.arraycopy(elements ,     0, this.data = data,    index, elementsSize);
		this.size = newSize;
		return elementsSize;
	}

	private int internalInputArray(final int index, final E[] elements, final int offset, final int length)
	{
		if(length < 0)
		{
			return this.internalReverseInputArray(index, elements, offset, -length);
		}

		// check for simple case without a required capacity increase
		if(this.data.length - this.size >= length)
		{
			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index + length, length);
			System.arraycopy(elements, offset, this.data, index         , length);
			this.size += length;
			return length;
		}

		// overflow-safe check for unreachable capacity
		if(Integer.MAX_VALUE - this.size < length)
		{
			// unreachable capacity
			throw new ArrayCapacityException((long)length + this.size);
		}

		// required and reachable capacity increase
		final int newSize = this.size + length;
		int newCapacity;
		if(XMath.isGreaterThanHighestPowerOf2(newSize))
		{
			// JVM technical limit
			newCapacity = Integer.MAX_VALUE;
		}
		else
		{
			newCapacity = this.data.length;
			while(newCapacity < newSize)
			{
				newCapacity <<= 1;
			}
		}

		/* copy elements in two steps:
		 *        old array             new array
		 * 1.) [    0; index] -> [        0;    index]
		 * 2.) [index;  size] -> [index+gap; size+gap]
		 *
		 * So it looks like this:
		 * --- - 1.)----       ----2.)----
		 * |||||||||||_______|||||||||||
		 * where this ^^^^^^^ is exactly enough space (the gap) for inserting "elements"
		 *
		 * this way, all elements are only copied once
		 */
		final E[] data;
		System.arraycopy(this.data,     0, data = newArray(newCapacity), 0, index);
		System.arraycopy(this.data, index, data, index + length, length);
		System.arraycopy(elements, offset, this.data = data,    index, length);
		this.size = newSize;
		return length;
	}

	private int internalReverseInputArray(final int index, final E[] elements, final int offset, final int length)
	{
		// check for simple case without a required capacity increase
		if(this.data.length - this.size >= length)
		{
			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index + length, length);
			XArrays.reverseArraycopy(elements, offset, this.data, index, length);
			this.size += length;
			return length;
		}

		// overflow-safe check for unreachable capacity
		if(Integer.MAX_VALUE - this.size < length)
		{
			// unreachable capacity
			throw new ArrayCapacityException((long)length + this.size);
		}

		// required and reachable capacity increase
		final int newSize = this.size + length;
		int newCapacity;
		if(XMath.isGreaterThanHighestPowerOf2(newSize))
		{
			// JVM technical limit
			newCapacity = Integer.MAX_VALUE;
		}
		else
		{
			newCapacity = this.data.length;
			while(newCapacity < newSize)
			{
				newCapacity <<= 1;
			}
		}

		/* copy elements in two steps:
		 *        old array             new array
		 * 1.) [    0; index] -> [        0;    index]
		 * 2.) [index;  size] -> [index+gap; size+gap]
		 *
		 * So it looks like this:
		 * --- - 1.)----       ----2.)----
		 * |||||||||||_______|||||||||||
		 * where this ^^^^^^^ is exactly enough space (the gap) for inserting "elements"
		 *
		 * this way, all elements are only copied once
		 */
		final Object[] data;
		System.arraycopy(this.data,     0, data = newArray(newCapacity), 0, index);
		System.arraycopy(this.data, index, data, index + length, length);
		XArrays.reverseArraycopy(elements, 0, this.data, index, -length);
		this.size = newSize;
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

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
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
			return this.internalCountingAddAll(AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, XTypes.to_int(elements.size()));
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
			return this.internalCountingAddAll(AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, XTypes.to_int(elements.size()));
		}

		final int oldSize = this.size;
		elements.iterate(this);
		return this.size - oldSize;
	}

	@Override
	public Equalator<? super E> equality()
	{
		return this.equalator;
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public EqBulkList<E> copy()
	{
		return new EqBulkList<>(this);
	}

	@Override
	public EqConstList<E> immure()
	{
		return new EqConstList<>(this.equalator, this);
	}

	@Override
	public EqBulkList<E> toReversed()
	{
		final E[] data, reversedData = newArray((data = this.data).length);
		for(int i = this.size, r = 0; i-- > 0;)
		{
			reversedData[r++] = data[i];
		}
		return new EqBulkList<>(this.equalator, reversedData, this.size);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array;
		System.arraycopy(this.data, 0, array = X.Array(type, this.size), 0, this.size);
		return array;
	}

	// executing //

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate(this.data, this.size, procedure);
		return procedure;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		AbstractArrayStorage.iterate(this.data, this.size, procedure);
		return procedure;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractArrayStorage#join(Object[], int, BiConsumer, Object)
	 */
	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join(this.data, this.size, joiner, aggregate);
		return aggregate;
	}

	// count querying //

	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.forwardConditionalCount(this.data, 0, this.size, new IsCustomEqual<>(this.equalator, element));
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
		return AbstractArrayStorage.forwardConditionalIndexOf(this.data, 0, this.size, new IsCustomEqual<>(this.equalator, element));
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalIndexOf(this.data, 0, this.size, predicate);
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return AbstractArrayStorage.reverseConditionalIndexOf(this.data, this.size - 1, 0, new IsCustomEqual<>(this.equalator, element));
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
		return AbstractArrayStorage.forwardQueryElement(this.data, 0, this.size, new IsCustomEqual<>(this.equalator, sample), null);
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
		return AbstractArrayStorage.forwardContains(this.data, 0, this.size, new IsCustomEqual<>(this.equalator, element));
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll(this.data, this.size, elements, this.equalator);
	}

	// boolean querying - equality //

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof EqBulkList<?>) || XTypes.to_int(samples.size()) != this.size)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}

		// equivalent to equalsContent()
		return XArrays.equals(this.data, 0, ((EqBulkList<E>)samples).data, 0, this.size, equalator);
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
	public EqBulkList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		if(sourceIndex >= this.size)
		{
			throw new IndexBoundsException(this.size, sourceIndex);
		}
		if(targetIndex >= this.size)
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

		final E shiftling = this.data[(int)sourceIndex];
		if(sourceIndex < targetIndex)
		{
			System.arraycopy(this.data, (int)sourceIndex + 1, this.data, (int)sourceIndex, X.checkArrayRange(targetIndex - sourceIndex));
		}
		else
		{
			System.arraycopy(this.data, (int)targetIndex, this.data, (int)targetIndex + 1, X.checkArrayRange(sourceIndex - targetIndex));
		}

		this.data[(int)targetIndex] = shiftling;
		return this;
	}

	@Override
	public EqBulkList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
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
	public EqBulkList<E> shiftBy(final long sourceIndex, final long distance)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance);
	}

	@Override
	public EqBulkList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance, length);
	}

	@Override
	public EqBulkList<E> swap(final long indexA, final long indexB)
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
		final E e = this.data[(int)indexA];
		this.data[(int)indexA] = this.data[(int)indexB];
		this.data[(int)indexB] = e;
		return this;
	}

	@Override
	public EqBulkList<E> swap(final long indexA, final long indexB, final long length)
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
	public EqBulkList<E> reverse()
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
	public final EqBulkList<E> setAll(final long offset, final E... elements)
	{
		if(offset < 0 || offset + elements.length > this.size)
		{
			throw new IndexOutOfBoundsException(exceptionStringRange(this.size, offset, offset + elements.length - 1));
		}
		System.arraycopy(elements, 0, this.data, X.checkArrayRange(offset), elements.length);
		return this;
	}

	@Override
	public EqBulkList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		AbstractArrayStorage.set(this.data, this.size, X.checkArrayRange(offset), src, srcIndex, srcLength);
		return this;
	}

	@Override
	public EqBulkList<E> set(
		final long                          offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                          elementsOffset,
		final long                          elementsLength
	)
	{
		AbstractArrayStorage.set(
			this.data,
			this.size,
			X.checkArrayRange(offset),
			elements,
			elementsOffset,
			elementsLength
		);
		return this;
	}

	@Override
	public EqBulkList<E> fill(final long offset, final long length, final E element)
	{
		AbstractArrayStorage.fill(this.data, this.size, X.checkArrayRange(offset), X.checkArrayRange(length), element);
		return this;
	}

	// sorting //

	@Override
	public EqBulkList<E> sort(final Comparator<? super E> comparator)
	{
		XSort.mergesort(this.data, 0, this.size, comparator);
		return this;
	}

	// replacing - single //

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the element is equal is defined by the specified {@link Equalator}.
	 */
	@Override
	public boolean replaceOne(final E element, final E replacement)
	{
		return AbstractArrayStorage.replaceOne(this.data, this.size, element, replacement, this.equalator);
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
		return AbstractArrayStorage.replace(this.data, this.size, element, replacement, this.equalator);
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
		return AbstractArrayStorage.replaceAll(this.data, this.size, elements, replacement, this.equalator, AbstractArrayCollection.<E>marker());
	}

	// replacing - mapped //

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
	// capacity methods //
	/////////////////////

	@Override
	public long currentCapacity()
	{
		return this.data.length;
	}

	@Override
	public long maximumCapacity()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isFull()
	{
		return this.size >= Integer.MAX_VALUE;
	}

	@Override
	public long remainingCapacity()
	{
		return Integer.MAX_VALUE - this.size;
	}

	@Override
	public long optimize()
	{
		final int requiredCapacity;
		if((requiredCapacity = XMath.pow2BoundMaxed(this.size)) != this.data.length)
		{
			System.arraycopy(this.data, 0, this.data = newArray(requiredCapacity), 0, this.size);
		}
		return this.data.length;
	}

	@Override
	public EqBulkList<E> ensureFreeCapacity(final long requiredFreeCapacity)
	{
		// as opposed to ensureCapacity(size + requiredFreeCapacity), this subtraction is overflow-safe
		if(this.data.length - this.size >= requiredFreeCapacity)
		{
			return this; // already enough free capacity
		}

		// calculate new capacity
		final int newSize = XTypes.to_int(this.size + requiredFreeCapacity);
		int newCapacity;
		if(XMath.isGreaterThanHighestPowerOf2(newSize))
		{
			// JVM technical limit
			newCapacity = Integer.MAX_VALUE;
		}
		else
		{
			newCapacity = this.data.length;
			while(newCapacity < newSize)
			{
				newCapacity <<= 1;
			}
		}

		// rebuild storage
		final E[] data = newArray(newCapacity);
		System.arraycopy(this.data, 0, data, 0, this.size);
		this.data = data;
		return this;
	}

	@Override
	public EqBulkList<E> ensureCapacity(final long minCapacity)
	{
		if(minCapacity > this.data.length)
		{
			this.data = newArray(pow2BoundMaxed(minCapacity), this.data, this.size);
		}
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	@Override
	public void accept(final E element)
	{
		this.internalAdd(element); // gets inlined, tests showed no performance difference.
	}

	/**
	 * Adds the passed element.
	 * Return value indicates new entry and is always true.
	 */
	@Override
	public boolean add(final E element)
	{
		this.internalAdd(element); // gets inlined, tests showed no performance difference.
		return true;
	}

	@SafeVarargs
	@Override
	public final EqBulkList<E> addAll(final E... elements)
	{
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return this;
	}

	@Override
	public EqBulkList<E> addAll(final E[] elements, final int offset, final int length)
	{
		if(length >= 0)
		{
			this.ensureFreeCapacity(length); // increaseCapacity
			System.arraycopy(elements, offset, this.data, this.size, length); // automatic bounds checks
			this.size += length;
		}
		else
		{
			final int bound;
			if((bound = length + length) < -1)
			{
				throw new ArrayIndexOutOfBoundsException(bound + 1);
			}
			this.ensureFreeCapacity(-length); // increaseCapacity
			final Object[] data = this.data;
			int size = this.size;
			for(int i = length; i > bound; i--)
			{
				data[size++] = elements[i];
			}
			this.size = size;
		}
		return this;
	}

	@Override
	public EqBulkList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.addAll(AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, XTypes.to_int(elements.size()));
		}
		return elements.iterate(this);
	}

	@Override
	public boolean nullAdd()
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
		}
		this.size++; // as overhang array elements are guaranteed to be null, the array setting can be spared
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	@Override
	public boolean nullPut()
	{
		return this.nullAdd();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link EqBulkList#add(Object)}.
	 */
	@Override
	public boolean put(final E element)
	{
		this.internalAdd(element); // gets inlined, tests showed no performance difference.
		return true;
	}

	@SafeVarargs
	@Override
	public final EqBulkList<E> putAll(final E... elements)
	{
		return this.addAll(elements);
	}

	@Override
	public EqBulkList<E> putAll(final E[] elements, final int offset, final int length)
	{
		return this.addAll(elements, offset, length);
	}

	@Override
	public EqBulkList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		return elements.iterate(this);
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	@Override
	public boolean prepend(final E element)
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 1, this.size);
		}
		else
		{
			System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		}
		this.data[0] = element;
		this.size++;
		return true;
	}

	@SafeVarargs
	@Override
	public final EqBulkList<E> prependAll(final E... elements)
	{
		this.internalInputArray(0, elements, elements.length);
		return this;
	}

	@Override
	public EqBulkList<E> prependAll(final E[] elements, final int offset, final int length)
	{
		this.internalInputArray(0, elements, offset, length);
		return this;
	}

	@Override
	public EqBulkList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		this.insertAll(0, elements);
		return this;
	}

	@Override
	public boolean nullPrepend()
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
		}
		else
		{
			System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		}
		this.data[0] = null;
		this.size++;
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	@Override
	public boolean preput(final E element)
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
		}
		else
		{
			System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		}
		this.data[0] = element;
		this.size++;
		return true;
	}

	@SafeVarargs
	@Override
	public final EqBulkList<E> preputAll(final E... elements)
	{
		this.internalInputArray(0, elements, elements.length);
		return this;
	}

	@Override
	public EqBulkList<E> preputAll(final E[] elements, final int offset, final int length)
	{
		this.internalInputArray(0, elements, offset, length);
		return this;
	}

	@Override
	public EqBulkList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		this.inputAll(0, elements);
		return this;
	}

	@Override
	public boolean nullPreput()
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
		}
		else
		{
			System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		}
		this.data[0] = null;
		this.size++;
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	@Override
	public boolean insert(final long index, final E element)
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new ArrayCapacityException();
		}
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				if(this.size >= this.data.length)
				{
					if(this.size >= Integer.MAX_VALUE)
					{
						throw new IndexOutOfBoundsException();
					}
					System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
				}
				this.data[this.size++] = element;
				return true;
			}
			throw new IndexBoundsException(this.size, index);
		}

		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			final Object[] oldData = this.data;
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, (int)index);
			System.arraycopy(oldData, (int)index, this.data, (int)index + 1, this.size - (int)index);
		}
		else
		{
			System.arraycopy(this.data, (int)index, this.data, (int)index + 1, this.size - (int)index);
		}

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

		return this.internalInputArray((int)index, elements, elements.length);
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

		return this.internalInputArray((int)index, elements, offset, length);
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
			? AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements)
			: elements.toArray() // anything else is probably not worth the hassle
		;

		return this.internalInputArray((int)index, elementsToAdd, elementsToAdd.length);
	}

	@Override
	public boolean nullInsert(final long index)
	{
		return this.insert(0, (E)null);
	}



	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	@Override
	public boolean input(final long index, final E element)
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new ArrayCapacityException();
		}
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				if(this.size >= this.data.length)
				{
					if(this.size >= Integer.MAX_VALUE)
					{
						throw new IndexOutOfBoundsException();
					}
					System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, this.size);
				}
				this.data[this.size++] = element;
				return true;
			}
			throw new IndexBoundsException(this.size, index);
		}

		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			final Object[] oldData = this.data;
			System.arraycopy(this.data, 0, this.data = newArray((int)(this.data.length * 2.0f)), 0, (int)index);
			System.arraycopy(oldData, (int)index, this.data, (int)index + 1, this.size - (int)index);
		}
		else
		{
			System.arraycopy(this.data, (int)index, this.data, (int)index + 1, this.size - (int)index);
		}
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
			? AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements)
			: elements.toArray() // anything else is probably not worth the hassle
		;
		return this.internalInputArray((int)index, elementsToAdd, elementsToAdd.length);
	}

	@Override
	public boolean nullInput(final long index)
	{
		return this.input(0, (E)null);
	}



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	@Override
	public void truncate()
	{
		this.size = 0;
		this.data = newArray(1);
	}

	@Override
	public long consolidate()
	{
		return 0; // nothing to do here
	}

	// removing - single //

	@Override
	public boolean removeOne(final E element)
	{
		if(AbstractArrayStorage.removeOne(this.data, this.size, element, this.equalator))
		{
			this.size--;
			return true;
		}
		return false;
	}

	@Override
	public E retrieve(final E element)
	{
		final E removedElement;
		if((removedElement = AbstractArrayStorage.retrieve(this.data, this.size, element, this.equalator, AbstractArrayCollection.<E>marker())) != AbstractArrayCollection.<E>marker())
		{
			this.size--;
			return removedElement;
		}
		return null;
	}

	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		final E e;
		if((e = AbstractArrayStorage.retrieve(this.data, this.size, predicate, AbstractArrayCollection.<E>marker())) != AbstractArrayCollection.<E>marker())
		{
			this.size--;
			return e;
		}
		return null;
	}

	// removing - multiple //

	@Override
	public long remove(final E element)
	{
		final int removeCount;
		this.size -= removeCount = removeAllFromArray(
			this.data, 0, this.size, element, this.equalator
		);
		return removeCount;
	}

	@Override
	public long nullRemove()
	{
		final int removeCount;
		this.size -= removeCount = XArrays.removeAllFromArray(this.data, 0, this.size, null);
		return removeCount;
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

	// reducing //

	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.reduce(this.data, this.size, predicate, AbstractArrayCollection.<E>marker());
		return removeCount;
	}

	// retaining //

	@SuppressWarnings("unchecked")
	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.retainAll(
			this.data, this.size, (XGettingCollection<E>)elements, this.equalator, AbstractArrayCollection.<E>marker()
		);
		return removeCount;
	}

	// processing //

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		this.size -= AbstractArrayStorage.process(this.data, this.size, procedure, AbstractArrayCollection.<E>marker());
		return procedure;
	}

	// moving //

	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		this.size -= AbstractArrayStorage.moveTo(this.data, this.size, target, predicate, AbstractArrayCollection.<E>marker());
		return target;
	}

	@Override
	public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		this.size -= AbstractArrayStorage.moveSelection(this.data, this.size, indices, target, AbstractArrayCollection.<E>marker());
		return target;
	}

	// removing - multiple all //

	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		/* it is nasty of course to cast from <? extends E> to <E>, but <?> like in JDK is no solution either
		 * and causes even more trouble.
		 * This is typesafe, as the algorithm uses identity comparison ( == ) which is applicable to
		 * incompatible types.
		 */
		final int removed;
		this.size -= removed = removeAllFromArray(
			this.data, 0, this.size, elements, this.equalator
		);
		return removed;
	}

	// removing - duplicates //

	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.removeDuplicates(
			this.data, this.size, equalator, AbstractArrayCollection.<E>marker()
		);
		return removeCount;
	}

	@Override
	public long removeDuplicates()
	{
		final int removeCount;
		this.size -= removeCount = AbstractArrayStorage.removeDuplicates(
			this.data, this.size, this.equalator, AbstractArrayCollection.<E>marker()
		);
		return removeCount;
	}

	// removing - indexed //

	@Override
	public E fetch()
	{
		final E element = this.data[0];
		System.arraycopy(this.data, 1, this.data, 0, --this.size);
		this.data[this.size] = null;
		return element;
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
		this.size -= removeCount = AbstractArrayStorage.removeSelection(this.data, this.size, indices, AbstractArrayCollection.<E>marker());
		return removeCount;
	}

	@Override
	public EqBulkList<E> removeRange(final long startIndex, final long length)
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
	public EqBulkList<E> retainRange(final long startIndex, final long length)
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
	public boolean set(final long index, final E element) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
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
		final Object[] array = newArray(this.size);
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
	public OldBulkList<E> old()
	{
		return new OldBulkList<>(this);
	}

	public static final class OldBulkList<E> extends AbstractBridgeXList<E>
	{
		OldBulkList(final EqBulkList<E> list)
		{
			super(list);
		}

		@Override
		public EqBulkList<E> parent()
		{
			return (EqBulkList<E>)super.parent();
		}

	}



	public static class Creator<E> implements XList.Creator<E>
	{
		private final int initialCapacity;
		private final Equalator<? super E> equalator;

		public Creator(final Equalator<? super E> equalator, final int initialCapacity)
		{
			super();
			this.initialCapacity = XMath.pow2BoundMaxed(initialCapacity);
			this.equalator = equalator;
		}

		public int getInitialCapacity()
		{
			return this.initialCapacity;
		}

		public Equalator<? super E> getEqualator()
		{
			return this.equalator;
		}

		@Override
		public EqBulkList<E> newInstance()
		{
			return new EqBulkList<>(this.equalator, AbstractArrayCollection.<E>newArray(this.initialCapacity), this.initialCapacity);
		}

	}

}
