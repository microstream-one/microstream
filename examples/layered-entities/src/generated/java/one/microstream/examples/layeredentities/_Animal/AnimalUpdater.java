package one.microstream.examples.layeredentities._Animal;

import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Animal;
import java.lang.String;


public interface AnimalUpdater extends Entity.Updater<Animal, AnimalUpdater>
{
	public static boolean setSpecies(final Animal animal, final String species)
	{
		return New(animal).species(species).update();
	}

	public static boolean setPartner(final Animal animal, final Animal partner)
	{
		return New(animal).partner(partner).update();
	}

	public AnimalUpdater species(String species);

	public AnimalUpdater partner(Animal partner);

	public static AnimalUpdater New(final Animal animal)
	{
		return new Default(animal);
	}

	public class Default
		extends Entity.Updater.Abstract<Animal, AnimalUpdater>
		implements AnimalUpdater
	{
		private String species;
		private Animal partner;

		protected Default(final Animal animal)
		{
			super(animal);
		}

		@Override
		public AnimalUpdater species(final String species)
		{
			this.species = species;
			return this;
		}

		@Override
		public AnimalUpdater partner(final Animal partner)
		{
			this.partner = partner;
			return this;
		}

		@Override
		public Animal createData(final Animal entityInstance)
		{
			return new AnimalData(entityInstance,
				this.species,
				this.partner);
		}

		@Override
		public AnimalUpdater copy(final Animal other)
		{
			final Animal data = Entity.data(other);
			this.species = data.species();
			this.partner = data.partner();
			return this;
		}
	}
}