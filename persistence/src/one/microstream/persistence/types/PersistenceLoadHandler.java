package one.microstream.persistence.types;

public interface PersistenceLoadHandler extends PersistenceObjectLookup
{
	@Override
	public Object lookupObject(long objectId);
	
	public PersistenceObjectRetriever getObjectRetriever();
	
	public void requireRoot(Object object, long objectId);
	
	public void validateType(Object object, long objectId);
	
}
