package one.microstream.collections.types;

import one.microstream.functional.IndexedAcceptor;

public interface XIndexIterable<E> extends XIterable<E>
{
	public <IP extends IndexedAcceptor<? super E>> IP iterateIndexed(IP procedure);
}
