package one.microstream.configuration.types;

import one.microstream.configuration.exceptions.ConfigurationExceptionValueMappingFailed;

/**
 * Function which maps String values from {@link Configuration}s to a certain type.
 *
 * @param <T> the target type
 */
@FunctionalInterface
public interface ConfigurationValueMappingFunction<T>
{
	/**
	 * Maps the given value of a {@link Configuration} to the target type.
	 * 
	 * @param config source configuration
	 * @param key the assigned key
	 * @param value the value to map
	 * @return the mapped value
	 * @throws ConfigurationExceptionValueMappingFailed if the mapping failed
	 */
	public T map(Configuration config, String key, String value);
}
