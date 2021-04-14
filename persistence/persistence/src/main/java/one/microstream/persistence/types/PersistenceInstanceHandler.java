package one.microstream.persistence.types;

public interface PersistenceInstanceHandler
{
	public void handle(long objectId, Object instance);
}
