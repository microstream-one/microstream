package net.jadoth.util;



/**
 * @author Thomas Muenz
 *
 */
public interface KeyValue<K, V>
{
	public K key();

	public V value();



	public final class Implementation<K, V> implements KeyValue<K, V>, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final K key;
		final V value;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final K key, final V value)
		{
			super();
			this.key   = key;
			this.value = value;
		}

		/**
		 * @return a String of pattern <code>[<i>key</i> -> <i>value</i>]</code>
		 */
		@Override
		public String toString()
		{
			return '[' + String.valueOf(this.key) + " -> " + String.valueOf(this.value) + ']';
		}

		@Override
		public K key()
		{
			return this.key;
		}

		@Override
		public V value()
		{
			return this.value;
		}

	}

}
