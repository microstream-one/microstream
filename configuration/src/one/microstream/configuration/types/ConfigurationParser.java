package one.microstream.configuration.types;

/**
 * A utility interface to parse values from external formats to a {@link Configuration#Builder()}.
 *
 * @see Configuration.Builder#load(ConfigurationMapper, Object)
 * @see ConfigurationMapper
 */
@FunctionalInterface
public interface ConfigurationParser
{
	/**
	 * Creates a {@link Configuration#Builder()} and adds all entries contained in the given input.
	 * 
	 * @param input the source to parse the entries from
	 * @return a new {@link Configuration#Builder()}
	 */
	public default Configuration.Builder parseConfiguration(final String input)
	{
		return this.parseConfiguration(
			Configuration.Builder(),
			input
		);
	}
	
	/**
	 * Parses all entries contained in the input to the given {@link Configuration#Builder()}.
	 * 
	 * @param the builder to map the entries to
	 * @param input the source to parse the entries from
	 * @return the given {@link Configuration#Builder()}
	 */
	public Configuration.Builder parseConfiguration(Configuration.Builder builder, String input);
		
}
