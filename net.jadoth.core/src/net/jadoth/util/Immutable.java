package net.jadoth.util;

import net.jadoth.concurrent.ThreadSafe;
import net.jadoth.hash.HashImmutable;

/**
 * @author Thomas Muenz
 *
 */
public interface Immutable extends HashImmutable, ThreadSafe
{
	// marker interface only
}
