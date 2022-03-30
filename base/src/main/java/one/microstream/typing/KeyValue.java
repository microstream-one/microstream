package one.microstream.typing;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

/**
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public interface KeyValue<K, V>
{
	public K key();

	public V value();


	
	public static <K, V> KeyValue<K, V> New(final K key, final V value)
	{
		return new KeyValue.Default<>(key, value);
	}

	public final class Default<K, V> implements KeyValue<K, V>, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final K key;
		final V value;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final K key, final V value)
		{
			super();
			this.key   = key;
			this.value = value;
		}

		/**
		 * @return a String of pattern {@code [<i>key</i> -> <i>value</i>]}
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
