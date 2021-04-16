package one.microstream.examples.layeredentities._Animal;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.examples.layeredentities.Animal;
import java.lang.String;


public class AnimalEntity extends EntityLayerIdentity implements Animal
{
	protected AnimalEntity()
	{
		super();
	}

	@Override
	protected Animal entityData()
	{
		return (Animal)super.entityData();
	}

	@Override
	public final String species()
	{
		return this.entityData().species();
	}

	@Override
	public final Animal partner()
	{
		return this.entityData().partner();
	}

	@Override
	public String toString()
	{
		return AnimalAppendable.toString(this);
	}
}