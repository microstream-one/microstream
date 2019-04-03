package one.microstream.storage.types;

public interface StorageLocker extends Runnable
{
	public default StorageLocker start()
	{
		this.setRunning(true);
		return this;
	}
	
	public default StorageLocker stop()
	{
		this.setRunning(false);
		return this;
	}
	
	public boolean isRunning();
	
	public StorageLocker setRunning(boolean running);
}
