package one.microstream.storage.restadapter.types;

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
