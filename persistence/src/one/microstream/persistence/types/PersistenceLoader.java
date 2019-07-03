package one.microstream.persistence.types;

public interface PersistenceLoader<M> extends PersistenceRetrieving
{
	public PersistenceRoots loadRoots();

	public void registerSkip(long objectId);



	public interface Creator<M>
	{
		public PersistenceLoader<M> createLoader(
			PersistenceTypeHandlerLookup<M> typeLookup,
			PersistenceObjectRegistry       registry  ,
			PersistenceSourceSupplier<M>    source
		);
	}

}
