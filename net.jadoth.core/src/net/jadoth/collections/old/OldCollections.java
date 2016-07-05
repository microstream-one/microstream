package net.jadoth.collections.old;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.JadothArrays;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.util.KeyValue;

public final class OldCollections
{

	public static final <C extends Collection<T>, T> C addBatch(final C c, final Iterable<T> elements)
	{
		if(elements != null)
		{
			for(final T t : elements)
			{
				c.add(t);
			}
		}
	    return c;
	}

	/**
	 * Populate collection.
	 *
	 * @param <T> the generic type
	 * @param c the c
	 * @param elements the elements
	 * @return the collection
	 */
	@SafeVarargs
	public static final <C extends Collection<T>, T> C addArray(final C c, final T... elements)
	{
		if(elements != null)
		{
			for(final T t : elements)
			{
				c.add(t);
			}
		}
	    return c;
	}

	@SafeVarargs
	public static final <T> LinkedList<T> LinkedList(final T...elements)
	{
		return addArray(new LinkedList<T>(), elements);
	}

	@SafeVarargs
	public static final <T> ArrayList<T> ArrayList(final T... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return new ArrayList<>();
		}
		return addArray(new ArrayList<T>(elements.length), elements);
	}

	public static final <T> ArrayList<T> ArrayList(final int initialCapacity, final Iterable<T> elements)
	{
		return addBatch(new ArrayList<T>(initialCapacity), elements);
	}

	@SafeVarargs
	public static<K, V, T extends Map<K, V>, S extends Map<? extends K, ? extends V>> T mergeInto(
		final T target, final S... maps
	)
	{
		for(final S map : maps)
		{
			target.putAll(map);
		}
		return target;
	}

	public static final <T, L extends List<T> & RandomAccess> int count(
		final L                    list     ,
		final Predicate<? super T> predicate
	)
	{
		int count = 0;
		for(int i = 0, size = list.size(); i < size; i++)
		{
			final T element = list.get(i);
			if(predicate.test(element))
			{
				count++;
			}
		}
		return count;
	}

	public static final <T, L extends List<T> & RandomAccess>  T search(
		final L list,
		final Predicate<? super T> predicate
	)
	{
		for(int i = 0, size = list.size(); i < size; i++)
		{
			final T element = list.get(i);
			if(predicate.test(element))
			{
				return element;
			}
		}
		return null;
	}

	public static final <T, L extends List<T> & RandomAccess> boolean applies(
		final L list,
		final Predicate<? super T> predicate
	)
	{
		for(int i = 0, size = list.size(); i < size; i++)
		{
			if(predicate.test(list.get(i)))
			{
				return true;
			}
		}
		return false;
	}

	public static <C extends Collection<T>, T> C append(
		final C collectionToEnhance, final Predicate<? super T> selectionPredicate, final T[] arrayToAppend
	)
	{
		for(final T t : arrayToAppend)
		{
			if(selectionPredicate.test(t))
			{
				collectionToEnhance.add(t);
			}
		}
		return collectionToEnhance;
	}

	public static <C extends Collection<T>, T> C append(
		final C collectionToEnhance, final Predicate<? super T> selectionPredicate, final Iterator<T> iterator
	)
	{
		while(iterator.hasNext())
		{
			final T element = iterator.next();
			if(selectionPredicate.test(element))
			{
				collectionToEnhance.add(element);
			}
		}
		return collectionToEnhance;
	}

	public static <C extends Collection<T>, T> C append(
		final C collectionToEnhance, final Predicate<? super T> selectionPredicate, final Iterable<T> collectionToAppend
	)
	{
		for(final T t : collectionToAppend)
		{
			if(selectionPredicate.test(t))
			{
				collectionToEnhance.add(t);
			}
		}
		return collectionToEnhance;
	}

	public static <T> ArrayList<T> filter(final ArrayList<T> arrayList, final Predicate<? super T> selectionPredicate)
	{
		final ArrayList<T> newList = new ArrayList<>(arrayList.size());
		for(int i = 0, size = arrayList.size(); i < size; i++)
		{
			final T element = arrayList.get(i);
			if(selectionPredicate.test(element))
			{
				newList.add(element);
			}
		}
		return newList;
	}

	public static <T, L extends List<T> & RandomAccess> L filter(
		final L list, final Predicate<? super T> selectionPredicate, final L targetList
	)
	{
		for(int i = 0, size = list.size(); i < size; i++)
		{
			final T element = list.get(i);
			if(selectionPredicate.test(element))
			{
				targetList.add(element);
			}
		}
		return targetList;
	}

	public static <C extends Collection<T>, T> C filter(
		final C sourceCollection, final Predicate<? super T> selectionPredicate, final C target
	)
	{
		for(final T t : sourceCollection)
		{
			if(selectionPredicate.test(t))
			{
				target.add(t);
			}
		}
		return target;
	}

	public static <T> ArrayList<T> reduce(final ArrayList<T> arrayList, final Predicate<? super T> reductionPredicate)
	{
		for(int i = 0, size = arrayList.size(); i < size; i++)
		{
			final T element = arrayList.get(i);
			if(reductionPredicate.test(element))
			{
				arrayList.remove(element);
			}
		}
		return arrayList;
	}

	/**
	 * Reduces <code>collection</code> by all elements that meet <code>reductionPredicate</code>.<br>
	 * Note that NO new collection instance is created but the collection itself is reduced.
	 *
	 * @param <T>
	 * @param collection the collection to be reduced
	 * @param reductionPredicate the predicate determining which elements shall be removed
	 * @return <code>collection</code> itself
	 */
	public static <C extends Collection<T>, T> C reduce(final C collection, final Predicate<? super T> reductionPredicate)
	{
		for(final T t : collection)
		{
			if(reductionPredicate.test(t))
			{
				collection.remove(t);
			}
		}
		return collection;
	}

	public static final StringBuilder appendIterableSeperated(
		final StringBuilder sb, final String elementSeperator, final Iterable<?> iterable
	)
	{
		if(elementSeperator == null)
		{
			return OldCollections.appendArray(sb, iterable);
		}

		boolean notFirst = false;
		for(final Object e : iterable)
		{
			if(notFirst)
			{
				sb.append(elementSeperator);
			}
			else
			{
				notFirst = true;
			}
			sb.append(e);
		}
		return sb;
	}

	public static final StringBuilder appendIterableSeperated(
		final StringBuilder sb, final char elementSeperator, final Iterable<?> iterable
	)
	{
		boolean notFirst = false;
		for(final Object e : iterable)
		{
			if(notFirst)
			{
				sb.append(elementSeperator);
			}
			else
			{
				notFirst = true;
			}
			sb.append(e);
		}
		return sb;
	}

	public static final StringBuilder appendIterable(final StringBuilder sb, final Iterable<?> iterable)
	{
		for(final Object e : iterable)
		{
			sb.append(e);
		}
		return sb;
	}

	public static final StringBuilder appendArraySeperated(
		final StringBuilder sb,
		final String elementSeperator,
		final Object... elements
	)
	{
		if(elementSeperator == null)
		{
			return OldCollections.appendArray(sb, elements);
		}
		if(elements == null)
		{
			return sb;
		}

		boolean notFirst = false;
		for(final Object e : elements)
		{
			if(notFirst)
			{
				sb.append(elementSeperator);
			}
			else
			{
				notFirst = true;
			}
			sb.append(e);
		}
		return sb;
	}

	public static final StringBuilder appendArraySeperated(
		final StringBuilder sb,
		final char elementSeperator,
		final Object... elements
	)
	{
		if(elements == null)
		{
			return sb;
		}

		boolean notFirst = false;
		for(final Object e : elements)
		{
			if(notFirst)
			{
				sb.append(elementSeperator);
			}
			else
			{
				notFirst = true;
			}
			sb.append(e);
		}
		return sb;
	}

	public static final StringBuilder appendArray(final StringBuilder sb, final Object... elements)
	{
		if(elements == null)
		{
			return sb;
		}

		for(final Object e : elements)
		{
			sb.append(e);
		}
		return sb;
	}

	public static final StringBuilder stringBuilderSeperated(final String elementSeperator, final Object... elements)
	{
		return appendArraySeperated(new StringBuilder(), elementSeperator, elements);
	}

	public static final StringBuilder stringBuilderSeperated(final char elementSeperator, final Object... elements)
	{
		return appendArraySeperated(new StringBuilder(), elementSeperator, elements);
	}

	public static final StringBuilder stringBuilder(final Object... elements)
	{
		return appendArray(new StringBuilder(), elements);
	}

	/**
	 * Alias for the annoying <code>collection.toArray((T[])Array.newInstance(elementType, collection.size()))</code>
	 *
	 * @param <T> the element type parameter of the list
	 * @param collection the collection whose elements shall be copied to an array
	 * @param elementType the type of the elements contained in <code>collection</code>.
	 * @return a new array object of type <T> containing all elements of <code>collection</code>.
	 */
	public static final <T> T[] toArray(final Collection<? extends T> collection, final Class<T> elementType)
	{
		return collection.toArray(JadothArrays.newArray(elementType, collection.size()));
	}

	@SafeVarargs
	public static <T> HashSet<T> OldHashSet(final T... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return new HashSet<>();
		}
		return addArray(new HashSet<T>(elements.length), elements);
	}

	@SafeVarargs
	public static <K, V> HashMap<K, V> OldHashMap(final KeyValue<? extends K, ? extends V>... keyValueTuples)
	{
		if(keyValueTuples == null || keyValueTuples.length == 0)
		{
			return new HashMap<>();
		}
		final HashMap<K, V> map = new HashMap<>(keyValueTuples.length);

		for(final KeyValue<? extends K, ? extends V> kv : keyValueTuples)
		{
			map.put(kv.key(), kv.value());
		}

		return map;
	}


	public static final <K, V> LinkedHashMap<K, V> OldLinkedHashMap(final XGettingMap<K, V> map)
	{
		final LinkedHashMap<K, V> lhm = new LinkedHashMap<>(Jadoth.to_int(map.size()));
		map.iterate(e -> lhm.put(e.key(), e.value()));
		return lhm;
	}


	private OldCollections()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
