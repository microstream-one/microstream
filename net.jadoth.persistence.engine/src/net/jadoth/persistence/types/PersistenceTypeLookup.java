package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionConsistency;


public interface PersistenceTypeLookup extends PersistenceTypeIdLookup
{
	@Override
	public long lookupTypeId(Class<?> type);

	public <T> Class<T> lookupType(long typeId);

	public void validateExistingTypeMapping(long typeId, Class<?> type);

	public void validateExistingTypeMappings(Iterable<? extends PersistenceTypeLink> mappings)
		throws PersistenceExceptionConsistency;

	public void validatePossibleTypeMappings(Iterable<? extends PersistenceTypeLink> mappings)
		throws PersistenceExceptionConsistency;

}
