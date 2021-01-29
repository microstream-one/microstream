package one.microstream.configuration.types;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.util.Optional;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingTable;
import one.microstream.configuration.exceptions.ConfigurationExceptionNoValueMapperFound;
import one.microstream.typing.KeyValue;
import one.microstream.util.cql.CQL;

public interface Configuration
{
	public final static char KEY_SEPARATOR = '.';
	
	
	public static interface Builder
	{
		public Builder mapperProvider(ConfigurationValueMapperProvider mapperProvider);
		
		public Builder set(String key, String value);
		
		public Builder setAll(XGettingCollection<KeyValue<String, String>> properties);

		@SuppressWarnings("unchecked")
		public Builder setAll(KeyValue<String, String>... properties);
		
		public Builder child(String key);
		
		public Configuration buildConfiguration();
		
		
		public static class Default implements Builder
		{
			private final String                               key;
			private       ConfigurationValueMapperProvider     mapperProvider;
			private final EqHashTable<String, Builder.Default> children   = EqHashTable.New();
			private final EqHashTable<String, String>          properties = EqHashTable.New();
			
			Default()
			{
				this(null);
			}

			private Default(
				final String key
			)
			{
				super();
				this.key = key;
			}
			
			private Builder.Default ensureChild(
				final String key
			)
			{
				notEmpty(key);
				
				return this.children.ensure(
					key,
					Builder.Default::new
				);
			}

			@Override
			public Builder mapperProvider(
				final ConfigurationValueMapperProvider mapperProvider
			)
			{
				this.mapperProvider = mapperProvider;
				
				return this;
			}

			@Override
			public Builder set(
				final String key  ,
				final String value
			)
			{
				notEmpty(key);
				notNull(value);
				
				int separator;
				if((separator = key.lastIndexOf(KEY_SEPARATOR)) > 0)
				{
					this.child(key.substring(0, separator))
						.set(key.substring(separator + 1), value)
					;
				}
				else
				{
					this.properties.put(key, value);
				}
				
				return this;
			}

			@Override
			public Builder child(
				final String key
			)
			{
				notEmpty(key);
					
				int separator;
				if((separator = key.indexOf(KEY_SEPARATOR)) > 0)
				{
					return this.ensureChild(key.substring(0, separator))
						.child(key.substring(separator + 1))
					;
				}
				
				return this.ensureChild(key);
			}

			@Override
			public Builder setAll(
				final XGettingCollection<KeyValue<String, String>> properties
			)
			{
				properties.iterate(kv -> this.set(kv.key(), kv.value()));
				
				return this;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Builder setAll(
				final KeyValue<String, String>... properties
			)
			{
				for(final KeyValue<String, String> kv : properties)
				{
					this.set(kv.key(), kv.value());
				}
				
				return this;
			}

			@Override
			public Configuration.Default buildConfiguration()
			{
				final ConfigurationValueMapperProvider mapperProvider = this.mapperProvider != null
					? this.mapperProvider
					: ConfigurationValueMapperProvider.Default().build()
				;
				
				final EqHashTable<String, Configuration.Default> children =
					CQL.from(this.children)
						.project(kv -> X.KeyValue(kv.key(), kv.value().buildConfiguration()))
						.executeInto(EqHashTable.New())
				;
				
				final Configuration.Default config = new Configuration.Default(
					this.key,
					children.immure(),
					this.properties.immure(),
					mapperProvider
				);
				
				children.values().iterate(child -> child.setParent(config));
				
				return config;
			}
			
		}
		
	}
	
	
	public static Builder Builder()
	{
		return new Builder.Default();
	}
	
	
	public String get(String key);
	
	public default Boolean getBoolean(final String key)
	{
		return this.get(key, Boolean.class);
	}
	
	public default Byte getByte(final String key)
	{
		return this.get(key, Byte.class);
	}
	
	public default Short getShort(final String key)
	{
		return this.get(key, Short.class);
	}
	
	public default Integer getInteger(final String key)
	{
		return this.get(key, Integer.class);
	}
	
	public default Long getLong(final String key)
	{
		return this.get(key, Long.class);
	}
	
	public default Float getFloat(final String key)
	{
		return this.get(key, Float.class);
	}
	
	public default Double getDouble(final String key)
	{
		return this.get(key, Double.class);
	}
	
	public <T> T get(String key, Class<T> type);
	
	public default Optional<String> opt(final String key)
	{
		return Optional.ofNullable(this.get(key));
	}
	
	public default Optional<Boolean> optBoolean(final String key)
	{
		return this.opt(key, Boolean.class);
	}
	
	public default Optional<Byte> optByte(final String key)
	{
		return this.opt(key, Byte.class);
	}
	
	public default Optional<Short> optShort(final String key)
	{
		return this.opt(key, Short.class);
	}
	
	public default Optional<Integer> optInteger(final String key)
	{
		return this.opt(key, Integer.class);
	}
	
	public default Optional<Long> optLong(final String key)
	{
		return this.opt(key, Long.class);
	}
	
	public default Optional<Float> optFloat(final String key)
	{
		return this.opt(key, Float.class);
	}
	
	public default Optional<Double> optDouble(final String key)
	{
		return this.opt(key, Double.class);
	}
	
	public default <T> Optional<T> opt(final String key, final Class<T> type)
	{
		return Optional.ofNullable(this.get(key, type));
	}
	
	public boolean contains(String key);
	
	public String key();
	
	public Iterable<String> keys();
	
	public Configuration child(String key);
	
	public Iterable<? extends Configuration> children();
		
	public Configuration parent();
	
	public default boolean isRoot()
	{
		return this.parent() == null;
	}
	
	public default Configuration root()
	{
		Configuration root = this;
		Configuration parent;
		while((parent = root.parent()) != null)
		{
			root = parent;
		}
		return root;
	}
	
	public void traverse(Consumer<Configuration> consumer);

	public XGettingTable<String, String> table();
	
	public XGettingTable<String, String> coalescedTable();
	
	public ConfigurationValueMapperProvider mapperProvider();
	
	public Configuration detach();
	
	
	public static class Default implements Configuration
	{
		private final String                                         key           ;
		private       Configuration                                  parent        ;
		private final XGettingTable<String, ? extends Configuration> children      ;
		private final XGettingTable<String, String>                  properties    ;
		private final ConfigurationValueMapperProvider               mapperProvider;
		private       XGettingTable<String, String>                  coalescedTable;
		
		Default(
			final String                                         key           ,
			final XGettingTable<String, ? extends Configuration> children      ,
			final XGettingTable<String, String>                  properties    ,
			final ConfigurationValueMapperProvider               mapperProvider
		)
		{
			super();
			this.key            = key           ;
			this.children       = children      ;
			this.properties     = properties    ;
			this.mapperProvider = mapperProvider;
		}
		
		void setParent(final Configuration parent)
		{
			this.parent = parent;
		}
		
		@Override
		public String get(
			final String key
		)
		{
			notEmpty(key);
			
			int separator;
			if((separator = key.lastIndexOf(KEY_SEPARATOR)) > 0)
			{
				final Configuration child = this.child(key.substring(0, separator));
				return child == null
					? null
					: child.get(key.substring(separator + 1))
				;
			}
			
			return this.properties.get(key);
		}

		@Override
		public Configuration child(
			final String key
		)
		{
			notEmpty(key);
			
			int separator;
			if((separator = key.indexOf(KEY_SEPARATOR)) > 0)
			{
				final Configuration child = this.children.get(key.substring(0, separator));
				return child == null
					? null
					: child.child(key.substring(separator + 1))
				;
			}
			
			return this.children.get(key);
		}

		@Override
		public <T> T get(
			final String   key ,
			final Class<T> type
		)
		{
			notNull(type);
			// key is checked in call below
			final String value = this.get(key);
			if(value == null)
			{
				return null;
			}
			
			final ConfigurationValueMapper<T> mapper = this.mapperProvider.get(type);
			if(mapper == null)
			{
				throw new ConfigurationExceptionNoValueMapperFound(this, type);
			}
			
			return mapper.map(this, key, value);
		}

		@Override
		public boolean contains(
			final String key
		)
		{
			return this.get(key) != null;
		}

		@Override
		public String key()
		{
			return this.key;
		}

		@Override
		public Iterable<String> keys()
		{
			return this.properties.keys();
		}

		@Override
		public Iterable<? extends Configuration> children()
		{
			return this.children.values();
		}

		@Override
		public Configuration parent()
		{
			return this.parent;
		}

		@Override
		public void traverse(
			final Consumer<Configuration> consumer
		)
		{
			consumer.accept(this);
			this.children.values().forEach(child -> child.traverse(consumer));
		}

		@Override
		public XGettingTable<String, String> table()
		{
			return this.properties;
		}
		
		@Override
		public synchronized XGettingTable<String, String> coalescedTable()
		{
			if(this.coalescedTable == null)
			{
				final EqHashTable<String, String> coalescedTable = EqHashTable.New();
				
				if(this.key == null)
				{
					coalescedTable.putAll(this.properties);
				}
				else
				{
					this.properties.iterate(kv ->
					{
						coalescedTable.put(
							VarString.New()
								.add(this.key)
								.add(KEY_SEPARATOR)
								.add(kv.key())
								.toString(),
							kv.value()
						);
					});
				}
				
				this.children.values().iterate(child ->
				{
					final XGettingTable<String, String> childTable = child.coalescedTable();
					childTable.iterate(kv ->
					{
						final String coalescedKey = this.key == null
							? kv.key()
							: VarString.New()
								.add(this.key)
								.add(KEY_SEPARATOR)
								.add(kv.key())
								.toString()
						;
						coalescedTable.put(coalescedKey, kv.value());
					});
				});
				
				this.coalescedTable = coalescedTable.immure();
			}
			
			return this.coalescedTable;
		}
		
		@Override
		public ConfigurationValueMapperProvider mapperProvider()
		{
			return this.mapperProvider;
		}
		
		@Override
		public Configuration detach()
		{
			return new Configuration.Default(
				this.key,
				this.children,
				this.properties,
				this.mapperProvider
			);
		}
		
	}
	
}
