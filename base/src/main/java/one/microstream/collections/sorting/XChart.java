package one.microstream.collections.sorting;

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

import one.microstream.collections.types.XBasicTable;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XList;
import one.microstream.typing.KeyValue;


public interface XChart<K, V> extends XBasicTable<K, V>, Sorted<KeyValue<K, V>>
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
	public boolean hasVolatileValues();
	
	@SuppressWarnings("unchecked")
	@Override
	public XChart<K, V> putAll(KeyValue<K, V>... elements);

	@Override
	public XChart<K, V> putAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XChart<K, V> putAll(XGettingCollection<? extends KeyValue<K, V>> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XChart<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XChart<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XChart<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);



	public interface Satellite<K, V> extends XBasicTable.Satellite<K, V>
	{
		@Override
		public XChart<K, V> parent();

	}

	public interface Keys<K, V> extends XBasicTable.Keys<K, V>, XSortation<K>
	{
		@Override
		public XChart<K, V> parent();

		@SuppressWarnings("unchecked")
		@Override
		public Keys<K, V> addAll(K... elements);
		
		@Override
		public Keys<K, V> addAll(K[] elements, int srcStartIndex, int srcLength);
		
		@Override
		public Keys<K, V> addAll(XGettingCollection<? extends K> elements);

		@SuppressWarnings("unchecked")
		@Override
		public Keys<K, V> putAll(K... elements);
		
		@Override
		public Keys<K, V> putAll(K[] elements, int srcStartIndex, int srcLength);
		
		@Override
		public Keys<K, V> putAll(XGettingCollection<? extends K> elements);

		@Override
		public XRank<K> toReversed();

		@Override
		public XRank<K> copy();

	}

	public interface Values<K, V> extends XBasicTable.Values<K, V>
	{
		@Override
		public XChart<K, V> parent();

		@Override
		public XList<V> copy();

	}

	public interface Bridge<K, V> extends XBasicTable.Bridge<K, V>
	{
		@Override
		public XChart<K, V> parent();
	}
	
	public interface EntriesBridge<K, V> extends XBasicTable.EntriesBridge<K, V>
	{
		@Override
		public XChart<K, V> parent();
	}

}
