package net.jadoth.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.old.AbstractBridgeXList;
import net.jadoth.collections.old.AbstractBridgeXSet;
import net.jadoth.collections.old.XArrayList;
import net.jadoth.collections.types.XAddingCollection;
import net.jadoth.collections.types.XAddingMap;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XImmutableList;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XMap;
import net.jadoth.collections.types.XReference;
import net.jadoth.collections.types.XSet;
import net.jadoth.functional.Aggregator;
import net.jadoth.functional.BiProcedure;
import net.jadoth.util.KeyValue;
import net.jadoth.util.branching.ThrowBreak;

public final class X
{
	private static final Empty<?> EMPTY = new Empty<>();

	private static final EmptyTable<?, ?> EMPTY_TABLE = new EmptyTable<>();

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

	@SafeVarargs
	public static <E> XList<E> List(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return new BulkList<>();
		}
		return new BulkList<E>(elements.length).addAll(elements);
	}

	@SafeVarargs
	public static <E> ConstList<E> ConstList(final E... elements) throws NullPointerException
	{
		// as opposed to list(), a ConstList without initializer array null makes no sense, so let it crash
		return new ConstList<>(elements);
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

	public static <E> XList<E> x(final List<E> oldList)
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

	public static <E> XSet<E> x(final Set<E> oldSet)
	{
		if(oldSet instanceof AbstractBridgeXSet<?>)
		{
			return ((AbstractBridgeXSet<E>)oldSet).parent();
		}

		throw new UnsupportedOperationException();
		// (19.05.2011 TM)FIXME: old set wrapper
	}

	public static <K, V> XMap<K, V> x(final Map<K, V> oldMap)
	{
		throw new UnsupportedOperationException();
		// (19.05.2011 TM)FIXME: old map wrapper
	}

	/**
	 * Static workaround for the Java typing deficiency that is not possible to
	 * define {@code public <T super E> T[] toArray(Class<T> type)}.
	 *
	 * @param <E> the collection's element type.
	 * @param <T> the component type used to create the array, possibly a super type of {@code E}
	 * @param collection the collection whose elements shall be copied to the array.
	 * @param type the {@link Class} representing type {@code T} at runtime.
	 * @return a new array instance of component type {@code T} containing all elements of the passed collection.
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends T> T[] toArray(final XGettingCollection<E> collection, final Class<T> type)
	{
//		final T[] array;
//		collection.copyTo(array = (T[])Array.newInstance(type, collection.size()), 0);
//		return array; // do not rely on reference returned by the implementation but return local variable.

		// (02.11.2011 TM)NOTE: pretty hacky typing-wise, but in the end safer regarding concurrency and the like.
		return collection.toArray((Class<E>)type); // nifty abuse of array covariance typing bug for return value ]:->
	}

	public static boolean hasNoContent(final XGettingCollection<?> collection)
	{
		return collection == null || collection.isEmpty();
	}

	public static <E, C extends Consumer<? super E>> C collectNotNull(final C target, final E element)
	{
		if(element != null)
		{
			target.accept(element);
		}
		return target;
	}

	public static <E, C extends XAddingCollection<? super E>> C addNotNull(final C target, final E element)
	{
		if(element != null)
		{
			target.add(element);
		}
		return target;
	}

	public static <E, C extends XAddingCollection<? super E>> C addNotNull(
		final C                     target  ,
		final XGettingCollection<E> elements
	)
	{
		if(elements != null)
		{
			target.addAll(elements);
		}
		return target;
	}

	public static <I, O, T extends Consumer<? super O>> T map(
		final XGettingCollection<? extends I> elements,
		final Function<? super I, O> mapper,
		final T target
	)
	{
		// procedure should get stack allocated
		elements.iterate(new Consumer<I>()
		{
			@Override
			public void accept(final I e)
			{
				target.accept(mapper.apply(e));
			}
		});
		return target;
	}

	public static final <E, R> R aggregate(
		final XIterable<? extends E>[] iterables,
		final Aggregator<? super E, R> aggregator
	)
	{
		for(final XIterable<? extends E> iterable : iterables)
		{
			iterable.iterate(aggregator);
		}
		return aggregator.yield();
	}

	public static <I, O, T extends Consumer<? super O>> T map(
		final XIterable<? extends I> elements,
		final Predicate<? super I> predicate,
		final Function<? super I, O> mapper,
		final T target
	)
	{
		elements.iterate(new Consumer<I>()
		{
			@Override
			public void accept(final I e)
			{
				target.accept(mapper.apply(e));
			}
		});
		return target;
	}

	@SafeVarargs
	public static final <K, V, M extends XAddingMap<? super K, ? super V>> M addAllTo(
		final M                                     collector,
		final KeyValue<? extends K, ? extends V>... elements
	)
	{
		for(final KeyValue<? extends K, ? extends V> keyValue : elements)
		{
			collector.add(keyValue.key(), keyValue.value());
		}
		return collector;
	}

	public static final <K, V, M extends XAddingMap<? super K, ? super V>> M addAllTo(
		final M   collector,
		final K[] keys     ,
		final V[] values
	)
	{
		if(keys.length != values.length)
		{
			throw new RuntimeException(); // (21.10.2013 TM)EXCP: proper exception
		}
		for(int i = 0; i < keys.length; i++)
		{
			collector.add(keys[i], values[i]);
		}
		return collector;
	}

	public static final <E> Function<XGettingList<E>, XImmutableList<E>> immurer()
	{
		return new Function<XGettingList<E>, XImmutableList<E>>()
		{
			@Override
			public final XImmutableList<E> apply(final XGettingList<E> input)
			{
				return input.immure();
			}
		};
	}

	public static final <K, V, C extends Consumer<? super V>>
	C query(final XGettingMap<K, V> map, final XIterable<? extends K> keys, final C collector)
	{
		keys.iterate(new Consumer<K>()
		{
			@Override
			public void accept(final K key)
			{
				collector.accept(map.get(key));
			}
		});
		return collector;
	}

	public static final <E, R> Aggregator<E, R> aggregator(
		final BiProcedure<? super E, ? super R> joiner   ,
		final R                                 aggregate
	)
	{
		return
			new Aggregator<E, R>()
			{
				@Override
				public void accept(final E element)
				{
					joiner.accept(element, aggregate);
				}
				
				@Override
				public R yield()
				{
					return aggregate;
				}
			}
		;
	}

	public static <T> XReference<T> Reference(final T object)
	{
		return new Singleton<>(object);
	}


	public static <K, V, C extends XAddingMap<? super K, ? super V>> C addAllDerivingKeys(
		final C                      target,
		final XGettingCollection<V>  values,
		final Function<? super V, K> mapper
	)
	{
		for(final V v : values)
		{
			target.add(mapper.apply(v), v);
		}
		return target;
	}

	
	public static <E> Consumer<E> conditional(final Predicate<? super E> condition, final Consumer<? super E> consumer)
	{
		return e ->
		{
			if(condition.test(e))
			{
				consumer.accept(e);
			}
		};
	}
	
	public static <E, C extends Consumer<? super E>> C iterateConditional(
		final XIterable<? extends E> elements,
		final Predicate<? super E> condition,
		final C logic
	)
	{
		try
		{
			elements.iterate(e ->
			{
				if(condition.test(e))
				{
					logic.accept(e);
				}
			});
		}
		catch(final ThrowBreak t)
		{
			return logic;
		}
		
		return logic;
	}


	private X()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
