package net.jadoth.collections.sorting;

import java.util.Comparator;

import net.jadoth.collections.interfaces.ExtendedSequence;


/**
 * Single concern type defining that a sub type is always sorted according to an internal {@link Comparator}.
 * <p>
 * This definition extends the definition of being ordered.
 * <p>
 * This type is mutually exclusive to {@link Sortable}.
 *
 * @author Thomas Muenz
 *
 * @param <E>
 */
public interface Sorted<E> extends ExtendedSequence<E>
{
	/**
	 *
	 * @return the {@link Comparator} that defines the sorting order of this {@link Sorted} instance.
	 */
	public Comparator<? super E> getComparator();
}
