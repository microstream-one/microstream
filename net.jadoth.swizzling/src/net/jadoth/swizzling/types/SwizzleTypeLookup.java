package net.jadoth.swizzling.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;


public interface SwizzleTypeLookup extends SwizzleTypeIdLookup
{
	@Override
	public long lookupTypeId(Class<?> type);

	public <T> Class<T> lookupType(long typeId);

	public void validateTypeMapping(long typeId, Class<?> type);

	public void validateExistingTypeMappings(XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency;

	public void validatePossibleTypeMappings(XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency;

}
