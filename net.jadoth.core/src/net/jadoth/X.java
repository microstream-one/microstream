package net.jadoth;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.jadoth.collections.ArrayView;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.ConstHashEnum;
import net.jadoth.collections.ConstList;
import net.jadoth.collections.Constant;
import net.jadoth.collections.Empty;
import net.jadoth.collections.EmptyTable;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.KeyValue;
import net.jadoth.collections.Singleton;
import net.jadoth.collections.interfaces.Sized;
import net.jadoth.collections.old.AbstractBridgeXList;
import net.jadoth.collections.old.AbstractBridgeXSet;
import net.jadoth.collections.old.XArrayList;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XMap;
import net.jadoth.collections.types.XReference;
import net.jadoth.collections.types.XSet;
import net.jadoth.exceptions.ArrayCapacityException;
import net.jadoth.util.JadothExceptions;
import net.jadoth.util._longKeyValue;

/**
 * Central class for factory and util methods.<br>
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
	// constants        //
	/////////////////////
	
	private static final Empty<?> EMPTY = new Empty<>();

	private static final EmptyTable<?, ?> EMPTY_TABLE = new EmptyTable<>();
	
	
	
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
		return BulkList.New(elements.length);
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
	public static <E> E[] ArrayOfSameType(final E[] sampleArray, final int length)
	{
		return (E[])Array.newInstance(sampleArray.getClass().getComponentType(), length);
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] Array(final Class<E> componentType, final int length)
	{
		return (E[])Array.newInstance(componentType, length);
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
	
	@SafeVarargs
	public static <E> E[] Array(final Class<E> componentType, final int length, final E... initialElements)
	{
		final E[] array = X.Array(componentType, length);
		
		if(initialElements != null)
		{
			// intentionally no min length logic to prevent swallowing of programming errors.
			System.arraycopy(initialElements, 0, array, 0, initialElements.length);
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

	public static boolean hasNoContent(final XGettingCollection<?> collection)
	{
		return collection == null || collection.isEmpty();
	}



	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private X()
	{
		// static only
		throw new UnsupportedOperationException();
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

	public static <T, K, V> KeyValue<K, V> toKeyValue(final T instance, final Function<? super T, KeyValue<K, V>> mapper)
	{
		return mapper.apply(instance);
	}

	public static <K, V> KeyValue<K, V> keyValue(final K key, final V value)
	{
		return KeyValue.New(key, value);
	}

	public static _longKeyValue _longKeyValue(final long key, final long value)
	{
		return new _longKeyValue.Implementation(key, value);
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
		if(capacity >= Jadoth.ARRAY_LENGTH_BOUND)
		{
			throw new ArrayCapacityException(capacity);
		}
		return (int)capacity;
	}
	
}
