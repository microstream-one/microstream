
package one.microstream.cache.types;

import one.microstream.typing.KeyValue;


public interface CacheEntry<K, V> extends javax.cache.Cache.Entry<K, V>, KeyValue<K, V>, Unwrappable
{
	@Override
	public default K key()
	{
		return this.getKey();
	}
	
	@Override
	public default V value()
	{
		return this.getValue();
	}
	
	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}
	
	static <K, V> CacheEntry<K, V> New(final K key, final V value)
	{
		return new Default<>(key, value);
	}

	public static class Default<K, V> implements CacheEntry<K, V>
	{
		private final K key;
		private final V value;

		Default(final K key, final V value)
		{
			super();

			this.key   = key;
			this.value = value;
		}

		@Override
		public K getKey()
		{
			return this.key;
		}

		@Override
		public V getValue()
		{
			return this.value;
		}

	}
	
}
