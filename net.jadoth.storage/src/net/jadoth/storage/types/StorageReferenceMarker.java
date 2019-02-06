package net.jadoth.storage.types;

import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;

public interface StorageReferenceMarker extends PersistenceObjectIdAcceptor
{
	public boolean tryFlush();
}
