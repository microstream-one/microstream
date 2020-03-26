package one.microstream.cache;

public class CacheConfigurationNotFoundException extends CacheConfigurationIoException
{
	public CacheConfigurationNotFoundException()
	{
		super();
	}

	public CacheConfigurationNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CacheConfigurationNotFoundException(String message)
	{
		super(message);
	}

	public CacheConfigurationNotFoundException(Throwable cause)
	{
		super(cause);
	}
	
}
