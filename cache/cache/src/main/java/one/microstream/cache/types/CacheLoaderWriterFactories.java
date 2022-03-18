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

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import one.microstream.X;
import one.microstream.storage.configuration.Configuration;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * 
 * @deprecated will be removed in a future release
 */
@Deprecated
public interface CacheLoaderWriterFactories<K, V>
{
	public Factory<CacheLoader<K, V>> loaderFactory();
	
	public Factory<CacheWriter<? super K, ? super V>> writerFactory();
	
	
	public static <K, V> CacheLoaderWriterFactories<K, V> New(
		final Configuration storageConfiguration
	)
	{
		return new ConfigurationBased<>(X.notNull(storageConfiguration));
	}
	
	
	public static class ConfigurationBased<K, V> implements CacheLoaderWriterFactories<K, V>
	{
		private final Configuration    storageConfiguration;
		private       CacheStore<K, V> cacheStore;

		ConfigurationBased(
			final Configuration storageConfiguration
		)
		{
			super();
			this.storageConfiguration = storageConfiguration;
		}
		
		protected CacheStore<K, V> cacheStore()
		{
			if(this.cacheStore == null)
			{
				final EmbeddedStorageManager storageManager = this.storageConfiguration
					.createEmbeddedStorageFoundation()
					.start()
				;
				this.cacheStore = CacheStore.New(
					"cache",
					storageManager
				);
			}
			
			return this.cacheStore;
		}

		@Override
		public Factory<CacheLoader<K, V>> loaderFactory()
		{
			return this::cacheStore;
		}

		@Override
		public Factory<CacheWriter<? super K, ? super V>> writerFactory()
		{
			return this::cacheStore;
		}
		
	}
}
