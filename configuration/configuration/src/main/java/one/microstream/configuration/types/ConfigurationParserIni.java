package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.util.HashMap;
import java.util.Map;

import one.microstream.configuration.types.Configuration.Builder;

/**
 * INI format parser for configurations.
 * 
 */
public interface ConfigurationParserIni extends ConfigurationParser
{
	/**
	 * Pseudo-constructor to create a new INI parser.
	 * 
	 * @return a new INI parser
	 */
	public static ConfigurationParserIni New()
	{
		return new ConfigurationParserIni.Default(
			ConfigurationMapperMap.New()
		);
	}
	
	/**
	 * Pseudo-constructor to create a new INI parser.
	 * 
	 * @return a new INI parser
	 */
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
			final Map<String, String> map = new HashMap<>();
			
			nextLine:
			for(String line : input.split("\\r?\\n"))
			{
				line = line.trim();
				if(line.isEmpty())
				{
					continue nextLine;
				}

				switch(line.charAt(0))
				{
					case '#': // comment
					case ';': // comment
					case '[': // section
						continue nextLine;
					default: // fall-through
				}

				final int separatorIndex = line.indexOf('=');
				if(separatorIndex == -1)
				{
					continue nextLine; // no key=value pair, ignore
				}

				final String key   = line.substring(0, separatorIndex).trim();
				final String value = line.substring(separatorIndex + 1).trim();
				map.put(key, value);
			}
			
			return this.mapper.mapConfiguration(builder, map);
		}
		
	}
}
