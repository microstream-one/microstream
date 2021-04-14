package one.microstream.cache.exceptions;

/**
 * 
 * @deprecated will be removed in a future release
 */
@Deprecated
public class CacheConfigurationNotFoundException extends CacheConfigurationIoException
{
	public CacheConfigurationNotFoundException()
	{
		super();
	}

	public CacheConfigurationNotFoundException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public CacheConfigurationNotFoundException(final String message)
	{
		super(message);
	}

	public CacheConfigurationNotFoundException(final Throwable cause)
	{
		super(cause);
	}
	
}
