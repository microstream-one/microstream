package one.microstream.configuration.exceptions;

import one.microstream.configuration.types.Configuration;

public class ConfigurationExceptionValueMappingFailed extends ConfigurationException
{
	private final String key  ;
	private final String value;
	
	public ConfigurationExceptionValueMappingFailed(
		final Configuration configuration,
		final String key,
		final String value
	)
	{
		super(configuration);
		this.key   = key;
		this.value = value;
	}

	public ConfigurationExceptionValueMappingFailed(
		final Configuration configuration,
		final Throwable cause,
		final String key,
		final String value
	)
	{
		super(configuration, cause);
		this.key   = key;
		this.value = value;
	}
	
	
	public String key()
	{
		return this.key;
	}
	
	public String value()
	{
		return this.value;
	}
	
}
