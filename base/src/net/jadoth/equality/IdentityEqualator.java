package net.jadoth.equality;

/**
 * Marker interface to indicate that an {@link Equalator} implementation uses identity comparison for determining
 * equality.
 *
 * @author Thomas Muenz
 *
 */
public interface IdentityEqualator<E> extends Equalator<E>
{
	// marker interface
}
