package one.microstream.persistence.types;

public interface PersistenceObjectIdResolver extends PersistenceObjectLookup
{
	@Override
	public Object lookupObject(long objectId);
	
	public PersistenceObjectRetriever getObjectRetriever();
	
	public void registerRoot(Object object, long objectId);
	
}
