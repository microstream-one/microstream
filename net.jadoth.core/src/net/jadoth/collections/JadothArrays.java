package net.jadoth.collections;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.equality.Equalator;
import net.jadoth.exceptions.IndexBoundsException;
import net.jadoth.functional.JadothFunctional;
import net.jadoth.math.FastRandom;
import net.jadoth.util.UtilStackTrace;

/**
 * Numerous utility methods for working with arrays, all of which are either missing in the JDK or botched up
 * in one way or another. For both writing AND reading code, there must be a LOT of util methods, so that the
 * source code can be as short but comprehensible possible. Keeping the number of util methods at the bare minimum
 * might make things simpler for the JDK provider, but not for the JDK user. There need to be a rich set of proper
 * general purpose tools, not the need to write almost every util method anew in every project.
 * 
 * @author Thomas Muenz
 */
public final class JadothArrays
{
	public static final void validateRange0toUpperBound(final int upperBound, final int offset, final int length)
	{
		if(offset < 0 || offset >= upperBound)
		{
			throw new IndexExceededException(upperBound, offset);
		}

		if(length > 0 && offset + length > upperBound)
		{
			throw new IndexExceededException(upperBound, offset + length);
		}
		else if(length < 0 && offset + length < -1)
		{
			throw new IndexExceededException(-1, offset + length);
		}
	}

	private static String exceptionRange(final int size, final int startIndex, final int length)
	{
		return "Range [" + (length < 0 ? startIndex + length + 1 + ";" + startIndex
			: startIndex + ";" + (startIndex + length - 1)) + "] not in [0;" + (size - 1) + "]";
	}
	
	private static String exceptionIndexOutOfBounds(final int size, final int index)
	{
		return "Index: " + index + ", Size: " + size;
	}

	
	
	public static final int validateArrayIndex(final int arrayLength, final int index)
	{
		if(index < 0 || index >= arrayLength)
		{
			throw UtilStackTrace.cutStacktraceByOne(new ArrayIndexOutOfBoundsException(index));
		}
		return index;
	}

	public static final int validIndex(final int index, final Object[] array) throws ArrayIndexOutOfBoundsException
	{
		if(index < 0 || array != null && index >= array.length)
		{
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return index;
	}

	public static final int validateArrayRange(
		final Object[] array ,
		final int      offset,
		final int      length
		)
	{
		// elements array range checking
		if(length >= 0)
		{
			if(offset < 0 || offset + length > array.length)
			{
				throw new IndexOutOfBoundsException(exceptionRange(array.length, offset, length));
			}
			if(length == 0)
			{
				return 0;
			}
			return +1; // incrementing direction
		}
		else if(length < 0)
		{
			if(offset + length < -1 || offset >= array.length)
			{
				throw new IndexOutOfBoundsException(exceptionRange(array.length, offset, length));
			}
			return -1; // decrementing direction
		}
		else if(offset < 0 || offset >= array.length)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(array.length, offset));
		}
		else
		{
			// handle length 0 special case not as escape condition but as last case to ensure index checking
			return 0;
		}
	}

	public static final void checkBounds(final Object[] array, final int start, final int bound)
	{
		if(bound < 0 || bound > array.length)
		{
			throw new IndexExceededException(array.length, bound);
		}
		if(start < 0 || start >= bound)
		{
			throw new IndexExceededException(array.length, start);
		}
	}

	/**
	 * Returns if the passed array is either null or has the length 0.
	 *
	 * @param array
	 * @return
	 */
	public static boolean hasNoContent(final Object[] array)
	{
		return array == null || array.length == 0;
	}
	
	public static final <T> T[] fill(
		final T[]                   array   ,
		final Supplier<? extends T> supplier
	)
	{
		return uncheckedFill(array, 0, array.length, supplier);
	}
	
	public static final <T> T[] fill(
		final T[]                   array   ,
		final int                   offset  ,
		final int                   bound   ,
		final Supplier<? extends T> supplier
	)
	{
		checkBounds(array, offset, bound);
		
		return uncheckedFill(array, offset, bound, supplier);
	}
	
	public static final <T> T[] uncheckedFill(
		final T[]                   array   ,
		final int                   offset  ,
		final int                   bound   ,
		final Supplier<? extends T> supplier
	)
	{
		for(int i = offset; i < bound; i++)
		{
			array[i] = supplier.get();
		}
		
		return array;
	}

	public static final <T> T[] fill(final T[] array, final T fillElement, final int fromIndex, final int toIndex)
	{
		if(fromIndex < 0 || fromIndex >= array.length)
		{
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if(toIndex < 0 || toIndex >= array.length)
		{
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}

		if(fromIndex < toIndex)
		{
			int i = fromIndex;
			while(i <= toIndex)
			{
				array[i++] = fillElement;
			}
		}
		else
		{
			int i = toIndex;
			while(fromIndex >= toIndex)
			{
				array[i--] = fillElement;
			}
		}
		return array;
	}

	public static final int[] fill(final int[] array, final int fillElement)
	{
		final int length = array.length;
		for(int i = 0; i < length; i++)
		{
			array[i] = fillElement;
		}
		return array;
	}

	public static final byte[] fill(final byte[] array, final byte fillElement)
	{
		final int length = array.length;
		for(int i = 0; i < length; i++)
		{
			array[i] = fillElement;
		}
		return array;
	}

	public static final int[] fill(final int[] array, final int fillElement, final int fromIndex, final int toIndex)
	{
		if(fromIndex < 0 || fromIndex >= array.length)
		{
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if(toIndex < 0 || toIndex >= array.length)
		{
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}

		if(fromIndex < toIndex)
		{
			while(fromIndex <= toIndex)
			{
				int i = fromIndex;
				array[i++] = fillElement;
			}
		}
		else
		{
			while(fromIndex >= toIndex)
			{
				int i = toIndex;
				array[i--] = fillElement;
			}
		}
		return array;
	}

	public static final <T> T[] clear(final T[] array)
	{
		final int length = array.length;
		for(int i = 0; i < length; i++)
		{
			array[i] = null;
		}
		return array;
	}

	public static final <T> T[] replicate(final T subject, final int times)
	{
		final T[] array = X.ArrayForElementType(subject, times);
		for(int i = 0; i < times; i++)
		{
			array[i] = subject;
		}
		return array;
	}

	public static final <T> T[] subArray(final T[] array, final int offset, final int length)
	{
		final T[] newArray; // bounds checks are done by VM.
		System.arraycopy(
			array, offset, newArray = X.ArrayOfSameType(array, length), 0, length
			);
		return newArray;
	}

	public static final byte[] subArray(final byte[] array, final int offset, final int length)
	{
		final byte[] newArray; // bounds checks are done by VM.
		System.arraycopy(array, offset, newArray = new byte[length], 0, length);
		return newArray;
	}

	public static final char[] subArray(final char[] array, final int offset, final int length)
	{
		final char[] newArray; // bounds checks are done by VM.
		System.arraycopy(array, offset, newArray = new char[length], 0, length);
		return newArray;
	}

	/**
	 * Compares two Object arrays by reference of their content.
	 * <p>
	 * Note that specific equality of each element is situational and thus cannot be a concern
	 * of a generic array comparison, just as it cannot be the concern of the element's class directly.
	 *
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static boolean equals(final Object[] array1, final Object[] array2)
	{
		if(array1 == array2)
		{
			return true;
		}
		if(array1 == null || array2 == null)
		{
			return false;
		}

		final int length = array1.length;
		if(array2.length != length)
		{
			return false;
		}

		for(int i = 0; i < length; i++)
		{
			if(array1[i] != array2[i])
			{
				return false;
			}
		}
		return true;
	}

	public static final <E> boolean equals(
		final E[] array1,
		final int startIndex1,
		final E[] array2,
		final int startIndex2,
		final int length,
		final Equalator<? super E> comparator
		)
	{
		//all bounds exceptions are provoked intentionally because no harm will be done by this method in those cases
		int a = startIndex1, b = startIndex2;

		for(final int aBound = startIndex1 + length; a < aBound; a++, b++)
		{
			if(!comparator.equal(array1[a], array2[b]))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * adds all elements of the first array and all elements of the second array into one result array.
	 * Handles null-arrays correctly.
	 * Always creates a new array instance.
	 *
	 * @param a1
	 * @param a2
	 * @return
	 */
	@SafeVarargs
	public static final <T> T[] add(final T[] a1, final T... a2)
	{
		// escape conditions (must clone to consistently return a new instance)
		if(a1 == null)
		{
			return a2 == null ? null : a2.clone();
		}
		if(a2 == null)
		{
			return a1.clone();
		}

		// actual adding of two non-null arrays
		final T[] a = X.ArrayOfSameType(a1, a1.length + a2.length);
		System.arraycopy(a1, 0, a,         0, a1.length);
		System.arraycopy(a2, 0, a, a1.length, a2.length);
		return a;
	}

	public static final int[] add(final int[] a1, final int... a2)
	{
		// escape conditions (must clone to consistently return a new instance)
		if(a1 == null)
		{
			return a2 == null ? null : a2.clone();
		}
		if(a2 == null)
		{
			return a1.clone();
		}

		// actual adding
		final int[] a = new int[a1.length + a2.length];
		System.arraycopy(a1, 0, a,         0, a1.length);
		System.arraycopy(a2, 0, a, a1.length, a2.length);
		return a;
	}

	public static final long[] add(final long[] a1, final long... a2)
	{
		// escape conditions (must clone to consistently return a new instance)
		if(a1 == null)
		{
			return a2 == null ? null : a2.clone();
		}
		if(a2 == null)
		{
			return a1.clone();
		}

		// actual adding
		final long[] a = new long[a1.length + a2.length];
		System.arraycopy(a1, 0, a,         0, a1.length);
		System.arraycopy(a2, 0, a, a1.length, a2.length);
		return a;
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static final <T> T[] combine(final T[]... arrays)
	{
		if(arrays == null || arrays.length == 0)
		{
			return null;
		}

		// (13.03.2012 TM)FIXME: check type consistency throughout all element arrays
		return combine((Class<T>)arrays[0].getClass().getComponentType(), arrays);
	}

	@SuppressWarnings("unchecked")
	public static final <T, S extends T> T[] combine(final Class<T> componentType, final S[]... arrays)
	{
		// escape conditions (must clone to consistently return a new instance)
		if(arrays == null || arrays.length == 0)
		{
			return null;
		}
		if(arrays.length == 1)
		{
			return arrays[0].clone();
		}

		long totalLength = 0;
		for(final S[] array : arrays)
		{
			totalLength += array.length;
		}
		if(totalLength > Integer.MAX_VALUE)
		{
			throw new ArrayIndexOutOfBoundsException(Long.toString(totalLength));
		}

		// actual adding
		final T[] combined = (T[])Array.newInstance(componentType, (int)totalLength);
		for(int c = 0, i = 0; c < arrays.length; c++)
		{
			System.arraycopy(arrays[c], 0, combined, i, arrays[c].length);
			i += arrays[c].length;
		}
		return combined;
	}

	public static final int[] _intAdd(final int[] a1, final int... a2)
	{
		// escape conditions (must clone to consistently return a new instance)
		if(a1 == null)
		{
			return a2 == null ? null : a2.clone();
		}
		if(a2 == null)
		{
			return a1.clone();
		}

		// actual adding
		final int[] a = new int[a1.length + a2.length];
		System.arraycopy(a1, 0, a,         0, a1.length);
		System.arraycopy(a2, 0, a, a1.length, a2.length);
		return a;
	}

	/**
	 * Merges the both passed arrays by taking all elements from <code>a1</code> (even duplicates) and adds all
	 * elements of <code>a2</code> (also duplicates as well) that are not already contained in <code>a1</code>.
	 *
	 * @param <T>
	 * @param a1
	 * @param a2
	 * @return
	 */
	@SafeVarargs
	public static final <T> T[] merge(final T[] a1, final T... a2)
	{
		// escape conditions (must clone to consistently return a new instance)
		if(a1 == null)
		{
			return a2 == null ? null : a2.clone();
		}
		if(a2 == null)
		{
			return a1.clone();
		}

		final int         a1Len  = a1.length;
		final BulkList<T> buffer = new BulkList<>(a1);

		a2:
		for(final T e : a2)
		{
			for(int i = 0; i < a1Len; i++)
			{
				if(e == a1[i])
				{
					continue a2; // element already contained in a1, skip
				}
			}
			buffer.add(e); // element not yet contained in a1, add to buffer
		}

		final T[] newArray = X.ArrayOfSameType(buffer.data, buffer.size);
		System.arraycopy(buffer.data, 0, newArray, 0, buffer.size);
		return newArray;
	}

	/**
	 * This method checks if <code>array</code> contains <code>element</code> by object identity
	 *
	 * @param <E> any type
	 * @param array the array to be searched in
	 * @param element the element to be searched (by identity)
	 * @return {@code true} if <code>array</code> contains <code>element</code> by object identity, else <tt>false</tt>
	 */
	public static final <E> boolean contains(final E[] array, final E element)
	{
		for(final E e : array)
		{
			if(e == element)
			{
				return true;
			}
		}
		return false;
	}

	public static final <E> boolean eqContains(final E[] array, final E element)
	{
		if(element == null)
		{
			for(final E e : array)
			{
				if(e == null)
				{
					return true;
				}
			}
		}
		else
		{
			for(final E e : array)
			{
				if(element.equals(e))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static final <T, S extends T> boolean contains(final T[] array, final S element, final Equalator<? super T> cmp)
	{
		for(final T t : array)
		{
			if(cmp.equal(element, t))
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static final <E> boolean containsId(final Collection<E> c, final E element)
	{
		// case XGettingCollection
		if(c instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)c).containsId(element);
		}

		// case old collection, use slow iterator
		for(final E t : c)
		{
			if(t == element)
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static final <E> boolean containS(final Collection<E> c, final E element)
	{
		// case XGettingCollection
		if(c instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)c).contains(element);
		}

		return c.contains(element);
	}

	@SuppressWarnings("unchecked")
	public static final <E> boolean contains(final Collection<? super E> c, final E sample, final Equalator<? super E> equalator)
	{
		// case XGettingCollection
		if(c instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)c).containsSearched(JadothFunctional.predicate(sample, equalator));
		}

		// case old collection, use slow iterator
		for(final Object t : c)
		{
			if(equalator.equal((E)t, sample))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Removed all occurances of {@code e} from array {@code array}.
	 * <p>
	 * @param array the array containing all elements.
	 * @param start the starting offset (inclusive lower bound)
	 * @param bound the bounding offset (exclusive upper bound)
	 * @param e     the element to be removed
	 *
	 * @return the number of removed elements
	 */
	public static <E> int removeAllFromArray(final E[] array, final int start, final int bound, final E e)
		throws ArrayIndexOutOfBoundsException
	{
		// "base" index marking all stable elements (or "progress")
		int base = start;

		// compress array by moving non-subjects block by block
		for(int i = base; i < bound;)
		{
			while(i < bound && array[i] == e)
			{
				// determine move offset (next occurance of non-e)
				i++;
			}
			final int moveOffset = i;

			while(i < bound && array[i] != e)
			{
				// determine move length (range until next e)
				i++;
			}
			final int moveLength = i - moveOffset;

			// execute move
			System.arraycopy(array, moveOffset, array, base, moveLength);
			base += moveLength;
		}

		// null out trailing slots until bound
		for(int i = base; i < bound; i++)
		{
			array[i] = null;
		}

		// calculate and return amount of removed elements
		return bound - base;
	}

	public static int removeAllFromArray(final int[] array, final int start, final int bound, final int e)
		throws ArrayIndexOutOfBoundsException
	{
		// "base" index marking all stable elements (or "progress")
		int base = start;

		// compress array by moving non-subjects block by block
		for(int i = base; i < bound;)
		{
			while(i < bound && array[i] == e)
			{
				// determine move offset (next occurance of non-e)
				i++;
			}
			final int moveOffset = i;

			while(i < bound && array[i] != e)
			{
				// determine move length (range until next e)
				i++;
			}
			final int moveLength = i - moveOffset;

			// execute move
			System.arraycopy(array, moveOffset, array, base, moveLength);
			base += moveLength;
		}

		// null out trailing slots until bound
		for(int i = base; i < bound; i++)
		{
			array[i] = Integer.MIN_VALUE;
		}

		// calculate and return amount of removed elements
		return bound - base;
	}

	static final class ArrayElementRemover<E> implements Consumer<E>
	{
		private final E marker;
		private final E[] array;
		private final int start;
		private final int bound;

		public ArrayElementRemover(final E[] array, final int start, final int bound, final E removeMarker)
		{
			super();
			this.array  = array       ;
			this.start  = start       ;
			this.bound  = bound       ;
			this.marker = removeMarker;
		}

		@Override
		public void accept(final E e)
		{
			final E   marker = this.marker;
			final E[] array  = this.array ;
			final int bound  = this.bound ;
			for(int i = this.start; i < bound; i++)
			{
				if(array[i] == e)
				{
					array[i] = marker;
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static final <E> E[] removeDuplicates(final E... elements)
	{
		return X.Enum(elements).toArray((Class<E>)elements.getClass().getComponentType());
	}

	//!\\ NOTE: copy of single-object version with only contains part changed! Maintain by copying!
	public static <E> int removeAllFromArray(
		final XGettingCollection<? extends E> elements,
		final E[] array,
		final int start,
		final int bound
	)
		throws ArrayIndexOutOfBoundsException
	{
		if(elements.isEmpty())
		{
			return 0;
		}

		// use a random (the "first") element to be removed as the remove marker, may even be null
		final E removeMarker = elements.get();

		final int removeCount;
		try
		{
			elements.iterate(new ArrayElementRemover<>(array, start, bound, removeMarker));
		}
		finally
		{
			// must ensure that removemarker is removed in any case
			removeCount = removeAllFromArray(array, start, bound, removeMarker);
		}
		return removeCount;
	}

	public static <E> int removeAllFromArray(
		final E[] array,
		final int start,
		final int bound,
		final E   e    ,
		final Equalator<? super E> equalator
		)
			throws ArrayIndexOutOfBoundsException
	{
		// "base" index marking all stable elements (or "progress")
		int base = start;

		// compress array by moving non-subjects block by block
		for(int i = base; i < bound;)
		{
			while(i < bound && equalator.equal(array[i], e))
			{
				// determine move offset (next occurance of non-e)
				i++;
			}
			final int moveOffset = i;

			while(i < bound && !equalator.equal(array[i], e))
			{
				// determine move length (range until next e)
				i++;
			}
			final int moveLength = i - moveOffset;

			// execute move
			System.arraycopy(array, moveOffset, array, base, moveLength);
			base += moveLength;
		}

		// null out trailing slots until bound
		for(int i = base; i < bound; i++)
		{
			array[i] = null;
		}

		// calculate and return amount of removed elements
		return bound - base;
	}

	//!\\ NOTE: copy of single-object version with only contains part changed! Maintain by copying!
	public static <E> int removeAllFromArray(
		final E[] array,
		final int start,
		final int bound,
		final XGettingCollection<? extends E> elements,
		final Equalator<? super E> equalator
		)
			throws ArrayIndexOutOfBoundsException
	{
		//determine first move target index
		int currentMoveTargetIndex = start;
		//if dest is the same as src, skip all to be retained objects
		while(currentMoveTargetIndex < bound
			&& !elements.containsSearched(JadothFunctional.predicate(array[currentMoveTargetIndex], equalator))
		)
		{
			currentMoveTargetIndex++;
		}

		int currentMoveSourceIndex = 0;
		int currentMoveLength = 0;
		int seekIndex = currentMoveTargetIndex;


		while(seekIndex < bound)
		{
			while(seekIndex < bound && elements.containsSearched(JadothFunctional.predicate(array[seekIndex], equalator)))
			{
				seekIndex++;
			}
			currentMoveSourceIndex = seekIndex;

			while(seekIndex < bound && !elements.containsSearched(JadothFunctional.predicate(array[seekIndex], equalator)))
			{
				seekIndex++;
			}
			currentMoveLength = seekIndex - currentMoveSourceIndex;

			System.arraycopy(array, currentMoveSourceIndex, array, currentMoveTargetIndex, currentMoveLength);
			currentMoveTargetIndex += currentMoveLength;
		}
		for(int i = currentMoveTargetIndex; i < bound; i++)
		{
			array[i] = null;
		}
		return bound - currentMoveTargetIndex;
	}

	public static final <T> T[] reverse(final T[] array)
	{
		final int halfSize = array.length >> 1;
		for(int i = 0, j = array.length - 1; i < halfSize; i++, j--)
		{
			final T e = array[i];
			array[i] = array[j];
			array[j] = e;
		}
		return array;
	}

	public static final <T> T[] toReversed(final T[] array)
	{
		final int len;
		final T[] rArray = X.ArrayOfSameType(array, len = array.length);
		for(int i = 0, r = len; i < len; i++)
		{
			rArray[--r] = array[i];
		}
		return rArray;
	}

	public static final <T> T[] toReversed(final T[] array, final int offset, final int length)
	{
		return length < 0
			? JadothArrays.reverseArraycopy(
				array,
				offset,
				X.ArrayOfSameType(array, -length),
				0,
				-length
				)
				: JadothArrays.reverseArraycopy(
					array,
					offset + length - 1,
					X.ArrayOfSameType(array, length),
					0,
					length
					)
					;
	}

	public static final <T> T[] copy(final T[] array)
	{
		final T[] newArray = X.ArrayOfSameType(array, array.length);
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	/**
	 * At least for Java 1.8, the types seem to not be checked.
	 * Passing a collection of Strings and a Number[] (meaning String extends Number) is not a compiler error.
	 * Bug / generics loophole.
	 */
	public static final <T, E extends T> T[] copyTo(
		final XGettingCollection<E> source,
		final T[]                   target
	)
		throws IndexBoundsException
	{
		return copyTo(source, target, 0);
	}

	public static final <T, E extends T> T[] copyTo(
		final XGettingCollection<E> source      ,
		final T[]                   target      ,
		final int                   targetOffset
	)
		throws IndexBoundsException
	{
		if(source.size() + targetOffset > target.length)
		{
			throw new IndexBoundsException(targetOffset, target.length, source.size() + targetOffset);
		}

		if(source instanceof AbstractSimpleArrayCollection)
		{
			final Object[] data = ((AbstractSimpleArrayCollection<?>)source).internalGetStorageArray();
			final int      size = ((AbstractSimpleArrayCollection<?>)source).internalSize();
			System.arraycopy(source, 0, data, targetOffset, size);
		}
		else
		{
			int t = targetOffset - 1;
			for(final E e : source)
			{
				target[++t] = e;
			}
		}

		return target;
	}

	@SafeVarargs
	public static final <T> T[] shuffle(final T... data)
	{
		final FastRandom random = new FastRandom();
		for(int i = data.length, j; i > 1; i--)
		{
			final T t = data[i - 1];
			data[i - 1] = data[j = random.nextInt(i)];
			data[j] = t;
		}
		return data;
	}

	public static final <E> E[] shuffle(final E[] array, final int startIndex, final int endIndex)
	{
		if(startIndex < 0 || endIndex >= array.length || startIndex > endIndex)
		{
			throw new IndexOutOfBoundsException("Range [" + startIndex + ';' + endIndex + "] not in [0;" + (array.length - 1) + "].");
		}
		final FastRandom random = new FastRandom();
		for(int i = endIndex, j; i > startIndex; i--)
		{
			final E t = array[i - 1];
			array[i - 1] = array[j = random.nextInt(i)];
			array[j] = t;
		}
		return array;
	}

	public static final int[] shuffle(final int... data)
	{
		return shuffle(new FastRandom(), data);
	}

	public static final int[] shuffle(final FastRandom random, final int... data)
	{
		for(int i = data.length, j; i > 1; i--)
		{
			final int t = data[i - 1];
			data[i - 1] = data[j = random.nextInt(i)];
			data[j] = t;
		}
		return data;
	}

	/**
	 * Convenience method, calling either {@link System#arraycopy(Object, int, Object, int, int)} for
	 * {@code length >= 0} or {@link JadothArrays#reverseArraycopy(Object[], int, Object[], int, int)} for {@code length < 0}
	 * and returns {@code dest}.<br>
	 * If length is known to be positive and performance badly matters or negative length shall be treated as an error,
	 * use {@link System#arraycopy(Object, int, Object, int, int)} directly. Otherwise, this method is a convenient
	 * alternative to handle more flexible bi-directional array copying.
	 *
	 * @param <T>
	 * @param <U>
	 * @param src
	 * @param srcPos
	 * @param dest
	 * @param destPos
	 * @param length
	 * @return
	 */
	public static <T, U extends T> T[] arraycopy(
		final U[] src,
		final int srcPos,
		final T[] dest,
		final int destPos,
		final int length
		)
	{
		if(length < 0)
		{
			return JadothArrays.reverseArraycopy(src, srcPos, dest, destPos, -length);
		}
		System.arraycopy(src, srcPos, dest, destPos, length);
		return dest;
	}

	public static final boolean containsNull(final Object[] data, final int offset, final int length)
	{
		final int endIndex, d; // bi-directional index movement
		if(length >= 0)
		{
			if(offset < 0 || (endIndex = offset + length - 1) >= data.length)
			{
				throw new IndexOutOfBoundsException(exceptionRange(data.length, offset, length));
			}
			if(length == 0)
			{
				return false;
			}
			d = +1; // incrementing direction
		}
		else if(length < 0)
		{
			if((endIndex = offset + length + 1) < 0 || offset >= data.length)
			{
				throw new IndexOutOfBoundsException(exceptionRange(data.length, offset, length));
			}
			d = -1; // decrementing direction
		}
		else if(offset < 0 || offset >= data.length)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(data.length, offset));
		}
		else
		{
			// handle length 0 special case not as escape condition but as last case to ensure index checking
			return false;
		}

		int i = offset - d;
		while(i != endIndex)
		{
			if(data[i += d] == null)
			{
				return true;
			}
		}
		return false;
	}

	public static <E> E[] copyRange(final E[] elements, final int offset, final int length)
	{
		final E[] copy = X.ArrayOfSameType(elements, length);
		System.arraycopy(elements, offset, copy, 0, length);
		return copy;
	}

	public static <E> E[] filter(final E[] elements, final Predicate<? super E> predicate)
	{
		return filterTo(elements, new BulkList<E>(), predicate).toArray(componentType(elements));
	}

	public static <E, C extends Consumer<? super E>> C filterTo(
		final E[] elements,
		final C target,
		final Predicate<? super E> predicate
		)
	{
		// once in a while, omiting the {} in stuff like that is really funny (though not more readable, ofc)
		for(final E e : elements)
		{
			if(predicate.test(e))
			{
				target.accept(e);
			}
		}
		return target;
	}

	public static <E> int replaceAllInArray(
		final E[] data,
		final int startLow,
		final int boundHigh,
		final E oldElement,
		final E newElement
		)
	{
		int replaceCount = 0;
		for(int i = startLow; i < boundHigh; i++)
		{
			if(data[i] == oldElement)
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static <E> int replaceAllInArray(
		final E[]                  data      ,
		final int                  startLow  ,
		final int                  boundHigh ,
		final E                    sample    ,
		final Equalator<? super E> equalator ,
		final E                    newElement
		)
	{
		int replaceCount = 0;
		for(int i = startLow; i < boundHigh; i++)
		{
			if(equalator.equal(data[i], sample))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static <T> T[] and(final T[] a1, final T[] a2)
	{
		final int length;
		final T[] target = X.ArrayOfSameType(a1, length = min(a1.length, a2.length));
		for(int i = 0; i < length; i++)
		{
			target[i] = a1[i] != null && a2[i] != null ? a1[i] : null;
		}
		return target;
	}

	public static <T> T[] or(final T[] a1, final T[] a2)
	{
		final int length;
		final T[] target = X.ArrayOfSameType(a1, length = min(a1.length, a2.length));
		for(int i = 0; i < length; i++)
		{
			target[i] = a1[i] != null ? a1[i] : a2[i] != null ? a2[i] : null;
		}
		return target;
	}

	public static <T> T[] not(final T[] a1, final T[] a2)
	{
		final int length;
		final T[] target = X.ArrayOfSameType(a1, length = min(a1.length, a2.length));
		for(int i = 0; i < length; i++)
		{
			target[i] = a2[i] == null ? a1[i] : null;
		}
		return target;
	}
	
	/**
	 * Orders the passed elements by the passed indices.
	 *
	 * @param elements the elements to be sorted according to the passed indices.
	 * @param indices the indices defining the order in which the passed elements shall be rearranged.
	 * @param target the target array to receive the sorted elements.
	 */
	public static <S, T extends S> S[] orderByIndices(
		final T[] elements,
		final int[] indices,
		final int indicesOffset,
		final S[] target
		)
			throws IllegalArgumentException
	{
		if(indicesOffset < 0)
		{
			throw new ArrayIndexOutOfBoundsException(indicesOffset);
		}

		final int targetLength = target.length;
		if(elements.length + indicesOffset > indices.length)
		{
			throw new ArrayIndexOutOfBoundsException(elements.length + indicesOffset);
		}

		final int indicesBound = indicesOffset + elements.length;
		for(int i = indicesOffset; i < indicesBound; i++)
		{
			if(indices[i] >= targetLength)
			{
				// indices < 0 explicitely valid for allowing items to be skipped
				throw new ArrayIndexOutOfBoundsException(indices[i]);
			}
		}

		for(int i = indicesOffset; i < indicesBound; i++)
		{
			if(indices[i] < 0)
			{
				continue;
			}
			target[indices[i]] = elements[i - indicesOffset];
		}
		return target;
	}

	public static final int min(final int... data)
	{
		if(data.length == 0)
		{
			return 0;
		}

		int loopMinElement = data[0];
		for(int i = 1; i < data.length; i++)
		{
			if(data[i] < loopMinElement)
			{
				loopMinElement = data[i];
			}
		}
		return loopMinElement;
	}

	public static final int max(final int... data)
	{
		if(data.length == 0)
		{
			return 0;
		}

		int loopMaxElement = data[0];
		for(int i = 1; i < data.length; i++)
		{
			if(data[i] >= loopMaxElement)
			{
				loopMaxElement = data[i];
			}
		}
		return loopMaxElement;
	}
	
	public static final <T> boolean applies(final T[] array, final Predicate<? super T> predicate)
	{
		for(final T t : array)
		{
			if(predicate.test(t))
			{
				return true;
			}
		}
		return false;
	}
	
	public static final <T> T search(final T[] array, final Predicate<? super T> predicate)
	{
		for(final T t : array)
		{
			if(predicate.test(t))
			{
				return t;
			}
		}
		return null;
	}
	
	public static final <T> int count(final T[] array, final Predicate<? super T> predicate)
	{
		int count = 0;
		for(final T t : array)
		{
			if(predicate.test(t))
			{
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Reverse order counterpart to {@link System#arraycopy(Object, int, Object, int, int)}.
	 * <p>
	 * Copies source elements from {@code src}, starting at {@code srcPos} in negative direction ({@code -length}
	 * and copies them one by one to {@code dest}, starting at {@code destPos} in positive direction ({@code +length},
	 * thus effectively copying the elements in reverse order.
	 *
	 * @param src      the source array.
	 * @param srcPos   starting position in the source array (the <i>highest</i> index for reverse iteration).
	 * @param dest     the destination array.
	 * @param destPos  starting position in the destination data (the <i>lowest</i> index in the target array).
	 * @param length   the number of array elements to be copied in reverse order.
	 *
	 * @exception ArrayIndexOutOfBoundsException if copying would cause access of data outside array bounds.
	 * @exception ArrayStoreException if an element in the {@code src} array could not be stored into the
	 *            {@code dest} array because of a type mismatch.
	 * @exception NullPointerException if either {@code src} or  {@code dest} is {@code null}.
	 */
	public static <T, U extends T> T[] reverseArraycopy(
		final U[] src    ,
		final int srcPos ,
		final T[] dest   ,
		final int destPos,
		final int length
		)
	{
		if(srcPos >= src.length)
		{
			throw new ArrayIndexOutOfBoundsException(srcPos);
		}
		if(destPos < 0)
		{
			throw new ArrayIndexOutOfBoundsException(destPos);
		}
		if(length < 0)
		{
			throw new ArrayIndexOutOfBoundsException(length);
		}
		if(srcPos - length < -1)
		{
			throw new ArrayIndexOutOfBoundsException(srcPos - length);
		}
		if(destPos + length > dest.length)
		{
			throw new ArrayIndexOutOfBoundsException(destPos + length);
		}

		final int destBound = destPos + length;
		for(int s = srcPos, d = destPos; d < destBound; s--, d++)
		{
			dest[d] = src[s];
		}
		return dest;
	}

	public static int[] reverseArraycopy(
		final int[] src    ,
		final int   srcPos ,
		final int[] dest   ,
		final int   destPos,
		final int   length
		)
	{
		if(srcPos >= src.length)
		{
			throw new ArrayIndexOutOfBoundsException(srcPos);
		}
		if(destPos < 0)
		{
			throw new ArrayIndexOutOfBoundsException(destPos);
		}
		if(length < 0)
		{
			throw new ArrayIndexOutOfBoundsException(length);
		}
		if(srcPos - length < -1)
		{
			throw new ArrayIndexOutOfBoundsException(srcPos - length);
		}
		if(destPos + length > dest.length)
		{
			throw new ArrayIndexOutOfBoundsException(destPos + length);
		}

		final int destBound = destPos + length;
		for(int s = srcPos, d = destPos; d < destBound; s--, d++)
		{
			dest[d] = src[s];
		}
		return dest;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T[] convertArray(final Object[] objects, final Class<T> type) throws ClassCastException
	{
		final T[] converted = (T[])Array.newInstance(type, objects.length);
		for(int i = 0; i < objects.length; i++)
		{
			converted[i] = (T)objects[i];
		}
		return converted;
	}

	@SuppressWarnings("unchecked")
	public static <E> Class<E> componentType(final E[] array)
	{
		// so much utility functionality missing in JDK. Or is it just me? -.-
		return (Class<E>)array.getClass().getComponentType();
	}

	public static final int arrayHashCode(final Object[] data, final int size)
	{
		int hashCode = 1;
		for(int i = 0; i < size; i++)
		{
			// CHECKSTYLE.OFF: MagicNumber: inherent algorithm component
			final Object obj;
			hashCode = 31 * hashCode + ((obj = data[i]) == null ? 0 : obj.hashCode());
			// CHECKSTYLE.ON: MagicNumber
		}
		return hashCode;
	}

	static final <E> boolean uncheckedContainsAll(
		final E[] subject,
		final int subjectLowOffset,
		final int subjectHighBound,
		final E[] elements,
		final int elementsLowOffset,
		final int elementsHighBound
		)
	{
		// cross-iterate both arrays
		main:
		for(int ei = elementsLowOffset; ei < elementsHighBound; ei++)
		{
			final E element = elements[ei];
			for(int di = subjectLowOffset; di < subjectHighBound; di++)
			{
				if(element == subject[di])
				{
					continue main;
				}
			}
			return false;  // one element was not found in the subject range, return false
		}
	return true;  // all elements have been found, return true
	}

	static final <E> boolean uncheckedContainsAll(
		final E[] subject,
		final int subjectLowOffset,
		final int subjectHighBound,
		final E[] elements,
		final int elementsLowOffset,
		final int elementsHighBound,
		final Equalator<? super E> equalator
		)
	{
		// cross-iterate both arrays
		main:
		for(int ei = elementsLowOffset; ei < elementsHighBound; ei++)
		{
			final E element = elements[ei];
			for(int di = subjectLowOffset; di < subjectHighBound; di++)
			{
				if(equalator.equal(element, subject[di]))
				{
					continue main;
				}
			}
			return false;  // one element was not found in the subject range, return false
		}
	return true;  // all elements have been found, return true
	}

	public static final boolean contains(final int[] values, final int value)
	{
		for(final int v : values)
		{
			if(v == value)
			{
				return true;
			}
		}
		return false;
	}

	@SafeVarargs
	public static <E> void iterate(final Consumer<? super E> iterator, final E... elements)
	{
		for(final E e : elements)
		{
			iterator.accept(e);
		}
	}

	public static <E> void iterate(final Consumer<? super E> iterator, final E[] elements, final int offset, final int length)
	{
		AbstractArrayStorage.validateRange0toUpperBound(elements.length, offset, length);

		for(int i = offset; i < length; i++)
		{
			iterator.accept(elements[i]);
		}
	}

	public static final int indexOf(final byte value, final byte[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final boolean value, final boolean[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final short value, final short[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final char value, final char[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final int value, final int[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final float value, final float[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final long value, final long[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final double value, final double[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(final byte[] data, final byte[] subject)
	{
		return indexOf(data, subject, 0);
	}
	
	public static final int indexOf(final byte[] data, final byte[] subject, final int startIndex)
	{
		final int  bound     = data.length - subject.length + 1;
		final byte firstByte = subject[0];

		outer:
		for(int i = startIndex; i < bound; i++)
		{
			// quick check for fitting first byte
			if(data[i] != firstByte)
			{
				continue;
			}

			// potential match location, check rest of the array
			for(int j = 1; j < subject.length; j++)
			{
				if(data[i + j] != subject[j])
				{
					continue outer;
				}
			}

			// all bytes matched, location found, return index.
			return i;
		}

		// no match found until bounding index, return miss
		return -1;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private JadothArrays()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
