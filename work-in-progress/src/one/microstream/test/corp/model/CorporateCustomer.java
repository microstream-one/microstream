package one.microstream.test.corp.model;


public final class CorporateCustomer extends Customer.Abstract
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Corporation corporation;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CorporateCustomer(final Corporation corporation)
	{
		super();
		this.corporation = corporation;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public Corporation corporation()
	{
		return this.corporation;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String contactId()
	{
		return this.corporation().contactId();
	}

	@Override
	public String name()
	{
		return this.corporation().name();
	}

	@Override
	public Person person()
	{
		return this.corporation().person();
	}

	@Override
	public Address address()
	{
		return this.corporation().address();
	}

	@Override
	public String note()
	{
		return this.corporation().note();
	}

	@Override
	public final void setAddress(final Address address)
	{
		this.corporation().setAddress(address);
	}

	@Override
	public final void setNote(final String note)
	{
		this.corporation().setNote(note);
	}

}
