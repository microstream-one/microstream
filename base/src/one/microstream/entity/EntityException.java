package one.microstream.entity;

/**
 * 
 * @author FH
 */
public class EntityException extends RuntimeException
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
}
