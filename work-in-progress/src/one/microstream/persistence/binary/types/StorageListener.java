package one.microstream.persistence.binary.types;

public interface StorageListener
{
	public default void objectStoredLazy(
		final long   objectId,
		final Object instance
	)
	{
		System.out.println("Lazy: " + objectId + " - " + instance);
	}
	
	public default void objectStoredEager(
		final long   objectId,
		final Object instance
	)
	{
		System.out.println("Eager: " + objectId + " - " + instance);
	}
}
