package one.microstream.persistence.types;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;


public interface PersistenceTypeLookup extends PersistenceTypeIdLookup
{
	@Override
	public long lookupTypeId(Class<?> type);

	public <T> Class<T> lookupType(long typeId);

	public boolean validateTypeMapping(long typeId, Class<?> type)
		throws PersistenceExceptionConsistency;

	public boolean validateTypeMappings(Iterable<? extends PersistenceTypeLink> mappings)
		throws PersistenceExceptionConsistency;

}
