package one.microstream.collections;

import one.microstream.collections.interfaces.ChainStorage;



/**
 * 
 *
 */
public abstract class AbstractChainCollection<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
extends AbstractExtendedCollection<E>
{
	protected abstract ChainStorage<E, K, V, EN> getInternalStorageChain();
	
	protected abstract void internalRemoveEntry(EN chainEntry);
	
	protected abstract long size();

	protected abstract int internalRemoveNullEntries();

	protected abstract int internalClear();

}
