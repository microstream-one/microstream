package one.microstream.examples.layeredentities._Human;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.examples.layeredentities.Human;
import one.microstream.examples.layeredentities.Address;
import java.lang.String;


public class HumanEntity extends EntityLayerIdentity implements Human
{
	protected HumanEntity()
	{
		super();
	}

	@Override
	protected Human entityData()
	{
		return (Human)super.entityData();
	}

	@Override
	public final Address address()
	{
		return this.entityData().address();
	}

	@Override
	public final Human partner()
	{
		return this.entityData().partner();
	}

	@Override
	public final String name()
	{
		return this.entityData().name();
	}

	@Override
	public String toString()
	{
		return HumanAppendable.toString(this);
	}
}