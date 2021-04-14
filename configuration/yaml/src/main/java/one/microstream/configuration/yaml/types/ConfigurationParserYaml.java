package one.microstream.configuration.yaml.types;

import static one.microstream.X.notNull;

import org.yaml.snakeyaml.Yaml;

import one.microstream.configuration.types.ConfigurationMapperMap;
import one.microstream.configuration.types.ConfigurationParser;
import one.microstream.configuration.types.Configuration.Builder;

public interface ConfigurationParserYaml extends ConfigurationParser
{
	public static ConfigurationParserYaml New()
	{
		return new ConfigurationParserYaml.Default(
			ConfigurationMapperMap.New()
		);
	}
	
	public static ConfigurationParserYaml New(
		final ConfigurationMapperMap mapper
	)
	{
		return new ConfigurationParserYaml.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserYaml
	{
		private final ConfigurationMapperMap mapper;
		
		Default(
			final ConfigurationMapperMap mapper
		)
		{
			super();
			this.mapper = mapper;
		}
	
		@Override
		public Builder parseConfiguration(
			final Builder builder,
			final String  input
		)
		{
			return this.mapper.mapConfiguration(
				builder,
				new Yaml().load(input)
			);
		}
		
	}
}
