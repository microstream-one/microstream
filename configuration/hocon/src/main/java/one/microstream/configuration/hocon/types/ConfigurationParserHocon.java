package one.microstream.configuration.hocon.types;

import static one.microstream.X.notNull;

import com.typesafe.config.ConfigFactory;

import one.microstream.configuration.types.Configuration.Builder;
import one.microstream.configuration.types.ConfigurationParser;

public interface ConfigurationParserHocon extends ConfigurationParser
{
	public static ConfigurationParserHocon New()
	{
		return new ConfigurationParserHocon.Default(
			ConfigurationMapperHocon.New()
		);
	}
	
	public static ConfigurationParserHocon New(
		final ConfigurationMapperHocon mapper
	)
	{
		return new ConfigurationParserHocon.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserHocon
	{
		private final ConfigurationMapperHocon mapper;

		Default(
			final ConfigurationMapperHocon mapper
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
				ConfigFactory.parseString(input).root()
			);
		}
		
	}
	
}
