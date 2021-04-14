
package one.microstream.storage.restadapter.exceptions;

import one.microstream.exceptions.BaseException;


public class StorageRestAdapterException extends BaseException
{
	public StorageRestAdapterException(
		final String message
	)
	{
		super(message);
	}
	
	public StorageRestAdapterException()
	{
		super();
	}
	
	public StorageRestAdapterException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public StorageRestAdapterException(
		final String message,
		final Throwable cause
	)
	{
		super(message, cause);
	}
	
	public StorageRestAdapterException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
