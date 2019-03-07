package one.microstream.test.corp.model;

public final class EMailAddress
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String value      ;
	final String description;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public EMailAddress(final String value, final String description)
	{
		super();
		this.value       = value      ;
		this.description = description;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final String value()
	{
		return this.value;
	}

	public final String description()
	{
		return this.description;
	}

}
