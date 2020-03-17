package one.microstream.persistence.types;

public interface PersistenceLocalObjectIdRegistry extends PersistenceAcceptor
{
	public long lookupObjectId(Object instance, PersistenceAcceptor receiver);
	
	public void iterateMergeableEntries(PersistenceAcceptor iterator);
}