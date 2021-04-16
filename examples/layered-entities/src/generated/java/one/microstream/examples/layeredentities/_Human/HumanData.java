package one.microstream.examples.layeredentities._Human;

import one.microstream.examples.layeredentities.Human;
import one.microstream.examples.layeredentities.Address;
import one.microstream.entity.EntityData;
import java.lang.String;


public class HumanData extends EntityData implements Human
{
	private final Address address;
	private final Human   partner;
	private final String  name   ;

	protected HumanData(final Human entity,
		final Address address,
		final Human   partner,
		final String  name   )
	{
		super(entity);

		this.address = address;
		this.partner = partner;
		this.name    = name   ;
	}

	@Override
	public Address address()
	{
		return this.address;
	}

	@Override
	public Human partner()
	{
		return this.partner;
	}

	@Override
	public String name()
	{
		return this.name;
	}

	@Override
	public String toString()
	{
		return HumanAppendable.toString(this);
	}
}