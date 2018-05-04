package net.jadoth.collections.types;

import net.jadoth.functional.IndexProcedure;

public interface XIndexIterable<E> extends XIterable<E>
{
	public <IP extends IndexProcedure<? super E>> IP iterateIndexed(IP procedure);
}
