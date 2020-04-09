package one.microstream.persistence.types;

public interface PersistenceLocalObjectIdRegistry extends PersistenceObjectIdRequestor
{
	public PersistenceObjectManager parentObjectManager();
	
	public long lookupObjectId(Object instance, PersistenceObjectIdRequestor objectIdRequestor);
	
	public void iterateMergeableEntries(PersistenceAcceptor iterator);
}