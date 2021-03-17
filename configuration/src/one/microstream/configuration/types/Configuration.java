package one.microstream.configuration.types;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingTable;
import one.microstream.configuration.exceptions.ConfigurationExceptionNoValueMapperFound;
import one.microstream.configuration.exceptions.ConfigurationExceptionValueMappingFailed;
import one.microstream.typing.KeyValue;
import one.microstream.util.cql.CQL;

/**
 * General purpose, immutable (builder-created), hierarchical configuration container.
 * Its content consists of key value pairs, which are either value entries &lt;String, String&gt;
 * or child-configuration entries &lt;String, Configuration&gt;.
 * <p>
 * The keys are just plain Strings with any content except the {@link #KEY_SEPARATOR} ('.'), because
 * it connects simple keys to full-qualified ones.
 * <p>
 * Example configuration:
 * <pre>
 * microstream
 *   |
 *   +-- storage
 *   |   |
 *   |   +-- storageDirectory = /home/my-storage
 *   |   |
 *   |   +-- backupDirectory = /home/backup-storage
 *   |
 *   +-- cache
 *   |   |
 *   |   +-- keyType = java.lang.String
 *   |   |
 *   |   +-- valueTytpe = java.lang.Double
 *   |
 *   +-- version = 1.2.3
 *   |
 *   +-- production = true
 * </pre>
 * To access the <code>storageDirectory</code> entry there are following ways.
 * Either get the child configurations and then the value entry
 * <pre>
 * String directory = configuration.child("microstream").child("storage").get("storageDirectory");
 * </pre>
 * or just use the full-qualified key, as a shortcut
 * <pre>
 * String directory = configuration.get("microstream.storage.storageDirectory");
 * </pre>
 * In order to translate the value entries into different types, {@link ConfigurationValueMapper}s are used.
 * Predefined value mappers are there for the most commonly used types in configurations:
 * {@link ConfigurationValueMapperProvider#Default()}.
 * <p>
 * Custom value mappers can be used as well, of course.
 * <pre>
 * ConfigurationValueMapperProvider valueMapperProvider = ConfigurationValueMapperProvider.Default()
 * 	.add(new MyValueMapper())
 * 	.build();
 * Configuration configuration = Configuration.Builder()
 * 	.valueMapperProvider(valueMapperProvider)
 * 	.build();
 * 
 * boolean production = configuration.getBoolean("microstream.production");
 * MyType  myType     = configuration.get("key", MyType.class);
 * </pre>
 * <p>
 * Configurations can be created with a {@link Configuration.Builder}. Builders are populated programmatically,
 * by a {@link ConfigurationMapper} or a {@link ConfigurationParser}.
 * <pre>
 * // create configuration from external file
 * Configuration configuration = Configuration.Load(
 * 	ConfigurationLoader.New("config-production.xml"),
 * 	ConfigurationParserXml.New()
 * );
 * 
 * // create configuration from Map
 * Map&lt;String, Object&gt; otherFrameworkConfig = ...;
 * Configuration configuration = ConfigurationMapperMap.New()
 * 	.mapConfiguration(otherFrameworkConfig)
 * 	.buildConfiguration();
 * 
 * // create configuration from different sources
 * Configuration.Builder()
 * 	.load(
 * 		ConfigurationLoader.New("config-base.xml"),
 *		ConfigurationParserXml.New()
 *	)
 *	.load(
 *		ConfigurationLoader.New("config-production.xml"),
 *		ConfigurationParserXml.New()
 *	)
 *	.buildConfiguration();
 * </pre>
 * Configurations can be exported as well:
 * <pre>
 * configuration.store(
 * 	ConfigurationStorer.New(Paths.get("home", "config-export.xml")),
 * 	ConfigurationAssemblerXml.New()
 * );
 * </pre>
 */
public interface Configuration
{
	/**
	 * The separator char ('.') which is used to connect simple keys to full-qualified ones.
	 */
	public final static char KEY_SEPARATOR = '.';
	
	
	/**
	 * Builder for {@link Configuration}s.
	 * <p>
	 * Child-configurations can be built by either using {@link #child(String)} builders,
	 * or with full-qualified keys, as described in {@link Configuration}.
	 *
	 */
	public static interface Builder
	{
		/**
		 * Maps values and child-configurations from the specified source into this builder.
		 * This can be used to get values from one or more external resources.
		 * 
		 * @param mapper the mapper for the source
		 * @param source the input source
		 * @return this builder
		 * @see ConfigurationMapper
		 */
		public default <S> Builder map(
			final ConfigurationMapper<S> mapper,
			final S                      source
		)
		{
			return mapper.mapConfiguration(this, source);
		}
		
		/**
		 * Loads values and child-configurations from the specified source into this builder.
		 * This can be used to get values from one or more external resources.
		 * 
		 * @param loader the loader to retrieve the input
		 * @param parser the parser to parse the input
		 * @return this builder
		 * @see ConfigurationLoader
		 * @see ConfigurationParser
		 */
		public default Builder load(
			final ConfigurationLoader loader,
			final ConfigurationParser parser
		)
		{
			return parser.parseConfiguration(this, loader.loadConfiguration());
		}
		
		/**
		 * Sets the {@link ConfigurationValueMapperProvider}.
		 * Use this method to insert user-defined value mappers.
		 * 
		 * @param valueMapperProvider the new mapper provider
		 * @return this builder
		 */
		public Builder valueMapperProvider(ConfigurationValueMapperProvider valueMapperProvider);
		
		/**
		 * Sets either a simple key-value pair (foo=bar) or an entry in a child-configuration (full.qualified.foo=bar).
		 * 
		 * @param key simple or full-qualified key, cannot be empty or <code>null</code>
		 * @param value the value to set, cannot be <code>null</code>
		 * @return this builder
		 */
		public Builder set(String key, String value);
		
		/**
		 * Sets many entries at once.
		 * 
		 * @param properties the key-value pairs
		 * @return this builder
		 * @see #set(String, String)
		 */
		public Builder setAll(XGettingCollection<KeyValue<String, String>> properties);

		/**
		 * Sets many entries at once.
		 * 
		 * @param properties the key-value pairs
		 * @return this builder
		 * @see #set(String, String)
		 */
		@SuppressWarnings("unchecked")
		public Builder setAll(KeyValue<String, String>... properties);
		
		/**
		 * Creates a builder for a child configuration.
		 * 
		 * @param key the key for the child configuration
		 * @return a new builder
		 * @see #child(String, Consumer)
		 */
		public Builder child(String key);
		
		/**
		 * Creates a child configuration.
		 * 
		 * @param key the key for the child configuration
		 * @param childBuilder the builder consumer for method chaining
		 * @return this builder
		 */
		public default Builder child(final String key, final Consumer<Builder> childBuilder)
		{
			childBuilder.accept(this.child(key));
			return this;
		}
		
		/**
		 * Finishes the building and returns the resulting {@link Configuration}.
		 * 
		 * @return the {@link Configuration} with all values and child-configurations from this builder
		 */
		public Configuration buildConfiguration();
		
		
		public static class Default implements Builder
		{
			private final String                               key                           ;
			private       ConfigurationValueMapperProvider     valueMapperProvider           ;
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
			public Builder valueMapperProvider(
				final ConfigurationValueMapperProvider valueMapperProvider
			)
			{
				this.valueMapperProvider = valueMapperProvider;
				
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
				final ConfigurationValueMapperProvider valueMapperProvider = this.valueMapperProvider != null
					? this.valueMapperProvider
					: ConfigurationValueMapperProvider.Default().build()
				;
				
				final EqHashTable<String, Configuration.Default> children =
					CQL.from(this.children)
						.project(kv -> X.KeyValue(kv.key(), kv.value().buildConfiguration()))
						.executeInto(EqHashTable.New())
				;
				
				final Configuration.Default config = new Configuration.Default(
					this.key                ,
					children.immure()       ,
					this.properties.immure(),
					valueMapperProvider
				);
				
				children.values().iterate(child -> child.setParent(config));
				
				return config;
			}
			
		}
		
	}
	
	
	/**
	 * Pseudo-constructor method to create a new {@link Builder}.
	 * 
	 * @return a new builder
	 */
	public static Builder Builder()
	{
		return new Builder.Default();
	}

	/**
	 * Convenience method to load a configuration from an external source.
	 * <p>
	 * This is shortcut for
	 * <pre>Configuration.Builder().load(loader, parser).buildConfiguration()</pre>
	 * 
	 * @param loader the loader to retrieve the input
	 * @param parser the parser to parse the input
	 * @return the created configuration
	 */
	public static Configuration Load(
		final ConfigurationLoader loader,
		final ConfigurationParser parser
	)
	{
		return Builder().load(loader, parser).buildConfiguration();
	}
	
	/**
	 * Gets the assigned value of the specified key,
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public String get(String key);
	
	/**
	 * Gets the assigned value of the specified key as {@link Boolean},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Boolean#parseBoolean(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Boolean getBoolean(final String key)
	{
		return this.get(key, Boolean.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Byte},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Byte#parseByte(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Byte getByte(final String key)
	{
		return this.get(key, Byte.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Short},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Short#parseShort(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Short getShort(final String key)
	{
		return this.get(key, Short.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Integer},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Integer#parseInteger(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Integer getInteger(final String key)
	{
		return this.get(key, Integer.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Long},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Long#parseLong(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Long getLong(final String key)
	{
		return this.get(key, Long.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Float},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Float#parseFloat(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Float getFloat(final String key)
	{
		return this.get(key, Float.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Double},
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed according to {@link Double#parseDouble(String)}.
	 * 
	 * @param key the key to look up
	 * @return the assigned value, or <code>null</code>
	 */
	public default Double getDouble(final String key)
	{
		return this.get(key, Double.class);
	}
	
	/**
	 * Gets the assigned value of the specified key.
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * <p>
	 * The String value is parsed by the registered {@link ConfigurationValueMapper} for the specified type.
	 * 
	 * @param key the key to look up
	 * @param type the type to map to
	 * @return the assigned value, or <code>null</code>
	 * @throws ConfigurationExceptionNoValueMapperFound if no {@link ConfigurationValueMapper} is found for the type
	 * @throws ConfigurationExceptionValueMappingFailed if the mapping to the target type fails
	 */
	public <T> T get(String key, Class<T> type);
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 */
	public default Optional<String> opt(final String key)
	{
		return Optional.ofNullable(this.get(key));
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getBoolean(String)
	 */
	public default Optional<Boolean> optBoolean(final String key)
	{
		return this.opt(key, Boolean.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getByte(String)
	 */
	public default Optional<Byte> optByte(final String key)
	{
		return this.opt(key, Byte.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getShort(String)
	 */
	public default Optional<Short> optShort(final String key)
	{
		return this.opt(key, Short.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getInteger(String)
	 */
	public default Optional<Integer> optInteger(final String key)
	{
		return this.opt(key, Integer.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getLong(String)
	 */
	public default Optional<Long> optLong(final String key)
	{
		return this.opt(key, Long.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getFloat(String)
	 */
	public default Optional<Float> optFloat(final String key)
	{
		return this.opt(key, Float.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return a filled or empty {@link Optional}
	 * @see #getDouble(String)
	 */
	public default Optional<Double> optDouble(final String key)
	{
		return this.opt(key, Double.class);
	}
	
	/**
	 * Gets the assigned value of the specified key as {@link Optional},
	 * which is empty if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @param type the type to map to
	 * @return a filled or empty {@link Optional}
	 * @see #get(String, Class)
	 * @throws ConfigurationExceptionNoValueMapperFound if no {@link ConfigurationValueMapper} is found for the type
	 * @throws ConfigurationExceptionValueMappingFailed if the mapping to the target type fails
	 */
	public default <T> Optional<T> opt(final String key, final Class<T> type)
	{
		return Optional.ofNullable(this.get(key, type));
	}
	
	/**
	 * Checks if this configuration contains the specified key.
	 * 
	 * @param key the key to look up
	 * @return <code>true</code> if this configuration contains the key, <code>false</code> otherwise
	 */
	public boolean contains(String key);
	
	/**
	 * Gets the key of this child-configuration or <code>null</code> if this is the root configuration.
	 * 
	 * @return this child-configuration's key
	 */
	public String key();
	
	/**
	 * Gets all keys of this configuration, but not of the child-configurations.
	 * 
	 * @return an iterable with all keys
	 */
	public Iterable<String> keys();
	
	/**
	 * Gets the assigned child-configuration of the specified key,
	 * or <code>null</code> if the configuration doesn't contain the key.
	 * 
	 * @param key the key to look up
	 * @return the assigned child-configuration, or <code>null</code>
	 */
	public Configuration child(String key);
	
	/**
	 * Gets all direct child-configurations.
	 * 
	 * @return all child-configurations
	 */
	public Iterable<? extends Configuration> children();
		
	/**
	 * Gets this configuration's parent, or <code>null</code> if this is the root configuration.
	 * 
	 * @return the parent or <code>null</code>
	 */
	public Configuration parent();
	
	/**
	 * Checks if this configuration is the root, meaning it has no parent.
	 * 
	 * @return <code>true</code> if this configuration is the root, <code>false</code> otherwise
	 */
	public default boolean isRoot()
	{
		return this.parent() == null;
	}
	
	/**
	 * Gets the root configuration, which may be this.
	 * 
	 * @return the configuration's root
	 */
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
	
	/**
	 * Traverses this and all child-configurations recursively.
	 * 
	 * @param consumer the consumer to accept all configurations
	 */
	public void traverse(Consumer<Configuration> consumer);

	/**
	 * Converts all entries of this configuration to a {@link XGettingTable}.
	 * 
	 * @return a {@link XGettingTable} containing all entries of this configurations
	 * @see #coalescedTable()
	 */
	public XGettingTable<String, String> table();
	
	/**
	 * Converts all entries of this configuration and all child-configurations recursively to a {@link XGettingTable}.
	 * 
	 * @return a {@link XGettingTable} containing all entries of this and all child-configurations
	 */
	public XGettingTable<String, String> coalescedTable();
	
	/**
	 * Converts all entries of this configuration to a {@link Map}.
	 * <p>
	 * Because configurations are immutable, changes made in the resulting map will not reflect back.
	 * 
	 * @return a {@link Map} containing all entries of this configurations
	 * @see #coalescedMap()
	 */
	public Map<String, String> map();
	
	/**
	 * Converts all entries of this configuration and all child-configurations recursively to a {@link Map}.
	 * <p>
	 * Because configurations are immutable, changes made in the resulting map will not reflect back.
	 * 
	 * @return a {@link Map} containing all entries of this and all child-configurations
	 */
	public Map<String, String> coalescedMap();
	
	/**
	 * Gets the value mapper provider which is assigned to this configuration.
	 * 
	 * @return the assigned value mapper
	 * @see #get(String, Class)
	 */
	public ConfigurationValueMapperProvider valueMapperProvider();
	
	/**
	 * Creates a new Configuration instance with all entries and child-configurations of this configuration,
	 * but with no parent, which makes it a root configuration.
	 * <p>
	 * The original configuration (this) remains untouched.
	 * 
	 * @return a new, detached configuration
	 */
	public Configuration detach();
	
	/**
	 * Stores this configuration to an external target.
	 * 
	 * @param storer the storer to write to
	 * @param assembler the assembler for the desired format
	 */
	public default void store(
		final ConfigurationStorer    storer   ,
		final ConfigurationAssembler assembler
	)
	{
		storer.storeConfiguration(
			assembler.assemble(this).toString()
		);
	}
	
	
	public static class Default implements Configuration
	{
		private final              String                                         key                ;
		private                    Configuration                                  parent             ;
		private final              XGettingTable<String, ? extends Configuration> children           ;
		private final              XGettingTable<String, String>                  properties         ;
		private final              ConfigurationValueMapperProvider               valueMapperProvider;
		private transient volatile XGettingTable<String, String>                  coalescedTable     ;
		
		Default(
			final String                                         key                ,
			final XGettingTable<String, ? extends Configuration> children           ,
			final XGettingTable<String, String>                  properties         ,
			final ConfigurationValueMapperProvider               valueMapperProvider
		)
		{
			super();
			this.key                 = key                ;
			this.children            = children           ;
			this.properties          = properties         ;
			this.valueMapperProvider = valueMapperProvider;
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
			
			final ConfigurationValueMapper<T> mapper = this.valueMapperProvider.get(type);
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
		public XGettingTable<String, String> coalescedTable()
		{
			/*
			 * Double-checked locking to reduce the overhead of acquiring a lock
			 * by testing the locking criterion.
			 */
			XGettingTable<String, String> coalescedTable;
			if((coalescedTable = this.coalescedTable) == null)
			{
				synchronized(this)
				{
					if((coalescedTable = this.coalescedTable) == null)
					{
						coalescedTable = this.coalescedTable = this.createCoalescedTable();
					}
				}
			}
			
			return coalescedTable;
		}
		
		private XGettingTable<String, String> createCoalescedTable()
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
			
			return coalescedTable.immure();
		}
		
		@Override
		public Map<String, String> map()
		{
			return this.toMap(this.properties);
		}
		
		@Override
		public Map<String, String> coalescedMap()
		{
			return this.toMap(this.coalescedTable());
		}
		
		private Map<String, String> toMap(final XGettingTable<String, String> table)
		{
			final Map<String, String> map = new HashMap<>(table.intSize());
			table.iterate(kv -> map.put(kv.key(), kv.value()));
			return map;
		}
		
		@Override
		public ConfigurationValueMapperProvider valueMapperProvider()
		{
			return this.valueMapperProvider;
		}
		
		@Override
		public Configuration detach()
		{
			return new Configuration.Default(
				this.key                ,
				this.children           ,
				this.properties         ,
				this.valueMapperProvider
			);
		}
		
	}
	
}
