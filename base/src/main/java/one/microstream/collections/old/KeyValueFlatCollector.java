package one.microstream.collections.old;

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

import java.util.function.BiConsumer;

import one.microstream.math.XMath;

public final class KeyValueFlatCollector<K, V> implements BiConsumer<K, V>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <K, V> KeyValueFlatCollector<K, V> New(final int keyValueEntryCount)
	{
		// note that an entry count of 0 is valid (e.g. empty map)
		return new KeyValueFlatCollector<>(
			new Object[XMath.notNegative(keyValueEntryCount) * 2]
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Object[] array;
	
	private int i = 0;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	KeyValueFlatCollector(final Object[] array)
	{
		super();
		this.array = array;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void accept(final K key, final V value)
	{
		this.array[this.i    ] = key  ;
		this.array[this.i + 1] = value;
		this.i += 2;
	}
	
	public final Object[] yield()
	{
		return this.array;
	}
	
}
