package one.microstream.test.corp.model;

public final class Address
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Contact       owner        ;
	      PostalAddress postalAddress;
	      CallAddress   callAddress  ;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Address(
		final Contact       owner        ,
		final PostalAddress postalAddress,
		final CallAddress   callAddress
	)
	{
		super();
		this.owner         = owner        ;
		this.postalAddress = postalAddress;
		this.callAddress   = callAddress  ;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public void setPostalAddress(final PostalAddress postalAddress)
	{
		this.postalAddress = postalAddress;
	}

	public void setCallAddress(final CallAddress callAddress)
	{
		this.callAddress = callAddress;
	}

	public final Contact owner()
	{
		return this.owner;
	}

	public final PostalAddress postalAddress()
	{
		return this.postalAddress;
	}

	public final CallAddress callAddress()
	{
		return this.callAddress;
	}

}
