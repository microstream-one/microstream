package net.jadoth.equality;

/**
 * Marker interface to indicate that an {@link Equalator} implementation uses valuetype-like comparison for determining
 * equality.
 *
 * @author Thomas Muenz
 *
 */
public interface ValueTypeEqualator<E> extends Equalator<E>
{
	// marker interface
}
