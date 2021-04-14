package one.microstream.collections.interfaces;

import java.lang.ref.WeakReference;

/**
 * 
 *
 */
public interface ConsolidatableCollection
{
	/**
	 * Consolidates the internal storage of this collection by discarding all elements of the internal storage that
	 * have become obsolete or otherwise unneeded anymore. (e.g. {@link WeakReference} entries whose reference has
	 * been cleared).<p>
	 * If this is not possible or not needed in the concrete implementation, this method does nothing and returns 0.
	 *
	 * @return the number of discarded entries.
	 */
	public long consolidate();
}
