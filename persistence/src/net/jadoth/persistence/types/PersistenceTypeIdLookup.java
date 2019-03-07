package net.jadoth.persistence.types;

public interface PersistenceTypeIdLookup
{
	public long lookupTypeId(Class<?> type);
}
