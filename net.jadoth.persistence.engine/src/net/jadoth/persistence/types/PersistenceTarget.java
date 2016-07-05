package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;

public interface PersistenceTarget<M>
{
	public void write(M[] data) throws PersistenceExceptionTransfer;
}
