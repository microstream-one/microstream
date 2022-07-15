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

import java.util.function.Consumer;

import one.microstream.typing.KeyValue;

public interface XGetting2DMap<K1, K2, V> extends XIterable<KeyValue<K1, ? extends XGettingMap<K2, V>>>
{
	public XGettingMap<K1, ? extends XGettingMap<K2, V>> get();
	
	public XGettingMap<K2, V> get(K1 key1);
	
	public V get(K1 key1, K2 key2);
	
	public <PK1 extends Consumer<? super K1>> PK1 iterateKeys1(PK1 procedure);
	
	public <PK2 extends Consumer<? super K2>> PK2 iterateKeys2(PK2 procedure);
	
	public <PV extends Consumer<? super V>> PV iterateValues(PV procedure);
	
	public <PIE extends Consumer<? super KeyValue<K2, V>>> PIE iterateInnerEntries(PIE procedure);
		
}
