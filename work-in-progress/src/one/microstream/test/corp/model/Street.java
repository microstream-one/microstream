package one.microstream.test.corp.model;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XTable;


public final class Street
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	String                       name              ;
	City                         city              ;
	EqHashTable<String, Contact> residentsPerNumber = EqHashTable.New();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Street(final String name, final City city)
	{
		super();
		this.name = name;
		this.city = city;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public String name()
	{
		return this.name;
	}

	public City city()
	{
		return this.city;
	}

	public XTable<String, Contact> residentsPerNumber()
	{
		return this.residentsPerNumber;
	}

	public synchronized void registerContact(final Contact contact)
	{
		this.residentsPerNumber.add(contact.address().postalAddress().number(), contact);
	}

}
