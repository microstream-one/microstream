
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.cache.configuration.OptionalFeature;


/**
 * JSR-107 compliant {@link javax.cache.spi.CachingProvider}.
 *
 */
public class CachingProvider implements javax.cache.spi.CachingProvider
{
	public static URI defaultURI()
	{
		try
		{
			return new URI("microstream");
		}
		catch(final URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private final WeakHashMap<ClassLoader, HashMap<URI, CacheManager>> cacheManagers = new WeakHashMap<>();

	public CachingProvider()
	{
		super();
	}

	@Override
	public ClassLoader getDefaultClassLoader()
	{
		return this.getClass().getClassLoader();
	}

	@Override
	public URI getDefaultURI()
	{
		return defaultURI();
	}

	@Override
	public Properties getDefaultProperties()
	{
		return null;
	}

	@Override
	public CacheManager getCacheManager()
	{
		return this.getCacheManager(
			this.getDefaultURI(),
			this.getDefaultClassLoader()
		);
	}

	@Override
	public CacheManager getCacheManager(
		final URI uri,
		final ClassLoader classLoader
	)
	{
		return this.getCacheManager(
			uri,
			classLoader,
			this.getDefaultProperties()
		);
	}

	@Override
	public synchronized CacheManager getCacheManager(
		final URI uri,
		final ClassLoader classLoader,
		final Properties properties
	)
	{
		final URI         managerURI         = uri == null
			? this.getDefaultURI()
			: uri;

		final ClassLoader managerClassLoader = classLoader == null
			? this.getDefaultClassLoader()
			: classLoader;

		final Properties  managerProperties  = properties == null
			? new Properties()
			: properties;

		return this.cacheManagers.computeIfAbsent(managerClassLoader, cl -> new HashMap<>())
			.computeIfAbsent(managerURI,
				key -> CacheManager.New(this, managerURI, managerClassLoader, managerProperties)
			);
	}

	@Override
	public synchronized void close()
	{
		/*
		 * Collect to list because CacheManager#close modifies this#cacheManagers
		 */
		final List<CacheManager> managers = this.cacheManagers.values().stream()
			.flatMap(kv -> kv.values().stream())
			.collect(Collectors.toList());
		managers.forEach(CacheManager::close);

		this.cacheManagers.clear();
	}

	@Override
	public synchronized void close(final ClassLoader classLoader)
	{
		final ClassLoader                managerClassLoader = classLoader == null
			? this.getDefaultClassLoader()
			: classLoader;

		final HashMap<URI, CacheManager> cacheManagersByURI;
		if((cacheManagersByURI = this.cacheManagers.remove(managerClassLoader)) != null)
		{
			cacheManagersByURI.values()
				.forEach(CacheManager::close);
		}
	}

	@SuppressWarnings("resource")
	@Override
	public synchronized void close(final URI uri, final ClassLoader classLoader)
	{
		final URI                        managerURI         = uri == null
			? this.getDefaultURI()
			: uri;

		final ClassLoader                managerClassLoader = classLoader == null
			? this.getDefaultClassLoader()
			: classLoader;

		final HashMap<URI, CacheManager> cacheManagersByURI;
		if((cacheManagersByURI = this.cacheManagers.get(managerClassLoader)) != null)
		{
			final CacheManager cacheManager;
			if((cacheManager = cacheManagersByURI.remove(managerURI)) != null)
			{
				cacheManager.close();

				if(cacheManagersByURI.size() == 0)
				{
					this.cacheManagers.remove(managerClassLoader);
				}
			}
		}
	}

	synchronized void remove(final URI uri, final ClassLoader classLoader)
	{
		final HashMap<URI, CacheManager> cacheManagersByURI;
		if((cacheManagersByURI = this.cacheManagers.get(classLoader)) != null)
		{
			if(cacheManagersByURI.remove(uri) != null && cacheManagersByURI.size() == 0)
			{
				this.cacheManagers.remove(classLoader);
			}
		}
	}

	@Override
	public boolean isSupported(final OptionalFeature optionalFeature)
	{
		switch(optionalFeature)
		{
			case STORE_BY_REFERENCE:
				return true;

			default:
				return false;
		}
	}

}
