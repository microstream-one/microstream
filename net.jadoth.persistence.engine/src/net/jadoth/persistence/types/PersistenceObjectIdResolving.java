package net.jadoth.persistence.types;

public interface PersistenceObjectIdResolving
{
	public Object lookupObject(long oid);
}
