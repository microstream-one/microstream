package one.microstream.entity._Customer;

import one.microstream.entity.Address;
import one.microstream.entity.Customer;
import one.microstream.entity.EntityLayerIdentity;


public class CustomerEntity extends EntityLayerIdentity implements Customer
{
	protected CustomerEntity()
	{
		super();
	}

	@Override
	protected Customer entityData()
	{
		return (Customer)super.entityData();
	}

	@Override
	public final String firstName()
	{
		return this.entityData().firstName();
	}

	@Override
	public final String lastName()
	{
		return this.entityData().lastName();
	}

	@Override
	public final Address address()
	{
		return this.entityData().address();
	}

	@Override
	public String toString()
	{
		return CustomerAppendable.toString(this);
	}
}