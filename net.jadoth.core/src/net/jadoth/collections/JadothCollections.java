package net.jadoth.collections;

import static net.jadoth.Jadoth.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

import net.jadoth.Jadoth;
import net.jadoth.collections.types.XAddingMap;
import net.jadoth.collections.types.XCollection;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XSet;
import net.jadoth.concurrent.ThreadSafe;
import net.jadoth.functional.AvgInteger;
import net.jadoth.functional.AvgIntegerNonNull;
import net.jadoth.functional.SumInteger;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;

/**
 * @author Thomas Muenz
 *
 */
public final class JadothCollections
{
	public static final boolean containsNull(final Collection<?> c) throws NullPointerException
	{
		notNull(c);
		try
		{
			return c.contains(null);
		}
		catch(final NullPointerException e)
		{
			//collection implementations not supporting null values may throw an exception according to contract
			return false;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// collection aggregates  //
	///////////////////////////

	public static final boolean hasNoElements(final XGettingCollection<?> c)
	{
		return c == null || c.isEmpty();
	}

	public static final long count(final XGettingCollection<?> c)
	{
		return c.size(); //kind of stupid
	}

	public static final Integer sum(final XGettingCollection<Integer> ints)
	{
		return ints.iterate(new SumInteger()).yield();
	}
	public static final Integer avg(final XGettingCollection<Integer> ints)
	{
		return new AvgInteger(ints).yield();
	}
	public static final Integer avg(final XGettingCollection<Integer> ints, final boolean includeNulls)
	{
		return (includeNulls ? new AvgInteger(ints) : new AvgIntegerNonNull(ints)).yield();
	}
	public static final Integer max(final XGettingCollection<Integer> ints)
	{
		return ints.max(JadothSort::compare);
	}
	public static final Integer min(final XGettingCollection<Integer> ints)
	{
		return ints.min(JadothSort::compare);
	}


	/**
	 * Convenience method for <code>new ArrayList<E>(xCollection)</code>.
	 * <p>
	 *
	 * @param <E> the collection element type.
	 * @param xCollection the extended collection implementation whore content shall be copied a new
	 *        {@link ArrayList} instance.
	 * @return a new {@link ArrayList} instance containing all elements of the passed {@link XGettingCollection}.
	 */
	public static final <E> ArrayList<E> ArrayList(final XGettingCollection<? extends E> xCollection)
	{
		// ArrayList collection constructor already uses toArray() directly as elementData
		return new ArrayList<>(xCollection.old());
	}

	@SafeVarargs
	public static final <E> ArrayList<E> ArrayList(final E... elements)
	{
		if(elements == null)
		{
			return new ArrayList<>();
		}

		final ArrayList<E> list = new ArrayList<>(elements.length);
		for(int i = 0; i < elements.length; i++)
		{
			list.add(elements[i]);
		}
		return list;
	}


	public static final <E> LinkedList<E> LinkedList(final XGettingCollection<? extends E> xCollection)
	{
		final LinkedList<E> linkedList = new LinkedList<>();

		// current array-backed list implementations all have none-volatile elements, but just to be sure.
		if(!xCollection.hasVolatileElements() && xCollection instanceof AbstractSimpleArrayCollection<?>)
		{
			// fastest way: just iterate over storage array
			final E[] elements = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)xCollection);
			for(int i = 0, size = Jadoth.to_int(xCollection.size()); i < size; i++)
			{
				linkedList.add(elements[i]);
			}
			return linkedList;
		}

		/* still faster than LinkedList.addAll() with intermediate Object[] creation:
		 * iterate source collection with stateless adding function instance.
		 */
		xCollection.iterate(e ->
		{
			linkedList.add(e);
		});
		return linkedList;
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


	public static <T> T[] toArray(final Collection<? extends T> collection, final Class<T> type)
	{
		final T[] array = JadothArrays.newArray(type, collection.size());
		collection.toArray(array);
		return array;
	}

	public static <K1, V1, K2, V2, M extends XAddingMap<? super K2, ? super V2>> M copyTo(
		final XGettingSequence<? extends KeyValue<K1, V1>> source     ,
		final M                                            target     ,
		final Function<? super K1, K2>                     keyMapper  ,
		final Function<? super V1, V2>                     valueMapper
	)
	{
		for(final KeyValue<K1, V1> e : source)
		{
			target.add(
				keyMapper.apply(e.key()),
				valueMapper.apply(e.value())
			);
		}
		return target;
	}


	public static VarString assembleString(final VarString vs, final XGettingCollection<?> collection)
	{
		if(collection.isEmpty())
		{
			return vs.add('[', ']');
		}

		vs.append('[');
		collection.iterate(e ->
			vs.add(e).add(',', ' ')
		);
		vs.deleteLast().setLast(']');

		return vs;
	}

	public static String toString(final XGettingCollection<?> collection)
	{
		// CHECKSTYLE.OFF: MagicNumber: special case not worth the hassle
		return assembleString(VarString.New((int)(collection.size() * 4.0f)), collection).toString();
		// CHECKSTYLE.ON: MagicNumber
	}




	private JadothCollections()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
