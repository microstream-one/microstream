package one.microstream;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
import one.microstream.collections.Singleton;
import one.microstream.collections.SynchCollection;
import one.microstream.collections.SynchList;
import one.microstream.collections.SynchSet;
import one.microstream.collections.interfaces.Sized;
import one.microstream.collections.old.AbstractBridgeXList;
import one.microstream.collections.old.AbstractBridgeXSet;
import one.microstream.collections.old.XArrayList;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XReference;
import one.microstream.collections.types.XSet;
import one.microstream.concurrency.ThreadSafe;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.WrapperRuntimeException;
import one.microstream.functional._intIndexedSupplier;
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
 * more than applying the same exception to constructur-like static methods. Resembling a constructor means:
 * 1.) Indicating by name that a new instance is created. 2.) Always returning a new instance, without exception.
 * No caching, no casting. For example: {@link #empty()} or {@link #asX(List)} are NOT constructor-like methods
 * because they do not (always) create new instances.
 * 
 * @author Thomas Muenz
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
		if(capacity >= INTEGER_RANGE_BOUND)
		{
			throw new ArrayCapacityException(capacity);
		}
		return (int)capacity;
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

	
	
	public static final boolean isNull(final Object reference)
	{
		return reference == null;
	}
	
	public static final boolean isNotNull(final Object reference)
	{
		return reference != null;
	}

	
	
	/**
	 * Transiently ensures the passed object to be not {@code null} by either returning it in case it is
	 * not {@code null} or throwing a {@link NullPointerException} otherwise.
	 * <p>
	 * <i>(Really, no idea why java.util.Objects.notNull got renamed to requireNotNull after some odd objection from
	 * some guy in the mailing list that the name would be misleading.
	 * Of course "notNull" means "the thing you pass has to be not null, otherwise you'll get an exception".
	 * What else could the meaning of a transient method named "notNull" be?
	 * If "requireNotNull" is needed to express this behavior, than what would "notNull" alone mean?<br>
	 * In the end, "requireNotNull" is just additional clutter, hence not usable and is replaced by
	 * this, still properly named "notNull" method.)<i>
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
			throw UtilStackTrace.cutStacktraceByOne(new NullPointerException());
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
	
	
	
	public static final Byte[] box(final byte... bytes)
	{
		if(bytes == null)
		{
			return null;
		}

		final int length;
		final Byte[] result = new Byte[length = bytes.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = bytes[i];
		}
		
		return result;
	}
	
	public static final Boolean[] box(final boolean... booleans)
	{
		if(booleans == null)
		{
			return null;
		}

		final int length;
		final Boolean[] result = new Boolean[length = booleans.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = booleans[i];
		}
		
		return result;
	}
	
	public static final Short[] box(final short... shorts)
	{
		if(shorts == null)
		{
			return null;
		}

		final int length;
		final Short[] result = new Short[length = shorts.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = shorts[i];
		}
		
		return result;
	}
	
	public static final Integer[] box(final int... ints)
	{
		if(ints == null)
		{
			return null;
		}
	
		final int length;
		final Integer[] result = new Integer[length = ints.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = ints[i];
		}
		
		return result;
	}
	
	public static final Character[] box(final char... chars)
	{
		if(chars == null)
		{
			return null;
		}

		final int length;
		final Character[] result = new Character[length = chars.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = chars[i];
		}
		
		return result;
	}
	
	public static final Float[] box(final float... floats)
	{
		if(floats == null)
		{
			return null;
		}

		final int length;
		final Float[] result = new Float[length = floats.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = floats[i];
		}
		
		return result;
	}
	
	public static final Long[] box(final long... longs)
	{
		if(longs == null)
		{
			return null;
		}

		final int length;
		final Long[] result = new Long[length = longs.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = longs[i];
		}
		
		return result;
	}
	
	public static final Double[] box(final double... doubles)
	{
		if(doubles == null)
		{
			return null;
		}

		final int length;
		final Double[] result = new Double[length = doubles.length];
		for(int i = 0; i < length; i++)
		{
			result[i] = doubles[i];
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
	
	
	
	@SafeVarargs
	public static <E> XList<E> List(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return BulkList.New();
		}
		return BulkList.New(elements);
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
		return new Singleton<>(element);
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

	@SafeVarargs
	public static <E> ConstHashEnum<E> ConstEnum(final E... elements) throws NullPointerException
	{
		if(elements == null || elements.length == 0)
		{
			return ConstHashEnum.New();
		}
		return ConstHashEnum.<E>New(elements);
	}

	public static <T> XReference<T> Reference(final T object)
	{
		return new Singleton<>(object);
	}
	
	
	@SuppressWarnings("unchecked")
	public static final <T> T[] ArrayForElementType(final T sampleInstance, final int length)
	{
		// fascinating how the JDK massively lacks basic util methods like this
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
		// nifty abuse of array covariance typing bug for return value ]:->
		return collection.toArray((Class<E>)type);
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
	 * @see X#Array(int, Supplier)
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
	
	public static <E> XList<E> asX(final List<E> oldList)
	{
		if(oldList instanceof AbstractBridgeXList<?>)
		{
			return ((AbstractBridgeXList<E>)oldList).parent();
		}
		else if(oldList instanceof ArrayList<?>)
		{
			return new XArrayList<>((ArrayList<E>)oldList);
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
	 * As usual, the JDK developers fail to create smoothly usable methods, so one has to clean up after them.<br>
	 * Usage:<br>
	 * <code>throw addSuppressed(new SomethingIsWrongException(), e);</code>
	 */
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
	
	public RuntimeException asUnchecked(final Exception e)
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
	 * @param subject
	 * @param logic
	 * @return
	 */
	public static final <S> S on(final S subject, final Consumer<? super S> logic)
	{
		logic.accept(subject);
		return subject;
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private X()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
