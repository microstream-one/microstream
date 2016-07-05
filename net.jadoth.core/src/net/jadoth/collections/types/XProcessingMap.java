package net.jadoth.collections.types;


public interface XProcessingMap<K, V> extends XRemovingMap<K, V>, XGettingMap<K, V>
{
	public interface Creator<K, V> extends XRemovingMap.Factory<K, V>, XGettingMap.Creator<K, V>
	{
		@Override
		public XProcessingMap<K, V> newInstance();
	}

	public V remove(final K key);

	@Override
	public XProcessingMap<K, V> copy();

}
