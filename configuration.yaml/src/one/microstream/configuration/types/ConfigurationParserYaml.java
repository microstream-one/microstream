package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import org.yaml.snakeyaml.Yaml;

import one.microstream.configuration.types.Configuration.Builder;

public interface ConfigurationParserYaml extends ConfigurationParser
{
	public static ConfigurationParserIni New()
	{
		return new ConfigurationParserIni.Default(
			ConfigurationMapperMap.New()
		);
	}
	
	public static ConfigurationParserIni New(
		final ConfigurationMapperMap mapper
	)
	{
		return new ConfigurationParserIni.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserIni
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
