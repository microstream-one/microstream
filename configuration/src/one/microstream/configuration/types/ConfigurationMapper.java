package one.microstream.configuration.types;

public interface ConfigurationMapper<S>
{
	public default Configuration.Builder mapConfiguration(final S source)
	{
		return this.mapConfiguration(
			Configuration.Builder(),
			source
		);
	}
	
	public Configuration.Builder mapConfiguration(Configuration.Builder builder, S source);
	
}
