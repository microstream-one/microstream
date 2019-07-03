package one.microstream.persistence.types;

public interface PersistenceSourceSupplier<M> extends PersistenceObjectRetriever
{
	@Override
	public Object getObject(long objectId);

	public PersistenceSource<M> source();
}
