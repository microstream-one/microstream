package one.microstream.concurrency;

import one.microstream.typing.Immutable;

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
