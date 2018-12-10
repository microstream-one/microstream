package net.jadoth.persistence.types;

public interface PersistenceObjectIdHolder
{
	public long currentObjectId();

	public PersistenceObjectIdHolder updateCurrentObjectId(long currentObjectId);
	
}
