package one.microstream.persistence.types;

public interface PersistenceTypeIdLookup
{
	public long lookupTypeId(Class<?> type);
}
