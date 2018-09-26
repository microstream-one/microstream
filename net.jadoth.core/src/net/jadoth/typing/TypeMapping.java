package net.jadoth.typing;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hashing.HashEqualator;

public interface TypeMapping<T>
{
	public boolean contains(TypePair typePair);
	
	public T lookup(TypePair typePair);
	
	public boolean add(TypePair typePair, T value);
	
	public boolean put(TypePair typePair, T value);
	
	public TypeMapping<T> register(TypePair typePair, T value);
	
		
	public default boolean contains(final Class<?> type1, final Class<?> type2)
	{
		return this.contains(TypePair.New(type1, type2));
	}
	
	public default T lookup(final Class<?> type1, final Class<?> type2)
	{
		return this.lookup(TypePair.New(type1, type2));
	}
	
	public default boolean add(final Class<?> type1, final Class<?> type2, final T value)
	{
		return this.add(TypePair.New(type1, type2), value);
	}
	
	public default boolean put(final Class<?> type1, final Class<?> type2, final T value)
	{
		return this.put(TypePair.New(type1, type2), value);
	}
	
	public default TypeMapping<T> register(final Class<?> type1, final Class<?> type2, final T value)
	{
		this.register(TypePair.New(type1, type2), value);
		return this;
	}
		
	
	public XGettingTable<TypePair, T> table();
	
	
	
	public static <T> TypeMapping<T> New()
	{
		return New(TypePair.HashEquality());
	}
	
	public static <T> TypeMapping<T> New(final HashEqualator<? super TypePair> hashEquality)
	{
		return new TypeMapping.Implementation<>(
			EqHashTable.New(hashEquality)
		);
	}
	
	public final class Implementation<V> implements TypeMapping<V>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<TypePair, V> table;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final EqHashTable<TypePair, V> table)
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
		public final XGettingTable<TypePair, V> table()
		{
			return this.table;
		}
		
	}
	
}
