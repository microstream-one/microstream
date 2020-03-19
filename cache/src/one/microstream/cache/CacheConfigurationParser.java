package one.microstream.cache;

import javax.cache.CacheException;
import javax.cache.configuration.Factory;

import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XTable;

public interface CacheConfigurationParser
{
	public CacheConfiguration<?, ?> parse(String data);
	
	public <K, V> CacheConfiguration<K, V> parse(String data, Class<K> keyType, Class<V> valueType);
	
	
	public static CacheConfigurationParser Default()
	{
		return new Default();
	}
	
	
	public static class Default implements CacheConfigurationParser, CacheConfigurationPropertyNames
	{
		Default()
		{
			super();
		}		
		
		@Override
		public CacheConfiguration<?, ?> parse(String data)
		{
			final XTable<String, String> properties = EqHashTable.New();			
			this.parseProperties(data, properties);

			final Class<?> keyType   = this.valueAsClass(properties.get(KEY_TYPE),   Object.class);
			final Class<?> valueType = this.valueAsClass(properties.get(VALUE_TYPE), Object.class);

			final CacheConfiguration.Builder<?, ?> builder = CacheConfiguration.Builder(
				keyType, 
				valueType
			);
						
			this.populateBuilder(properties, builder);
						
			return builder.build();
		}
		
		@Override
		public <K, V> CacheConfiguration<K, V> parse(String data, Class<K> keyType, Class<V> valueType)
		{
			final XTable<String, String> properties = EqHashTable.New();			
			this.parseProperties(data, properties);

			final CacheConfiguration.Builder<K, V> builder = CacheConfiguration.Builder(
				keyType, 
				valueType
			);
						
			this.populateBuilder(properties, builder);
						
			return builder.build();
		}

		private void populateBuilder(
			final XTable<String, String> properties, 
			final CacheConfiguration.Builder<?, ?> builder
		)
		{
			properties.iterate(kv -> 
			{
				switch(kv.key())
				{
					case CACHE_LOADER_FACTORY:
						builder.cacheLoaderFactory(this.valueAsFactory(kv.value()));
					break;
					
					case CACHE_WRITER_FACTORY:
						builder.cacheWriterFactory(this.valueAsFactory(kv.value()));
					break;
					
					case EXPIRY_POLICY_FACTORY:
						builder.expiryPolicyFactory(this.valueAsFactory(kv.value()));
					break;
					
					case EVICTION_MANAGER_FACTORY:
						builder.evictionManagerFactory(this.valueAsFactory(kv.value()));
					break;
					
					case READ_THROUGH:
						if(Boolean.valueOf(kv.value()))
						{
							builder.readThrough();
						}
					break;
					
					case WRITE_THROUGH:
						if(Boolean.valueOf(kv.value()))
						{
							builder.writeThrough();
						}
					break;
					
					case STORE_BY_VALUE:
						if(Boolean.valueOf(kv.value()))
						{
							builder.storeByValue();
						}
						else
						{
							builder.storeByReference();
						}
					break;
					
					case STATISTICS_ENABLED:
						if(Boolean.valueOf(kv.value()))
						{
							builder.enableStatistics();
						}
					break;
					
					case MANAGEMENT_ENABLED:
						if(Boolean.valueOf(kv.value()))
						{
							builder.enableManagement();
						}
					break;
				}
			});
		}
				
		protected Class<?> valueAsClass(String value, Class<?> defaultValue)
		{
			try
			{
				return XChars.isEmpty(value)
					? defaultValue
					: Class.forName(value)
				;
			}
			catch(ClassNotFoundException e)
			{
				throw new CacheException(e);
			}
		}
		
		@SuppressWarnings("unchecked")
		protected <T> Factory<T> valueAsFactory(String value)
		{
			try
			{
				return Factory.class.cast(Class.forName(value).newInstance());
			}
			catch(ClassNotFoundException | ClassCastException | 
				  InstantiationException | IllegalAccessException e
			)
			{
				throw new CacheException(e);
			}
		}

		protected void parseProperties(
			final String data, 
			final XTable<String, String> properties
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
