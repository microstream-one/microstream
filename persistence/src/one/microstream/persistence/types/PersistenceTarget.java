package one.microstream.persistence.types;

import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;

public interface PersistenceTarget<M>
{
	public void write(M data) throws PersistenceExceptionTransfer;
	
	/**
	 * Prepare to write to this target. E.g. open a defined file.
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
