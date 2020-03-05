package one.microstream.persistence.types;

public interface PersistenceLoader extends PersistenceRetrieving
{
	public PersistenceRoots loadRoots();

	public void registerSkip(long objectId);



	public interface Creator<D>
	{
		public PersistenceLoader createLoader(
			PersistenceTypeHandlerLookup<D> typeLookup,
			PersistenceObjectRegistry       registry  ,
			Persister                       persister ,
			PersistenceSourceSupplier<D>    source
		);
	}

}
