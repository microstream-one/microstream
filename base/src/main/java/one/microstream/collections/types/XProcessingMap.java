package one.microstream.collections.types;

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

public interface XProcessingMap<K, V> extends XRemovingMap<K, V>, XGettingMap<K, V>
{
	public interface Creator<K, V> extends XRemovingMap.Factory<K, V>, XGettingMap.Creator<K, V>
	{
		@Override
		public XProcessingMap<K, V> newInstance();
	}

	/*
	 * 'removeFor' to avoid ambiguity with XProcessingCollection for the name "remove".
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
