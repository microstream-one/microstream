package one.microstream.collections.types;

import one.microstream.functional.IndexedAcceptor;

public interface XIndexIterable<E> extends XIterable<E>
{
	/**
	 * Iterates over elements with the {@link IndexedAcceptor} to use
	 * not only the element itself but also its coherent index.
	 * @param <IP> type of procedure
	 * @param procedure which is executed when iterating
	 * @return Given procedure
	 */
	public <IP extends IndexedAcceptor<? super E>> IP iterateIndexed(IP procedure);
}
