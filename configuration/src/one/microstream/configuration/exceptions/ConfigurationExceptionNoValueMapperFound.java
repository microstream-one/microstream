package one.microstream.configuration.exceptions;

import one.microstream.configuration.types.Configuration;

public class ConfigurationExceptionNoValueMapperFound extends ConfigurationException
{
	private final Class<?> type;
	
	public ConfigurationExceptionNoValueMapperFound(
		final Configuration configuration,
		final Class<?>      type
		
	)
	{
		super(
			configuration,
			"No configuration value mapper found for type " + type.getName()
		);
		this.type = type;
	}
	
	public Class<?> type()
	{
		return this.type;
	}
	
}
