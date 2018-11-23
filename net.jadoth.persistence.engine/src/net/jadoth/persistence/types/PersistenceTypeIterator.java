package net.jadoth.persistence.types;

public interface PersistenceTypeIterator
{
	public void apply(Class<?> type, long typeId);
}
