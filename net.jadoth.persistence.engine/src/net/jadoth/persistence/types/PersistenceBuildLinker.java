package net.jadoth.persistence.types;

public interface PersistenceBuildLinker extends PersistenceObjectIdResolver
{
	public PersistenceObjectRetriever getObjectRetriever();
}
