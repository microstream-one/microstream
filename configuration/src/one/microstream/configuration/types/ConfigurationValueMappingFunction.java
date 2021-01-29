package one.microstream.configuration.types;

public interface ConfigurationValueMappingFunction<T>
{
	public T map(Configuration config, String key, String value);
}
