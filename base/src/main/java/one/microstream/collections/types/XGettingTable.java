package one.microstream.collections.types;

import java.util.function.Consumer;

import one.microstream.typing.KeyValue;

/**
 * Map plus order
 *
 * 
 *
 * @param <K>
 * @param <V>
 */
public interface XGettingTable<K, V> extends XGettingMap<K, V>, XGettingEnum<KeyValue<K, V>>
{
	@Override
	public Keys<K, V> keys();

	@Override
	public Values<K, V> values();

	@Override
	public XGettingTable<K, V> view();

	@Override
	public XImmutableTable<K, V> immure();

	@Override
	public EntriesBridge<K, V> old();

	@Override
	public Bridge<K, V> oldMap();

	@Override
	public XGettingTable<K, V> copy();



	public interface Satellite<K, V> extends XGettingMap.Satellite<K, V>
	{
		@Override
		public XGettingTable<K, V> parent();

	}

	public interface Keys<K, V> extends XGettingMap.Keys<K, V>, XGettingEnum<K>, Satellite<K, V>
	{
		@Override
		public XGettingTable<K, V> parent();
	}

	public interface Values<K, V> extends XGettingMap.Values<K, V>, Satellite<K, V>, XGettingList<V>
	{
		@Override
		public XGettingTable<K, V> parent();

		@Override
		public XGettingList<V> copy(); // values in an unordered map is a practical example for a bag

		@Override
		public <P extends Consumer<? super V>> P iterate(P procedure);

	}

	public interface Bridge<K, V> extends XGettingMap.Bridge<K, V>, Satellite<K, V>
	{
		@Override
		public XGettingTable<K, V> parent();

	}

	public interface EntriesBridge<K, V> extends XGettingMap.EntriesBridge<K, V>
	{
		@Override
		public XGettingTable<K, V> parent();
	}

}
