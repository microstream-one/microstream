package one.microstream.storage.types;


public interface StorageController extends StorageActivePart
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

}
