package net.jadoth.collections.types;


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

}
