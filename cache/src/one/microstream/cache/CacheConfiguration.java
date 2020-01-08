
package one.microstream.cache;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.function.Predicate;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import one.microstream.reflect.XReflect;


public interface CacheConfiguration<K, V> extends CompleteConfiguration<K, V>
{
	public Factory<EvictionManager<K, V>> getEvictionManagerFactory();
	
	public Predicate<? super Field> getSerializerFieldPredicate();
	
	
	public static <K, V> Builder<K, V> Builder(final Class<K> keyType, final Class<V> valueType)
	{
		return new Builder.Default<>(keyType, valueType);
	}
	
	public static interface Builder<K, V>
	{
		public Builder<K, V> addListenerConfiguration(CacheEntryListenerConfiguration<K, V> listenerConfigurations);
		
		public Builder<K, V> cacheLoaderFactory(Factory<CacheLoader<K, V>> cacheLoaderFactory);
		
		public Builder<K, V> cacheWriterFactory(Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory);
		
		public Builder<K, V> expiryPolicyFactory(Factory<ExpiryPolicy> expiryPolicyFactory);
		
		public Builder<K, V> evictionManagerFactory(Factory<EvictionManager<K, V>> evictionManagerFactory);
		
		public Builder<K, V> readThrough();
		
		public Builder<K, V> writeThrough();
		
		public Builder<K, V> storeByValue();
		
		public Builder<K, V> storeByReference();
		
		public Builder<K, V> enableStatistics();
		
		public Builder<K, V> enableManagement();
		
		public Builder<K, V> serializerFieldPredicate(Predicate<? super Field> serializerFieldPredicate);
		
		public CacheConfiguration<K, V> build();
		
		public static class Default<K, V> implements Builder<K, V>
		{
			private final Class<K>                                 keyType;
			private final Class<V>                                 valueType;
			private HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations;
			private Factory<CacheLoader<K, V>>                     cacheLoaderFactory;
			private Factory<CacheWriter<? super K, ? super V>>     cacheWriterFactory;
			private Factory<ExpiryPolicy>                          expiryPolicyFactory;
			private Factory<EvictionManager<K, V>>                 evictionManagerFactory;
			private boolean                                        isReadThrough;
			private boolean                                        isWriteThrough;
			private boolean                                        isStoreByValue;
			private boolean                                        isStatisticsEnabled;
			private boolean                                        isManagementEnabled;
			private Predicate<? super Field>                       serializerFieldPredicate;
			
			Default(final Class<K> keyType, final Class<V> valueType)
			{
				super();
				
				this.keyType   = notNull(keyType);
				this.valueType = notNull(valueType);
			}
			
			@Override
			public Builder<K, V>
				addListenerConfiguration(final CacheEntryListenerConfiguration<K, V> listenerConfiguration)
			{
				this.listenerConfigurations.add(listenerConfiguration);
				return this;
			}
			
			@Override
			public Builder<K, V> cacheLoaderFactory(final Factory<CacheLoader<K, V>> cacheLoaderFactory)
			{
				this.cacheLoaderFactory = cacheLoaderFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> cacheWriterFactory(final Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory)
			{
				this.cacheWriterFactory = cacheWriterFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> expiryPolicyFactory(final Factory<ExpiryPolicy> expiryPolicyFactory)
			{
				this.expiryPolicyFactory = expiryPolicyFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> evictionManagerFactory(final Factory<EvictionManager<K, V>> evictionManagerFactory)
			{
				this.evictionManagerFactory = evictionManagerFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> readThrough()
			{
				this.isReadThrough = true;
				return this;
			}
			
			@Override
			public Builder<K, V> writeThrough()
			{
				this.isWriteThrough = true;
				return this;
			}
			
			@Override
			public Builder<K, V> storeByValue()
			{
				this.isStoreByValue = true;
				return this;
			}
			
			@Override
			public Builder<K, V> storeByReference()
			{
				this.isStoreByValue = false;
				return this;
			}
			
			@Override
			public Builder<K, V> enableStatistics()
			{
				this.isStatisticsEnabled = true;
				return this;
			}
			
			@Override
			public Builder<K, V> enableManagement()
			{
				this.isManagementEnabled = true;
				return this;
			}
			
			@Override
			public Builder<K, V> serializerFieldPredicate(final Predicate<? super Field> serializerFieldPredicate)
			{
				this.serializerFieldPredicate = serializerFieldPredicate;
				return this;
			}
			
			@Override
			public CacheConfiguration<K, V> build()
			{
				final Factory<ExpiryPolicy> expiryPolicyFactory = coalesce(
					this.expiryPolicyFactory,
					DefaultExpiryPolicyFactory()
				);
				
				final Factory<EvictionManager<K, V>> evictionManagerFactory = coalesce(
					this.evictionManagerFactory,
					DefaultEvictionManagerFactory()
				);
				
				final Predicate<? super Field> serializerFieldPredicate = coalesce(
					this.serializerFieldPredicate,
					DefaultSerializerFieldPredicate()
				);
				
				return new CacheConfiguration.Default<>(this.keyType,
					this.valueType,
					this.listenerConfigurations,
					this.cacheLoaderFactory,
					this.cacheWriterFactory,
					expiryPolicyFactory,
					evictionManagerFactory,
					this.isReadThrough,
					this.isWriteThrough,
					this.isStoreByValue,
					this.isStatisticsEnabled,
					this.isManagementEnabled,
					serializerFieldPredicate
				);
			}
			
		}
		
	}
	
	public static Factory<ExpiryPolicy> DefaultExpiryPolicyFactory()
	{
		return EternalExpiryPolicy.factoryOf();
	}
	
	public static <K, V> Factory<EvictionManager<K, V>> DefaultEvictionManagerFactory()
	{
		return () -> null;
	}
	
	public static Predicate<? super Field> DefaultSerializerFieldPredicate()
	{
		return XReflect::isNotTransient;
	}
	
	public static <K, V> CacheConfiguration<K, V> New(final Configuration<K, V> other)
	{
		final HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations = new HashSet<>();
		
		if(other instanceof CompleteConfiguration)
		{
			final CompleteConfiguration<K, V> complete = (CompleteConfiguration<K, V>)other;
			
			for(final CacheEntryListenerConfiguration<K, V> listenerConfig : complete
				.getCacheEntryListenerConfigurations())
			{
				listenerConfigurations.add(listenerConfig);
			}
			
			final Factory<ExpiryPolicy> expiryPolicyFactory = coalesce(
				complete.getExpiryPolicyFactory(),
				DefaultExpiryPolicyFactory()
			);
			
			final Factory<EvictionManager<K, V>> evictionManagerFactory;
			final Predicate<? super Field>       serializerFieldPredicate;
			if(other instanceof CacheConfiguration)
			{
				final CacheConfiguration<K, V> msCacheConfig = (CacheConfiguration<K, V>)other;
				evictionManagerFactory   = msCacheConfig.getEvictionManagerFactory();
				serializerFieldPredicate = msCacheConfig.getSerializerFieldPredicate();
			}
			else
			{
				evictionManagerFactory   = DefaultEvictionManagerFactory();
				serializerFieldPredicate = DefaultSerializerFieldPredicate();
			}
			
			return new Default<>(
				complete.getKeyType(),
				complete.getValueType(),
				listenerConfigurations,
				complete.getCacheLoaderFactory(),
				complete.getCacheWriterFactory(),
				expiryPolicyFactory,
				evictionManagerFactory,
				complete.isReadThrough(),
				complete.isWriteThrough(),
				complete.isStoreByValue(),
				complete.isStatisticsEnabled(),
				complete.isManagementEnabled(),
				serializerFieldPredicate
			);
		}
		
		return new Default<>(
			other.getKeyType(),
			other.getValueType(),
			listenerConfigurations,
			null,
			null,
			DefaultExpiryPolicyFactory(),
			DefaultEvictionManagerFactory(),
			false,
			false,
			other.isStoreByValue(),
			false,
			false,
			DefaultSerializerFieldPredicate());
	}
	
	public static class Default<K, V> implements CacheConfiguration<K, V>
	{
		private final Class<K>                                       keyType;
		private final Class<V>                                       valueType;
		private final HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations;
		private final Factory<CacheLoader<K, V>>                     cacheLoaderFactory;
		private final Factory<CacheWriter<? super K, ? super V>>     cacheWriterFactory;
		private final Factory<ExpiryPolicy>                          expiryPolicyFactory;
		private final Factory<EvictionManager<K, V>>                 evictionManagerFactory;
		private final boolean                                        isReadThrough;
		private final boolean                                        isWriteThrough;
		private final boolean                                        isStoreByValue;
		private final boolean                                        isStatisticsEnabled;
		private final boolean                                        isManagementEnabled;
		private final Predicate<? super Field>                       serializerFieldPredicate;
		
		Default(
			final Class<K>                                       keyType,
			final Class<V>                                       valueType,
			final HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations,
			final Factory<CacheLoader<K, V>>                     cacheLoaderFactory,
			final Factory<CacheWriter<? super K, ? super V>>     cacheWriterFactory,
			final Factory<ExpiryPolicy>                          expiryPolicyFactory,
			final Factory<EvictionManager<K, V>>                 evictionManagerFactory,
			final boolean                                        isReadThrough,
			final boolean                                        isWriteThrough,
			final boolean                                        isStoreByValue,
			final boolean                                        isStatisticsEnabled,
			final boolean                                        isManagementEnabled,
			final Predicate<? super Field>                       serializerFieldPredicate
		)
		{
			super();
			
			this.keyType                  = keyType;
			this.valueType                = valueType;
			this.listenerConfigurations   = listenerConfigurations;
			this.cacheLoaderFactory       = cacheLoaderFactory;
			this.cacheWriterFactory       = cacheWriterFactory;
			this.expiryPolicyFactory      = expiryPolicyFactory;
			this.evictionManagerFactory   = evictionManagerFactory;
			this.isReadThrough            = isReadThrough;
			this.isWriteThrough           = isWriteThrough;
			this.isStatisticsEnabled      = isStatisticsEnabled;
			this.isStoreByValue           = isStoreByValue;
			this.isManagementEnabled      = isManagementEnabled;
			this.serializerFieldPredicate = serializerFieldPredicate;
		}
		
		@Override
		public Class<K> getKeyType()
		{
			return this.keyType;
		}
		
		@Override
		public Class<V> getValueType()
		{
			return this.valueType;
		}
		
		@Override
		public Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations()
		{
			return this.listenerConfigurations;
		}
		
		@Override
		public Factory<CacheLoader<K, V>> getCacheLoaderFactory()
		{
			return this.cacheLoaderFactory;
		}
		
		@Override
		public Factory<CacheWriter<? super K, ? super V>> getCacheWriterFactory()
		{
			return this.cacheWriterFactory;
		}
		
		@Override
		public Factory<ExpiryPolicy> getExpiryPolicyFactory()
		{
			return this.expiryPolicyFactory;
		}
		
		@Override
		public Factory<EvictionManager<K, V>> getEvictionManagerFactory()
		{
			return this.evictionManagerFactory;
		}
		
		@Override
		public boolean isReadThrough()
		{
			return this.isReadThrough;
		}
		
		@Override
		public boolean isWriteThrough()
		{
			return this.isWriteThrough;
		}
		
		@Override
		public boolean isStoreByValue()
		{
			return this.isStoreByValue;
		}
		
		@Override
		public boolean isStatisticsEnabled()
		{
			return this.isStatisticsEnabled;
		}
		
		@Override
		public boolean isManagementEnabled()
		{
			return this.isManagementEnabled;
		}
		
		@Override
		public Predicate<? super Field> getSerializerFieldPredicate()
		{
			return this.serializerFieldPredicate;
		}
		
		@Override
		public int hashCode()
		{
			final int prime  = 31;
			int       result = 1;
			result = prime * result + (this.cacheLoaderFactory == null ? 0 : this.cacheLoaderFactory.hashCode());
			result = prime * result + (this.cacheWriterFactory == null ? 0 : this.cacheWriterFactory.hashCode());
			result = prime * result + (this.expiryPolicyFactory == null ? 0 : this.expiryPolicyFactory.hashCode());
			result = prime * result + (this.isManagementEnabled ? 1231 : 1237);
			result = prime * result + (this.isReadThrough ? 1231 : 1237);
			result = prime * result + (this.isStatisticsEnabled ? 1231 : 1237);
			result = prime * result + (this.isStoreByValue ? 1231 : 1237);
			result = prime * result + (this.isWriteThrough ? 1231 : 1237);
			result = prime * result + (this.keyType == null ? 0 : this.keyType.hashCode());
			result = prime * result + (this.listenerConfigurations == null ? 0 : this.listenerConfigurations.hashCode());
			result = prime * result + (this.valueType == null ? 0 : this.valueType.hashCode());
			result = prime * result + (this.serializerFieldPredicate == null ? 0 : this.serializerFieldPredicate.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			if(this == obj)
			{
				return true;
			}
			if(!(obj instanceof CacheConfiguration))
			{
				return false;
			}
			final CacheConfiguration<?, ?> other = (CacheConfiguration<?, ?>)obj;
			if(this.cacheLoaderFactory == null)
			{
				if(other.getCacheLoaderFactory() != null)
				{
					return false;
				}
			}
			else if(!this.cacheLoaderFactory.equals(other.getCacheLoaderFactory()))
			{
				return false;
			}
			if(this.cacheWriterFactory == null)
			{
				if(other.getCacheWriterFactory() != null)
				{
					return false;
				}
			}
			else if(!this.cacheWriterFactory.equals(other.getCacheWriterFactory()))
			{
				return false;
			}
			if(this.expiryPolicyFactory == null)
			{
				if(other.getExpiryPolicyFactory() != null)
				{
					return false;
				}
			}
			else if(!this.expiryPolicyFactory.equals(other.getExpiryPolicyFactory()))
			{
				return false;
			}
			if(this.isManagementEnabled != other.isManagementEnabled())
			{
				return false;
			}
			if(this.isReadThrough != other.isReadThrough())
			{
				return false;
			}
			if(this.isStatisticsEnabled != other.isStatisticsEnabled())
			{
				return false;
			}
			if(this.isStoreByValue != other.isStoreByValue())
			{
				return false;
			}
			if(this.isWriteThrough != other.isWriteThrough())
			{
				return false;
			}
			if(this.keyType == null)
			{
				if(other.getKeyType() != null)
				{
					return false;
				}
			}
			else if(!this.keyType.equals(other.getKeyType()))
			{
				return false;
			}
			if(this.listenerConfigurations == null)
			{
				if(other.getCacheEntryListenerConfigurations() != null)
				{
					return false;
				}
			}
			else if(!this.listenerConfigurations.equals(other.getCacheEntryListenerConfigurations()))
			{
				return false;
			}
			if(this.valueType == null)
			{
				if(other.getValueType() != null)
				{
					return false;
				}
			}
			else if(!this.valueType.equals(other.getValueType()))
			{
				return false;
			}
			if(this.serializerFieldPredicate == null)
			{
				if(other.getSerializerFieldPredicate() != null)
				{
					return false;
				}
			}
			else if(!this.serializerFieldPredicate.equals(other.getSerializerFieldPredicate()))
			{
				return false;
			}
			return true;
		}
		
	}
	
}
