package one.microstream.persistence.types;

public interface PersistenceReferenceLoader extends PersistenceObjectIdAcceptor
{
	public void requireReferenceEager(long objectId);
}
