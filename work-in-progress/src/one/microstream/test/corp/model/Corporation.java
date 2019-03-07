package one.microstream.test.corp.model;

public class Corporation extends Contact.AbstractImplementation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final String name   ;
	private final String taxId  ;
	private       Person contact;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Corporation(
		final String  contactId,
		final String  name     ,
		final String  taxId    ,
		final Person  contact  ,
		final Address address
	)
	{
		super(contactId, address);
		this.taxId = taxId;
		this.name = name;
		this.contact = contact;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public void setContact(final Person contact)
	{
		this.contact = contact;
	}

	public String taxId()
	{
		return this.taxId;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String name()
	{
		return this.name;
	}

	@Override
	public Person person()
	{
		return this.contact;
	}

	@Override
	public String toString()
	{
		return this.name();
	}

}
