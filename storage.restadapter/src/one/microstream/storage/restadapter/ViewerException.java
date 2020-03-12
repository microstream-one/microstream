
package one.microstream.storage.restadapter;

import one.microstream.exceptions.BaseException;


public class ViewerException extends BaseException
{
	public ViewerException(
		final String message
	)
	{
		super(message);
	}
	
	public ViewerException()
	{
		super();
	}
	
	public ViewerException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public ViewerException(
		final String message,
		final Throwable cause
	)
	{
		super(message, cause);
	}
	
	public ViewerException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
