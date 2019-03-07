package one.microstream.typing;

import one.microstream.concurrency.ThreadSafe;
import one.microstream.hashing.HashImmutable;

/**
 * @author Thomas Muenz
 *
 */
public interface Immutable extends HashImmutable, ThreadSafe
{
	// marker interface only
}
