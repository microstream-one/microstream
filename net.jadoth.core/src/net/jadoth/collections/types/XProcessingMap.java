package net.jadoth.collections.types;


public interface XProcessingMap<K, V> extends XRemovingMap<K, V>, XGettingMap<K, V>
{
	public interface Creator<K, V> extends XRemovingMap.Factory<K, V>, XGettingMap.Creator<K, V>
	{
		@Override
		public XProcessingMap<K, V> newInstance();
	}

	/*
	 * (05.07.2016 TM)NOTE: must be named different from XRemovingCollection#remove
	 * Otherwise, javac gets confused. Even though the Eclipse compiler understands it correctly
	 */
	public V removeFor(final K key);

	@Override
	public XProcessingMap<K, V> copy();

}
