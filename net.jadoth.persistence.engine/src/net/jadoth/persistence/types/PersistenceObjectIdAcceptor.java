package net.jadoth.persistence.types;

public interface PersistenceObjectIdAcceptor
{
	// (06.02.2019 TM)FIXME: JET-49: refactor to "acceptObjectId".
	public void accept(long objectId);
}
