package one.microstream.persistence.types;

public interface PersistenceObjectIdConsumer extends PersistenceAcceptor
{
	public long lookupObjectId(Object instance, PersistenceObjectIdConsumer receiver);
}