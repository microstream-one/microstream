package net.jadoth.persistence.types;

public interface PersistenceLoadHandler extends PersistenceObjectIdResolver
{
	public PersistenceObjectRetriever getObjectRetriever();
}
