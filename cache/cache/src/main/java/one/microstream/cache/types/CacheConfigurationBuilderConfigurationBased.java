package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import javax.cache.CacheException;
import javax.cache.configuration.Factory;

import one.microstream.chars.XChars;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationLoader;
import one.microstream.configuration.types.ConfigurationParserIni;
import one.microstream.configuration.types.ConfigurationParserXml;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageFoundationCreatorConfigurationBased;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

import java.lang.reflect.InvocationTargetException;

public interface CacheConfigurationBuilderConfigurationBased
{
	@FunctionalInterface
	public static interface ClassResolver
	{
		public Class<?> loadClass(String name) throws ClassNotFoundException;


		public static ClassResolver New()
		{
			return Class::forName;
		}
	}


	public CacheConfiguration.Builder<?, ?> buildCacheConfiguration(
		Configuration configuration
	);
	
	public <K, V> CacheConfiguration.Builder<K, V> buildCacheConfiguration(
		Configuration configuration, CacheConfiguration.Builder<K, V> builder
	);
	
	
	public static CacheConfigurationBuilderConfigurationBased New()
	{
		return new CacheConfigurationBuilderConfigurationBased.Default(
			ClassResolver.New()
		);
	}
	
	public static CacheConfigurationBuilderConfigurationBased New(
		final ClassResolver classResolver
	)
	{
		return new CacheConfigurationBuilderConfigurationBased.Default(
			notNull(classResolver)
		);
	}
	
	
	public static class Default implements
	CacheConfigurationBuilderConfigurationBased,
	CacheConfigurationPropertyNames
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
		public CacheConfiguration.Builder<?, ?> buildCacheConfiguration(
			final Configuration configuration
		)
		{
			final CacheConfiguration.Builder<?, ?> builder = CacheConfiguration.Builder(
				this.getClass(configuration, KEY_TYPE  ),
				this.getClass(configuration, VALUE_TYPE)
			);
			
			return this.buildCacheConfiguration(configuration, builder);
		}
		
		@Override
		public <K, V> CacheConfiguration.Builder<K, V> buildCacheConfiguration(
			final Configuration                    configuration,
			final CacheConfiguration.Builder<K, V> builder
		)
		{
			configuration.opt(EXPIRY_POLICY_FACTORY).ifPresent(value ->
				builder.expiryPolicyFactory(this.valueAsFactory(value))
			);
			configuration.opt(EVICTION_MANAGER_FACTORY).ifPresent(value ->
				builder.evictionManagerFactory(this.valueAsFactory(value))
			);
			configuration.optBoolean(STORE_BY_VALUE).ifPresent(value ->
				builder.storeByValue(value)
			);
			configuration.optBoolean(STATISTICS_ENABLED).ifPresent(value ->
				builder.enableStatistics(value)
			);
			configuration.optBoolean(MANAGEMENT_ENABLED).ifPresent(value ->
				builder.enableManagement(value)
			);

			final CacheStore<K, V> cacheStore = this.buildCacheStore(configuration);
			if(cacheStore != null)
			{
				builder
					.cacheLoaderFactory(() -> cacheStore)
					.cacheWriterFactory(() -> cacheStore)
					.readThrough(configuration.optBoolean(READ_THROUGH).orElse(true))
					.writeThrough(configuration.optBoolean(WRITE_THROUGH).orElse(true))
				;
			}
			else
			{
				configuration.opt(CACHE_LOADER_FACTORY).ifPresent(value ->
				{
					builder
						.cacheLoaderFactory(this.valueAsFactory(value))
						.readThrough(configuration.optBoolean(READ_THROUGH).orElse(true))
					;
				});
				configuration.opt(CACHE_WRITER_FACTORY).ifPresent(value ->
				{
					builder
						.cacheWriterFactory(this.valueAsFactory(value))
						.writeThrough(configuration.optBoolean(WRITE_THROUGH).orElse(true))
					;
				});
			}
			
			return builder;
		}
		
		private <K, V> CacheStore<K, V> buildCacheStore(
			final Configuration configuration
		)
		{
			Configuration storageConfiguration = configuration.child(STORAGE);
			if(storageConfiguration == null)
			{
				final String resourceName = configuration.get(STORAGE_CONFIGURATION_RESOURCE_NAME);
				if(!XChars.isEmpty(resourceName))
				{
					storageConfiguration = Configuration.Load(
						ConfigurationLoader.New(resourceName),
						resourceName.toLowerCase().endsWith(".xml")
							? ConfigurationParserXml.New()
							: ConfigurationParserIni.New()
					);
				}
			}
			if(storageConfiguration != null)
			{
				final EmbeddedStorageManager storageManager = EmbeddedStorageFoundationCreatorConfigurationBased
					.New(storageConfiguration)
					.createEmbeddedStorageFoundation()
					.createEmbeddedStorageManager()
				;
				final String cacheKey = storageConfiguration.opt(STORAGE_KEY)
					.orElse(CachingProvider.defaultURI() + "::cache")
				;
				return CacheStore.New(cacheKey, storageManager);
			}
			
			return null;
		}
		
		private Class<?> getClass(
			final Configuration configuration,
			final String        key
		)
		{
			final String name = configuration.opt(key).orElse(null);
			
			try
			{
				return XChars.isEmpty(name)
					? Object.class
					: this.classResolver.loadClass(name)
				;
			}
			catch(final ClassNotFoundException e)
			{
				throw new CacheException(e);
			}
		}

		private <T> Factory<T> valueAsFactory(
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
				NoSuchMethodException | InvocationTargetException e
			)
			{
				throw new CacheException(e);
			}
		}
		
	}
	
}
