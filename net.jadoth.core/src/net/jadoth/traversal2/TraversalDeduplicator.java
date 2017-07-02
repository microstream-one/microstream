package net.jadoth.traversal2;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.hash.HashEqualator;
import net.jadoth.util.KeyValue;

public final class TraversalDeduplicator implements TraversalAcceptor
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static TraversalDeduplicator New(final Class<?>... types)
	{
		final HashTable<Class<?>, EqHashEnum<Object>> registry = HashTable.New();
		for(final Class<?> type : types)
		{
			registry.add(type, EqHashEnum.New());
		}
		
		return new TraversalDeduplicator(registry);
	}
	
	public static TraversalDeduplicator New(final XGettingMap<Class<?>, HashEqualator<Object>> types)
	{
		final HashTable<Class<?>, EqHashEnum<Object>> registry = HashTable.New();
		for(final KeyValue<Class<?>, HashEqualator<Object>> e : types)
		{
			registry.add(e.key(), EqHashEnum.New(e.value()));
		}
		
		return new TraversalDeduplicator(registry);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final HashTable<Class<?>, EqHashEnum<Object>> registry;
	
	
	

	TraversalDeduplicator(final HashTable<Class<?>, EqHashEnum<Object>> registry)
	{
		super();
		this.registry = registry;
	}




	@Override
	public Object acceptInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
	{
		final EqHashEnum<Object> typeRegistry = this.registry.get(instance.getClass());
		if(typeRegistry == null)
		{
			return instance;
		}
		
		return typeRegistry.deduplicate(instance);
	}

}
