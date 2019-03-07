package net.jadoth.persistence.types;

public interface PersistenceInstanceHandler
{
	public void handle(long objectId, Object instance);
}
