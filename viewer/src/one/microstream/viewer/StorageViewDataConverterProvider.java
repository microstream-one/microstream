package one.microstream.viewer;

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
		this.converter.add("application/json", new StorageViewDataConverterJson());
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
