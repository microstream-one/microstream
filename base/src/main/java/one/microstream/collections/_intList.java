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

import one.microstream.collections.interfaces._intCollecting;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional._intFunction;
import one.microstream.functional._intIndexProcedure;
import one.microstream.functional._intPredicate;
import one.microstream.functional._intProcedure;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;


/**
 *
 * 
 * @version 0.95, 2011 - 12 - 12
 */
public final class _intList implements _intCollecting, Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static String exceptionStringRange(final int size, final int startIndex, final int length)
	{
		return "Range [" + (length < 0
			? startIndex + length + 1 + ";" + startIndex
			: startIndex + ";" + (startIndex + length - 1)) + "] not in [0;" + (size - 1) + "]"
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int[] data; // the storage array containing the elements
	private int   size; // the current element count (logical size)



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Default constructor instantiating an empty instance with default (minimum) capacity.
	 */
	public _intList()
	{
		super();
		this.size = 0;
		this.data = new int[1];
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
	 * @param initialCapacity the desired custom initial capacity.
	 */
	public _intList(final int initialCapacity)
	{
		super();
		this.size = 0;
		this.data = new int[XMath.pow2BoundMaxed(initialCapacity)];
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
	public _intList(final _intList original) throws NullPointerException
	{
		super();
		this.size = original.size;
		this.data = original.data.clone();
	}

	/**
	 * Convenience initial data constructor, instantiating a new instance containing all elements of the passed
	 * array. The element size of the new instance will be equal to the passed array's length.
	 * <p>
	 * Note that providing no element at all in the VarArgs parameter will automatically cause the
	 * default constructor {@link #_intList()} to be used instead. Explicitely providing an {@code null} array
	 * reference will cause a {@link NullPointerException}.
	 *
	 * @param elements the initial elements for the new instance.
	 * @throws NullPointerException if an explicit {@code null} array reference was passed.
	 *
	 * @see #_intList()
	 */
	public _intList(final int... elements) throws NullPointerException
	{
		super();
		System.arraycopy(
			elements,
			0,
			this.data = new int[XMath.pow2BoundMaxed(this.size = elements.length)],
			0,
			this.size
		);
	}

	/**
	 * Detailed initializing constructor allowing to specify initial capacity and a custom array range of initial data.
	 * <p>
	 * The actual initial capacity will be calculated based on the higher of the two values {@code initialCapacity}
	 * and {@code srcLength} as described in {@link #_intList(int)}.
	 * <p>
	 * The specified initial elements array range is copied via {@link System#arraycopy}.
	 *
	 * @param initialCapacity the desired initial capacity for the new instance.
	 * @param src the source array containg the desired range of initial elements.
	 * @param srcStart the start index of the desired range of initial elements in the source array.
	 * @param srcLength the length of the desired range of initial elements in the source array.
	 */
	public _intList(final int initialCapacity, final int[] src, final int srcStart, final int srcLength)
	{
		super();
		System.arraycopy(
			src,
			srcStart,
			this.data = new int[XMath.pow2BoundMaxed(initialCapacity >= srcLength ? initialCapacity : srcLength)],
			0,
			this.size = srcLength
		);
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
	 * @param storageArray the array to be used as the storage for the new instance.
	 * @param size the element size of the new instance.
	 */
	_intList(final int[] storageArray, final int size)
	{
		super();
		this.size = size;
		this.data = storageArray;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/* this method is highly optimized for performance, yielding up to around 300% the speed of
	 * java.util.ArrayList.add() when adding elements to an already big enough storage.
	 * Moving the storage increase part to a private increaseStorage() would make it faster when
	 * regular increasing is needed, but puzzlingly then the alreay-big-enough performance
	 * advantage drops to around 110% faster instead of 300% faster (even though the single not called
	 * increase method should be removed by HotSpot compiling. Seems there is a bug or at least
	 * some heavy confusion going on there.
	 * As a consequence, storage increasing has NOT been moved to a private method, thus maintaining
	 * the huge alreay-big-enough performance advantage, but making it slower in regular-growth-cases
	 * (also very strange).
	 * Maybe one of the two HotSpot compiling problems improves in the future, so that both cases
	 * of advanced performance are reachable by optimization.
	 */
	void internalAdd(final int element)
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
				throw new ArrayCapacityException();
			}
			System.arraycopy(this.data, 0, this.data = new int[(int)(this.data.length * 2.0f)], 0, this.size);
		}
		this.data[this.size++] = element;
	}

	private int internalInputArray(final int index, final int[] elements, final int elementsSize)
	{
		// check for simple case without a required capacity increase
		if(this.data.length - this.size >= elementsSize)
		{
			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index + elementsSize, this.size - index);
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
		 * where this ^^^^^^^ is exactely enough space (the gap) for inserting "elements"
		 *
		 * this way, all elements are only copied once
		 */
		final int[] data;
		System.arraycopy(this.data,     0, data = new int[newCapacity], 0, index);
		System.arraycopy(this.data, index, data, index + elementsSize, this.size - index);
		System.arraycopy(elements ,     0, this.data = data,    index, elementsSize);
		this.size = newSize;
		return elementsSize;
	}

	private int internalInputArray(final int index, final int[] elements, final int offset, final int length)
	{
		if(length < 0)
		{
			return this.internalReverseInputArray(index, elements, offset, -length);
		}

		// check for simple case without a required capacity increase
		if(this.data.length - this.size >= length)
		{
			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index + length, this.size - index);
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
		 * where this ^^^^^^^ is exactely enough space (the gap) for inserting "elements"
		 *
		 * this way, all elements are only copied once
		 */
		final int[] data;
		System.arraycopy(this.data,     0, data = new int[newCapacity], 0, index);
		System.arraycopy(this.data, index, data, index + length, this.size - index);
		System.arraycopy(elements, offset, this.data = data,    index, length);
		this.size = newSize;
		return length;
	}

	private int internalReverseInputArray(final int index, final int[] elements, final int offset, final int length)
	{
		// check for simple case without a required capacity increase
		if(this.data.length - this.size >= length)
		{
			// simply free up enough space at index and slide in new elements
			System.arraycopy(this.data, index, this.data, index + length, this.size - index);
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
		 * where this ^^^^^^^ is exactely enough space (the gap) for inserting "elements"
		 *
		 * this way, all elements are only copied once
		 */
		final int[] data;
		System.arraycopy(this.data,     0, data = new int[newCapacity], 0, index);
		System.arraycopy(this.data, index, data, index + length, this.size - index);
		XArrays.reverseArraycopy(elements, offset, this.data = data, index, length);
		this.size = newSize;
		return length;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	protected int[] internalGetStorageArray()
	{
		return this.data;
	}

	protected int internalSize()
	{
		return this.size;
	}

	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.size}; // trivial section
	}

	protected int internalCountingAddAll(final int[] elements) throws UnsupportedOperationException
	{
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return elements.length;
	}

	protected int internalCountingAddAll(final int[] elements, final int offset, final int length)
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
			throw new IndexBoundsException(bound);
		}
		this.ensureFreeCapacity(-length); // increaseCapacity
		final int[] data = this.data;
		int size = this.size;
		for(int i = offset; i > bound; i--)
		{
			data[size++] = elements[i];
		}
		this.size = size;
		return -length;
	}

	protected int internalCountingAddAll(final _intList elements) throws UnsupportedOperationException
	{
		final int oldSize = this.size;
		elements.copyTo(this);
		return this.size - oldSize;
	}

	protected int internalCountingPutAll(final int[] elements) throws UnsupportedOperationException
	{
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return elements.length;
	}

	protected int internalCountingPutAll(final int[] elements, final int offset, final int length)
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
			throw new IndexBoundsException(bound);
		}
		this.ensureFreeCapacity(-length); // increaseCapacity
		final int[] data = this.data;
		int size = this.size;
		for(int i = offset; i > bound; i--)
		{
			data[size++] = elements[i];
		}
		this.size = size;
		return -length;
	}

	protected int internalCountingPutAll(final _intList elements) throws UnsupportedOperationException
	{
		final int oldSize = this.size;
		elements.copyTo(this);
		return this.size - oldSize;
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	public _intList copy()
	{
		return new _intList(this);
	}

	// (30.03.2012 TM)TODO _intConsList
//	public ConstList immure()
//	{
//		return new ConstList(this);
//	}

	public _intList toReversed()
	{
		final int[] data, reversedData = new int[(data = this.data).length];
		for(int i = this.size, r = 0; i-- > 0;)
		{
			reversedData[r++] = data[i];
		}
		return new _intList(reversedData, this.size);
	}

	public int[] toArray()
	{
		final int[] array;
		System.arraycopy(this.data, 0, array = new int[this.size], 0, this.size);
		return array;
	}

	// executing //

	public _intList iterate(final _intProcedure procedure)
	{
		Abstract_intArrayStorage.iterate(this.data, this.size, procedure);
		return this;
	}

	public _intList iterate(final _intIndexProcedure procedure)
	{
		Abstract_intArrayStorage.iterate(this.data, this.size, procedure);
		return this;
	}

	// count querying //

	public int count(final int element)
	{
		return Abstract_intArrayStorage.count(this.data, this.size, element);
	}

	public int count(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.count(this.data, this.size, predicate);
	}

	// index querying //

	public int indexOf(final int element)
	{
		return Abstract_intArrayStorage.indexOf(this.data, this.size, element);
	}

	public int indexOf(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.indexOf(this.data, this.size, predicate);
	}

	public int lastIndexOf(final int element)
	{
		return Abstract_intArrayStorage.rngIndexOF(this.data, this.size, this.size - 1, -this.size, element);
	}

	public int lastIndexOf(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.lastIndexOf(this.data, this.size, predicate);
	}

	public int scan(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.scan(this.data, this.size, predicate);
	}

	// element querying //

	public int first()
	{
		return this.data[0];
	}

	public int last()
	{
		return this.data[this.size - 1];
	}

	public int search(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.search(this.data, this.size, predicate);
	}

	public int max()
	{
		return Abstract_intArrayStorage.max(this.data, this.size);
	}

	public int min()
	{
		return Abstract_intArrayStorage.min(this.data, this.size);
	}

	// boolean querying //

	public boolean hasVolatileElements()
	{
		return false;
	}

	public boolean nullAllowed()
	{
		return false;
	}

	public boolean isSorted(final boolean ascending)
	{
		return Abstract_intArrayStorage.isSorted(this.data, this.size, ascending);
	}

	// boolean querying - applies //

	public boolean containsSearched(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.containsSearched(this.data, this.size, predicate);
	}

	public boolean applies(final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.appliesAll(this.data, this.size, predicate);
	}

	// boolean querying - contains //

	public boolean contains(final int element)
	{
		return Abstract_intArrayStorage.contains(this.data, this.size, element);
	}

	public boolean containsAll(final _intList elements)
	{
		return Abstract_intArrayStorage.containsAll(this.data, this.size, elements.data, 0, elements.size);
	}

	// data set procedures //

	public <C extends _intCollecting> C copyTo(final C target)
	{
		return Abstract_intArrayStorage.copyTo(this.data, this.size, target);
	}

	public _intCollecting copyTo(final _intCollecting target, final _intPredicate predicate)
	{
		return Abstract_intArrayStorage.copyTo(this.data, this.size, target, predicate);
	}

	public int[] copyTo(final int[] target, final int offset)
	{
		System.arraycopy(this.data, 0, target, offset, this.size);
		return target;
	}

	public int[] copyTo(final int[] target, final int targetOffset, final int offset, final int length)
	{
		return Abstract_intArrayStorage.rngCopyTo(this.data, this.size, offset, length, target, targetOffset);
	}

	public _intCollecting copySelection(final _intCollecting target, final long... indices)
	{
		return Abstract_intArrayStorage.copySelection(this.data, this.size, indices, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// setting methods //
	////////////////////

	// (30.03.2012 TM)TODO _intViewList
//	public ListView view()
//	{
//		return new ListView(this);
//	}

//	public SubListView view(final int fromIndex, final int toIndex)
//	{
//		return new SubListView(this, fromIndex, toIndex); // range check is done in constructor
//	}

	public _intList shiftTo(final int sourceIndex, final int targetIndex)
	{
		if(sourceIndex >= this.size)
		{
			throw new IndexBoundsException(this.size, sourceIndex);
		}
		if(targetIndex >= this.size)
		{
			throw new IndexBoundsException(this.size, sourceIndex);
		}
		if(sourceIndex == targetIndex)
		{
			if(sourceIndex < 0)
			{
				throw new IndexBoundsException(this.size, sourceIndex);
			}
			return this;
		}

		final int shiftling = this.data[sourceIndex];
		if(sourceIndex < targetIndex)
		{
			System.arraycopy(this.data, sourceIndex + 1, this.data, sourceIndex, targetIndex - sourceIndex);
		}
		else
		{
			System.arraycopy(this.data, targetIndex, this.data, targetIndex + 1, sourceIndex - targetIndex);
		}

		this.data[targetIndex] = shiftling;
		return this;
	}

	public _intList shiftTo(final int sourceIndex, final int targetIndex, final int length)
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

		final int[] shiftlings;
		System.arraycopy(this.data, sourceIndex, shiftlings = new int[length], 0, length);
		if(sourceIndex < targetIndex)
		{
			System.arraycopy(this.data, sourceIndex + length, this.data, sourceIndex, targetIndex - sourceIndex);
		}
		else
		{
			System.arraycopy(this.data, targetIndex, this.data, targetIndex + length, sourceIndex - targetIndex);
		}

		System.arraycopy(shiftlings, 0, this.data, targetIndex, length);
		return this;
	}

	public _intList shiftBy(final int sourceIndex, final int distance)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance);
	}

	public _intList shiftBy(final int sourceIndex, final int distance, final int length)
	{
		return this.shiftTo(sourceIndex, sourceIndex + distance, length);
	}

	public _intList swap(final int indexA, final int indexB) throws IndexBoundsException, IndexOutOfBoundsException
	{
		if(indexA >= this.size)
		{
			throw new IndexBoundsException(this.size, indexA);
		}
		if(indexB >= this.size)
		{
			throw new IndexBoundsException(this.size, indexB);
		}
		final int t = this.data[indexA];
		this.data[indexA] = this.data[indexB];
		this.data[indexB] = t;
		return this;
	}

	public _intList swap(final int indexA, final int indexB, final int length)
	{
		Abstract_intArrayStorage.swap(this.data, this.size, indexA, indexB, length);
		return this;
	}

	public _intList reverse()
	{
		Abstract_intArrayStorage.reverse(this.data, this.size);
		return this;
	}

	// direct setting //

	public void setFirst(final int element)
	{
		this.data[0] = element;
	}

	public void setLast(final int element)
	{
		this.data[this.size - 1] = element;
	}

	public _intList set(final int offset, final int... elements)
	{
		if(offset < 0 || offset + elements.length > this.size)
		{
			throw new IndexOutOfBoundsException(exceptionStringRange(this.size, offset, offset + elements.length - 1));
		}
		System.arraycopy(elements, 0, this.data, offset, elements.length);
		return this;
	}

	public _intList set(final int offset, final int[] src, final int srcIndex, final int srcLength)
	{
		Abstract_intArrayStorage.set(this.data, this.size, offset, src, srcIndex, srcLength);
		return this;
	}

	public _intList fill(final int offset, final int length, final int element)
	{
		Abstract_intArrayStorage.fill(this.data, this.size, offset, length, element);
		return this;
	}

	// sorting //

	public _intList sort()
	{
		XSort.sort(this.data, 0, this.size);
		return this;
	}

	// replacing - single //

	public boolean replaceOne(final int element, final int replacement)
	{
		return Abstract_intArrayStorage.replaceOne(this.data, this.size, element, replacement);
	}

	public boolean replaceOne(final _intPredicate predicate, final int substitute)
	{
		return Abstract_intArrayStorage.substituteOne(this.data, this.size, predicate, substitute);
	}

	// replacing - multiple //

	public int replace(final int element, final int replacement)
	{
		return Abstract_intArrayStorage.replace(this.data, this.size, element, replacement);
	}

	public int replace(final _intPredicate predicate, final int substitute)
	{
		return Abstract_intArrayStorage.substitute(this.data, this.size, predicate, substitute);
	}

	// replacing - multiple all //

	@Deprecated
	public int replaceAll(final _intList elements, final int replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME _intList#replaceAll()
	}

	// replacing - mapped //

	public int modify(final _intFunction mapper)
	{
		return Abstract_intArrayStorage.modify(this.data, this.size, mapper);
	}

	public int modify(final _intPredicate predicate, final _intFunction mapper)
	{
		return Abstract_intArrayStorage.modify(this.data, this.size, predicate, mapper);
	}



	///////////////////////////////////////////////////////////////////////////
	// capacity methods //
	/////////////////////

	public int currentCapacity()
	{
		return this.data.length;
	}

	public int maximumCapacity()
	{
		return Integer.MAX_VALUE;
	}

	public boolean isFull()
	{
		return this.size >= Integer.MAX_VALUE;
	}

	public int freeCapacity()
	{
		return Integer.MAX_VALUE - this.size;
	}

	public int optimize()
	{
		final int requiredCapacity;
		if((requiredCapacity = XMath.pow2BoundMaxed(this.size)) != this.data.length)
		{
			System.arraycopy(this.data, 0, this.data = new int[requiredCapacity], 0, this.size);
		}
		return this.data.length;
	}

	public _intList ensureFreeCapacity(final int requiredFreeCapacity)
	{
		// as opposed to ensureCapacity(size + requiredFreeCapacity), this subtraction is overflow-safe
		if(this.data.length - this.size >= requiredFreeCapacity)
		{
			return this; // already enough free capacity
		}

		// overflow-safe check for unreachable capacity
		if(Integer.MAX_VALUE - this.size < requiredFreeCapacity)
		{
			throw new ArrayCapacityException((long)requiredFreeCapacity + this.size);
		}

		// calculate new capacity
		final int newSize = this.size + requiredFreeCapacity;
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
		final int[] data = new int[newCapacity];
		System.arraycopy(this.data, 0, data, 0, this.size);
		this.data = data;
		return this;
	}

	public _intList ensureCapacity(final int minCapacity)
	{
		if(minCapacity > this.data.length)
		{
			final int[] data = new int[XMath.pow2BoundMaxed(minCapacity)];
			System.arraycopy(this.data, 0, data, 0, this.size);
			this.data = data;
		}
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	public void accept(final int element)
	{
		this.internalAdd(element); // gets inlined, tests showed no performance difference.
	}

	@Override
	public boolean add(final int element)
	{
		this.internalAdd(element); // gets inlined, tests showed no performance difference.
		return true;
	}

	public _intList add(final int... elements)
	{
		this.ensureFreeCapacity(elements.length); // increaseCapacity
		System.arraycopy(elements, 0, this.data, this.size, elements.length);
		this.size += elements.length;
		return this;
	}

	public _intList addAll(final int[] elements, final int offset, final int length)
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
			if((bound = offset + length) < -1)
			{
				throw new ArrayIndexOutOfBoundsException(bound + 1);
			}
			this.ensureFreeCapacity(-length); // increaseCapacity
			final int[] data = this.data;
			int size = this.size;
			for(int i = offset; i > bound; i--)
			{
				data[size++] = elements[i];
			}
			this.size = size;
		}
		return this;
	}

	public _intList addAll(final _intList elements)
	{
		return elements.copyTo(this);
	}

	public boolean nullAdd()
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = new int[(int)(this.data.length * 2.0f)], 0, this.size);
		}
		this.data[size] = 0;  // It has to be here. For example, operation 'remove' changes the removed value to Integer.MIN_VALUE.
		this.size++;
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	public boolean nullPut()
	{
		return this.nullAdd();
	}

	public boolean put(final int element)
	{
		this.internalAdd(element); // gets inlined, tests showed no performance difference.
		return true;
	}

	public _intList put(final int... elements)
	{
		return this.add(elements);
	}

	public _intList putAll(final int[] elements, final int offset, final int length)
	{
		return this.addAll(elements, offset, length);
	}

	public _intList putAll(final _intList elements)
	{
		return elements.copyTo(this);
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	public boolean prepend(final int element)
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new CapacityExceededException();
			}
			System.arraycopy(this.data, 0, this.data = new int[(int)(this.data.length * 2.0f)], 1, this.size);
		}
		else
		{
			System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		}
		this.data[0] = element;
		this.size++;
		return true;
	}

	public _intList prepend(final int... elements)
	{
		this.internalInputArray(0, elements, elements.length);
		return this;
	}

	public _intList prependAll(final int[] elements, final int offset, final int length)
	{
		this.internalInputArray(0, elements, offset, length);
		return this;
	}

	public _intList prependAll(final _intList elements)
	{
		this.insertAll(0, elements);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	public boolean preput(final int element)
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new IndexOutOfBoundsException();
			}
			System.arraycopy(this.data, 0, this.data = new int[(int)(this.data.length * 2.0f)], 1, this.size);
		}
		else
		{
			System.arraycopy(this.data, 0, this.data, 1, this.size); // ignore size == 0 corner case
		}
		this.data[0] = element;
		this.size++;
		return true;
	}

	public _intList preput(final int... elements)
	{
		this.internalInputArray(0, elements, elements.length);
		return this;
	}

	public _intList preputAll(final int[] elements, final int offset, final int length)
	{
		this.internalInputArray(0, elements, offset, length);
		return this;
	}

	public _intList preputAll(final _intList elements)
	{
		this.inputAll(0, elements);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	public boolean insert(final int index, final int element)
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
					System.arraycopy(this.data, 0, this.data = new int[(int)(this.data.length * 2.0f)], 0, this.size);
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
			final int[] oldData = this.data;
			System.arraycopy(this.data, 0, this.data = new int[(int)(this.data.length * 2.0f)], 0, index);
			System.arraycopy(oldData, index, this.data, index + 1, this.size - index);
		}
		else
		{
			System.arraycopy(this.data, index, this.data, index + 1, this.size - index);
		}
		this.data[index] = element;
		this.size++;
		return true;
	}

	public int insert(final int index, final int... elements) throws IndexOutOfBoundsException
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingAddAll(elements);
			}
			throw new IndexBoundsException(this.size, index);
		}
		return this.internalInputArray(index, elements, elements.length);
	}

	public int insertAll(final int index, final int[] elements, final int offset, final int length)
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingAddAll(elements, offset, length);
			}
			throw new IndexBoundsException(this.size, index);
		}
		return this.internalInputArray(index, elements, offset, length);
	}

	public int insertAll(final int index, final _intList elements)
	{
		if(index >= this.size || index < 0)
		{
			if(index == this.size)
			{
				return this.internalCountingAddAll(elements);
			}
			throw new IndexBoundsException(this.size, index);
		}
		return this.internalInputArray(index, elements.data, elements.size);
	}




	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	public boolean input(final int index, final int element)
	{
		return insert(index, element);
	}

	public int input(final int index, final int... elements) throws IndexOutOfBoundsException
	{
		return insert(index, elements);
	}

	public int inputAll(final int index, final int[] elements, final int offset, final int length)
	{
		return insertAll(index, elements, offset, length);
	}

	public int inputAll(final int index, final _intList elements)
	{
		return insertAll(index, elements);
	}




	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	public void truncate()
	{
		this.size = 0;
		this.data = new int[1];
	}

	public int consolidate()
	{
		return 0; // nothing to do here
	}

	// removing - single //

	public boolean removeOne(final int element)
	{
		if(Abstract_intArrayStorage.removeOne(this.data, this.size, element))
		{
			this.size--;
			return true;
		}
		return false;
	}

	public int retrieve(final int element)
	{
		final int removedElement;
		if((removedElement = Abstract_intArrayStorage.retrieve(this.data, this.size, element, Integer.MIN_VALUE)) != 0)
		{
			this.size--;
			return removedElement;
		}
		return 0;
	}

	public int retrieve(final _intPredicate predicate)
	{
		final int e;
		if((e = Abstract_intArrayStorage.retrieve(this.data, this.size, predicate, Integer.MIN_VALUE)) != 0)
		{
			this.size--;
			return e;
		}
		return 0;
	}

	// removing - multiple //

	public int remove(final int element)
	{
		int removeCount;
		this.size -= removeCount = removeAllFromArray(this.data, 0, this.size, element);
		return removeCount;
	}

	public int removeAt(final int index) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		final int oldValue = this.data[index];

		final int moveCount;
		if((moveCount = this.size - 1 - index) > 0)
		{
			System.arraycopy(this.data, index + 1, this.data, index, moveCount);
		}
		this.data[--this.size] = 0;

		return oldValue;
	}

	// reducing //

	public int remove(final _intPredicate predicate)
	{
		final int removeCount;
		this.size -= removeCount = Abstract_intArrayStorage.reduce(this.data, this.size, predicate, Integer.MIN_VALUE);
		return removeCount;
	}

	// processing //

	public _intList process(final _intProcedure procedure)
	{
		this.size -= Abstract_intArrayStorage.process(this.data, this.size, procedure, Integer.MIN_VALUE);
		return this;
	}

	// moving //

	public _intCollecting moveTo(final _intCollecting target, final _intPredicate predicate)
	{
		this.size -= Abstract_intArrayStorage.moveTo(this.data, this.size, target, predicate, Integer.MIN_VALUE);
		return target;
	}

	public _intCollecting moveSelection(final _intCollecting target, final long... indices)
	{
		this.size -= Abstract_intArrayStorage.moveSelection(this.data, this.size, indices, target, Integer.MIN_VALUE);
		return target;
	}

	// removing - multiple all //

	public int removeAll(final _intList elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME _intList#removeAll()
//		final int removed;
//		this.size -= removed = removeAllFromArray(
//			elements, this.data, 0, this.size, this.data, 0, this.size, false
//		);
//		return removed;
	}

	// removing - duplicates //

	public int removeDuplicates()
	{
		final int removeCount;
		this.size -= removeCount = Abstract_intArrayStorage.removeDuplicates(this.data, this.size, Integer.MIN_VALUE);
		return removeCount;
	}

	// removing - indexed //

	public int fetch()
	{
		final int element = this.data[0];
		System.arraycopy(this.data, 1, this.data, 0, --this.size);
		this.data[this.size] = 0;
		return element;
	}

	public int pop()
	{
		final int element = this.data[this.size - 1]; // get element and provoke index exception
		this.data[--this.size] = 0; // update state
		return element;
	}

	public int pinch()
	{
		if(this.size == 0)
		{
			return 0;
		}
		final int element = this.data[0];
		System.arraycopy(this.data, 1, this.data, 0, --this.size);
		this.data[this.size] = 0;
		return element;
	}

	public int pick()
	{
		if(this.size == 0)
		{
			return 0;
		}
		final int element = this.data[--this.size];
		this.data[this.size] = 0;
		return element;
	}

	public int removeSelection(final int[] indices)
	{
		final int removeCount;
		this.size -= removeCount = Abstract_intArrayStorage.removeSelection(this.data, this.size, indices, Integer.MIN_VALUE);
		return removeCount;
	}

	public _intList removeRange(final int startIndex, final int length)
	{
		this.size -= Abstract_intArrayStorage.removeRange(this.data, this.size, startIndex, length);
		return this;
	}

	// (30.03.2012 TM)TODO _intSubList
//	public SubList range(final int fromIndex, final int toIndex)
//	{
//		// range check is done in constructor
//		return new SubList(this, fromIndex, toIndex);
//	}




	public boolean isEmpty()
	{
		return this.size == 0;
	}

	public int get(final int index) throws ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		return this.data[index];
	}

	public boolean set(final int index, final int element) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		this.data[index] = element;
		return false;
	}

	public int setGet(final int index, final int element) throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		final int old = this.data[index];
		this.data[index] = element;
		return old;
	}

	public int size()
	{
		return this.size;
	}

	@Override
	public String toString()
	{
		return Abstract_intArrayStorage.toString(this.data, this.size);
	}

	public void clear()
	{
		// remaining values are irrelevant as long as size is used correctly
		this.size = 0;
	}



	public static class Factory
	{
		private final int initialCapacity;

		public Factory(final int initialCapacity)
		{
			super();
			this.initialCapacity = XMath.pow2BoundMaxed(initialCapacity);
		}

		public int getInitialCapacity()
		{
			return this.initialCapacity;
		}

		public _intList newInstance()
		{
			return new _intList(new int[this.initialCapacity], this.initialCapacity);
		}

	}

}
