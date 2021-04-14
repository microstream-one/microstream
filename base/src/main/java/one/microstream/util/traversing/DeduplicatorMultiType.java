package one.microstream.util.traversing;

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
