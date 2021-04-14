package one.microstream.collections.interfaces;

/**
 * Marker type indicating that a collection releases references to its elements.
 * <p>
 * This mainly applies to removing, but also to setting, replacing and to all kinds of putting in set collections.
 *
 * 
 *
 * @param <E>
 */
public interface ReleasingCollection<E>
{
	// empty marker interface (so far)
}
