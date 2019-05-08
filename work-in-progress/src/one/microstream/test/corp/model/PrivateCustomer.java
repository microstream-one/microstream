package one.microstream.test.corp.model;

public final class PrivateCustomer extends Customer.Abstract
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Person person;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PrivateCustomer(final Person person)
	{
		super();
		this.person = person;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Person person()
	{
		return this.person;
	}

	@Override
	public String contactId()
	{
		return this.person().contactId();
	}

	@Override
	public String name()
	{
		return this.person().name();
	}

	@Override
	public Address address()
	{
		return this.person().address();
	}

	@Override
	public String note()
	{
		return this.person().note();
	}

	@Override
	public void setAddress(final Address address)
	{
		this.person().setAddress(address);
	}

	@Override
	public void setNote(final String note)
	{
		this.person().setNote(note);
	}

}
