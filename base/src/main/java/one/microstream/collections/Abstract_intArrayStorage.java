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

import java.util.Random;
import java.util.function.BiConsumer;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.interfaces.ChainStorage;
import one.microstream.collections.interfaces._intCollecting;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional._intFunction;
import one.microstream.functional._intIndexProcedure;
import one.microstream.functional._intPredicate;
import one.microstream.functional._intProcedure;
import one.microstream.math.FastRandom;
import one.microstream.math.XMath;


/**
 * Abstract class with static delegate array logic methods to be used as pseudo-array-inherent methods.
 * <p>
 * The passed array and size are handled as pseudo implementation details and thus are never sanity-checked.
 * <p>
 * See {@link ChainStorage} and for example {@link ChainStorageStrong} as a comparable actual logic implementation without
 * delegate-pseudo-character.
 *
 * 
 */
public abstract class Abstract_intArrayStorage
{
	// CHECKSTYLE.OFF: FinalParameter: A LOT of methods use that pattern in this class
	
	static final String exceptionSkipNegative(final int skip)
	{
		return "Skip count may not be negative: " + skip;
	}

	static final String exceptionRange(final int size, final int offset, final int length)
	{
		return "Range ["
			+ (length < 0 ? offset + length + 1 + ";" + offset
			:  length > 0 ? offset + ";" + (offset + length - 1)
			:  offset + ";" + offset
			) + "] not in [0;" + (size - 1) + "]";
	}

	static final String exceptionIndexOutOfBounds(final long size, final long index)
	{
		return "Index: " + index + ", Size: " + size;
	}

	private static String exceptionIllegalSwapBounds(
		final int size,
		final int indexA,
		final int indexB,
		final int length
	)
	{
		return "Illegal swap bounds: (" + indexA + " [" + length + "] -> "
			+ indexB + " [" + length + "]) in range [0;" + (size - 1) + "]"
		;
	}

	public static final int checkIterationDirection(final int size, final int offset, final int length)
	{
		if(length > 0)
		{
			if(offset < 0 || offset + length > size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			return 1;
		}
		else if(length < 0)
		{
			if(offset >= size || offset + length < -1)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			return -1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		return 0;
	}

	public static final void validateRange0Based(final int size, final int offset, final int length)
	{
		if(length > 0)
		{
			if(offset < 0 || offset + length > size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
		}
		else if(length < 0)
		{
			if(offset >= size || offset + length < -1)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	public static final int addAll(
		final int[] data,
		      int size,
		final int[] elements,
		final int srcIndex,
		final int srcLength,
		final _intPredicate predicate
	)
	{
		// determine traversal direction and check array bounds
		if(srcIndex < 0 || srcIndex >= elements.length)
		{
			throw new IndexBoundsException(elements.length, srcIndex);
		}
		final int d;
		if(srcLength > 0)
		{
			if(srcIndex + srcLength >= elements.length)
			{
				throw new IndexBoundsException(elements.length, srcIndex + srcLength);
			}
			d = 1;
		}
		else if(srcLength < 0)
		{
			if(srcIndex + srcLength < 0)
			{
				throw new IndexBoundsException(elements.length, srcIndex + srcLength);
			}
			d = -1;
		}
		else
		{
			return 0;
		}

		int element;
		final int endIndex = srcIndex + srcLength - d;
		int i = srcIndex - d;

		// main loop with limit
		while(i != endIndex)
		{
			if(predicate.test(element = elements[i += d]))
			{
				data[size++] = element;
			}
		}
		return size;
	}

	public static final int addAll(
		final int[] data,
		      int size,
		final int[] elements,
		final int srcIndex,
		final int srcLength,
		final _intPredicate predicate,
		      int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}
		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0)
		{
			return 0; // spare unnecessary traversal
		}

		// determine traversal direction and check array bounds
		if(srcIndex < 0 || srcIndex >= elements.length)
		{
			throw new IndexBoundsException(elements.length, srcIndex);
		}
		final int d;
		if(srcLength > 0)
		{
			if(srcIndex + srcLength >= elements.length)
			{
				throw new IndexBoundsException(elements.length, srcIndex + srcLength);
			}
			d = 1;
		}
		else if(srcLength < 0)
		{
			if(srcIndex + srcLength < 0)
			{
				throw new IndexBoundsException(elements.length, srcIndex + srcLength);
			}
			d = -1;
		}
		else
		{
			return 0;
		}

		int element;
		final int endIndex = srcIndex + srcLength - d;
		int i = srcIndex - d;

		// prepend skipping to spare the check inside the main loop
		while(i != endIndex && skip != 0)
		{
			if(predicate.test(element = elements[i += d]))
			{
				skip--;
			}
		}

		// main loop with limit
		while(i != endIndex && lim != 0)
		{
			if(predicate.test(element = elements[i += d]))
			{
				data[size++] = element;
				lim--;
			}
		}
		return size;
	}



	///////////////////////////////////////////////////////////////////////////
	// containing //
	///////////////


	public static final boolean contains(
		final int[] data,
		final int size,
		final int element
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(element == data[i])
			{
				return true;
			}
		}
		return false;
	}

	public static final boolean rngContains(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int element
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] == element)
			{
				return true;
			}
		}
		return false;
	}

	public static final boolean contains(
		final int[] data,
		final int size,
		final _intPredicate predicate
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				return true;
			}
		}
		return false;
	}

	public static final boolean rngContains(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				return true;
			}
		}
		return false;
	}

	public static final boolean containsAll(
		final int[] data,
		final int size,
		final int[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		// cross-iterate both arrays
		main:
		for(int ei = elementsOffset, bound = elementsOffset + elementsLength; ei < bound; ei++)
		{
			final int element = elements[ei];
			for(int di = 0; di < size; di++)
			{
				if(element == data[di])
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true;  // all elements have been found, return true
	}

	public static final boolean rngContainsAll(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intList elements
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		final int endIndex = offset + length - d;

		// other list's data array and size
		final int[] eData = elements.internalGetStorageArray();
		final int eSize = elements.size();

		main:
		for(int ei = 0; ei < eSize; ei++)
		{
			final int element = eData[ei];
			for(int di = offset - d; di != endIndex;)
			{
				if(element == data[di += d])
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true;  // all elements have been found, return true
	}



	///////////////////////////////////////////////////////////////////////////
	// counting //
	/////////////

	public static final int count(
		final int[] data,
		final int size,
		final int element
	)
	{
		int count = 0;
		for(int i = 0; i < size; i++)
		{
			if(data[i] == element)
			{
				count++;
			}
		}
		return count;
	}

	public static final int rngCount(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int element
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int count = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] == element)
			{
				count++;
			}
		}
		return count;
	}

	public static final int count(
		final int[] data,
		final int size,
		final _intPredicate predicate
	)
	{
		int count = 0;
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				count++;
			}
		}
		return count;
	}

	public static final int rngCount(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int count = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				count++;
			}
		}
		return count;
	}



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic //
	////////////////////

	// copying //

	public static final <C extends _intCollecting> C copyTo(
		final int[] data,
		final int size,
		final C target
	)
	{
		if(target instanceof _intList)
		{
			((_intList)target).addAll(data, 0, size);
			return target;
		}
		for(int i = 0; i < size; i++)
		{
			target.add(data[i]);
		}
		return target;
	}

	public static final <C extends _intCollecting> C rngCopyTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final C target
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int endIndex = offset + length - d;

		if(target instanceof _intList)
		{
			((_intList)target).addAll(data, offset, length);
			return target;
		}

		for(int i = offset - d; i != endIndex;)
		{
			target.add(data[i += d]);
		}
		return target;
	}

	public static final <C extends _intCollecting> C copyTo(
		final int[] data,
		final int size,
		final C target,
		final _intPredicate predicate
	)
	{
		// trying to optimize for bulk procedure types here is hardly possible due to predicate

		for(int i = 0; i < size; i++)
		{
			final int element;
			if(predicate.test(element = data[i]))
			{
				target.add(element);
			}
		}
		return target;
	}

	public static final <C extends _intCollecting> C rngCopyTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final C target,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int endIndex = offset + length - d;

		if(target instanceof _intList)
		{
			// (30.03.2012 TM)FIXME: fix
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME _intArrayStorage#rngCopyTo()
//			XUtilsCollection.addAll((_intList)target, data, offset, length, predicate);
//			return target;
		}

		for(int i = offset - d; i != endIndex;)
		{
			final int element;
			if(predicate.test(element = data[i += d]))
			{
				target.add(element);
			}
		}
		return target;
	}

	public static final <C extends _intCollecting> C copyTo(
		final int[] data,
		final int size,
		final C target,
		final _intPredicate predicate,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return target; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;

		while(i != lastIndex && skip != 0)
		{
			if(predicate.test(data[++i]))
			{
				skip--;
			}
		}
		while(i != lastIndex && lim != 0)
		{
			final int element;
			if(predicate.test(element = data[++i]))
			{
				target.add(element);
				lim--;
			}
		}
		return target;
	}

	public static final <C extends _intCollecting> C rngCopyTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final C target,
		final _intPredicate predicate,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return target; // spare unnecessary traversal
		}

		int i = offset - d;
		while(i != endIndex && skip != 0)
		{
			if(predicate.test(data[i += d]))
			{
				skip--;
			}
		}
		while(i != endIndex && lim != 0)
		{
			final int element;
			if(predicate.test(element = data[i += d]))
			{
				target.add(element);
				lim--;
			}
		}
		return target;
	}

	public static final int[] rngCopyTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int[] target,
		final int targetOffset
	)
	{
		if(length >= 0)
		{
			if(length == 0)
			{
				return target;
			}
			System.arraycopy(data, offset, target, targetOffset, length);
			return target;
		}
		else
		{
			final int endIndex= offset + length + 1;
			if(endIndex  < 0)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			else if(offset < 0 || offset >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
			}

			for(int i = offset, t = targetOffset; i >= endIndex; i--)
			{
				target[t++] = data[i];
			}
			return target;
		}
	}

	public static final <C extends _intCollecting> C copySelection(
		final int[] data,
		final int size,
		final long[] indices,
		final C target
	)
	{
		final int length = indices.length;

		// validate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		// actual copying
		for(int i = 0; i < length; i++)
		{
			target.add(data[(int)indices[i]]); // manual loop to spare temporary variable
		}

		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	// searching //
	//////////////

	public static final Integer search(
		final int[] data,
		final int size,
		final _intPredicate predicate
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				return data[i];
			}
		}
		return null;
	}

	public static final Integer rngSearch(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return null;
		}
		final int endIndex = offset + length - d;

		for(final int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i]))
			{
				return data[i];
			}
		}

		return null;
	}

	public static final int max(final int[] data, final int size)
	{
		if(size == 0)
		{
			return 0; // kind of buggy, but what to do ^^
		}

		int loopMaxElement = data[0];
		for(int i = 1; i < size; i++)
		{
			if(loopMaxElement < data[i])
			{
				loopMaxElement = data[i];
			}
		}
		return loopMaxElement;
	}

	public static final int min(final int[] data, final int size)
	{
		if(size == 0)
		{
			return 0; // kind of buggy, but what to do ^^
		}

		int loopMaxElement = data[0];
		for(int i = 1; i < size; i++)
		{
			if(loopMaxElement > data[i])
			{
				loopMaxElement = data[i];
			}
		}
		return loopMaxElement;
	}



	///////////////////////////////////////////////////////////////////////////
	// executing //
	//////////////

	public static final void rngIterate(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intProcedure procedure
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			procedure.accept(data[i += d]);
		}
	}

	public static final void iterate(
		final int[] data,
		final int size,
		final _intProcedure procedure
	)
	{
		for(int i = 0; i < size; i++)
		{
			procedure.accept(data[i]);
		}
	}

	public static final void iterate(
		final int[] data,
		final int size,
		final _intIndexProcedure procedure
	)
	{
		for(int i = 0; i < size; i++)
		{
			procedure.accept(data[i], i);
		}
	}

	public static final void rngIterate(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intIndexProcedure procedure
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			procedure.accept(data[i += d], i);
		}
	}

	public static final void iterate(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final _intProcedure procedure
	)
	{
		for(int i = 0; i < size; i++)
		{
			final int element;
			if(predicate.test(element = data[i]))
			{
				procedure.accept(element);
			}
		}
	}

	public static final void rngIterate(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final _intProcedure procedure
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		int element;
		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(element = data[i += d]))
			{
				procedure.accept(element);
			}
		}
	}

	public static final void iterate(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final _intProcedure procedure,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return ; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(i != lastIndex && skip != 0)
		{
			if(predicate.test(data[++i]))
			{
				skip--;
			}
		}

		int element;
		while(i != lastIndex && lim != 0)
		{
			if(predicate.test(element = data[++i]))
			{
				procedure.accept(element);
				lim--;
			}
		}
	}

	public static final void rngIterate(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final _intProcedure procedure,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return; // spare unnecessary traversal
		}

		int i = offset - d;
		while(i != endIndex && skip != 0)
		{
			if(predicate.test(data[i += d]))
			{
				skip--;
			}
		}
		while(i != endIndex && lim != 0)
		{
			final int element;
			if(predicate.test(element = data[i += d]))
			{
				procedure.accept(element);
				lim--;
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// indexing //
	/////////////

	public static final int indexOf(
		final int[] data,
		final int size,
		final int element
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == element)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int rngIndexOF(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int element
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] == element)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int indexOf(
		final int[] data,
		final int size,
		final _intPredicate predicate
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				return i;
			}
		}
		return -1;
	}

	public static final int rngIndexOf(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				return i;
			}
		}

		return -1;
	}

	public static final int lastIndexOf(
		final int[] data,
		final int size,
		final _intPredicate predicate
	)
	{
		for(int i = size; i-- > 0;)
		{
			if(predicate.test(data[i]))
			{
				return i;
			}
		}
		return -1;
	}

	public static final int scan(
		final int[] data,
		final int size,
		final _intPredicate predicate
	)
	{
		int foundIndex = -1;
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				foundIndex = i;
			}
		}
		return foundIndex;
	}

	public static final int rngScan(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		final int endIndex = offset + length - d;

		int foundIndex = -1;
		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				foundIndex = i;
			}
		}
		return foundIndex;
	}



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	public static final int rngRemove(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int element
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		return removeAllFromArray(data, start, bound, element);
	}

	// removing - multiple, limited //

	public static final int remove(
		final int[] data,
		final int size,
		final int element,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}
		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0;
		}

		int i = 0;
		while(i < size && skip != 0)
		{
			if(data[i++] == element)
			{
				skip--;
			}
		}
		final int removeStartIndex = i;
		while(i < size && lim != 0)
		{
			if(data[i] == element)
			{
				data[i] = removeMarker;
				lim--;
			}
			i++;
		}
		return removeAllFromArray(data, removeStartIndex, i, removeMarker);
	}

	public static final int rngRemove(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int element,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		int i = offset - d;
		while(i != endIndex && skip != 0)
		{
			if(element == data[i += d])
			{
				skip--;
			}
		}
		int removeStartIndex = i + d;
		while(i != endIndex && lim != 0)
		{
			if(element == data[i += d])
			{
				data[i] = removeMarker;
				lim--;
			}
		}
		if(d < 0)
		{
			final int temp = removeStartIndex;
			removeStartIndex = i;
			i = temp;
		}
		return removeAllFromArray(data, removeStartIndex, ++i, element);
	}

	// reducing //

	public static final int reduce(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final int removeMarker
	)
	{
		if(size == 0)
		{
			return 0;
		}

		final int removeCount;
		int i = 0;
		try
		{
			while(i < size)
			{
				if(predicate.test(data[i]))
				{
					data[i] = removeMarker;
				}
				i++;
			}
		}
		finally
		{
			//even if predicate throws an execption, the remove markers have to be cleared
			removeCount = XArrays.removeAllFromArray(data, 0, i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngReduce(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final int removeMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount, removeStartIndex;
		int i = offset - d;
		try
		{
			while(i != endIndex)
			{
				if(predicate.test(data[i += d]))
				{
					data[i] = removeMarker;
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// reducing - limited //

	public static final int reduce(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}
		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(skip != 0)
		{
			if(predicate.test(data[++i]))
			{
				skip--;
			}
			if(i == lastIndex)
			{
				return 0; // spare unnecessary remove call
			}
		}

		final int removeCount, removeStartIndex = i + 1;
		try
		{
			while(i != lastIndex && lim != 0)
			{
				if(predicate.test(data[++i]))
				{
					data[i] = removeMarker;
					lim--;
				}
			}
		}
		finally
		{
			removeCount = XArrays.removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngReduce(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int removeCount;
		int i = offset - d;
		while(skip != 0)
		{
			if(predicate.test(data[i += d]))
			{
				skip--;
			}
			if(i == endIndex)
			{
				return 0;
			}
		}
		int removeStartIndex = i + d;
		try
		{
			while(i != endIndex && lim != 0)
			{
				if(predicate.test(data[i += d]))
				{
					data[i] = removeMarker;
					lim--;
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				final int temp = removeStartIndex;
				removeStartIndex = i;
				i = temp;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// retaining //

	public static final int retainAll(
		final int[] data,
		final int size,
		final _intList elements,
		final int removeMarker
	)
	{
		if(elements.size() == 0)
		{
			// effectively clear the array, return size as remove count.
			for(int i = size; i-- > 0;)
			{
				data[i] = 0;
			}
			return size;
		}

		final int removeCount;
		final int lastIndex = size - 1;
		int i = -1;
		try
		{
			while(i < lastIndex)
			{
				if(!elements.contains(data[++i]))
				{
					data[i] = removeMarker;
				}
			}
		}
		finally
		{
			removeCount = XArrays.removeAllFromArray(data, 0, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngRetainAll(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intList elements,
		final int removeMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount, removeStartIndex;
		int i = offset - d;
		try
		{
			while(i != endIndex)
			{
				if(!elements.contains( data[i += d]))
				{
					data[i] = removeMarker;
				}
			}

		}
		finally
		{
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// processing //

	public static final int process(
		final int[] data,
		final int size,
		final _intProcedure procedure,
		final int removeMarker
	)
	{
		int i = 0;
		final int removeCount;
		try
		{
			for(; i < size; i++)
			{
				procedure.accept(data[i]);
				data[i] = removeMarker; // only needed in case of exception
			}
			// no exception occured, so completely clear array right away
			while(i-- > 0)
			{
				data[i] = 0;
			}
		}
		finally
		{
			removeCount = removeAllFromArray(data, 0, i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngProcess(
		final int[] data,
		final int size,
		final int offset,
		final int length, final _intProcedure procedure,
		final int removeMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int i = offset - d;
		final int removeCount;
		try
		{
			while(i != endIndex)
			{
				procedure.accept(data[i += d]);
				data[i] = removeMarker;
			}
		}
		finally
		{
			final int removeStartIndex;
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// moving //

	public static final int moveTo(
		final int[] data,
		final int size,
		final _intCollecting target,
		final _intPredicate predicate,
		final int removeMarker
	)
	{
		final int lastIndex = size - 1;
		final int removeCount;
		int i = -1;
		try
		{
			while(i < lastIndex)
			{
				final int element;
				if(predicate.test(element = data[++i]))
				{
					target.add(element);
					data[i] = removeMarker;
				}
			}
		}
		finally
		{
			//can't return until remove markers are cleared, so do this in any case
			removeCount = XArrays.removeAllFromArray(data, 0, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngMoveTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intCollecting target,
		final _intPredicate predicate,
		final int removeMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int element;
		int i = offset - d;
		final int removeCount;
		try
		{
			while(i != endIndex)
			{
				if(predicate.test(element = data[i += d]))
				{
					target.add(element);
					data[i] = removeMarker;
				}
			}
		}
		finally
		{
			final int removeStartIndex;
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final int moveSelection(
		final int[] data,
		final int size,
		final long[] indices,
		final _intCollecting target,
		final int removeMarker
	)
	{
		final int length;
		if((length = indices.length) == 0)
		{
			return 0;
		}

		// validate all indices before moving the first element
		long min, max = min = indices[0];
		for(int i = 1; i < length; i++)
		{
			if(indices[i] < min)
			{
				min = indices[i];
			}
			else if(indices[i] > max)
			{
				max = indices[i];
			}
		}
		if(min < 0)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, min));
		}
		if(max >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, max));
		}

		final int removeCount;
		// actual moving
		try
		{
			for(int i = 0; i < length; i++)
			{
				target.add(data[(int)indices[i]]); // manual loop to spare temporary variable
				data[(int)indices[i]] = removeMarker;
			}
		}
		finally
		{
			removeCount = XArrays.removeAllFromArray(data, (int)min, (int)++max, removeMarker);
		}
		return removeCount;
	}

	// moving - limited //

	public static final int moveTo(
		final int[] data,
		final int size,
		final _intCollecting target,
		final _intPredicate predicate,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(skip != 0)
		{
			if(predicate.test(data[++i]))
			{
				skip--;
			}
			if(i == lastIndex)
			{
				return 0; // spare unnecessary remove call
			}
		}
		final int removeStartIndex = i + 1;
		final int removeCount;
		try
		{
			while(i != lastIndex && lim != 0)
			{
				final int element;
				if(predicate.test(element = data[++i]))
				{
					target.add(element);
					data[i] = removeMarker;
					lim--;
				}
			}
		}
		finally
		{
			removeCount = XArrays.removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngMoveTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intCollecting target,
		final _intPredicate predicate,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		int i = offset - d;
		while(skip != 0)
		{
			if(predicate.test(data[i += d]))
			{
				skip--;
			}
			if(i == endIndex)
			{
				return 0; // spare unnecessary remove call
			}
		}
		int removeStartIndex = i + d;
		final int removeCount;
		try
		{
			while(i != endIndex && lim != 0)
			{
				final int element;
				if(predicate.test(element = data[i += d]))
				{
					target.add(element);
					data[i] = removeMarker;
					lim--;
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				final int temp = removeStartIndex;
				removeStartIndex = i;
				i = temp;
			}
		 removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// removing - multiple all, limited //

	public static final int removeAll(
		final int[] data,
		final int size,
		final _intList elements,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(skip != 0)
		{
			if(elements.contains(data[++i]))
			{
				skip--;
			}
			if(i == lastIndex)
			{
				return 0;
			}
		}
		final int removeCount, removeStartIndex = i + 1;
		try
		{
			while(i != lastIndex && lim != 0)
			{
				if(elements.contains(data[++i]))
				{
					data[i] = removeMarker;
					lim--;
				}
			}
		}
		finally
		{
			removeCount = XArrays.removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final int rngRemoveAll(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intList elements,
		int skip,
		final Integer limit,
		final int removeMarker
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		if(elements.size() == 0)
		{
			return 0;
		}

		int i = offset - d;
		while(skip != 0)
		{
			if(elements.contains(data[i += d]))
			{
				skip--;
			}
			if(i == endIndex)
			{
				return 0;
			}
		}
		final int removeCount;
		int removeStartIndex = i + d;
		try
		{
			while(i != endIndex && lim != 0)
			{
				if(elements.contains(data[i += d]))
				{
					data[i] = removeMarker;
					lim--;
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				final int temp = removeStartIndex;
				removeStartIndex = i;
				i = temp;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// removing - duplicates //

	public static final int removeDuplicates(
		final int[] data,
		final int size,
		final int removeMarker
	)
	{
		return rngRemoveDuplicates(data, size, 0, size, removeMarker);
	}

	public static final int rngRemoveDuplicates(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int removeMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount;
		int i = offset - d;
		int removeStartIndex = offset;
		try
		{
			while(i != endIndex)
			{
				final int ei = data[i += d];
				if(ei == removeMarker)
				{
					continue;
				}
				for(int j = i; j != endIndex;)
				{
					final int ej;
					if((ej = data[j += d]) == removeMarker)
					{
						continue;
					}
					if(ei == ej)
					{
						data[j] = removeMarker;
					}
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				final int temp = removeStartIndex;
				removeStartIndex = i;
				i = temp;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// removing - indexed //

	public static final int removeSelection(
		final int[] data,
		final int size,
		final int[] indices,
		final int removeMarker
	)
	{
		final int length;
		if((length = indices.length) == 0)
		{
			return 0;
		}

		// validate all indices before moving the first element
		int min, max = min = indices[0];
		for(int i = 1, idx; i < length; i++)
		{
			if((idx = indices[i]) < min)
			{
				min = idx;
			}
			else if(idx > max)
			{
				max = idx;
			}
		}
		if(min < 0)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, min));
		}
		if(max >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, max));
		}

		// actual moving
		final int removeCount;
		try
		{
			for(int i = 0; i < length; i++)
			{
				data[indices[i]] = removeMarker;
			}
		}
		finally
		{
			removeCount = XArrays.removeAllFromArray(data, min, size, removeMarker);
		}
		return removeCount;
	}

	public static final int removeRange(
		final int[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}

		System.arraycopy(data, bound, data, start, size - bound);

		// free old array buckets
		for(int i = size; i < size; i++)
		{
			data[i] = 0;
		}
		return length;
	}

	public static final int retrieve(
		final int[] data,
		      int size,
		final int element,
		final int notFoundMarker
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == element)
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = 0;
				return element;
			}
		}
		return notFoundMarker;
	}

	public static final int retrieve(
		final int[] data,
		      int size,
		final _intPredicate predicate,
		final int notFoundMarker
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				final int oldElement = data[i];
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = 0;
				return oldElement;
			}
		}
		return notFoundMarker;
	}

	public static final boolean removeOne(final int[] data, int size, final int element)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == element)
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = 0;
				return true;
			}
		}
		return false;
	}

	public static final int rngRetrieve(
		final int[] data,
		      int size,
		final int offset,
		final int length,
		final int element,
		final int notFoundMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return notFoundMarker;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(element == data[i += d])
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = 0;
				return element;
			}
		}
		return notFoundMarker;
	}

	public static final int rngRetrieve(
		final int[] data,
		      int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final int notFoundMarker
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return notFoundMarker;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				final int oldElement = data[i];
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = 0;
				return oldElement;
			}
		}
		return notFoundMarker;
	}

	public static final boolean rngRemoveOne(
		final int[] data,
		      int size,
		final int offset,
		final int length,
		final int element
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(element == data[i += d])
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = 0;
				return true;
			}
		}
		return false;
	}



	///////////////////////////////////////////////////////////////////////////
	// replacing //
	//////////////

	// replacing - single //

	public static final boolean replaceOne(
		final int[] data,
		final int size,
		final int oldElement,
		final int newElement
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == oldElement)
			{
				data[i] = newElement;
				return true;
			}
		}
		return false;
	}

	public static final int rngReplaceOne(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int oldElement,
		final int newElement
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(oldElement == data[i += d])
			{
				data[i] = newElement;
				return i;
			}
		}

		return -1;
	}

	public static final boolean substituteOne(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final int replacement
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				data[i] = replacement;
				return true;
			}
		}
		return false;
	}

	public static final int rngReplaceOne(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final int newElement
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				data[i] = newElement;
				return i;
			}
		}
		return -1;
	}

	// replacing - multiple //

	public static final int replace(
		final int[] data,
		final int size,
		final int oldElement,
		final int newElement
	)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			if(data[i] == oldElement)
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int rngReplace(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int oldElement,
		final int newElement
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] == oldElement)
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int substitute(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final int newElement
	)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int rngReplace(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final int newElement
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	// replacing - multiple, limited //

	public static final int replace(
		final int[] data,
		final int size,
		final int oldElement,
		final int newElement,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(i != lastIndex && skip != 0)
		{
			if(oldElement == data[++i])
			{
				skip--;
			}
		}
		int replaceCount = 0;
		while(i != lastIndex && lim != 0)
		{
			if(oldElement == data[++i])
			{
				data[i] = newElement;
				replaceCount++;
				lim--;
			}
		}
		return replaceCount;
	}

	public static final int rngReplace(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int oldElement,
		final int newElement,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		int i = offset - d;
		while(i != endIndex && skip != 0)
		{
			if(oldElement == data[i += d])
			{
				skip--;
			}
		}
		int replaceCount = 0;
		while(i != endIndex && lim != 0)
		{
			if(oldElement == data[i += d])
			{
				data[i] = newElement;
				replaceCount++;
				lim--;
			}
		}
		return replaceCount;
	}

	public static final int replace(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final int newElement,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(i != lastIndex && skip != 0)
		{
			if(predicate.test(data[++i]))
			{
				skip--;
			}
		}
		int replaceCount = 0;
		while(i != lastIndex && lim != 0)
		{
			if(predicate.test(data[++i]))
			{
				data[i] = newElement;
				lim--;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int rngReplace(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intPredicate predicate,
		final int newElement,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		int replaceCount = 0;
		int i = offset - d;
		while(i != endIndex && skip != 0)
		{
			if(predicate.test(data[i += d]))
			{
				skip--;
			}
		}
		while(i != endIndex && lim != 0)
		{
			if(predicate.test(data[i += d]))
			{
				data[i] = newElement;
				replaceCount++;
				lim--;
			}
		}
		return replaceCount;
	}

	// replacing - multiple all //

	public static final int rngReplaceAll(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intList oldElements,
		final int newElement
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(oldElements.contains(data[i += d]))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	// replacing - multiple all, limited //

	public static final int replaceAll(
		final int[] data,
		final int size,
		final _intList oldElements,
		final int newElement,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		final int lastIndex = size - 1;
		int i = -1;
		while(i != lastIndex && skip != 0)
		{
			if(oldElements.contains(data[++i]))
			{
				skip--;
			}
		}
		int replaceCount = 0;
		while(i != lastIndex && lim != 0)
		{
			if(oldElements.contains(data[++i]))
			{
				data[i] = newElement;
				lim--;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int rngReplaceAll(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intList oldElements,
		final int newElement,
		int skip,
		final Integer limit
	)
	{
		if(skip < 0)
		{
			throw new IllegalArgumentException(exceptionSkipNegative(skip));
		}

		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int lim = limit == null ? Integer.MAX_VALUE : limit.intValue();
		if(lim <= 0 || size == 0)
		{
			return 0; // spare unnecessary traversal
		}

		int replaceCount = 0;
		int i = offset - d;
		while(i != endIndex && skip != 0)
		{
			if(oldElements.contains(data[i += d]))
			{
				skip--;
			}
		}
		while(i != endIndex && lim != 0)
		{
			if(oldElements.contains(data[i += d]))
			{
				data[i] = newElement;
				replaceCount++;
				lim--;
			}
		}
		return replaceCount;
	}

	// replacing - mapped //

	public static final int modify(final int[] data, final int size, final _intFunction mapper)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			if(data[i] != (data[i] = mapper.apply(data[i])))
			{
				// tricky :D
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int modify(
		final int[] data,
		final int size,
		final _intPredicate predicate,
		final _intFunction mapper
	)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				data[i] = mapper.apply(data[i]);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final int rngModify(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final _intFunction mapper
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] != (data[i] = mapper.apply(data[i])))
			{
				// setting array right away is faster than buffer var.
				replaceCount++;
			}
		}
		return replaceCount;
	}



	///////////////////////////////////////////////////////////////////////////
	// setting //
	////////////

	public static final void swap(
		final int[] data,
		final int size,
		final int indexA,
		final int indexB
	)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(indexA >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indexA));
		}
		if(indexB >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indexB));
		}
		final int t = data[indexA];
		data[indexA] = data[indexB];
		data[indexB] = t;
	}

	public static final void swap(
		final int[] data,
		final int size,
		      int indexA,
		      int indexB,
		final int length
	)
	{
		if(length == 0 || indexA == indexB)
		{
			return;
		}
		else if(indexA > indexB)
		{
			final int t = indexA;
			indexA = indexB;
			indexB = t;
		}

		final int bound;
		if(indexA < 0 || length < 0 || (bound = indexA + length) >= indexB || indexB + length >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIllegalSwapBounds(size, indexA, indexB, length));
		}

		while(indexA < bound)
		{
			final int t = data[indexA];
			data[indexA++] = data[indexB];
			data[indexB++] = t;
		}
	}

	public static final void reverse(
		final int[] data,
		final int size
	)
	{
		final int halfSize = size >> 1;
		for(int i = 0, j = size - 1; i < halfSize; i++, j--)
		{
			final int element = data[i];
			data[i] = data[j];
			data[j] = element;
		}
	}

	public static final void rngReverse(
		final int[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		int low, high;
		if(length >= 0)
		{
			if((low = offset) < 0 || (high = offset + length - 1) >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			if(length == 0)
			{
				return;
			}
		}
		else if(length < 0)
		{
			if((low = offset + length + 1) < 0 || (high = offset) >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
		}
		else
		{
			return; // handle length 0 special case not as escape condition but as last case to ensure index checking
		}

		while(low < high)
		{
			final int element = data[low];
			data[low++] = data[high];
			data[high--] = element;
		}
	}

	// direct setting //

	public static final void set(
		final int[] data,
		final int size,
		final int offset,
		final int... elements
	)
	{
		if(offset < 0 || offset + elements.length > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset + elements.length - 1));
		}
		System.arraycopy(elements, 0, data, offset, elements.length);
	}

	public static final void set(
		final int[] data,
		final int size,
		final int offset,
		final int[] src,
		final int srcIndex,
		final int srcLength
	)
	{
		if(srcLength < 0)
		{
			if(offset < 0 || offset - srcLength > size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset - srcLength + 1));
			}
			if(srcIndex >= src.length)
			{
				throw new ArrayIndexOutOfBoundsException(srcIndex);
			}
			final int bound;
			if((bound = offset + srcLength) < -1)
			{
				throw new ArrayIndexOutOfBoundsException(bound + 1);
			}
			for(int s = srcIndex, i = offset; s > bound; s--)
			{
				data[i++] = src[s];
			}
			return;
		}

		if(offset < 0 || offset + srcLength > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset + src.length - 1));
		}
		System.arraycopy(src, srcIndex, data, offset, srcLength);
	}

	public static final void fill(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final int element
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			data[i += d] = element;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	public static final String toString(final int[] data, final int size)
	{
		if(size == 0)
		{
			return "[]";
		}
		final VarString vc = VarString.New((int)(size * 2.0f)).append('[');
		for(int i = 0; i < size; i++)
		{
			vc.add(data[i]).add(',', ' ');
		}
		vc.deleteLast().setLast(']');
		return vc.toString();
	}

	public static final VarString appendTo(final int[] data, final int size, final VarString vc)
	{
		for(int i = 0; i < size; i++)
		{
			vc.add(data[i]);
		}
		return vc;
	}

	public static final VarString appendTo(
		final int[] data,
		final int size,
		final VarString vc,
		final char separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		for(int i = 0; i < size; i++)
		{
			vc.add(data[i]).append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final VarString appendTo(
		final int[] data,
		final int size,
		final VarString vc,
		final String separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		else if(separator == null || separator.isEmpty())
		{
			for(int i = 0; i < size; i++)
			{
				vc.add(data[i]);
			}
		}
		else
		{
			final char[] sepp = XChars.readChars(separator);
			for(int i = 0; i < size; i++)
			{
				vc.add(data[i]).add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}

	public static final VarString appendTo(
		final int[] data,
		final int size,
		final VarString vc,
		final BiConsumer<VarString, Integer> appender
	)
	{
		if(size == 0)
		{
			return vc;
		}
		for(int i = 0; i < size; i++)
		{
			appender.accept(vc, data[i]);
		}
		return vc;
	}

	public static final VarString appendTo(
		final int[] data,
		final int size,
		final VarString vc,
		final BiConsumer<VarString, Integer> appender,
		final char separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		for(int i = 0; i < size; i++)
		{
			appender.accept(vc, data[i]);
			vc.append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final VarString appendTo(
		final int[] data,
		final int size,
		final VarString vc,
		final BiConsumer<VarString, Integer> appender,
		final String separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		else if(separator == null || separator.isEmpty())
		{
			for(int i = 0; i < size; i++)
			{
				appender.accept(vc, data[i]);
			}
		}
		else
		{
			final char[] sepp = XChars.readChars(separator);
			for(int i = 0; i < size; i++)
			{
				appender.accept(vc, data[i]);
				vc.add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}

	public static final VarString rngAppendTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;
		for(int i = offset - d; i != endIndex;)
		{
			vc.add(data[i += d]);
		}
		return vc;
	}

	public static final VarString rngAppendTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final char separator
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			vc.add(data[i += d]).append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final VarString rngAppendTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final String separator
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		if(separator == null || separator.isEmpty())
		{
			for(int i = offset - d; i != endIndex;)
			{
				vc.add(data[i += d]);
			}
		}
		else
		{
			final char[] sepp = XChars.readChars(separator);
			for(int i = offset - d; i != endIndex;)
			{
				vc.add(data[i += d]).add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}

	public static final VarString rngAppendTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final BiConsumer<VarString, Integer> appender
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			appender.accept(vc, data[i += d]);
		}
		return vc;
	}

	public static final VarString rngAppendTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final BiConsumer<VarString, Integer> appender,
		final char separator
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			appender.accept(vc, data[i += d]);
			vc.append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final VarString rngAppendTo(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final BiConsumer<VarString, Integer> appender,
		final String separator
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		if(separator == null || separator.isEmpty())
		{
			for(int i = offset - d; i != endIndex;)
			{
				appender.accept(vc, data[i += d]);
			}
		}
		else
		{
			final char[] sepp = XChars.readChars(separator);
			for(int i = offset - d; i != endIndex;)
			{
				appender.accept(vc, data[i += d]);
				vc.add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}



	///////////////////////////////////////////////////////////////////////////
	// sorting //
	////////////

	public static final boolean isSorted(
		final int[] data,
		final int size,
		final boolean ascending
	)
	{
		return ascending
			? isSortedAscending(data, size)
			: isSortedDescending(data, size)
		;
	}

	public static final boolean isSortedAscending(final int[] data, final int size)
	{
		if(size <= 1)
		{
			return true;
		}
		int loopLastElement = data[0];
		for(int i = 1; i < size; i++)
		{
			if(loopLastElement > data[i])
			{
				return false;
			}
			loopLastElement = data[i];
		}
		return true;
	}

	public static final boolean isSortedDescending(final int[] data, final int size)
	{
		if(size <= 1)
		{
			return true;
		}
		int loopLastElement = data[0];
		for(int i = 1; i < size; i++)
		{
			if(loopLastElement < data[i])
			{
				return false;
			}
			loopLastElement = data[i];
		}
		return true;
	}

	public static final void shuffle(final int[] data, final int size)
	{
		final FastRandom random = new FastRandom();
		for(int i = size, j; i > 1; i--)
		{
			final int t = data[i - 1];
			data[i - 1] = data[j = random.nextInt(i)];
			data[j] = t;
		}
	}

	public static final void rngShuffle(
		final int[] data,
		final int size,
		final int offset, final int length
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		final Random r = XMath.random();
		for(int i = offset - d, j; i != endIndex;)
		{
			final int t = data[i += d];
			data[i] = data[j = r.nextInt(i)];
			data[j] = t;
		}
	}

	public static final int[] toReversed(final int[] array, final int size)
	{
		final int[] rArray = new int[size];
		for(int i = 0, r = size; i < size; i++)
		{
			rArray[--r] = array[i];
		}
		return rArray;
	}

	public static final boolean rngHasUniqueValues(
		final int[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return true;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			final int element = data[i += d];
			for(int j = i; j != endIndex;)
			{
				if(data[j += d] == element)
				{
					return false;
				}
			}
		}
		return true;
	}

	public static final boolean containsSearched(
		final int[]         data     ,
		final int           size     ,
		final _intPredicate predicate
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				return true;
			}
		}
		
		return false;
	}

	public static final boolean appliesAll(
		final int[]         data     ,
		final int           size     ,
		final _intPredicate predicate
	)
	{
		if(size == 0)
		{
			// must check for the special case of no entries (predicate cannot apply).
			return false;
		}
		
		for(int i = 0; i < size; i++)
		{
			if(!predicate.test(data[i]))
			{
				return false;
			}
		}
		
		return true;
	}

	public static final boolean rngAppliesAll(
		final int[]         data     ,
		final int           size     ,
		final int           offset   ,
		final int           length   ,
		final _intPredicate predicate
	)
	{
		final int d; // bi - directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		
		final int endIndex = offset + length - d;
		for(int i = offset - d; i != endIndex;)
		{
			if(!predicate.test(data[i += d]))
			{
				return false;
			}
		}
		
		return true;
	}

	public static final int[] rngToArray(
		final int[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
		}
		if(length == 0)
		{
			return new int[0];
		}
		else if(length > 0)
		{
			final int[] array = new int[length];
			System.arraycopy(data, offset, array, 0, length);
			return array;
		}

		// reverse iteration direction
		final int boundIndex;
		if((boundIndex = offset + length) < -1)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}

		final int[] array = new int[-length];
		for(int i = offset, j = 0; i > boundIndex; i--)
		{
			array[j++] = data[i];
		}
		return array;
	}

	public static final int[] rngToArray(
		final int[] data,
		final int size,
		final int offset,
		final int length,
		      int[] a
	)
	{
		if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
		}

		// length 0 special case with marker element null at index 0 ("after" the last element)
		if(length == 0)
		{
			if(a.length == 0)
			{
				return new int[0]; // length-one array with 0
			}
			return a;
		}

		if(length > 0)
		{
			if(a.length < length)
			{
				a = new int[length];
			}
			// convenient and more performant case: increasing iteration direction, arraycopy can be used
			System.arraycopy(data, offset, a, 0, length);
		}
		else
		{
			// reverse iteration direction
			final int boundIndex;
			if((boundIndex = offset + length) < -1)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			if(a.length < -length)
			{
				a = new int[-length];
			}

			for(int i = offset, j = 0; i > boundIndex; i--)
			{
				a[j++] = data[i];
			}
		}

		// marker element null
		if(a.length > size)
		{
			a[size] = 0;
		}
		return a;
	}


	public static int arrayHashCode(final int[] data, final int size)
	{
		int hashCode = 1;
		for(int i = 0; i < size; i++)
		{
			// CHECKSTYLE.OFF: MagicNumber: copied from JDK hashcode methods
			hashCode = 31 * hashCode + data[i];
			// CHECKSTYLE.ON: MagicNumber
		}
		return hashCode;
	}

	// CHECKSTYLE.ON: FinalParameter

}
