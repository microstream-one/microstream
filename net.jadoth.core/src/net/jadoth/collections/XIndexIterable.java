package net.jadoth.collections;

import net.jadoth.functional.IndexProcedure;

public interface XIndexIterable<E> extends XIterable<E>
{
	public <IP extends IndexProcedure<? super E>> IP iterate(IP procedure);
}
