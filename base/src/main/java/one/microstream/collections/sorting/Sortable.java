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
	public Sortable<E> sort(Comparator<? super E> comparator);
}
