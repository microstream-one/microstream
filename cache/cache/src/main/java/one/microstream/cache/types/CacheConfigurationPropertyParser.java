package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.cache.CacheException;
import javax.cache.configuration.Factory;

import one.microstream.cache.types.CacheConfiguration.Builder;
import one.microstream.storage.configuration.Configuration;
import one.microstream.storage.configuration.ConfigurationPropertyParser;

/**
 * @deprecated replaced by {@link CacheConfigurationBuilderConfigurationBased}, will be removed in version 8
 */
@Deprecated
public interface CacheConfigurationPropertyParser
{
	@FunctionalInterface
	public static interface ClassResolver
	{
		public Class<?> loadClass(
			String name
		)
			throws ClassNotFoundException;


		public static ClassResolver Default()
		{
			return Class::forName;
		}
	}


	public <K, V> void parseProperties(
		Map<String, String> properties,
		CacheConfiguration.Builder<K, V> builder
	);


	public static CacheConfigurationPropertyParser New()
	{
		return New(ClassResolver.Default());
	}

	public static CacheConfigurationPropertyParser New(
		final ClassResolver classResolver
	)
	{
		return new Default(notNull(classResolver));
	}

	public static class Default implements CacheConfigurationPropertyParser, CacheConfigurationPropertyNames
	{
		private final ClassResolver classResolver;

		Default(
			final ClassResolver classResolver
		)
		{
			super();
			this.classResolver = classResolver;
		}

		@Override
		public <K, V> void parseProperties(
			final Map<String, String> properties,
			final Builder<K, V> builder
		)
		{
			properties.entrySet().forEach(kv ->
				this.parseProperty(kv.getKey(), kv.getValue(), builder)
			);

			if(properties.get(STORAGE_CONFIGURATION_RESOURCE_NAME) == null)
			{
				this.processStorageProperties(properties, builder);
			}
		}

		@SuppressWarnings("incomplete-switch")
		protected <K, V> void parseProperty(
			final String name,
			final String value,
			final CacheConfiguration.Builder<K, V> builder
		)
		{
			switch(name)
			{
				case STORAGE_CONFIGURATION_RESOURCE_NAME:
					final Configuration storageConfiguration = Configuration.Load(value);
					if(storageConfiguration == null)
					{
						throw new CacheException("Storage configuration not found: " + value);
					}
					final CacheLoaderWriterFactories<K, V> factories =
						CacheLoaderWriterFactories.New(storageConfiguration);
					builder
						.cacheLoaderFactory(factories.loaderFactory())
						.cacheWriterFactory(factories.writerFactory())
						.writeThrough()
						.readThrough();
				break;

				case CACHE_LOADER_FACTORY:
					builder.cacheLoaderFactory(this.valueAsFactory(value));
				break;

				case CACHE_WRITER_FACTORY:
					builder.cacheWriterFactory(this.valueAsFactory(value));
				break;

				case EXPIRY_POLICY_FACTORY:
					builder.expiryPolicyFactory(this.valueAsFactory(value));
				break;

				case EVICTION_MANAGER_FACTORY:
					builder.evictionManagerFactory(this.valueAsFactory(value));
				break;

				case READ_THROUGH:
					builder.readThrough(Boolean.valueOf(value));
				break;

				case WRITE_THROUGH:
					builder.writeThrough(Boolean.valueOf(value));
				break;

				case STORE_BY_VALUE:
					if(Boolean.valueOf(value))
					{
						builder.storeByValue();
					}
					else
					{
						builder.storeByReference();
					}
				break;

				case STATISTICS_ENABLED:
					if(Boolean.valueOf(value))
					{
						builder.enableStatistics();
					}
				break;

				case MANAGEMENT_ENABLED:
					if(Boolean.valueOf(value))
					{
						builder.enableManagement();
					}
				break;
			}
		}

		@SuppressWarnings("unchecked")
		protected <T> Factory<T> valueAsFactory(
			final String value
		)
		{
			try
			{
				return Factory.class.cast(
					this.classResolver
						.loadClass(value)
						.getDeclaredConstructor()
						.newInstance()
				);
			}
			catch(ClassNotFoundException | ClassCastException |
				  InstantiationException | IllegalAccessException |
				  IllegalArgumentException | InvocationTargetException |
				  NoSuchMethodException | SecurityException e
			)
			{
				throw new CacheException(e);
			}
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		protected void processStorageProperties(
			final Map<String, String> properties,
			final CacheConfiguration.Builder builder
		)
		{
			final String              prefix            = "storage.";
			final Map<String, String> storageProperties = properties.entrySet().stream()
				.filter(kv -> kv.getKey().startsWith(prefix))
				.collect(Collectors.toMap(
					kv -> kv.getKey().substring(prefix.length()),
					kv -> kv.getValue()
				))
			;
			if(storageProperties.size() > 0L)
			{
				final Configuration storageConfiguration = Configuration.Default();
				ConfigurationPropertyParser.New()
					.parseProperties(
						storageProperties,
						storageConfiguration
					)
				;
				final CacheLoaderWriterFactories<?, ?> factories =
					CacheLoaderWriterFactories.New(storageConfiguration);
				builder
					.cacheLoaderFactory(factories.loaderFactory())
					.cacheWriterFactory(factories.writerFactory())
					.writeThrough()
					.readThrough();
			}
		}

	}

}
