package net.jadoth.swizzling.types;

import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;


public interface SwizzleTypeLookup extends SwizzleTypeIdLookup
{
	@Override
	public long lookupTypeId(Class<?> type);

	public <T> Class<T> lookupType(long typeId);

	public void validateTypeMapping(long typeId, Class<?> type);

	public void validateExistingTypeMappings(Iterable<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency;

	public void validatePossibleTypeMappings(Iterable<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency;

}
