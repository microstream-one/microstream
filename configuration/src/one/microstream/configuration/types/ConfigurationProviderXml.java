package one.microstream.configuration.types;

public interface ConfigurationProviderXml extends ConfigurationProvider
{
	public static ConfigurationProvider New(
		final ConfigurationLoader loader
	)
	{
		return ConfigurationProvider.New(
			loader                      ,
			ConfigurationParserXml.New(),
			ConfigurationMapperXml.New()
		);
	}
	
}
