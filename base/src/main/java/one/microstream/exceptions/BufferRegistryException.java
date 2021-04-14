
package one.microstream.exceptions;

public class BufferRegistryException extends MemoryException
{
	public BufferRegistryException()
	{
		super();
	}
	
	public BufferRegistryException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public BufferRegistryException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
	
	public BufferRegistryException(final String message)
	{
		super(message);
	}
	
	public BufferRegistryException(final Throwable cause)
	{
		super(cause);
	}
	
}
