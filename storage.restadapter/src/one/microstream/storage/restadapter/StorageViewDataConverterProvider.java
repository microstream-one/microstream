package one.microstream.storage.restadapter;

import one.microstream.collections.EqHashTable;

public interface StorageViewDataConverterProvider
{
	/**
	 * get converter for the requested format
	 *
	 * @param format
	 * @return StorageViewDataConverter
	 */
	StorageViewDataConverter getConverter(String format);

	/**
	 * register new data converter
	 *
	 * @param converter
	 * @param format
	 * @return true if successful registered, otherwise false
	 */
	boolean register(StorageViewDataConverter converter, String format);

	class Default implements StorageViewDataConverterProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		EqHashTable<String, StorageViewDataConverter> converters = EqHashTable.New();


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default()
		{
			super();
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageViewDataConverter getConverter(final String format)
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
		public boolean register(final StorageViewDataConverter converter, final String format)
		{
			return this.converters.add(format, converter);
		}
	}

}