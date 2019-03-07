package net.jadoth.persistence.types;

public interface PersistenceObjectIdResolver
{
	public Object lookupObject(long oid);
}
