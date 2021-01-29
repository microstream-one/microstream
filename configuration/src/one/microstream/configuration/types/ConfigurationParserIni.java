package one.microstream.configuration.types;

import java.util.HashMap;
import java.util.Map;

public interface ConfigurationParserIni extends ConfigurationParser<Map<String, String>>
{
	public static ConfigurationParserIni New()
	{
		return new ConfigurationParserIni.Default();
	}
	
	
	public static class Default implements ConfigurationParserIni
	{
		Default()
		{
			super();
		}
	
		@Override
		public Map<String, String> parseConfiguration(
			final String data
		)
		{
			final Map<String, String> map = new HashMap<>();
			
			nextLine:
			for(String line : data.split("\\r?\\n"))
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
			
			return map;
		}
		
	}
}
