package one.microstream.storage.types;

import one.microstream.storage.exceptions.StorageException;

public interface StorageExceptionHandler
{
	public void handleException(Throwable exception, StorageChannel channel);
	
	
	
	public final class Default implements StorageExceptionHandler
	{
		@Override
		public void handleException(final Throwable exception, final StorageChannel channel)
		{
			if(exception instanceof StorageException)
			{
				throw (StorageException)exception;
			}
			throw new StorageException(exception);
		}
		
	}
	
}
