package one.microstream.examples.layeredentities._Address;

import one.microstream.examples.layeredentities.Address;
import one.microstream.entity.EntityData;
import java.lang.String;


public class AddressData extends EntityData implements Address
{
	private final String street ;
	private final String city   ;
	private final String zipCode;

	protected AddressData(final Address entity,
		final String street ,
		final String city   ,
		final String zipCode)
	{
		super(entity);

		this.street  = street ;
		this.city    = city   ;
		this.zipCode = zipCode;
	}

	@Override
	public String street()
	{
		return this.street;
	}

	@Override
	public String city()
	{
		return this.city;
	}

	@Override
	public String zipCode()
	{
		return this.zipCode;
	}

	@Override
	public String toString()
	{
		return AddressAppendable.toString(this);
	}
}