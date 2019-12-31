package one.microstream.persistence.types;

public interface PersistenceLoader<D> extends PersistenceRetrieving
{
	public PersistenceRoots loadRoots();

	public void registerSkip(long objectId);



	public interface Creator<D>
	{
		public PersistenceLoader<D> createLoader(
			PersistenceTypeHandlerLookup<D> typeLookup,
			PersistenceObjectRegistry       registry  ,
			PersistenceSourceSupplier<D>    source
		);
	}

}
