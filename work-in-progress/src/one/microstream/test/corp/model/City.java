package one.microstream.test.corp.model;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XTable;

public final class City
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String                      name         ;
	final EqHashTable<String, Street> streetsByName;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public City(final String name)
	{
		super();
		this.name          = name             ;
		this.streetsByName = EqHashTable.New();
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final String name()
	{
		return this.name;
	}

	public final XTable<String, Street> streetsByName()
	{
		return this.streetsByName;
	}

	public synchronized void registerStreet(final Street street)
	{
		this.streetsByName.add(street.name(), street);
	}

}
