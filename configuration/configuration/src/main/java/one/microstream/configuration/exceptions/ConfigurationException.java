package one.microstream.configuration.exceptions;

import one.microstream.configuration.types.Configuration;
import one.microstream.exceptions.BaseException;

public class ConfigurationException extends BaseException
{
	private final Configuration configuration;
	
	public ConfigurationException(final Configuration configuration)
	{
		super();
		this.configuration = configuration;
	}

	public ConfigurationException(final Configuration configuration, final Throwable cause)
	{
		super(cause);
		this.configuration = configuration;
	}

	public ConfigurationException(final Configuration configuration, final String message)
	{
		super(message);
		this.configuration = configuration;
	}

	public ConfigurationException(final Configuration configuration, final String message, final Throwable cause)
	{
		super(message, cause);
		this.configuration = configuration;
	}

	public ConfigurationException(
		final Configuration configuration     ,
		final String        message           ,
		final Throwable     cause             ,
		final boolean       enableSuppression ,
		final boolean       writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.configuration = configuration;
	}
	
	public Configuration configuration()
	{
		return this.configuration;
	}
	
}
