package one.microstream.persistence.types;

public interface PersistenceLoadHandler extends PersistenceObjectIdResolver
{
	public PersistenceObjectRetriever getObjectRetriever();
}
