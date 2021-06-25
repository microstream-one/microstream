package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import java.util.ServiceLoader;

import one.microstream.collections.EqHashTable;

public interface StorageViewDataConverterRegistry extends StorageViewDataConverterProvider
{
	@Override
	public StorageViewDataConverter getConverter(String format);

	/**
	 * Registers a new data converter.
	 *
	 * @param converter
	 * @param format
	 * @return true if successful registered, otherwise false
	 */
	public boolean addConverter(StorageViewDataConverter converter, String format);
	
	
	public static StorageViewDataConverterRegistry New()
	{
		final StorageViewDataConverterRegistry registry = new StorageViewDataConverterRegistry.Default();
		
		final ServiceLoader<StorageViewDataConverter> serviceLoader = 
			ServiceLoader.load(StorageViewDataConverter.class);

		for (final StorageViewDataConverter converter : serviceLoader)
		{
			for (final String  format : converter.getFormatStrings())
			{
				registry.addConverter(converter, format);
			}
		}
		
		return registry;
	}
	

	
	public static class Default implements StorageViewDataConverterRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final EqHashTable<String, StorageViewDataConverter> converters;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default()
		{
			super();
			this.converters = EqHashTable.New();
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageViewDataConverter getConverter(
			final String format
		)
		{
			return this.converters.get(format);
		}

		/**
		 * register new data converter
		 *
		 * @param converter
		 * @param format
		 * @return true if successful registered, otherwise false
		 */
		@Override
		public boolean addConverter(
			final StorageViewDataConverter converter,
			final String                   format
		)
		{
			return this.converters.add(format, converter);
		}
	}
}
