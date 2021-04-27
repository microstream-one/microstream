/**
 *
 */
package one.microstream.collections.sorting;

import java.util.Comparator;

/**
 * Single concern type defining that a sub type can be sorted according to an external {@link Comparator}.
 * <p>
 * This type is mutually exclusive to {@link Sorted}.
 *
 * 
 *
 */
public interface Sortable<E>
{
	/**
	 * Sorts this collection according to the given comparator
	 * and returns itself.
	 * @param comparator to sort this collection
	 * @return this
	 */
	public Sortable<E> sort(Comparator<? super E> comparator);
}
