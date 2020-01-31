package one.microstream.storage.restadapter;

import one.microstream.collections.EqHashTable;

public class StorageViewDataConverterProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	EqHashTable<String, StorageViewDataConverter> converter = EqHashTable.New();


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageViewDataConverterProvider()
	{
		final StorageViewDataConverterJson jsonConverter = new StorageViewDataConverterJson();
		this.converter.add("application/json", jsonConverter);
		this.converter.add("json", jsonConverter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public StorageViewDataConverter get(final String format)
	{
		return this.converter.get(format);
	}

	/**
	 * register new data converter
	 *
	 * @param converter
	 * @param format
	 * @return true if successful registered, otherwise false
	 */
	public boolean register(final StorageViewDataConverter converter, final String format)
	{
		return this.converter.add(format, converter);
	}

}
