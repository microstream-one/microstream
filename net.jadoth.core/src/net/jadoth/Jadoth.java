/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.exceptions.NumberRangeException;
import net.jadoth.functional.BiProcedure;
import net.jadoth.util.JadothExceptions;
import net.jadoth.util.aspects.LockedAspectWrapper;
import net.jadoth.util.branching.AbstractBranchingThrow;
import net.jadoth.util.branching.ThrowBreak;


/**
 * This is a central framework util class containing all the framework's util methods. This approach is made to sustain
 * ease of use in distinction to the countless ambiguous "package.path.util.Util" classes of various other frameworks.
 * <p>
 * See this class as an extension to the Java language that enhances the native Java synatax by constructs like<br>
 * <code>list(T...)</code>,<br>
 * <code>now()</code>,<br>
 * <code>isEmpty(CharSequence)</code>,<br>
 * <code>coalesce(T,T)</code>,<br>
 * <code>notNull(T)</code>,<br>
 * etc.
 * <p>
 * The extension can be naturally used if this class is added to the IDE's static member favorites.
 */
public final class Jadoth
{
	public static final long INTEGER_UPPER_BOUND = Integer.MAX_VALUE + 1L;

	// let's hope this changes at some point in the future
	public static final long ARRAY_LENGTH_BOUND    = INTEGER_UPPER_BOUND;

	public static final int to_int(final long value) throws NumberRangeException
	{
		// " >= " and "<" proved to be faster than their complements, probably due to simple sign checking
		if(value >= INTEGER_UPPER_BOUND || value < Integer.MIN_VALUE)
		{
			throw new NumberRangeException();
		}
		return (int)value;
	}



	///////////////////////////////////////////////////////////////////////////
	// Collection Factory Methods //
	///////////////////////////////

	/**
	 * Transiently ensures the passed object to be not null by either returning it in case it is not {@code null}
	 * or throwing a {@link NullPointerException} otherwise.
	 * <p>
	 * Really, no idea why java.util.Objects.notNull got renamed to requireNotNull after some odd objection from some
	 * guy that the name would be misleading. Of course "notNull" means "the thing you pass has to be not null,
	 * otherwise you'll get an exception". What else could the meaning of a transient method named "notNull" be?
	 * If "requireNotNull" is needed to express this behavior, than what would "notNull" alone mean?<br>
	 * In the end, "requireNotNull" is just additional 100% clutter, hence not usable and is replaced by
	 * this, still properly named "notNull" method.
	 *
	 * @param <T> the type of the object to be ensured to be not {@code null}.
	 * @param object the object to be ensured to be not {@code null}.
	 * @return the passed object, guaranteed to be not {@code null}.
	 * @throws NullPointerException if {@code null} was passed.
	 */
	public static final <T> T notNull(final T object) throws NullPointerException
	{
		if(object == null)
		{
			// removing this method's stack trace entry is kind of a hack. On the other hand, it's not.
			throw JadothExceptions.cutStacktraceByOne(new NullPointerException());
		}
		return object;
	}
	
	/**
	 * This method is a complete dummy, simply serving as a semantical counterpart to {@link #notNull(Object)}.<br>
	 * The use is small, but still there:<br>
	 * - the sourcecode is easier to read if the same structure is used next to a {@link #notNull(Object)} call
	 *   instead of missing method calls and comments (like "may be null" or "optional").
	 * - the IDE can search for all occurances of this method, listing all places where something may be null.
	 * 
	 * @param object the passed reference.
	 * @return the passed reference without doing ANYTHING else.
	 */
	public static final <T> T mayNull(final T object)
	{
		return object;
	}

	public static final <T> T coalesce(final T firstElement, final T secondElement)
	{
		return firstElement == null ? secondElement : firstElement;
	}

	@SafeVarargs
	public static final <T> T coalesce(final T... elements)
	{
		for(int i = 0; i < elements.length; i++)
		{
			// spare foreach's unnecessary variable assignment on each check
			if(elements[i] != null)
			{
				return elements[i];
			}
		}
		return null;
	}

	public static final boolean equal(final Object o1, final Object o2)
	{
		// leave identity comparison to equals() implementation as this method should mostly be called on value types
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	/**
	 * {@link AbstractBranchingThrow} to indicate the abort of a loop or procedure, with a negative or unknown result.
	 */
	public static final transient ThrowBreak BREAK = new ThrowBreak();

	/**
	 * Generic mutex aspect via proxy instance. With implicit (hidden) mutex object.
	 *
	 * @param <T>
	 * @param subject
	 * @return
	 */
	public static final <T> T mutex(final T subject)
	{
		return LockedAspectWrapper.wrapLocked(subject);
	}

	/**
	 * Generic mutex aspect via proxy instance. With explicit mutex object.
	 *
	 * @param <T>
	 * @param subject
	 * @param mutex
	 * @return
	 */
	public static final <T> T mutex(final T subject, final Object mutex)
	{
		return LockedAspectWrapper.wrapLocked(subject, mutex);
	}

	public static final Integer[] box(final int[] _intArray)
	{
		if(_intArray == null)
		{
			return null;
		}

		final Integer[] returnArray = new Integer[_intArray.length];
		for(int i = 0, length = returnArray.length; i < length; i++)
		{
			returnArray[i] = _intArray[i];
		}
		return returnArray;
	}

	public static final double unbox(final Double d)
	{
		return d == null ? 0.0D : d.doubleValue();
	}

	public static final double unbox(final Double d, final double nullSubstitute)
	{
		return d == null ? nullSubstitute : d.doubleValue();
	}

	public static final int[] unbox(final Integer[] intArray)
	{
		return Jadoth.unbox(intArray, 0);
	}

	public static final int[] unbox(final Integer[] intArray, final int nullReplacement)
	{
		if(intArray == null)
		{
			return null;
		}

		final int[] returnArray = new int[intArray.length];
		for(int i = 0, length = returnArray.length; i < length; i++)
		{
			final Integer objI = intArray[i];
			returnArray[i] = objI == null ? nullReplacement : objI.intValue();
		}
		return returnArray;
	}

	public static final int[] unbox(final XGettingCollection<Integer> ints)
	{
		return Jadoth.unbox(ints, 0);
	}

	public static final int[] unbox(final XGettingCollection<Integer> ints, final int nullReplacement)
	{
		if(ints == null)
		{
			return null;
		}

		final int[] returnArray = new int[ints.intSize()];

		int i = 0;
		for(final Integer e : ints)
		{
			returnArray[i++] = e == null ? nullReplacement : e.intValue();
		}

		return returnArray;
	}

	public static double[][] unbox(final Double[][] matrix)
	{
		if(matrix == null)
		{
			return null; // correct array unboxing, no problem here.
		}
		final int rowCount;
		if((rowCount = matrix.length) == 0)
		{
			return new double[0][]; // unbox empty matrix, column count is not applicable here
		}

		final int colCount = matrix[0].length;
		final double[][] unboxed = new double[rowCount][colCount];
		for(int r = rowCount; r-- > 0;)
		{
			for(int c = colCount; c-- > 0;)
			{
				unboxed[r][c] = matrix[r][c] == null ? 0.0D : matrix[r][c].doubleValue();
			}
		}
		return unboxed;
	}

	public static double[][] unbox(final Double[][] matrix, final double nullSubstitute)
	{
		if(matrix == null)
		{
			return null; // correct array unboxing, no problem here.
		}
		final int rowCount;
		if((rowCount = matrix.length) == 0)
		{
			return new double[0][]; // unbox empty matrix, column count is not applicable here
		}

		final int colCount = matrix[0].length;
		final double[][] unboxed = new double[rowCount][colCount];
		for(int r = rowCount; r-- > 0;)
		{
			for(int c = colCount; c-- > 0;)
			{
				unboxed[r][c] = matrix[r][c] == null ? nullSubstitute : matrix[r][c].doubleValue();
			}
		}
		return unboxed;
	}

	public static final Long asLong(final Number value)
	{
		return value == null
			? null
			: value instanceof Long
				? (Long)value
				: Long.valueOf(value.longValue())
		;
	}


	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really true" (not false and not unknown).
	 *
	 * @param b a <code>Boolean</code> object.<br>
	 * @return <tt>false</tt> if <code>b</code> is {@code null} or <tt>false</tt>
	 */
	public static final boolean isTrue(final Boolean b)
	{
		return b == null ? false : b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really false" (not true and not unknown).
	 *
	 * @param b a <code>Boolean</code> object.
	 * @return <tt>false</tt> if <code>b</code> is {@code null} or {@code true}, otherwise {@code true}
	 */
	public static final boolean isFalse(final Boolean b)
	{
		return b == null ? false : !b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "unknown" (not true and not false).
	 *
	 * @param b a <code>Boolean</code> object.
	 * @return <tt>false</tt> if <code>b</code> is {@code null}, otherwise {@code true}
	 */
	public static final boolean isNull(final Boolean b)
	{
		return b == null;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really not true" (either false or unknown).
	 *
	 * @param b a <code>Boolean</code> object.
	 * @return {@code true} if <code>b</code> is {@code null} or <tt>false</tt>, otherwise <tt>false</tt>
	 */
	public static final boolean isNotTrue(final Boolean b)
	{
		return b == null ? true : !b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really not false" (either true or unknown).
	 *
	 * @param b a <code>Boolean</code> object.
	 * @return {@code true} if <code>b</code> is {@code null} or {@code true}, otherwise <tt>false</tt>
	 */
	public static final boolean isNotFalse(final Boolean b)
	{
		return b == null ? true : b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "known" (either true or false).
	 *
	 * @param b a <code>Boolean</code> object.
	 * @return {@code true} if <code>b</code> is not {@code null}, otherwise <tt>false</tt>
	 */
	public static final boolean isNotNull(final Boolean b)
	{
		return b != null;
	}

	public static void closeSilent(final Closeable closable)
	{
		if(closable == null)
		{
			return;
		}
		try
		{
			closable.close();
		}
		catch(final Exception t)
		{
			// sshhh, silence!
		}
	}

	public static void closeSilent(final AutoCloseable closable)
	{
		if(closable == null)
		{
			return;
		}
		try
		{
			closable.close();
		}
		catch(final Exception t)
		{
			// sshhh, silence!
		}
	}

	public static void closeNonNull(final Closeable c) throws IOException
	{
		if(c == null)
		{
			return;
		}
		c.close();
	}

	public static <E, C extends Consumer<? super E>> C executeOptional(final E optionalInstance, final C logic)
	{
		if(optionalInstance != null)
		{
			logic.accept(optionalInstance);
		}
		return logic;
	}

	public static <E, A> A aggregateOver(
		final Iterable<? extends E> iterable  ,
		final A                     aggregator,
		final BiProcedure<E, A>     logic
	)
	{
		for(final E e : iterable)
		{
			logic.accept(e, aggregator);
		}
		return aggregator;
	}

	public static <E, A> A aggregateOver(
		final E[]                   elements  ,
		final A                     aggregator,
		final BiProcedure<E, A>     logic
	)
	{
		for(final E e : elements)
		{
			logic.accept(e, aggregator);
		}
		return aggregator;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private Jadoth()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
