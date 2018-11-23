package net.jadoth.persistence.types;

public interface PersistenceObjectLookup extends PersistenceObjectIdResolving
{
	public long lookupObjectId(Object object);
}
