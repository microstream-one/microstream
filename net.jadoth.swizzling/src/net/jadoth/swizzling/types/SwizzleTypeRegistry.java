package net.jadoth.swizzling.types;

import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.util.Flag;

public interface SwizzleTypeRegistry extends SwizzleTypeLookup
{
	public boolean registerType(long tid, Class<?> type) throws SwizzleExceptionConsistency;
	
	public default boolean registerTypes(final Iterable<? extends SwizzleTypeLink<?>> types)
		throws SwizzleExceptionConsistency
	{
		// validate all type mappings before registering anything
		this.validatePossibleTypeMappings(types);
		
		final Flag hasChanged = Flag.New();
		
		// register type identities (typeId<->type) first to make all types available for type handler creation
		types.forEach(e ->
		{
			if(this.registerType(e.typeId(), e.type()) && hasChanged.isOff())
			{
				hasChanged.on();
			}
		});
		
		return hasChanged.isOn();
	}
	
	

}
