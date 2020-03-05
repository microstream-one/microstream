package one.microstream.storage.types;

import one.microstream.storage.exceptions.StorageException;

public interface StorageController extends StorageActivePart, AutoCloseable
{
	public StorageController start();

	public boolean shutdown();

	public boolean isAcceptingTasks();

	public boolean isRunning();

	public boolean isStartingUp();

	public boolean isShuttingDown();

	public default boolean isShutdown()
	{
		return !this.isRunning();
	}

	public void checkAcceptingTasks();
	
	public long initializationTime();
	
	public long operationModeTime();
	
	public default long initializationDuration()
	{
		return this.operationModeTime() - this.initializationTime();
	}
	
	@Override
	public default void close() throws StorageException
	{
		boolean success;
		try
		{
			success = this.shutdown();
		}
		catch(final Exception e)
		{
			// (09.12.2019 TM)EXCP: proper exception
			throw new StorageException("Shutdown failed.", e);
		}
		
		if(!success)
		{
			// (09.12.2019 TM)EXCP: proper exception
			throw new StorageException("Shutdown failed.");
		}
	}

}
