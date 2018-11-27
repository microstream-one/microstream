package net.jadoth.persistence.types;

public interface PersistenceAcceptor
{
	public void accept(long objectId, Object instance);
}