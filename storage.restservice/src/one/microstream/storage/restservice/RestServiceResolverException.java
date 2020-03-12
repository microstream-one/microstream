
package one.microstream.storage.restservice;

import one.microstream.exceptions.BaseException;


/**
 * Exception thrown by RestServiceResolver
 *
 */
public class RestServiceResolverException extends BaseException
{
	public RestServiceResolverException(
		final String message
	)
	{
		super(message);
	}
	
	public RestServiceResolverException()
	{
		super();
	}
	
	public RestServiceResolverException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public RestServiceResolverException(
		final String message,
		final Throwable cause
	)
	{
		super(message, cause);
	}
	
	public RestServiceResolverException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
