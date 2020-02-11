
package one.microstream.storage.restclient;

public class StorageRestClientException extends RuntimeException
{
	public StorageRestClientException()
	{
		super();
	}
	
	public StorageRestClientException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public StorageRestClientException(
		final String message,
		final Throwable cause
	)
	{
		super(message, cause);
	}
	
	public StorageRestClientException(
		final String message
	)
	{
		super(message);
	}
	
	public StorageRestClientException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
