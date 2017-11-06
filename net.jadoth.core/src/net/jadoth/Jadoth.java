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
import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.jadoth.collections.DownwrapList;
import net.jadoth.collections.ListView;
import net.jadoth.collections.interfaces.Sized;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XList;
import net.jadoth.exceptions.ArrayCapacityException;
import net.jadoth.exceptions.NumberRangeException;
import net.jadoth.functional.BiProcedure;
import net.jadoth.reference.LinkReference;
import net.jadoth.util.Flag;
import net.jadoth.util.JadothExceptions;
import net.jadoth.util.KeyValue;
import net.jadoth.util._longKeyValue;
import net.jadoth.util.aspects.AspectWrapper;
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
	public static boolean[] booleans(final boolean... elements)
	{
		return elements;
	}

	public static byte[] bytes(final byte... elements)
	{
		return elements;
	}

	public static short[] shorts(final short... elements)
	{
		return elements;
	}

	public static int[] ints(final int... elements)
	{
		return elements;
	}

	public static long[] longs(final long... elements)
	{
		return elements;
	}

	public static float[] floats(final float... elements)
	{
		return elements;
	}

	public static double[] doubles(final double... elements)
	{
		return elements;
	}

	public static char[] chars(final char... elements)
	{
		return elements;
	}

	@SafeVarargs
	public static <T> T[] array(final T... elements)
	{
		return elements;
	}

	public static Object[] objects(final Object... elements)
	{
		return elements;
	}

	public static String[] strings(final String... elements)
	{
		return elements;
	}

	private static final long INTEGER_UPPER_BOUND = Integer.MAX_VALUE + 1L;

	// let's hope this changes at some point in the future
	private static final long ARRAY_LENGTH_BOUND    = INTEGER_UPPER_BOUND;

	// hopefully, this can be removed at some point in the future ... :(
	/**
	 * Central validation point for Java's current technical limitation of max int as max array capacity.
	 * Note that because of dependencies of many types to arrays (e.g. toArray() methods, etc.), this limitation
	 * indirectly affects many other types, for example String, collections, ByteBuffers (which is extremely painful).
	 *
	 * It can be read that there are plans to overcome this outdated insufficiency for Java 9 or 10,
	 * so it's best to have one central point of validation that can later easily be refactored out.
	 *
	 * @param capacity the desired (array-dependent) capacity which may effectively be not greater than
	 * {@link Integer}.MAX_VALUE.
	 * @return the safely downcasted capacity as an int value.
	 * @throws ArrayCapacityException if the passed capacity is greater than {@link Integer}.MAX_VALUE
	 */
	public static final int checkArrayRange(final long capacity) throws ArrayCapacityException
	{
		// " >= " proved to be faster in tests than ">" (probably due to simple sign checking)
		if(capacity >= ARRAY_LENGTH_BOUND)
		{
			throw new ArrayCapacityException(capacity);
		}
		return (int)capacity;
	}

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
	 * If "requireNotNull" is needed to express this behaviour, than what would "notNull" alone mean?<br>
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

	public static final <S extends Sized> S notEmpty(final S sized)
	{
		if(sized.isEmpty())
		{
			throw JadothExceptions.cutStacktraceByOne(new IllegalArgumentException());
		}
		return sized;
	}

	public static final <E> E[] notEmpty(final E[] array)
	{
		if(array.length == 0)
		{
			throw JadothExceptions.cutStacktraceByOne(new IllegalArgumentException());
		}
		return array;
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

	public static <K, V> KeyValue<K, V> keyValue(final K key, final V value)
	{
		return new KeyValue.Implementation<>(key, value);
	}
	
	public static <T, K, V> KeyValue<K, V> toKeyValue(final T instance, final Function<? super T, KeyValue<K, V>> mapper)
	{
		return mapper.apply(instance);
	}

	public static _longKeyValue _longKeyValue(final long key, final long value)
	{
		return new _longKeyValue.Implementation(key, value);
	}

	@SafeVarargs
	public static <T> LinkReference<T> chain(final T... objects)
	{
		if(objects == null)
		{
			return null;
		}

		final LinkReference<T> chain = new LinkReference.Implementation<>(objects[0]);

		if(objects.length > 1)
		{
			LinkReference<T> loopRef = chain;
			for(int i = 1; i < objects.length; i++)
			{
				loopRef = loopRef.link(objects[i]);
			}
		}
		return chain;
	}

	public static final boolean equal(final Object o1, final Object o2)
	{
		// leave identity comparison to equals() implementation as this method should mostly be called on value types
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	/**
	 * {@link AbstractBranchingThrow} to indicate the abort of a loop or procedure, with a negative or unknown result.
	 */
	public static final transient ThrowBreak    BREAK    = new ThrowBreak();

	/**
	 * {@link BranchingThrow} to indicate the abort of a loop or procedure, with a positive or as far as present result.
	 */
//	public static final ThrowReturn   BREAK   = new ThrowReturn();

	/**
	 * {@link BranchingThrow} to indicate the skipping of the remaining (secondary) actions of a loop's current step.
	 */
//	public static final ThrowContinue CONTINUE = new ThrowContinue();

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

	/**
	 * Reduces the type of the passed instance to a super type interface. It can best be seen as a reflective-wise
	 * "hard" upcast wrapper (hence the name).
	 * <p>
	 * This is effectively a generic decorater implementation realized via dynamic proxy instantiation.
	 * <p>
	 * A very good example is a read-only access on a mutable collection instance:<br>
	 * The type {@link XList} extends the type {@link XGettingList} (and combines it with other aspects like
	 * adding, removing, etc. to create a full scale general purpose list type).<br>
	 * In certain situations, it is necessary that certain code (e.g. an external framework) can only read
	 * but never modify the collection's content. Just casting the {@link XList} instance won't suffice here,
	 * as the receiving code could still do an {@code instanceof } check and downcast the passed instance.<br>
	 * What is really needed is an actual decorator instance, wrapping the general purpose type instance and
	 * relaying only the reading procedures.<br>
	 * For this particular example, there's an explicit decorator type, {@link ListView}.<br>
	 * For other situations, where there is no explicit decorator type (or not yet), this method provides
	 * a solution to create a generic decorator instance.
	 * <p>
	 * Note that the genericity comes at the price of performance, as it purely consists of reflection calls.
	 *
	 * @param <T>
	 * @param <S>
	 * @param subject the subject to be upwrapped to the given upwrap type.
	 * @param upwrapType the interface super type the passed subject shall be upwrapped to.
	 * @return a reflection wise upwrapped instance of type {@literal T} of the passed subject.
	 * @throws IllegalArgumentException if the passed unwrap type is not an interface.
	 */
	@SuppressWarnings("unchecked")
	public static final <T, S extends T> T upwrap(final S subject, final Class<T> upwrapType)
	{
		if(!upwrapType.isInterface())
		{
			throw new IllegalArgumentException("upwrap type is not an interface");
		}
		return (T)java.lang.reflect.Proxy.newProxyInstance(
			subject.getClass().getClassLoader(),
			new Class<?>[]{upwrapType},
			new AspectWrapper<T>(subject)
		);
	}

	/**
	 * Magically causes an instance of type {@code T} to be usable as if it was of type {@code S extends T}.
	 * It can best be seen as a reflective-wise "hard" downcast wrapper (hence the name).
	 * <p>
	 * <b>Caution: This technique is pure sin!</b>
	 * <p>
	 * It is the type-wise complementary to {@link #downwrap(Object, Class)} and a generic decorator version of
	 * explicit downwrapping implementations like {@link DownwrapList}. It's documentation applies to the mechanics
	 * of this method as well: it has to be seen as a workaround tool for special situations (e.g. compatibility to a
	 * foreign codebase API). Relying on it by design is nothing but bad and broken.
	 * <p>
	 * If this intentionally scarce documentation was not enough, do not use this method!
	 *
	 * @param <T>
	 * @param <S>
	 * @param subject the subject to be downwrapped to the given downwrap type.
	 * @param downwrapType the interface sub type the passed subject shall be downwrapped to.
	 * @return a reflection wise downwrapped instance of type {@literal S} of the passed subject.
	 */
	@SuppressWarnings("unchecked")
	public static final <T, S extends T> S downwrap(final T subject, final Class<S> downwrapType)
	{
		if(!downwrapType.isInterface())
		{
			throw new IllegalArgumentException("downwrap type is not an interface");
		}
		return (S)java.lang.reflect.Proxy.newProxyInstance(
			subject.getClass().getClassLoader(),
			new Class<?>[]{downwrapType},
			new AspectWrapper<>(subject)
		);
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

	public static final Long toLong(final Number value)
	{
		return value == null
			? null
			: value instanceof Long
				? (Long)value
				: Long.valueOf(value.longValue())
		;
	}


	public static final <T extends Throwable> T removeHighestStrackTraceElement(final T throwable)
	{
		final StackTraceElement[] stackTrace = throwable.getStackTrace();
		final StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length - 1];
		System.arraycopy(stackTrace, 1, newStackTrace, 0, newStackTrace.length);
		throwable.setStackTrace(newStackTrace);
		return throwable;
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

	/**
	 * Returns {@code value.toString()} if the passed value is not {@literal null}, otherwise {@literal null}.
	 * <p>
	 * Note that this is a different behaviour than {@link String#valueOf(Object)} has, as the latter returns
	 * the string {@code "null"} for a passed {@literal null} reference.
	 * <p>
	 * The behaviour of this method is needed for example for converting values in a generic data structure
	 * (e.g. a Object[] array) to string values but have {@literal null} values
	 * (information about missing values) maintained.
	 *
	 * @param value the value to be projected to its string representation if not null.
	 * @return a string representation of an actual passed value or a transient {@literal null}.
	 *
	 * @see Object#toString()
	 * @see String#valueOf(Object)
	 */
	public static String valueString(final Object value)
	{
		return value == null ? null : value.toString();
	}

	public static String systemString(final Object object)
	{
		return object == null
			? null
			: object.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(object))
		;
	}

	public static String nonNullString(final Object object)
	{
		return object == null ? "" : object.toString();
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


	public static Flag Flag(final boolean state)
	{
		return Flag.Simple.New(state);
	}

	@SuppressWarnings("unchecked") // safe by method parameter
	public static <T> Class<T> getClass(final T instance)
	{
		return (Class<T>)instance.getClass(); // why oh why?
	}

	@SafeVarargs
	public static <T> T doWith(final T subject, final Consumer<? super T>... actions)
	{
		for(final Consumer<? super T> action : actions)
		{
			action.accept(subject);
		}
		return subject;
	}


	/**
	 * Instantiaties a new array instance that has a compent type defined by the passed {@link Class}
	 * {@literal componentType},
	 * a length as defined by the passed {@literal length} value and that is filled in order with elements supplied
	 * by the passed {@link Supplier} instance.
	 *
	 * @param length        the length of the array to be created.
	 * @param componentType the component type of the array to be created.
	 * @param supplier      the function supplying the instances that make up the array's elements.
	 * @return a new array instance filled with elements provided by the passed supplier.
	 * @see #Array(int, Supplier)
	 */
	public static <E> E[] Array(final int length, final Class<E> componentType, final Supplier<E> supplier)
	{
		@SuppressWarnings("unchecked")
		final E[] array = (E[])Array.newInstance(componentType, length);

		for(int i = 0; i < array.length; i++)
		{
			array[i] = supplier.get();
		}

		return array;
	}

	/**
	 * Instantiaties a new array instance that has a component type defined by the method's type parameter {@literal E},
	 * a length as defined by the passed {@literal length} value and that is filled in order with elements supplied
	 * by the passed {@link Supplier} instance.
	 * <p>
	 *
	 * <p>
	 * Note this is a convenience method with the following restrictions:
	 * <ul>
	 * <li>The array's precise component type is the type of the first element provided by the passed supplier.</li>
	 * <li>If the passed length is 0, there will still be requested one element from the supplier to determine the
	 * array's runtime component type. In that case, zhe provided instance will be discarded afterwards, however.</li>
	 * <li>If the supplier returns {@literal null} on the first call, a {@link NullPointerException}
	 * will the thrown.</li>
	 * </ul>
	 * To avoid these restrictions, see the slightly more verbose method {@link #Array(int, Class, Supplier)}.
	 *
	 * @param length   the length of the array to be created.
	 * @param supplier the function supplying the instances that make up the array's elements.
	 * @return a new array instance filled with elements provided by the passed supplier.
	 * @throws NullPointerException if the first instance returned by the supplier is {@literal null}.
	 * @see #Array(int, Class, Supplier)
	 */
	public static <E> E[] Array(final int length, final Supplier<E> supplier)
	{
		final E first = supplier.get();

		@SuppressWarnings("unchecked")
		final E[] array = (E[])Array.newInstance(first.getClass(), length);

		if(length > 0)
		{
			array[0] = first;
			for(int i = 1; i < array.length; i++)
			{
				array[i] = supplier.get();
			}
		}

		return array;
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



	private Jadoth()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

/* code header replacements:
\t\t// override methods //\R\t\t/////////////////////
\t\t// methods //\R\t\t////////////
*/