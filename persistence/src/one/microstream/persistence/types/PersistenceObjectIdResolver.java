package one.microstream.persistence.types;

public interface PersistenceObjectIdResolver
{
	public Object lookupObject(long objectId);
}
