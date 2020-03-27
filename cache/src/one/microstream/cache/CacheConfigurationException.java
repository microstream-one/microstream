package one.microstream.cache;

import javax.cache.CacheException;

public class CacheConfigurationException extends CacheException
{
	public CacheConfigurationException()
	{
		super();
	}

	public CacheConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CacheConfigurationException(String message)
	{
		super(message);
	}

	public CacheConfigurationException(Throwable cause)
	{
		super(cause);
	}
}
