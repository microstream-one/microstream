package one.microstream.configuration.types;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public interface ConfigurationParserYaml extends ConfigurationParser<Map<String, ?>>
{
	public static ConfigurationParserYaml New()
	{
		return data -> new Yaml().load(data);
	}
	
}
