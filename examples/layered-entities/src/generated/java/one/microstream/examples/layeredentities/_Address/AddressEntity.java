package one.microstream.examples.layeredentities._Address;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.examples.layeredentities.Address;
import java.lang.String;


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
	public final String street()
	{
		return this.entityData().street();
	}

	@Override
	public final String city()
	{
		return this.entityData().city();
	}

	@Override
	public final String zipCode()
	{
		return this.entityData().zipCode();
	}

	@Override
	public String toString()
	{
		return AddressAppendable.toString(this);
	}
}