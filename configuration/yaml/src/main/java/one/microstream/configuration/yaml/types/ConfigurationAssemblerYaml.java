package one.microstream.configuration.yaml.types;

import static one.microstream.X.notNull;

import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import one.microstream.chars.VarString;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationAssembler;

public interface ConfigurationAssemblerYaml extends ConfigurationAssembler
{
	public static ConfigurationAssemblerYaml New()
	{
		final DumperOptions options = new DumperOptions();
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		
		return new ConfigurationAssemblerYaml.Default(options);
	}
	
	public static ConfigurationAssemblerYaml New(
		final DumperOptions options
	)
	{
		return new ConfigurationAssemblerYaml.Default(
			notNull(options)
		);
	}
	
	
	public static class Default implements ConfigurationAssemblerYaml
	{
		private final DumperOptions options;
		
		Default(
			final DumperOptions options
		)
		{
			super();
			this.options = options;
		}
		
		@Override
		public VarString assemble(
			final VarString     vs           ,
			final Configuration configuration
		)
		{
			return vs.add(
				new Yaml(this.options).dump(this.toMap(configuration))
			);
		}
		
		private Map<String, ?> toMap(
			final Configuration configuration
		)
		{
			final Map<String, Object> map = new HashMap<>();
			
			configuration.keys().forEach(key ->
				map.put(key, configuration.get(key))
			);
			
			configuration.children().forEach(child ->
				map.put(child.key(), this.toMap(child))
			);
			
			return map;
		}
		
	}
	
}
