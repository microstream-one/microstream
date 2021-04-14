package one.microstream.collections.interfaces;

import one.microstream.collections.types.XSequence;
import one.microstream.collections.types.XTable;

/**
 * Marker interface indicating that a type is ordered. Order super type for {@link XSequence} and {@link XTable}.
 * <p>
 * An ordered collection is defined as a collection where size-changing procedures like adding (putting) or removing
 * an element does not affect the order of the remaining elements contained in the collection (with "remaining"
 * meaning to exclude all elements that have to be removed from the collection for adding the new element).
 * Note that this applies to straight forward collection types like lists where every element is simply appended at the
 * end as well as to sorted collections, where new elements are sorted in at the appropriate place in the collection.
 * <p>
 * This definition does NOT apply to pure set or bag implementations, like {@link java.util.HashSet}, where elements
 * do have an internal order as well, but one that can dramatically change with potentionally any newly added element.
 *
 * 
 */
public interface ExtendedSequence<E> extends ExtendedCollection<E>
{
	// marker interface
}
