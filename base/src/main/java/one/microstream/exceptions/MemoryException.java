
package one.microstream.exceptions;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class MemoryException extends BaseException
{
	public MemoryException()
	{
		super();
	}
	
	public MemoryException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public MemoryException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
	
	public MemoryException(final String message)
	{
		super(message);
	}
	
	public MemoryException(final Throwable cause)
	{
		super(cause);
	}
	
}
