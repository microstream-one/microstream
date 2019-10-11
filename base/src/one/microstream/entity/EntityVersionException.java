
package one.microstream.entity;

public class EntityVersionException extends EntityException
{
	public EntityVersionException()
	{
		super();
	}
	
	public EntityVersionException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
	
	public EntityVersionException(final String message)
	{
		super(message);
	}
	
	public EntityVersionException(final Throwable cause)
	{
		super(cause);
	}
}
