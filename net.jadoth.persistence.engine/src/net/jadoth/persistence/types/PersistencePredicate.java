package net.jadoth.persistence.types;

public interface PersistencePredicate
{
	public boolean test(long objectId, Object instance);
}