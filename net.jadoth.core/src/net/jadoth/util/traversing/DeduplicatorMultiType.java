package net.jadoth.util.traversing;

import java.util.function.Function;

import net.jadoth.chars.XStrings;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.hashing.HashEqualator;
import net.jadoth.typing.KeyValue;


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
		System.out.println(XStrings.systemString(instance));
		
		final EqHashEnum<Object> typeRegistry = this.registry.get(instance.getClass());
		if(typeRegistry == null)
		{
			return instance;
		}
		
		return typeRegistry.deduplicate(instance);
	}

}
