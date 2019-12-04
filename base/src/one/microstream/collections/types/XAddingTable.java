package one.microstream.collections.types;

import one.microstream.typing.KeyValue;

public interface XAddingTable<K, V> extends XAddingMap<K, V>, XAddingSequence<KeyValue<K, V>>
{
	public interface Creator<K, V>
	{
		public XAddingTable<K, V> newInstance();
	}


	@SuppressWarnings("unchecked")
	@Override
	public XAddingTable<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XAddingTable<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingTable<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);


}
