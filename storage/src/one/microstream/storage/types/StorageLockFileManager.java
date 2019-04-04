package one.microstream.storage.types;

public interface StorageLockFileManager extends Runnable
{
	public default StorageLockFileManager start()
	{
		this.setRunning(true);
		return this;
	}
	
	public default StorageLockFileManager stop()
	{
		this.setRunning(false);
		return this;
	}
	
	public boolean isRunning();
	
	public StorageLockFileManager setRunning(boolean running);
}
