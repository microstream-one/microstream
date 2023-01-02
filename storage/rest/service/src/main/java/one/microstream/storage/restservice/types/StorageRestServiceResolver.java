package one.microstream.storage.restservice.types;

/*-
 * #%L
 * microstream-storage-restservice
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

import java.util.Iterator;
import java.util.ServiceLoader;

import one.microstream.storage.restadapter.types.StorageRestAdapter;
import one.microstream.storage.restservice.exceptions.StorageRestServiceNotFoundException;
import one.microstream.storage.types.StorageManager;

/**
 * Service loader for {@link StorageRestService}s.
 *
 */
public final class StorageRestServiceResolver
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Get the first found implementation of the StorageRestService interface.
	 *
	 * @param storage storage to initialize the service with
	 * @return StorageRestService instance
	 */
	public static StorageRestService resolve(final StorageManager storage)
	{
		final StorageRestServiceProvider provider = resolveProvider();
		if(provider != null)
		{
			return provider.provideService(
				StorageRestAdapter.New(storage)
			);
		}

		throw new StorageRestServiceNotFoundException("No StorageRestServer implementation found");
	}

	/**
	 * Get the first found implementation of the StorageRestService interface.
	 *
	 * @param storageRestAdapter rest adapter to initialize the service with
	 * @return StorageRestService instance
	 */
	public static StorageRestService resolve(final StorageRestAdapter storageRestAdapter)
	{
		final StorageRestServiceProvider provider = resolveProvider();
		if(provider != null)
		{
			return provider.provideService(storageRestAdapter);
		}

		throw new StorageRestServiceNotFoundException("No StorageRestServer implementation found");
	}

	public static StorageRestServiceProvider resolveProvider()
	{
		final ServiceLoader<StorageRestServiceProvider> serviceLoader =
			ServiceLoader.load(StorageRestServiceProvider.class);
		final Iterator<StorageRestServiceProvider> iterator = serviceLoader.iterator();
		return iterator.hasNext()
			? iterator.next()
			: null
		;
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private StorageRestServiceResolver()
	{
		throw new Error();
	}
}
