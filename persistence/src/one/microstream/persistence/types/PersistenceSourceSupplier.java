package one.microstream.persistence.types;

public interface PersistenceSourceSupplier<D> extends PersistenceObjectRetriever
{
	@Override
	public Object getObject(long objectId);

	public PersistenceSource<D> source();
}
