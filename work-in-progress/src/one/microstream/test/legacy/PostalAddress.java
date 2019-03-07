package one.microstream.test.legacy;

public final class PostalAddress
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String city  ;
	final String street;
	final String number;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PostalAddress(final String city, final String street, final String number)
	{
		super();
		this.city   = city  ;
		this.street = street;
		this.number = number;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public final String city()
	{
		return this.city;
	}
		
	public final String street()
	{
		return this.street;
	}

	public final String number()
	{
		return this.number;
	}

	@Override
	public final String toString()
	{
		return this.city() + ' ' + this.street() + ' ' + this.number();
	}

}
