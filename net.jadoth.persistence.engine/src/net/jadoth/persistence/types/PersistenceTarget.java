package net.jadoth.persistence.types;

import java.io.File;

import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;

public interface PersistenceTarget<M>
{
	public void write(M[] data) throws PersistenceExceptionTransfer;
	
	/**
	 * Prepare to write to this target. E.g. open a defined {@link File}.
	 * 
	 */
	public default void prepareTarget()
	{
		// no-op by default.
	}
	
	/**
	 * Take actions to deactivate/close/destroy the target because it won't be written to again.
	 */
	public default void closeTarget()
	{
		// no-op by default.
	}
}
