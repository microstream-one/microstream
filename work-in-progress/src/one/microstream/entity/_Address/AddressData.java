package one.microstream.entity._Address;

import one.microstream.entity.Address;
import one.microstream.entity.EntityData;


public class AddressData extends EntityData implements Address
{
	private final String line1;
	private final String line2;
	private final String city ;

	protected AddressData(final Address entity,
		final String line1,
		final String line2,
		final String city )
	{
		super(entity);

		this.line1 = line1;
		this.line2 = line2;
		this.city  = city ;
	}

	@Override
	public String line1()
	{
		return this.line1;
	}

	@Override
	public String line2()
	{
		return this.line2;
	}

	@Override
	public String city()
	{
		return this.city;
	}

	@Override
	public String toString()
	{
		return AddressAppendable.toString(this);
	}
}