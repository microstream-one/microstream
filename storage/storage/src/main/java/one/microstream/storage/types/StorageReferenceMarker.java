package one.microstream.storage.types;

import one.microstream.persistence.types.PersistenceObjectIdAcceptor;

public interface StorageReferenceMarker extends PersistenceObjectIdAcceptor
{
	public boolean tryFlush();
	
	public void reset();
}
