package net.jadoth.typing;

import net.jadoth.concurrency.ThreadSafe;
import net.jadoth.hashing.HashImmutable;

/**
 * @author Thomas Muenz
 *
 */
public interface Immutable extends HashImmutable, ThreadSafe
{
	// marker interface only
}
