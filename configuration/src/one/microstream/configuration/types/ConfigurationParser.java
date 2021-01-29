package one.microstream.configuration.types;

@FunctionalInterface
public interface ConfigurationParser
{
	public default Configuration.Builder parseConfiguration(final String input)
	{
		return this.parseConfiguration(
			Configuration.Builder(),
			input
		);
	}
	
	public Configuration.Builder parseConfiguration(Configuration.Builder builder, String input);
		
}
