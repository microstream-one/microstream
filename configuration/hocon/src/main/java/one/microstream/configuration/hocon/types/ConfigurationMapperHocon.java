package one.microstream.configuration.hocon.types;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationMapper;
import one.microstream.configuration.types.Configuration.Builder;

public interface ConfigurationMapperHocon extends ConfigurationMapper<ConfigObject>
{
	public static ConfigurationMapperHocon New()
	{
		return new ConfigurationMapperHocon.Default();
	}
	
	
	public static class Default implements ConfigurationMapperHocon
	{
		Default()
		{
			super();
		}
		
		@Override
		public Builder mapConfiguration(
			final Builder      builder,
			final ConfigObject source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		private void mapConfiguration(
			final Builder      builder,
			final ConfigObject source ,
			final String       prefix
		)
		{
			source.entrySet().forEach(e ->
			{
				final String      key   = prefix.concat(e.getKey());
				final ConfigValue value = e.getValue();
				if(value instanceof ConfigObject)
				{
					this.mapConfiguration(
						builder,
						(ConfigObject)value,
						key + Configuration.KEY_SEPARATOR
					);
				}
				else if(value != null)
				{
					builder.set(key, value.unwrapped().toString());
				}
			});
		}
		
	}
	
}
