package one.microstream.configuration.types;

@FunctionalInterface
public interface ConfigurationParser<T>
{
	public T parseConfiguration(String data);
		
}
