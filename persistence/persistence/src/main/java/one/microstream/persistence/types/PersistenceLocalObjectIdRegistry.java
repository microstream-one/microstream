package one.microstream.persistence.types;

public interface PersistenceLocalObjectIdRegistry<D> extends PersistenceObjectIdRequestor<D>
{
	public PersistenceObjectManager<D> parentObjectManager();
	
	public <T> long lookupObjectId(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);
	
	public void iterateMergeableEntries(PersistenceAcceptor iterator);
}