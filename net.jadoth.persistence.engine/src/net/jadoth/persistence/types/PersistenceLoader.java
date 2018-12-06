package net.jadoth.persistence.types;

public interface PersistenceLoader<M> extends PersistenceRetrieving
{
	public PersistenceRoots loadRoots();

	public void registerSkip(long oid);



	public interface Creator<M>
	{
		public PersistenceLoader<M> createBuilder(
			final PersistenceTypeHandlerLookup<M> typeLookup,
			final PersistenceObjectRegistry       registry  ,
			PersistenceSourceSupplier<M>          source
		);
	}

}
