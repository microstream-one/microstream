package one.microstream.storage.types;

import one.microstream.storage.exceptions.StorageException;

@FunctionalInterface
public interface StorageExceptionHandler
{
	public void handleException(Throwable exception, StorageChannel channel);
	
	
	
	public static void defaultHandleException(final Throwable exception, final StorageChannel channel)
	{
		// logic encapsulated in static method to be reusable by other implementors.
		if(exception instanceof StorageException)
		{
			throw (StorageException)exception;
		}
		throw new StorageException(exception);
	}
	
	
	
	public static StorageExceptionHandler New()
	{
		return new StorageExceptionHandler.Default();
	}
	
	public final class Default implements StorageExceptionHandler
	{
		Default()
		{
			super();
		}
		
		@Override
		public void handleException(final Throwable exception, final StorageChannel channel)
		{
			StorageExceptionHandler.defaultHandleException(exception, channel);
		}
		
	}
	
}
