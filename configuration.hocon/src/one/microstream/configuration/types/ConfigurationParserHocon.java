package one.microstream.configuration.types;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

public interface ConfigurationParserHocon extends ConfigurationParser<ConfigObject>
{
	public static ConfigurationParserHocon New()
	{
		return data -> ConfigFactory.parseString(data).root();
	}
	
}
