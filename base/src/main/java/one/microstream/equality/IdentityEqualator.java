package one.microstream.equality;

/**
 * Marker interface to indicate that an {@link Equalator} implementation uses identity comparison for determining
 * equality.
 *
 * 
 *
 */
public interface IdentityEqualator<E> extends Equalator<E>
{
	// marker interface
}
