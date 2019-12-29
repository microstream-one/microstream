package one.microstream.persistence.types;

public interface PersistenceLoadHandler extends PersistenceObjectLookup
{
	@Override
	public Object lookupObject(long objectId);
	
	/* (28.12.2019 TM)TODO: priv#199: change to something more central like EmbeddedStorageManager.
	 * Tricky on the persistence level ...
	 */
	public PersistenceObjectRetriever getObjectRetriever();
	
	public void validateType(Object object, long objectId);
	
	public void requireRoot(Object rootInstance, long rootObjectId);
	
	@Deprecated
	public void registerCustomRootRefactoring(Object rootInstance, long customRootObjectId);
	
	@Deprecated
	public void registerDefaultRootRefactoring(Object rootInstance, long defaultRootObjectId);
	
}
