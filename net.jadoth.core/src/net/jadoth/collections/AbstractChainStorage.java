package net.jadoth.collections;

import net.jadoth.collections.interfaces.ChainStorage;


public abstract class AbstractChainStorage<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
implements ChainStorage<E, K, V, EN>
{
	protected abstract EN head();

	protected abstract void disjoinEntry(EN entry);

	protected abstract boolean moveToStart(EN entry);

	protected abstract boolean moveToEnd(EN entry);
}
