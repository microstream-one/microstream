package one.microstream.collections.types;

import java.lang.ref.WeakReference;

import one.microstream.collections.interfaces.ConsolidatableCollection;
import one.microstream.collections.interfaces.ExtendedCollection;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.collections.interfaces.ReleasingCollection;
import one.microstream.collections.interfaces.Truncateable;

public interface XRemovingCollection<E>
extends
ExtendedCollection<E>,
Truncateable,
ConsolidatableCollection,
OptimizableCollection,
ReleasingCollection<E>
{
	public interface Factory<E> extends XFactory<E>
	{
		@Override
		public XRemovingCollection<E> newInstance();
	}


	// removing procedures //

	/**
	 * Clears all elements from the collection while leaving the capacity as it is.
	 */
	@Override
	public void clear();

	/**
	 * Clears (and reinitializes if needed) this collection in the fastest possible way, i.e. by allocating a new and
	 * empty internal storage of default capacity. The collection will be empty after calling this method.
	 */
	@Override
	public void truncate();

	/**
	 * Consolidates the internal storage of this collection by discarding all elements of the internal storage that
	 * have become obsolete or otherwise unneeded anymore. (e.g. {@link WeakReference} entries whose reference has
	 * been cleared).<p>
	 * If this is not possible or not needed in the concrete implementation, this method does nothing and returns 0.
	 *
	 * @return the number of discarded entries.
	 */
	@Override
	public long consolidate();

	/**
	 * Optimizes internal memory usage by rebuilding the storage to only occupy as much memory as needed to store
	 * the currently contained elements in terms of the collection's current memory usage configuration
	 * (e.g. hash density).<p>
	 * If this is not possible or not needed in the concreate implementation, this method does nothing.<p>
	 * <p>
	 * Note that this method can consume a considerable amount of time depending on the implementation and should
	 * only be called intentionally and accurately when reducing occupied memory is needed.
	 *
	 * @return the amount of elements that can be added before the internal storage has to be adjusted.
	 */
	@Override
	public long optimize();

	public long nullRemove();

	// (29.09.2012 TM)XXX: rename to removeFirst (first occurance for non-sequence, first in order for sequence)
	// (29.09.2012 TM)XXX: add removeLast()? Would be more efficient for array storages to scan backwards.
	public boolean removeOne(E element);

	public long remove(E element);

	public long removeAll(XGettingCollection<? extends E> elements);

	public long retainAll(XGettingCollection<? extends E> elements);

	public long removeDuplicates();

}
