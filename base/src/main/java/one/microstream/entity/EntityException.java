
package one.microstream.entity;

import one.microstream.exceptions.BaseException;


/**
 * 
 * 
 */
public class EntityException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public EntityException()
	{
		super();
	}
	
	public EntityException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
	
	public EntityException(final String message)
	{
		super(message);
	}
	
	public EntityException(final Throwable cause)
	{
		super(cause);
	}
	
	public EntityException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
