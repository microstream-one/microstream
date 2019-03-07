package net.jadoth.collections.types;

import net.jadoth.functional.IndexedAcceptor;

public interface XIndexIterable<E> extends XIterable<E>
{
	public <IP extends IndexedAcceptor<? super E>> IP iterateIndexed(IP procedure);
}
