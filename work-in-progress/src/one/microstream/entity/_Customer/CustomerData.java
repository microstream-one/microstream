package one.microstream.entity._Customer;

import one.microstream.entity.Address;
import one.microstream.entity.Customer;
import one.microstream.entity.EntityData;


public class CustomerData extends EntityData implements Customer
{
	private final String  firstName;
	private final String  lastName ;
	private final Address address  ;

	protected CustomerData(final Customer entity,
		final String  firstName,
		final String  lastName ,
		final Address address  )
	{
		super(entity);

		this.firstName = firstName;
		this.lastName  = lastName ;
		this.address   = address  ;
	}

	@Override
	public String firstName()
	{
		return this.firstName;
	}

	@Override
	public String lastName()
	{
		return this.lastName;
	}

	@Override
	public Address address()
	{
		return this.address;
	}

	@Override
	public String toString()
	{
		return CustomerAppendable.toString(this);
	}
}