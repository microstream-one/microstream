package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.util.Iterator;
import java.util.Optional;

import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingMap;

public interface ConfigurationValueMapperProvider extends Iterable<ConfigurationValueMapper<?>>
{
	public static interface Builder
	{
		public <T> Builder add(ConfigurationValueMapper<T> mapper);
		
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
		
	
	public <T> ConfigurationValueMapper<T> get(Class<T> type);
	
	public <T> Optional<ConfigurationValueMapper<T>> opt(Class<T> type);
	
	
	public static Builder Builder()
	{
		return new Builder.Default();
	}
	
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
