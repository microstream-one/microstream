package one.microstream.persistence.types;

public interface PersistenceLoadHandler extends PersistenceObjectLookup
{
	@Override
	public Object lookupObject(long objectId);
	
	public default PersistenceObjectRetriever getObjectRetriever()
	{
		return this.getPersister();
	}
	
	public Persister getPersister();
	
	public void validateType(Object object, long objectId);
	
	public void requireRoot(Object rootInstance, long rootObjectId);
	
	@Deprecated
	public void registerCustomRootRefactoring(Object rootInstance, long customRootObjectId);
	
	@Deprecated
	public void registerDefaultRootRefactoring(Object rootInstance, long defaultRootObjectId);
	
}
