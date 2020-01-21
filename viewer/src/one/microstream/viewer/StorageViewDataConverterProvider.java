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

}
