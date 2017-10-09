package net.jadoth.swizzling.types;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.util.KeyValue;

public interface SwizzleTypeRegistry extends SwizzleTypeLookup
{
	public boolean registerType(long tid, Class<?> type) throws SwizzleExceptionConsistency;
	
	public default boolean registerIdToTypeMappings(final XGettingMap<Long, Class<?>> types)
	{
		synchronized(this)
		{
			for(final KeyValue<Long, Class<?>> e : types)
			{
				this.validatePossibleTypeMapping(e.key(), e.value());
			}
			
			final long currentSize = this.typeCount();
			
			for(final KeyValue<Long, Class<?>> e : types)
			{
				this.registerType(e.key(), e.value());
			}
			
			return this.typeCount() > currentSize;
		}
	}
	
	public default boolean registerTypeToIdMappings(final XGettingMap<Class<?>, Long> types)
	{
		synchronized(this)
		{
			for(final KeyValue<Class<?>, Long> e : types)
			{
				this.validatePossibleTypeMapping(e.value(), e.key());
			}
			
			final long currentSize = this.typeCount();
			
			for(final KeyValue<Class<?>, Long> e : types)
			{
				this.registerType(e.value(), e.key());
			}
			
			return this.typeCount() > currentSize;
		}
	}

}
