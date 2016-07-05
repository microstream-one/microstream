package net.jadoth.storage.types;


public interface StorageController
{
	public default StorageController start()
	{
		return this.start(null, null);
	}

	public StorageController start(
		StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		StorageTypeDictionary       oldTypes
	);

	public boolean shutdown();

	public boolean isAcceptingTasks();

	public boolean isRunning();

	public boolean isStartingUp();

	public boolean isShuttingDown();

	public boolean isShutdown();

	public void checkAcceptingTasks();

}
