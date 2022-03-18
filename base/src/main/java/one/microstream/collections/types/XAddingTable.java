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

public interface XAddingTable<K, V> extends XAddingMap<K, V>, XAddingSequence<KeyValue<K, V>>
{
	public interface Creator<K, V>
	{
		public XAddingTable<K, V> newInstance();
	}


	@SuppressWarnings("unchecked")
	@Override
	public XAddingTable<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XAddingTable<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingTable<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);


}
