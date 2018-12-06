package net.jadoth.persistence.types;



public interface PersistenceLoader<M> extends PersistenceRetrieving
{
	public PersistenceRoots loadRoots();

	public void registerSkip(long oid);



	public interface Creator<M>
	{
		public PersistenceLoader<M> createBuilder(
			PersistenceContext<M>        district,
			PersistenceSourceSupplier<M> source
		);
	}

}
