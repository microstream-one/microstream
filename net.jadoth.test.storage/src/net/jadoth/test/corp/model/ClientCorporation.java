package net.jadoth.test.corp.model;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XTable;

public final class ClientCorporation extends Corporation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final EqHashTable<Integer, BusinessYear> businessYears  = EqHashTable.New();
	private final EqHashTable<String, Customer>      customersById  = EqHashTable.New();
	private final EqHashTable<String, Vendor>        vendorsById    = EqHashTable.New();
	private final EqHashTable<String, City>          citiesByName   = EqHashTable.New();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ClientCorporation(
		final String  contactId,
		final String  name     ,
		final String  taxId    ,
		final Person  contact  ,
		final Address address
	)
	{
		super(contactId, name, taxId, contact, address);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final XTable<Integer, BusinessYear> businessYears()
	{
		return this.businessYears;
	}

	public final XTable<String, Customer> customersById()
	{
		return this.customersById;
	}

	public final XTable<String, Vendor> vendorsById()
	{
		return this.vendorsById;
	}

	public final XTable<String, City> citiesByName()
	{
		return this.citiesByName;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final String toString()
	{
		return this.name();
	}

}
