package one.microstream.examples.layeredentities._Animal;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Animal;
import java.lang.String;


public interface AnimalCreator extends Entity.Creator<Animal, AnimalCreator>
{
	public AnimalCreator species(String species);

	public AnimalCreator partner(Animal partner);

	public static AnimalCreator New()
	{
		return new Default();
	}

	public static AnimalCreator New(final Animal other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Animal, AnimalCreator>
		implements AnimalCreator
	{
		private String species;
		private Animal partner;

		protected Default()
		{
			super();
		}

		@Override
		public AnimalCreator species(final String species)
		{
			this.species = species;
			return this;
		}

		@Override
		public AnimalCreator partner(final Animal partner)
		{
			this.partner = partner;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new AnimalEntity();
		}

		@Override
		public Animal createData(final Animal entityInstance)
		{
			return new AnimalData(entityInstance,
				this.species,
				this.partner);
		}

		@Override
		public AnimalCreator copy(final Animal other)
		{
			final Animal data = Entity.data(other);
			this.species = data.species();
			this.partner = data.partner();
			return this;
		}
	}
}