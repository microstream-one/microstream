package one.microstream.cache.types;

import static one.microstream.X.notNull;

import java.util.HashMap;
import java.util.Map;

import one.microstream.cache.exceptions.CacheConfigurationException;
import one.microstream.chars.XChars;

/**
 * 
 * @deprecated replaced by generic {@link one.microstream.configuration.types.ConfigurationParser}, will be removed in a future release
 */
@Deprecated
public interface CacheConfigurationParser
{
	/**
	 * Parses the configuration from the given input.
	 *
	 * @param data the input to parse
	 * @return the parsed configuration
	 * @throws CacheConfigurationException if an error occurs while parsing
	 */
	public CacheConfiguration<?, ?> parse(
		String data
	);

	/**
	 * Parses the configuration from the given input.
	 *
	 * @param data the input to parse
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the parsed configuration
	 * @throws CacheConfigurationException if an error occurs while parsing
	 */
	public <K, V> CacheConfiguration<K, V> parse(
		String data,
		Class<K> keyType,
		Class<V> valueType
	);

	/**
	 * Creates a new {@link CacheConfigurationParser}.
	 */
	public static CacheConfigurationParser New()
	{
		return New(CacheConfigurationPropertyParser.New());
	}

	/**
	 * Creates a new {@link CacheConfigurationParser}.
	 *
	 * @param propertyParser a custom property parser
	 */
	public static CacheConfigurationParser New(
		final CacheConfigurationPropertyParser propertyParser
	)
	{
		return new Default(notNull(propertyParser));
	}


	public static class Default implements CacheConfigurationParser, CacheConfigurationPropertyNames
	{
		private final CacheConfigurationPropertyParser propertyParser;

		Default(
			final CacheConfigurationPropertyParser propertyParser
		)
		{
			super();
			this.propertyParser = propertyParser;
		}

		@Override
		public CacheConfiguration<?, ?> parse(
			final String data
		)
		{
			final Map<String, String> properties = new HashMap<>();
			this.parseProperties(data, properties);

			final Class<?> keyType   = this.valueAsClass(properties.get(KEY_TYPE),   Object.class);
			final Class<?> valueType = this.valueAsClass(properties.get(VALUE_TYPE), Object.class);

			final CacheConfiguration.Builder<?, ?> builder = CacheConfiguration.Builder(
				keyType,
				valueType
			);

			this.propertyParser.parseProperties(properties, builder);

			return builder.build();
		}

		@Override
		public <K, V> CacheConfiguration<K, V> parse(
			final String data,
			final Class<K> keyType,
			final Class<V> valueType
		)
		{
			final Map<String, String> properties = new HashMap<>();
			this.parseProperties(data, properties);

			final CacheConfiguration.Builder<K, V> builder = CacheConfiguration.Builder(
				keyType,
				valueType
			);

			this.propertyParser.parseProperties(properties, builder);

			return builder.build();
		}

		protected Class<?> valueAsClass(
			final String value,
			final Class<?> defaultValue
		)
		{
			try
			{
				return XChars.isEmpty(value)
					? defaultValue
					: Class.forName(value)
				;
			}
			catch(final ClassNotFoundException e)
			{
				throw new CacheConfigurationException(e);
			}
		}

		protected void parseProperties(
			final String data,
			final Map<String, String> properties
		)
		{
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
					default:  // fall-through
				}

				final int separatorIndex = line.indexOf('=');
				if(separatorIndex == -1)
				{
					continue nextLine; // no key=value pair, ignore
				}

				final String name  = line.substring(0, separatorIndex).trim();
				final String value = line.substring(separatorIndex + 1).trim();
				properties.put(name, value);
			}
		}

	}

}
