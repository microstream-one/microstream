package one.microstream.test.corp.model;

import one.microstream.collections.types.XTable;

public final class BusinessYear
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final ClientCorporation     owner ;
	final Integer               year  ;
	final XTable<String, Order> orders;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BusinessYear(
		final ClientCorporation     owner ,
		final Integer               year  ,
		final XTable<String, Order> orders
	)
	{
		super();
		this.owner  = owner ;
		this.year   = year  ;
		this.orders = orders;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final ClientCorporation owner()
	{
		return this.owner;
	}

	public final Integer year()
	{
		return this.year;
	}

	public final XTable<String, Order> orders()
	{
		return this.orders;
	}

}
