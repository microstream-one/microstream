package one.microstream.collections;

import one.microstream.typing.KeyValue;

public abstract class AbstractChainKeyValueCollection<K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
extends AbstractChainCollection<KeyValue<K, V>, K, V, EN>
{
	@Override
	protected abstract long size();

	@Override
	protected abstract void internalRemoveEntry(EN chainEntry);

	@Override
	protected abstract int internalRemoveNullEntries();

	@Override
	protected abstract int internalClear();

}
