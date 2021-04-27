package one.microstream.collections.interfaces;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * @param <E> type of contained elements
 * 
 *
 */
// basic interface that contains all general procedures that are common to any type of extended collection
public interface ExtendedCollection<E>
{
	/**
	 * Defines if null-elements are allowed inside the collection or not.
	 * @return {@code true} if null is allowed inside the collection; {@code false} if not
	 */
	// funnily, this is the only method (so far) common to both getting and adding concerns.
	public boolean nullAllowed();

	/**
	 * Tells if this collection contains volatile elements.<br>
	 * An element is volatile, if it can become no longer reachable by the collection without being removed from the
	 * collection. Examples are {@link WeakReference} of {@link SoftReference} or implementations of collection entries
	 * that remove the element contained in an entry by some means outside the collection.<br>
	 * Note that {@link WeakReference} instances that are added to a a simple (non-volatile) implementation of a
	 * collection do <b>not</b> make the collection volatile, as the elements themselves (the reference instances) are still
	 * strongly referenced.
	 *
	 * @return {@code true} if the collection contains volatile elements.
	 */
	public boolean hasVolatileElements();


	public interface Creator<E, C extends ExtendedCollection<E>>
	{
		public C newInstance();
	}

}
