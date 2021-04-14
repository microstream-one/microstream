package one.microstream.collections;

import one.microstream.collections.interfaces.ChainKeyValueStorage;
import one.microstream.typing.KeyValue;


public abstract class AbstractChainKeyValueStorage<K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
extends ChainStorageStrong<KeyValue<K, V>, K, V, EN>
implements ChainKeyValueStorage<K, V, EN>
{
	public AbstractChainKeyValueStorage(final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent, final EN head)
	{
		super(parent, head);
	}

}
