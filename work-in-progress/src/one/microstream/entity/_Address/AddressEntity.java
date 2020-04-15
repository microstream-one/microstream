package one.microstream.entity._Address;

import one.microstream.entity.Address;
import one.microstream.entity.EntityLayerIdentity;


public class AddressEntity extends EntityLayerIdentity implements Address
{
	protected AddressEntity()
	{
		super();
	}

	@Override
	protected Address entityData()
	{
		return (Address)super.entityData();
	}

	@Override
	public final String line1()
	{
		return this.entityData().line1();
	}

	@Override
	public final String line2()
	{
		return this.entityData().line2();
	}

	@Override
	public final String city()
	{
		return this.entityData().city();
	}

	@Override
	public String toString()
	{
		return AddressAppendable.toString(this);
	}
}