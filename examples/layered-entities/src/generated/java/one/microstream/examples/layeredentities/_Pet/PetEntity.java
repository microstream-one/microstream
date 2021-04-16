package one.microstream.examples.layeredentities._Pet;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.examples.layeredentities.Animal;
import java.lang.String;
import one.microstream.examples.layeredentities.Pet;


public class PetEntity extends EntityLayerIdentity implements Pet
{
	protected PetEntity()
	{
		super();
	}

	@Override
	protected Pet entityData()
	{
		return (Pet)super.entityData();
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
	public final String name()
	{
		return this.entityData().name();
	}

	@Override
	public String toString()
	{
		return PetAppendable.toString(this);
	}
}