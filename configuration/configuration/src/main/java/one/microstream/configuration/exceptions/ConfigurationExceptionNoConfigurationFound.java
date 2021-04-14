package one.microstream.configuration.exceptions;

public class ConfigurationExceptionNoConfigurationFound extends ConfigurationException
{
	public ConfigurationExceptionNoConfigurationFound(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(null, message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final String message,
		final Throwable cause
	)
	{
		super(null, message, cause);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final String message
	)
	{
		super(null, message);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final Throwable cause
	)
	{
		super(null, cause);
	}

	public ConfigurationExceptionNoConfigurationFound()
	{
		super(null);
	}
	
}
