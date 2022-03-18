package one.microstream.util.traversing;

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

import java.util.function.Function;

import one.microstream.chars.XChars;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingMap;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.KeyValue;


public final class DeduplicatorMultiType implements Function<Object, Object>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static DeduplicatorMultiType New(final Class<?>... types)
	{
		final HashTable<Class<?>, EqHashEnum<Object>> registry = HashTable.New();
		for(final Class<?> type : types)
		{
			registry.add(type, EqHashEnum.New());
		}
		
		return new DeduplicatorMultiType(registry);
	}
	
	public static DeduplicatorMultiType New(final XGettingMap<Class<?>, HashEqualator<Object>> types)
	{
		final HashTable<Class<?>, EqHashEnum<Object>> registry = HashTable.New();
		for(final KeyValue<Class<?>, HashEqualator<Object>> e : types)
		{
			registry.add(e.key(), EqHashEnum.New(e.value()));
		}
		
		return new DeduplicatorMultiType(registry);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final HashTable<Class<?>, EqHashEnum<Object>> registry;
	
		
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	DeduplicatorMultiType(final HashTable<Class<?>, EqHashEnum<Object>> registry)
	{
		super();
		this.registry = registry;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Object apply(final Object instance)
	{
		System.out.println(XChars.systemString(instance));
		
		final EqHashEnum<Object> typeRegistry = this.registry.get(instance.getClass());
		if(typeRegistry == null)
		{
			return instance;
		}
		
		return typeRegistry.deduplicate(instance);
	}

}
