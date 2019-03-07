package one.microstream.typing;

import one.microstream.collections.types.XGettingTable;

public interface TypeMappingLookup<V>
{
	public boolean contains(TypePair typePair);
	
	public V lookup(TypePair typePair);
		
		
	public default boolean contains(final Class<?> type1, final Class<?> type2)
	{
		return this.contains(TypePair.New(type1, type2));
	}
	
	public default V lookup(final Class<?> type1, final Class<?> type2)
	{
		return this.lookup(TypePair.New(type1, type2));
	}
	
	
	public XGettingTable<TypePair, V> table();
		
}
