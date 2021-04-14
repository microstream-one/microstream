package one.microstream.storage.types;

public interface StorageChannelResetablePart extends StorageHashChannelPart
{
	/**
	 * Closes all resources (files, locks, etc.).
	 * Clears all variable length items (cache, registry, etc.).
	 * Resets internal state to initial values.
	 * For itself and all its parts (entity cache, file manager, etc.).
	 * Basically a "back to just being born" action.
	 */
	public void reset();
}
