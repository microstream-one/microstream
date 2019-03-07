package net.jadoth.storage.types;

import net.jadoth.storage.exceptions.StorageException;

public interface StorageExceptionHandler
{
	public void handleException(Throwable exception, StorageChannel channel);
	
	
	
	public final class Implementation implements StorageExceptionHandler
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
