package one.microstream.storage.types;

public interface StorageActivePart
{
	/**
	 * Queries whether the part is actually active right now. This might return <code>true</code> even
	 * despite some "running" flag being set to <code>false</code> because there might be one last
	 * loop cycle execution before checking the "running" flag again.
	 * 
	 * @return if the part is actually active right now.
	 */
	public boolean isActive();
}
