package one.microstream.examples.layeredentities._Animal;

import one.microstream.examples.layeredentities.Animal;
import one.microstream.entity.EntityData;
import java.lang.String;


public class AnimalData extends EntityData implements Animal
{
	private final String species;
	private final Animal partner;

	protected AnimalData(final Animal entity,
		final String species,
		final Animal partner)
	{
		super(entity);

		this.species = species;
		this.partner = partner;
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
	public String toString()
	{
		return AnimalAppendable.toString(this);
	}
}