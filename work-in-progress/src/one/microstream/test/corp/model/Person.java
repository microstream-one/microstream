package one.microstream.test.corp.model;

public final class Person extends Contact.Abstract
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private String firstname;
	private String lastname ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Person(final String contactId, final String firstname, final String lastname, final Address address)
	{
		super(contactId, address);
		this.firstname = firstname;
		this.lastname  = lastname ;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public void setFirstname(final String firstname)
	{
		this.firstname = firstname;
	}

	public void setLastname(final String lastname)
	{
		this.lastname = lastname;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public String firstname()
	{
		return this.firstname;
	}

	public String lastname()
	{
		return this.lastname;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String name()
	{
		return this.firstname() + ' ' + this.lastname();
	}

	@Override
	public Person person()
	{
		return this;
	}

	@Override
	public String toString()
	{
		return this.name();
	}

}
