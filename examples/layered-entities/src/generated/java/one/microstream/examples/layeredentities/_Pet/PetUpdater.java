package one.microstream.examples.layeredentities._Pet;

import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Animal;
import java.lang.String;
import one.microstream.examples.layeredentities.Pet;


public interface PetUpdater extends Entity.Updater<Pet, PetUpdater>
{
	public static boolean setSpecies(final Pet pet, final String species)
	{
		return New(pet).species(species).update();
	}

	public static boolean setPartner(final Pet pet, final Animal partner)
	{
		return New(pet).partner(partner).update();
	}

	public static boolean setName(final Pet pet, final String name)
	{
		return New(pet).name(name).update();
	}

	public PetUpdater species(String species);

	public PetUpdater partner(Animal partner);

	public PetUpdater name(String name);

	public static PetUpdater New(final Pet pet)
	{
		return new Default(pet);
	}

	public class Default
		extends Entity.Updater.Abstract<Pet, PetUpdater>
		implements PetUpdater
	{
		private String species;
		private Animal partner;
		private String name   ;

		protected Default(final Pet pet)
		{
			super(pet);
		}

		@Override
		public PetUpdater species(final String species)
		{
			this.species = species;
			return this;
		}

		@Override
		public PetUpdater partner(final Animal partner)
		{
			this.partner = partner;
			return this;
		}

		@Override
		public PetUpdater name(final String name)
		{
			this.name = name;
			return this;
		}

		@Override
		public Pet createData(final Pet entityInstance)
		{
			return new PetData(entityInstance,
				this.species,
				this.partner,
				this.name   );
		}

		@Override
		public PetUpdater copy(final Pet other)
		{
			final Pet data = Entity.data(other);
			this.species = data.species();
			this.partner = data.partner();
			this.name    = data.name   ();
			return this;
		}
	}
}