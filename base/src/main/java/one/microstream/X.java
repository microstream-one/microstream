package one.microstream;

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

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import one.microstream.branching.AbstractBranchingThrow;
import one.microstream.branching.ThrowBreak;
import one.microstream.chars.VarString;
import one.microstream.collections.ArrayView;
import one.microstream.collections.BulkList;
import one.microstream.collections.ConstHashEnum;
import one.microstream.collections.ConstList;
import one.microstream.collections.Constant;
import one.microstream.collections.Empty;
import one.microstream.collections.EmptyTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.LimitList;
import one.microstream.collections.Singleton;
import one.microstream.collections.SynchCollection;
import one.microstream.collections.SynchList;
import one.microstream.collections.SynchSet;
import one.microstream.collections.interfaces.Sized;
import one.microstream.collections.old.AbstractBridgeXList;
import one.microstream.collections.old.AbstractBridgeXSet;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XReference;
import one.microstream.collections.types.XSet;
import one.microstream.concurrency.ThreadSafe;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.exceptions.WrapperRuntimeException;
import one.microstream.functional.BooleanTerm;
import one.microstream.functional._intIndexedSupplier;
import one.microstream.functional._intProcedure;
import one.microstream.math.XMath;
import one.microstream.typing.KeyValue;
import one.microstream.typing._longKeyValue;
import one.microstream.util.UtilStackTrace;

/**
 * Central class for general utility methods regarding collections, arrays and some basic general functionality that is
 * missing in Java like {@link #notNull(Object)} or {@link #ints(int...)}.<br>
 * <br>
 * This class uses the following sound extension of the java naming conventions:<br>
 * Static methods that resemble a constructor, begin with an upper case letter. This is consistent with existing naming
 * rules: method names begin with a lower case letter EXCEPT for constructor methods. This extension does nothing
 * more than applying the same exception to constructor-like static methods. Resembling a constructor means:
 * 1.) Indicating by name that a new instance is created. 2.) Always returning a new instance, without exception.
 * No caching, no casting. For example: {@link #empty()} or {@link #asX(List)} are NOT constructor-like methods
 * because they do not (always) create new instances.
 *
 */
public final class X
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final Empty<?> EMPTY = new Empty<>();

	private static final EmptyTable<?, ?> EMPTY_TABLE = new EmptyTable<>();
	
	/**
	 * {@link AbstractBranchingThrow} to indicate the abort of a loop or procedure, with a negative or unknown result.
	 */
	private static final transient ThrowBreak BREAK = new ThrowBreak();
		
	private static final long INTEGER_RANGE_BOUND = Integer.MAX_VALUE + 1L;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	public static final <T> Empty<T> empty()
	{
		return (Empty<T>)EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static final <K, V> EmptyTable<K, V> emptyTable()
	{
		return (EmptyTable<K, V>)EMPTY_TABLE;
	}
	
	
	
	public static ThrowBreak BREAK()
	{
		/*
		 * "break()" is not possible, of course.
		 * "Break" collides with the constructor-method naming.
		 * "breaK" and the like look something between weird and ugly and might cause confusion when typing.
		 * So, since it's actually just an encapsulated constant, the most favorable variant is "BREAK()".
		 */
		return BREAK;
	}
	
	
	
	// hopefully, this can be removed at some point in the future ...
	/**
	 * Central validation point for Java's current technical limitation of max int as max array capacity.
	 * Note that because of dependencies of many types to arrays (e.g. toArray() methods, etc.), this limitation
	 * indirectly affects many other types, for example String, collections, ByteBuffers (which is extremely painful).
	 *
	 * @param capacity the desired (array-dependent) capacity which may effectively be not greater than
	 * {@link Integer}.MAX_VALUE.
	 * @return the safely downcasted capacity as an int value.
	 * @throws ArrayCapacityException if the passed capacity is greater than {@link Integer}.MAX_VALUE
	 */
	public static final int checkArrayRange(final long capacity) throws ArrayCapacityException
	{
		// " >= " proved to be faster in tests than ">" (probably due to simple sign checking)
		if(capacity >= INTEGER_RANGE_BOUND)
		{
			throw new ArrayCapacityException(capacity);
		}
		return (int)capacity;
	}
	
	/**
	 * Transiently ensures the passed object to be not {@code null} by either returning it in case it is
	 * not {@code null} or throwing a {@link NullPointerException} otherwise.
	 * <p>
	 * <i>(Really, no idea why java.util.Objects.notNull got renamed to requireNotNull</i>
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
			throw UtilStackTrace.cutStacktraceByOne(new NullPointerException());
		}
		return object;
	}
	
	/**
	 * This method is a complete dummy, simply serving as a semantic counterpart to {@link #notNull(Object)}.<br>
	 * The use is small, but still there:<br>
	 * - the sourcecode is easier to read if the same structure is used next to a {@link #notNull(Object)} call
	 *   instead of missing method calls and comments (like "may be null" or "optional").
	 * - the IDE can search for all occurrences of this method, listing all places where something may be null.
	 * 
	 * @param <T> the object's type
	 * @param object the passed reference.
	 * @return the passed reference without doing ANYTHING else.
	 */
	public static final <T> T mayNull(final T object)
	{
		return object;
	}
	
	
	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really true" (not false and not unknown).
	 *
	 * @param b a {@code Boolean} object.<br>
	 * @return <code>false</code> if {@code b} is {@code null} or <code>false</code>
	 */
	public static final boolean isTrue(final Boolean b)
	{
		return b == null ? false : b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really false" (not true and not unknown).
	 *
	 * @param b a {@code Boolean} object.
	 * @return <code>false</code> if {@code b} is {@code null} or <code>true</code>, otherwise <code>true</code>
	 */
	public static final boolean isFalse(final Boolean b)
	{
		return b == null ? false : !b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really not true" (either false or unknown).
	 *
	 * @param b a {@code Boolean} object.
	 * @return <code>true</code> if {@code b} is {@code null} or <code>false</code>, otherwise <code>false</code>
	 */
	public static final boolean isNotTrue(final Boolean b)
	{
		return b == null ? true : !b;
	}

	/**
	 * Helper method to project ternary values to binary logic.<br>
	 * Useful for checking "really not false" (either true or unknown).
	 *
	 * @param b a {@code Boolean} object.
	 * @return <code>true</code> if {@code b} is {@code null} or <code>true</code>, otherwise <code>false</code>
	 */
	public static final boolean isNotFalse(final Boolean b)
	{
		return b == null ? true : b;
	}

	
	
	public static final boolean isNull(final Object reference)
	{
		return reference == null;
	}
	
	public static final boolean isNotNull(final Object reference)
	{
		return reference != null;
	}

	
	public static final <T> T coalesce(final T firstElement, final T secondElement)
	{
		return firstElement == null
			? secondElement
			: firstElement
		;
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

	
	
	public static final <T> boolean equal(final T o1, final T o2)
	{
		// leave identity comparison to equals() implementation as this method should mostly be called on value types
		return o1 == null
			? o2 == null
			: o1.equals(o2)
		;
	}
	
	
	
	public static final byte unbox(final Byte d)
	{
		return d == null
			? 0
			: d.byteValue()
		;
	}

	public static final byte unbox(final Byte d, final byte nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.byteValue()
		;
	}
	
	public static final boolean unbox(final Boolean d)
	{
		return d != null && d.booleanValue();
	}

	public static final boolean unbox(final Boolean d, final boolean nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.booleanValue()
		;
	}
	
	public static final short unbox(final Short d)
	{
		return d == null
			? 0
			: d.shortValue()
		;
	}

	public static final short unbox(final Short d, final short nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.shortValue()
		;
	}
	
	public static final char unbox(final Character d)
	{
		return d == null
			? (char) 0
			: d.charValue()
		;
	}
	
	public static final int unbox(final Integer d)
	{
		return d == null
			? 0
			: d.intValue()
		;
	}

	public static final int unbox(final Integer d, final int nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.intValue()
		;
	}

	public static final char unbox(final Character d, final char nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.charValue()
		;
	}
	
	public static final float unbox(final Float d)
	{
		return d == null
			? 0f
			: d.floatValue()
		;
	}

	public static final float unbox(final Float d, final float nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.floatValue()
		;
	}
	
	public static final long unbox(final Long d)
	{
		return d == null
			? 0L
			: d.longValue()
		;
	}

	public static final long unbox(final Long d, final long nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.longValue()
		;
	}
	
	public static final double unbox(final Double d)
	{
		return d == null
			? 0.0D
			: d.doubleValue()
		;
	}

	public static final double unbox(final Double d, final double nullSubstitute)
	{
		return d == null
			? nullSubstitute
			: d.doubleValue()
		;
	}
	
	
	
	public static final Byte[] box(final byte... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Byte[] result = new Byte[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Boolean[] box(final boolean... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Boolean[] result = new Boolean[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Short[] box(final short... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Short[] result = new Short[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Integer[] box(final int... values)
	{
		if(values == null)
		{
			return null;
		}
	
		final int length = values.length;
		final Integer[] result = new Integer[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Character[] box(final char... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Character[] result = new Character[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Float[] box(final float... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Float[] result = new Float[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Long[] box(final long... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Long[] result = new Long[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final Double[] box(final double... values)
	{
		if(values == null)
		{
			return null;
		}

		final int length = values.length;
		final Double[] result = new Double[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final int[] unbox(final Integer[] array)
	{
		return unbox(array, 0);
	}

	public static final int[] unbox(final Integer[] array, final int nullReplacement)
	{
		if(array == null)
		{
			return null;
		}

		final int[] result = new int[array.length];
		for(int i = 0, length = result.length; i < length; i++)
		{
			final Integer value = array[i];
			result[i] = value == null
				? nullReplacement
				: value.intValue()
			;
		}
		
		return result;
	}
	
	public static final long[] unbox(final Long[] array)
	{
		return unbox(array, 0);
	}

	public static final long[] unbox(final Long[] array, final long nullReplacement)
	{
		if(array == null)
		{
			return null;
		}

		final long[] result = new long[array.length];
		for(int i = 0, length = result.length; i < length; i++)
		{
			final Long value = array[i];
			result[i] = value == null
				? nullReplacement
				: value.longValue()
			;
		}
		
		return result;
	}

	public static final int[] unbox(final XGettingCollection<Integer> ints)
	{
		return unbox(ints, 0);
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

	
	
	public static boolean[] booleans(final boolean... elements)
	{
		return elements;
	}

	public static byte[] bytes(final byte... elements)
	{
		return elements;
	}
	
	public static byte[] toBytes(final int value)
	{
		final byte[] bytes = {
			(byte)(value >>> 24 & 0xFF),
			(byte)(value >>> 16 & 0xFF),
			(byte)(value >>>  8 & 0xFF),
			(byte)(value        & 0xFF)
		};
		
		return bytes;
	}
	
	public static byte[] toBytes(final long value)
	{
		final byte[] bytes = {
			(byte)(value >>> 56 & 0xFF),
			(byte)(value >>> 48 & 0xFF),
			(byte)(value >>> 40 & 0xFF),
			(byte)(value >>> 32 & 0xFF),
			(byte)(value >>> 24 & 0xFF),
			(byte)(value >>> 16 & 0xFF),
			(byte)(value >>>  8 & 0xFF),
			(byte)(value        & 0xFF)
		};
		
		return bytes;
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
	
	/**
	 * Utility method to create a list of integers from 1 to the passed {@code n} value.
	 * Useful for executing a logic via {@link XList#iterate(Consumer)} exactly {@code n} times.
	 * 
	 * @param n the amount of integers
	 * @return a list of integers from 1 to {@code n}
	 */
	public static XList<Integer> times(final int n)
	{
		final LimitList<Integer> integers = LimitList.New(n);
		for(int i = 1; i <= n; i++)
		{
			integers.add(Integer.valueOf(i));
		}
		
		return integers;
	}
	
	/**
	 * Utility method to create a list of integers from {@code firstValue} to {@code lastValue}.
	 * 
	 * @param firstValue the lower limit
	 * @param lastValue the upper limit
	 * @return a list of integers from {@code firstValue} to {@code lastValue}
	 */
	public static XList<Integer> range(final int firstValue, final int lastValue)
	{
		final int low, high, direction;
		if(firstValue <= lastValue)
		{
			low = firstValue;
			high = lastValue;
			direction = 1;
		}
		else
		{
			high = firstValue;
			low  = lastValue ;
			direction = -1;
		}
		
		final LimitList<Integer> integers = LimitList.New(high - low + 1);
		for(int i = firstValue - direction; i != lastValue;)
		{
			integers.add(Integer.valueOf(i += direction));
		}
		
		return integers;
	}
	
	@SafeVarargs
	public static <E> XList<E> List(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return BulkList.New();
		}
		return BulkList.New(elements);
	}
	
	public static <E> XList<E> List(final Iterable<? extends E> elements)
	{
		final BulkList<E> newInstance = BulkList.New();
		elements.forEach(newInstance);
		
		return newInstance;
	}

	@SafeVarargs
	public static <E> ConstList<E> ConstList(final E... elements) throws NullPointerException
	{
		return ConstList.New(elements);
	}
	
	@SafeVarargs
	public static <E> ArrayView<E> ArrayView(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return new ArrayView<>();
		}
		return new ArrayView<>(elements);
	}

	public static <E> Singleton<E> Singleton(final E element)
	{
		return Singleton.New(element);
	}

	public static <E> Constant<E> Constant(final E element)
	{
		return new Constant<>(element);
	}

	@SafeVarargs
	public static <E> HashEnum<E> Enum(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return HashEnum.New();
		}
		return HashEnum.<E>New(elements);
	}
	
	public static <E> HashEnum<E> Enum(final Iterable<? extends E> elements)
	{
		final HashEnum<E> newInstance = HashEnum.New();
		elements.forEach(newInstance);
		
		return newInstance;
	}

	@SafeVarargs
	public static <E> ConstHashEnum<E> ConstEnum(final E... elements) throws NullPointerException
	{
		if(elements == null || elements.length == 0)
		{
			return ConstHashEnum.New();
		}
		return ConstHashEnum.<E>New(elements);
	}

	public static <K, V> HashTable<K, V> Table(final K key, final V value)
	{
		return HashTable.New(KeyValue(key, value));
	}
	
	@SafeVarargs
	public static <K, V> HashTable<K, V> Table(final KeyValue<? extends K, ? extends V>... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return HashTable.New();
		}
		return HashTable.<K, V>New(elements);
	}

	public static <T> XReference<T> Reference(final T object)
	{
		return Singleton.New(object);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Iterable<?> Iterable(final Object array)
	{
		return () -> new Iterator()
		{
			      int position = 0;
			final int length   = Array.getLength(array);
			
			@Override
			public boolean hasNext()
			{
				return this.position < this.length;
			}

			@Override
			public Object next()
			{
				return Array.get(array, this.position++);
			}
		};
	}
	
	
	@SuppressWarnings("unchecked")
	public static final <T> T[] ArrayForElementType(final T sampleInstance, final int length)
	{
		return (T[])Array.newInstance(sampleInstance.getClass(), length);
	}
	
	@SuppressWarnings("unchecked")
	public static <E> E[] ArrayOfSameType(final E[] sampleArray, final int length)
	{
		return (E[])Array.newInstance(sampleArray.getClass().getComponentType(), length);
	}
	
	@SuppressWarnings("unchecked")
	public static final <E> E[] ArrayOfSameType(final E[] sampleArray)
	{
		return (E[])Array.newInstance(sampleArray.getClass().getComponentType(), sampleArray.length);
	}
	
	public static <E> E[] Array(final E element)
	{
		return Array(1, element);
	}
	
	public static <E> E[] Array(final int length, final E element)
	{
		@SuppressWarnings("unchecked")
		final E[] newArray = (E[])Array.newInstance(element.getClass(), length);
		newArray[0] = element;
		
		return newArray;
	}
	
	public static <E> E[] Array(final Class<E> componentType, final E element)
	{
		return Array(componentType, element, 1);
	}
	
	public static <E> E[] Array(final Class<E> componentType, final E element, final int length)
	{
		final E[] newArray = Array(componentType, length);
		newArray[0] = element;
		
		return newArray;
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] Array(final Class<E> componentType, final int length)
	{
		return (E[])Array.newInstance(componentType, length);
	}
	
	/**
	 * Static workaround for the Java typing deficiency that it is not possible to
	 * define {@code public <T super E> T[] toArray(Class<T> type)}.
	 *
	 * @param <E> the collection's element type.
	 * @param <T> the component type used to create the array, possibly a super type of {@code E}
	 * @param collection the collection whose elements shall be copied to the array.
	 * @param type the {@link Class} representing type {@code T} at runtime.
	 * @return a new array instance of component type {@code T} containing all elements of the passed collection.
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends T> T[] Array(final Class<T> type, final XGettingCollection<E> collection)
	{
		// (02.11.2011 TM)NOTE: pretty hacky typing-wise, but in the end safer regarding concurrency and the like.
		// nifty abuse of array covariance typing bug for return value
		return collection.toArray((Class<E>)type);
	}
	
	/**
	 * Instantiates a new array instance that has a component type defined by the passed {@link Class}
	 * {@literal componentType},
	 * a length as defined by the passed {@literal length} value and that is filled in order with elements supplied
	 * by the passed {@link Supplier} instance.
	 * 
	 * @param <E> the component type
	 * @param length        the length of the array to be created.
	 * @param componentType the component type of the array to be created.
	 * @param supplier      the function supplying the instances that make up the array's elements.
	 * @return a new array instance filled with elements provided by the passed supplier.
	 * @see X#Array(Class, int, Supplier)
	 */
	public static <E> E[] Array(final Class<E> componentType, final int length, final Supplier<E> supplier)
	{
		final E[] array = X.Array(componentType, length);

		for(int i = 0; i < array.length; i++)
		{
			array[i] = supplier.get();
		}

		return array;
	}
	
	public static <E> E[] Array(final Class<E> componentType, final int length, final _intIndexedSupplier<E> supplier)
	{
		final E[] array = X.Array(componentType, length);

		for(int i = 0; i < array.length; i++)
		{
			array[i] = supplier.get(i);
		}

		return array;
	}
	
	public static <T> WeakReference<T> WeakReference(final T referent)
	{
		return new WeakReference<>(referent);
	}
	
	@SuppressWarnings("unchecked") // damn type erasure
	public static <T> WeakReference<T>[] WeakReferences(final int length)
	{
		return new WeakReference[length];
	}
	
	@SafeVarargs
	public static <T> WeakReference<T>[] WeakReferences(final T... referents)
	{
		if(referents == null)
		{
			return null;
		}
		
		final WeakReference<T>[] weakReferences = WeakReferences(referents.length);
		for(int i = 0; i < referents.length; i++)
		{
			weakReferences[i] = WeakReference(referents[i]);
		}
		
		return weakReferences;
	}
	
	/**
	 * Removes all <code>null</code> entries and entries with <code>null</code>-referents.
	 * 
	 * @param <T> the component type
	 * @param array the array to consolidate
	 * @return the consolidated array with non-null values
	 */
	public static <T> WeakReference<T>[] consolidateWeakReferences(final WeakReference<T>[] array)
	{
		int liveEntryCount = 0;
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == null)
			{
				continue;
			}
			if(array[i].get() == null)
			{
				array[i] = null;
				continue;
			}
			liveEntryCount++;
		}
		
		// check for no-op
		if(liveEntryCount == array.length)
		{
			return array;
		}
		
		final WeakReference<T>[] newArray = X.WeakReferences(liveEntryCount);
		for(int i = 0, n = 0; i < array.length; i++)
		{
			if(array[i] != null)
			{
				newArray[n++] = array[i];
			}
		}
		
		return newArray;
	}
	
	public static <E> XList<E> asX(final List<E> oldList)
	{
		if(oldList instanceof AbstractBridgeXList<?>)
		{
			return ((AbstractBridgeXList<E>)oldList).parent();
		}
		else if(oldList instanceof ArrayList<?>)
		{
			throw new one.microstream.meta.NotImplementedYetError();
		}

		throw new UnsupportedOperationException();
		// (19.05.2011 TM)FIXME: generic old list wrapper
	}

	public static <E> XSet<E> asX(final Set<E> oldSet)
	{
		if(oldSet instanceof AbstractBridgeXSet<?>)
		{
			return ((AbstractBridgeXSet<E>)oldSet).parent();
		}

		throw new UnsupportedOperationException();
		// (19.05.2011 TM)FIXME: old set wrapper
	}

	public static <K, V> XMap<K, V> asX(final Map<K, V> oldMap)
	{
		throw new UnsupportedOperationException();
		// (19.05.2011 TM)FIXME: old map wrapper
	}

	
	
	public static boolean hasNoContent(final XGettingCollection<?> collection)
	{
		return collection == null || collection.isEmpty();
	}

	public static final <S extends Sized> S notEmpty(final S sized)
	{
		if(sized.isEmpty())
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
		}
		return sized;
	}

	public static final <E> E[] notEmpty(final E[] array)
	{
		if(array.length == 0)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
		}
		return array;
	}

	public static <T, K, V> KeyValue<K, V> toKeyValue(
		final T                                   instance,
		final Function<? super T, KeyValue<K, V>> mapper
		)
	{
		return mapper.apply(instance);
	}

	public static <K, V> KeyValue<K, V> KeyValue(final K key, final V value)
	{
		return KeyValue.New(key, value);
	}

	public static _longKeyValue _longKeyValue(final long key, final long value)
	{
		return new _longKeyValue.Default(key, value);
	}

	

	public static String toString(final XGettingCollection<?> collection)
	{
		// CHECKSTYLE.OFF: MagicNumber: special case not worth the hassle
		return X.assembleString(VarString.New((int)(collection.size() * 4.0f)), collection).toString();
		// CHECKSTYLE.ON: MagicNumber
	}

	public static VarString assembleString(final VarString vs, final XGettingCollection<?> collection)
	{
		if(collection.isEmpty())
		{
			return vs.add('[', ']');
		}
	
		/*
		 * Intentionally no check for this collection, because it is the author's opinion that there is no sane
		 * case where circular references in collections make sense.
		 * 1.) Properly typed collections and algorithms don't even allow it on a compiler-level
		 * 2.) Any complex special corner case structures, where this MIGHT be reasonable,
		 *     should typewise be segmented into appropriate types, not overly crazy nested collections.
		 *     The non-collection instances would serve as an assembly-block.
		 * 3.) In 99.99% of all cases, such a check would mean a significant performance reduction. Such a cost
		 *     just to cover a crazy case that should never happen is viable.
		 * 4.) Any string chosen to represent that case (like JKD's "(this collection)" can create ambiguities
		 *     for the calling application.
		 *
		 * In other words: A framework should not worsen/ruin its code just to try and compensate bad programming
		 * in a dubious makeshift way. Bad user code creates errors/crashes. It happens all the time. It can't be
		 * the responsibility of a framework to compensate some of them. The cleanest and best thing to do is to
		 * indicate the error (in this case by an overflow error) instead of covering it up.
		 */
	
		vs.append('[');
		collection.iterate(e ->
			vs.add(e).add(',', ' ')
		);
		vs.deleteLast().setLast(']');
	
		return vs;
	}

	/**
	 * Ensures that the returned {@link XList} instance based on the passed list is thread safe to use.<br>
	 * This normally means wrapping the passed list in a {@link SynchList}, making it effectively synchronized.<br>
	 * If the passed list already is thread safe (indicated by the marker interface {@link ThreadSafe}), then the list
	 * itself is returned without further actions. This automatically ensures that a {@link SynchList} is not
	 * redundantly wrapped again in another {@link SynchList}.
	 *
	 * @param <E> the element type.
	 * @param list the {@link XList} instance to be synchronized.
	 * @return a thread safe {@link XList} using the passed list.
	 */
	public static <E> XList<E> synchronize(final XList<E> list)
	{
		// if type of passed list is already thread safe, there's no need to wrap it in a SynchronizedXList
		if(list instanceof ThreadSafe)
		{
			return list;
		}
		// wrap not thread safe list types in a SynchronizedXList
		return new SynchList<>(list);
	}

	/**
	 * Ensures that the returned {@link XSet} instance based on the passed set is thread safe to use.<br>
	 * This normally means wrapping the passed set in a {@link SynchSet}, making it effectively synchronized.<br>
	 * If the passed set already is thread safe (indicated by the marker interface {@link ThreadSafe}), then the set
	 * itself is returned without further actions. This automatically ensures that a {@link SynchSet} is not
	 * redundantly wrapped again in another {@link SynchSet}.
	 *
	 * @param <E> the element type.
	 * @param set the {@link XSet} instance to be synchronized.
	 * @return a thread safe {@link XSet} using the passed set.
	 */
	public static <E> XSet<E> synchronize(final XSet<E> set)
	{
		// if type of passed set is already thread safe, there's no need to wrap it in a SynchronizedXSet
		if(set instanceof ThreadSafe)
		{
			return set;
		}
		// wrap not thread safe set types in a SynchronizedXSet
		return new SynchSet<>(set);
	}

	/**
	 * Ensures that the returned {@link XCollection} instance based on the passed collection is thread safe to use.<br>
	 * This normally means wrapping the passed collection in a {@link SynchCollection}, making it effectively synchronized.<br>
	 * If the passed collection already is thread safe (indicated by the marker interface {@link ThreadSafe}), then the collection
	 * itself is returned without further actions. This automatically ensures that a {@link SynchCollection} is not
	 * redundantly wrapped again in another {@link SynchCollection}.
	 *
	 * @param <E> the element type.
	 * @param collection the {@link XCollection} instance to be synchronized.
	 * @return a thread safe {@link XCollection} using the passed collection.
	 */
	public static <E> XCollection<E> synchronize(final XCollection<E> collection)
	{
		// if type of passed collection is already thread safe, there's no need to wrap it in a SynchronizedXCollection
		if(collection instanceof ThreadSafe)
		{
			return collection;
		}
		// wrap not thread safe set types in a SynchronizedXCollection
		return new SynchCollection<>(collection);
	}

	/**
	 * Converts an {@link Iterable} into an array.
	 * 
	 * @param <E> the element type.
	 * @param iterable the iterable to convert
	 * @param type the component type of the array to be created
	 * @return an array containing the values of the iterable
	 */
	@SuppressWarnings("unchecked") // type-safety ensured by logic
	public static <E> E[] toArray(final Iterable<? extends E> iterable, final Class<E> type)
	{
		if(iterable instanceof XGettingCollection)
		{
			return ((XGettingCollection<E>)iterable).toArray(type);
		}
		if(iterable instanceof Collection)
		{
			final Collection<E> collection = (Collection<E>)iterable;
			return collection.toArray((E[])Array.newInstance(type, collection.size()));
		}
		return StreamSupport.stream(iterable.spliterator(), false).toArray(
			size -> (E[])Array.newInstance(type, size)
		);
	}
	

	public static final <T extends Throwable> T addSuppressed(final T throwable, final Throwable suppressed)
	{
		throwable.addSuppressed(suppressed);
		return throwable;
	}

	public static final <T extends Throwable> T addSuppressed(final T throwable, final Throwable... suppresseds)
	{
		for(final Throwable suppressed : suppresseds)
		{
			throwable.addSuppressed(suppressed);
		}
		return throwable;
	}
	
	public static RuntimeException asUnchecked(final Exception e)
	{
		return e instanceof RuntimeException
			? (RuntimeException)e
			: new WrapperRuntimeException(e)
		;
	}
	
	/**
	 * Abbreviation for "execute on". Read as "on [subject] execute [logic]"
	 * Nifty little helper logic that allows to execute custom logic on a subject instance but still return that
	 * instance. Useful for method chaining.
	 * 
	 * @param <S> the subject's type
	 * @param subject the subject to execute the logic on
	 * @param logic the logic to execute
	 * @return the subject
	 */
	public static final <S> S on(final S subject, final Consumer<? super S> logic)
	{
		logic.accept(subject);
		return subject;
	}
	
	/**
	 * Forces the passed {@literal condition} to evaluate to true by throwing an {@link Error} otherwise.
	 * 
	 * @param condition the condition to check
	 * 
	 * @throws Error if the passed {@literal condition} fails.
	 */
	public static void check(final BooleanTerm condition)
		throws Error
	{
		check(condition, null, 1);
	}
	
	/**
	 * Forces the passed {@literal condition} to evaluate to true by throwing an {@link Error} otherwise.
	 * 
	 * @param condition the condition to check
	 * @param message the custom error message
	 * 
	 * @throws Error if the passed {@literal condition} fails.
	 */
	public static void check(final BooleanTerm condition, final String message)
		throws Error
	{
		check(condition, message, 1);
	}
	
	/**
	 * Forces the passed {@literal condition} to evaluate to true by throwing an {@link Error} otherwise.
	 * 
	 * @param condition the condition to check
	 * @param message the custom error message
	 * @param stackLevels the amount of stack levels for the resulting error
	 * 
	 * @throws Error if the passed {@literal condition} fails.
	 */
	public static void check(final BooleanTerm condition, final String message, final int stackLevels)
		throws Error
	{
		if(condition.evaluate())
		{
			// debug-friendly abort-condition
			return;
		}
		
		throw UtilStackTrace.cutStacktraceByN(
			new Error("Check failed" + (message == null ? "." : ": " + message)),
			stackLevels + 1
		);
	}
	
	// used in WIP validation example. Do not remove. Also generally useful.
	public static <T> T validate(final T value, final Predicate<? super T> validator)
		throws IllegalArgumentException
	{
		return validate(value, validator, X::illegalArgument);
	}
	
	public static IllegalArgumentException illegalArgument(final Object object)
	{
		return UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
	}
	
	public static <T, E extends Exception> T validate(
		final T                      value    ,
		final Predicate<? super T>   validator,
		final Function<? super T, E> exceptor
	)
		throws E
	{
		if(validator.test(value))
		{
			return value;
		}
		
		throw exceptor.apply(value);
	}
	
	public static <P extends _intProcedure> P repeat(final int amount, final P logic)
	{
		return repeat(0, amount, logic);
	}
	
	public static <P extends _intProcedure> P repeat(final int startValue, final int length, final P logic)
	{
		final int bound = startValue + XMath.positive(length);
		
		for(int i = startValue; i < bound; i++)
		{
			logic.accept(i);
		}
		
		return logic;
	}
	
	public static <P extends Runnable> P repeat(final int amount, final P logic)
	{
		return repeat(0, amount, logic);
	}
	
	public static <P extends Runnable> P repeat(final int startValue, final int length, final P logic)
	{
		final int bound = startValue + XMath.positive(length);
		
		for(int i = startValue; i < bound; i++)
		{
			logic.run();
		}
		
		return logic;
	}
		
	public static final long validateIndex(
		final long availableLength,
		final long index
	)
		throws IndexBoundsException
	{
		if(index < 0)
		{
			throw IndexBoundsException(0, availableLength, index, "Index < 0", 1);
		}
		if(index >= availableLength)
		{
			throw IndexBoundsException(0, availableLength, index, "Index >= bound", 1);
		}
		
		return index;
	}
	
	public static final long validateRange(final long bound, final long startIndex, final long length)
	{
		if(startIndex < 0)
		{
			throw IndexBoundsException(0, bound, startIndex, "StartIndex < 0", 1);
		}
		if(startIndex >= bound)
		{
			throw IndexBoundsException(0, bound, startIndex, "StartIndex >= bound", 1);
		}
		
		if(length < 0)
		{
			throw IndexBoundsException(startIndex, bound, length, "Length < 0", 1);
		}
		if(startIndex + length > bound)
		{
			throw IndexBoundsException(0, bound, startIndex + length, "Range > bound", 1);
		}
		
		return startIndex + length;
	}
	
	public static final IndexBoundsException IndexBoundsException(
		final long   startIndex        ,
		final long   indexBound        ,
		final long   index             ,
		final String message           ,
		final int    stackTraceCutDepth
	)
	{
		return UtilStackTrace.cutStacktraceByN(
			new IndexBoundsException(startIndex, indexBound, index, message),
			stackTraceCutDepth + 1
		);
	}
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private X()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
