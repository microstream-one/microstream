package one.microstream.storage.types;


public interface StorageController
{
	public StorageController start();

	public boolean shutdown();

	public boolean isAcceptingTasks();

	public boolean isRunning();

	public boolean isStartingUp();

	public boolean isShuttingDown();

	public boolean isShutdown();

	public void checkAcceptingTasks();
	
	public long initializationTime();
	
	public long operationModeTime();
	
	public default long initializationDuration()
	{
		return this.operationModeTime() - this.initializationTime();
	}

}
