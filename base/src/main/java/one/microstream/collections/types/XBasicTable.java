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

import one.microstream.typing.KeyValue;



/**
 * @param <K> type of contained keys
 * @param <V> type of contained values
 * 
 */
public interface XBasicTable<K, V>
extends
XMap<K, V>,
XGettingTable<K, V>,
XBasicEnum<KeyValue<K, V>>,
XAddingTable<K, V>
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
	public XBasicTable<K, V> copy();

	public boolean hasVolatileValues();
	
	@SuppressWarnings("unchecked")
	@Override
	public XBasicTable<K, V> putAll(KeyValue<K, V>... elements);

	@Override
	public XBasicTable<K, V> putAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XBasicTable<K, V> putAll(XGettingCollection<? extends KeyValue<K, V>> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBasicTable<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XBasicTable<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicTable<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);




	public interface Satellite<K, V> extends XMap.Satellite<K, V>, XGettingTable.Satellite<K, V>
	{
		@Override
		public XBasicTable<K, V> parent();

	}

	public interface Keys<K, V> extends XMap.Keys<K, V>, XGettingTable.Keys<K, V>, XBasicEnum<K>
	{
		@Override
		public XBasicTable<K, V> parent();

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
		public XBasicEnum<K> copy();

	}

	public interface Values<K, V> extends XMap.Values<K, V>, XGettingTable.Values<K, V>, XDecreasingList<V>
	{
		@Override
		public XBasicTable<K, V> parent();

		@Override
		public XList<V> copy();

		@Override
		public XGettingList<V> view();
	}

	public interface Bridge<K, V> extends XMap.Bridge<K, V>, XGettingTable.Bridge<K, V>
	{
		@Override
		public XBasicTable<K, V> parent();
	}
	
	public interface EntriesBridge<K, V> extends XMap.EntriesBridge<K, V>, XGettingTable.EntriesBridge<K, V>
	{
		@Override
		public XBasicTable<K, V> parent();
	}

}

