package net.jadoth.concurrent;

import net.jadoth.util.Immutable;

/**
 * Marker interface to indicate that a subtype of it is thread safe to use. This applies to both {@link Synchronized}
 * and {@link Immutable} types.
 *
 * @author Thomas Muenz
 *
 */
public interface ThreadSafe
{
	// marker interface only
}
