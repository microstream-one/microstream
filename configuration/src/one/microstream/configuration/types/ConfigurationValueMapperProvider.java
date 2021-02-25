package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;

import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingMap;

/**
 * Provider for {@link ConfigurationValueMapper}s which are used in a {@link Configuration}.
 * 
 */
public interface ConfigurationValueMapperProvider extends Iterable<ConfigurationValueMapper<?>>
{
	/**
	 * Builder for a {@link ConfigurationValueMapperProvider}.
	 *
	 */
	public static interface Builder
	{
		/**
		 * Adds a value mapper for a certain type. An possibly existing mapper for this type will be overwritten.
		 * 
		 * @param <T> the target type
		 * @param mapper the mapper to add
		 * @return this builder
		 */
		public <T> Builder add(ConfigurationValueMapper<T> mapper);
		
		/**
		 * Builds the resulting {@link ConfigurationValueMapperProvider}.
		 * 
		 * @return a new {@link ConfigurationValueMapperProvider}
		 */
		public ConfigurationValueMapperProvider build();
		
		
		public static class Default implements Builder
		{
			private final HashTable<Class<?>, ConfigurationValueMapper<?>> table = HashTable.New();
			
			Default()
			{
				super();
			}
		
			@Override
			public <T> Builder add(
				final ConfigurationValueMapper<T> mapper
			)
			{
				this.table.put(mapper.type(), mapper);
				return this;
			}
			
			@Override
			public ConfigurationValueMapperProvider build()
			{
				return ConfigurationValueMapperProvider.New(this.table);
			}
			
		}
		
	}
		
	
	/**
	 * Gets the value mapper for the specified type, or <code>null</code> if none was found.
	 * 
	 * @param <T> the target type
	 * @param type the target's type class
	 * @return a {@link ConfigurationValueMapper} or <code>null</code> if none was found
	 */
	public <T> ConfigurationValueMapper<T> get(Class<T> type);
	
	/**
	 * Returns an optional value mapper for the specified type.
	 * @param <T> the target type
	 * @param type the target's type class
	 * @return an {@link Optional} with the value mapper or an empty one if none was found
	 */
	public <T> Optional<ConfigurationValueMapper<T>> opt(Class<T> type);
	
	
	/**
	 * Pseudo-constructor method to create a new empty {@link Builder} for a {@link ConfigurationValueMapperProvider}.
	 * 
	 * @return a new builder
	 */
	public static Builder Builder()
	{
		return new Builder.Default();
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link Builder} for a {@link ConfigurationValueMapperProvider},
	 * with all value mappers which are provided by default for the following types:
	 * <ul>
	 * <li>{@link Boolean}</li>
	 * <li>{@link Byte}</li>
	 * <li>{@link Short}</li>
	 * <li>{@link Integer}</li>
	 * <li>{@link Long}</li>
	 * <li>{@link Float}</li>
	 * <li>{@link Double}</li>
	 * <li>{@link ByteSize}</li>
	 * <li>{@link Duration}</li>
	 * </ul>
	 * 
	 * @return a new builder
	 */
	public static Builder Default()
	{
		return Builder()
			.add(ConfigurationValueMapper.Boolean())
			.add(ConfigurationValueMapper.Byte())
			.add(ConfigurationValueMapper.Short())
			.add(ConfigurationValueMapper.Integer())
			.add(ConfigurationValueMapper.Long())
			.add(ConfigurationValueMapper.Float())
			.add(ConfigurationValueMapper.Double())
			.add(ConfigurationValueMapper.ByteSize())
			.add(ConfigurationValueMapper.Duration())
		;
	}
	
	
	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationValueMapperProvider}.
	 * 
	 * @param table the provided {@link ConfigurationValueMapper}s
	 * @return a new {@link ConfigurationValueMapperProvider}
	 */
	public static ConfigurationValueMapperProvider New(
			final XGettingMap<Class<?>, ConfigurationValueMapper<?>> table
	)
	{
		return new Default(
			notNull(table).immure()
		);
	}
	
	
	public static class Default implements ConfigurationValueMapperProvider
	{
		private final XGettingMap<Class<?>, ConfigurationValueMapper<?>> table;

		Default(
			final XGettingMap<Class<?>, ConfigurationValueMapper<?>> table
		)
		{
			super();
			this.table = table;
		}
	
		@Override
		public Iterator<ConfigurationValueMapper<?>> iterator()
		{
			return this.table.values().iterator();
		}

		@SuppressWarnings("unchecked") // type-safety ensured by logic
		@Override
		public <T> ConfigurationValueMapper<T> get(
			final Class<T> type
		)
		{
			return (ConfigurationValueMapper<T>)this.table.get(type);
		}

		@Override
		public <T> Optional<ConfigurationValueMapper<T>> opt(
			final Class<T> type
		)
		{
			return Optional.ofNullable(this.get(type));
		}
		
	}
	
}
