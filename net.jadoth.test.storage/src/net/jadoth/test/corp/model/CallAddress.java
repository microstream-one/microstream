package net.jadoth.test.corp.model;


public final class CallAddress
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final PhoneNumber  phoneNumber  ;
	final EMailAddress eMailAddresse;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CallAddress(final PhoneNumber phoneNumber, final EMailAddress eMailAddresse)
	{
		super();
		this.phoneNumber   = phoneNumber  ;
		this.eMailAddresse = eMailAddresse;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final PhoneNumber phoneNumber()
	{
		return this.phoneNumber;
	}

	public final EMailAddress eMailAddresse()
	{
		return this.eMailAddresse;
	}

}
