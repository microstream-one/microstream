package one.microstream.collections.types;

public interface XProcessingMap<K, V> extends XRemovingMap<K, V>, XGettingMap<K, V>
{
	public interface Creator<K, V> extends XRemovingMap.Factory<K, V>, XGettingMap.Creator<K, V>
	{
		@Override
		public XProcessingMap<K, V> newInstance();
	}

	/*
	 * (06.07.2016 TM)NOTE: javac reported an ambiguity with XProcessingCollection here for the name "remove".
	 * Hence it got changed to "removeFor".
	 */
	public V removeFor(final K key);

	@Override
	public XProcessingMap<K, V> copy();


	@Override
	public Keys<K, V> keys();

	@Override
	public Values<K, V> values();



	public interface Keys<K, V> extends XGettingMap.Keys<K, V>, XProcessingSet<K>
	{
		@Override
		public XImmutableSet<K> immure();

	}

	public interface Values<K, V> extends XGettingMap.Values<K, V>, XProcessingBag<V>
	{
		// empty so far
	}

}
