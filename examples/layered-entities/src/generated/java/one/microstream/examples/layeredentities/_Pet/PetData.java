package one.microstream.examples.layeredentities._Pet;

import one.microstream.examples.layeredentities.Animal;
import one.microstream.entity.EntityData;
import java.lang.String;
import one.microstream.examples.layeredentities.Pet;


public class PetData extends EntityData implements Pet
{
	private final String species;
	private final Animal partner;
	private final String name   ;

	protected PetData(final Pet entity,
		final String species,
		final Animal partner,
		final String name   )
	{
		super(entity);

		this.species = species;
		this.partner = partner;
		this.name    = name   ;
	}

	@Override
	public String species()
	{
		return this.species;
	}

	@Override
	public Animal partner()
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
		return PetAppendable.toString(this);
	}
}