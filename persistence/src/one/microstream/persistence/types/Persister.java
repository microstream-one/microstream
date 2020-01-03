package one.microstream.persistence.types;

public interface Persister extends PersistenceObjectRetriever, PersistenceStoring
{
	@Override
	public Object getObject(long objectId);
	
	@Override
	public long store(Object instance);
	
	@Override
	public long[] storeAll(Object... instances);
	
	@Override
	public void storeAll(Iterable<?> instances);

	
	public Storer createLazyStorer();
	
	public Storer createStorer();

	public Storer createEagerStorer();
	
}
