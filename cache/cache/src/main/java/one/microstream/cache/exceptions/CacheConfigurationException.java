package one.microstream.cache.exceptions;

import javax.cache.CacheException;

/**
 * 
 * @deprecated will be removed in a future release
 */
@Deprecated
public class CacheConfigurationException extends CacheException
{
	public CacheConfigurationException()
	{
		super();
	}

	public CacheConfigurationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public CacheConfigurationException(final String message)
	{
		super(message);
	}

	public CacheConfigurationException(final Throwable cause)
	{
		super(cause);
	}
}
