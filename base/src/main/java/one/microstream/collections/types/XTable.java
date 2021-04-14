package one.microstream.collections.types;

import java.util.Comparator;

import one.microstream.typing.KeyValue;



/**
 * 
 *
 */
public interface XTable<K, V> extends XBasicTable<K, V>, XEnum<KeyValue<K, V>>
{
	@Override
	public Keys<K, V> keys();

	@Override
	public Values<K, V> values();

	@Override
	public EntriesBridge<K, V> old();
	
	@Override
	public Bridge<K, V> oldMap();
	
	@Override
	public XTable<K, V> copy();

	@Override
	public boolean hasVolatileValues();

	@Override
	public XTable<K, V> sort(Comparator<? super KeyValue<K, V>> comparator);
	
	@SuppressWarnings("unchecked")
	@Override
	public XTable<K, V> putAll(KeyValue<K, V>... elements);

	@Override
	public XTable<K, V> putAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XTable<K, V> putAll(XGettingCollection<? extends KeyValue<K, V>> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XTable<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XTable<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XTable<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);



	public interface Satellite<K, V> extends XBasicTable.Satellite<K, V>
	{
		@Override
		public XTable<K, V> parent();

	}

	public interface Keys<K, V> extends XBasicTable.Keys<K, V>, XEnum<K>
	{
		@Override
		public XTable<K, V> parent();

		@SuppressWarnings("unchecked")
		@Override
		public Keys<K, V> putAll(K... elements);

		@Override
		public Keys<K, V> putAll(K[] elements, int srcStartIndex, int srcLength);

		@Override
		public Keys<K, V> putAll(XGettingCollection<? extends K> elements);

		@SuppressWarnings("unchecked")
		@Override
		public Keys<K, V> addAll(K... elements);

		@Override
		public Keys<K, V> addAll(K[] elements, int srcStartIndex, int srcLength);

		@Override
		public Keys<K, V> addAll(XGettingCollection<? extends K> elements);

		@Override
		public XEnum<K> copy();

	}

	public interface Values<K, V> extends XBasicTable.Values<K, V>
	{
		@Override
		public XTable<K, V> parent();

		@Override
		public XList<V> copy();

	}

	public interface Bridge<K, V> extends XBasicTable.Bridge<K, V>
	{
		@Override
		public XTable<K, V> parent();

	}
	
	public interface EntriesBridge<K, V> extends XBasicTable.EntriesBridge<K, V>
	{
		@Override
		public XTable<K, V> parent();
	}

}
