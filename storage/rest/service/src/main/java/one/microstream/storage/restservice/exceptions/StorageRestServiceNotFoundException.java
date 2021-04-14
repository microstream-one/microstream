
package one.microstream.storage.restservice.exceptions;

import one.microstream.exceptions.BaseException;


/**
 * Exception thrown by RestServiceResolver
 *
 */
public class StorageRestServiceNotFoundException extends BaseException
{
	public StorageRestServiceNotFoundException(
		final String message
	)
	{
		super(message);
	}
	
	public StorageRestServiceNotFoundException()
	{
		super();
	}
	
	public StorageRestServiceNotFoundException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public StorageRestServiceNotFoundException(
		final String message,
		final Throwable cause
	)
	{
		super(message, cause);
	}
	
	public StorageRestServiceNotFoundException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
