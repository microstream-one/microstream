package one.microstream.storage.types;

public interface StorageKillable
{
	/**
	 * Stops all threads, releases all resources (e.g. close files) without considering any internal state
	 * or waiting for any action to be completed.<p>
	 * Useful only in simple error cases, for example
	 * 
	 * @param cause
	 */
	public void killStorage(Throwable cause);
}
