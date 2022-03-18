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

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XTable;
import one.microstream.hashing.HashEqualator;

public interface TypeMapping<V> extends TypeMappingLookup<V>
{
	public boolean add(TypePair typePair, V value);
	
	public boolean put(TypePair typePair, V value);
	
	public TypeMapping<V> register(TypePair typePair, V value);
	
	
	
	public default boolean add(final Class<?> type1, final Class<?> type2, final V value)
	{
		return this.add(TypePair.New(type1, type2), value);
	}
	
	public default boolean put(final Class<?> type1, final Class<?> type2, final V value)
	{
		return this.put(TypePair.New(type1, type2), value);
	}
	
	public default TypeMapping<V> register(final Class<?> type1, final Class<?> type2, final V value)
	{
		this.register(TypePair.New(type1, type2), value);
		return this;
	}
	

	@Override
	public XTable<TypePair, V> table();
	
	

	public static <T> TypeMapping<T> New()
	{
		return New(TypePair.HashEquality());
	}
	
	public static <T> TypeMapping<T> New(final HashEqualator<? super TypePair> hashEquality)
	{
		return new TypeMapping.Default<>(
			EqHashTable.New(hashEquality)
		);
	}
	
	public final class Default<V> implements TypeMapping<V>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<TypePair, V> table;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final EqHashTable<TypePair, V> table)
		{
			super();
			this.table = table;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean contains(final TypePair typePair)
		{
			synchronized(this.table)
			{
				return this.table.keys().contains(typePair);
			}
		}

		@Override
		public final V lookup(final TypePair typePair)
		{
			synchronized(this.table)
			{
				return this.table.get(typePair);
			}
		}

		@Override
		public final boolean add(final TypePair typePair, final V value)
		{
			synchronized(this.table)
			{
				return this.table.add(typePair, value);
			}
		}

		@Override
		public final boolean put(final TypePair typePair, final V value)
		{
			synchronized(this.table)
			{
				return this.table.put(typePair, value);
			}
		}

		@Override
		public final TypeMapping<V> register(final TypePair typePair, final V value)
		{
			synchronized(this.table)
			{
				// registering without feedback is a definite command that must be reliable, hence put.
				this.table.put(typePair, value);
			}
			
			return this;
		}

		@Override
		public final XTable<TypePair, V> table()
		{
			return this.table;
		}
		
	}
	
}
