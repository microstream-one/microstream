package one.microstream.examples.layeredentities._Pet;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Animal;
import java.lang.String;
import one.microstream.examples.layeredentities.Pet;


public interface PetCreator extends Entity.Creator<Pet, PetCreator>
{
	public PetCreator species(String species);

	public PetCreator partner(Animal partner);

	public PetCreator name(String name);

	public static PetCreator New()
	{
		return new Default();
	}

	public static PetCreator New(final Pet other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Pet, PetCreator>
		implements PetCreator
	{
		private String species;
		private Animal partner;
		private String name   ;

		protected Default()
		{
			super();
		}

		@Override
		public PetCreator species(final String species)
		{
			this.species = species;
			return this;
		}

		@Override
		public PetCreator partner(final Animal partner)
		{
			this.partner = partner;
			return this;
		}

		@Override
		public PetCreator name(final String name)
		{
			this.name = name;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new PetEntity();
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
		public PetCreator copy(final Pet other)
		{
			final Pet data = Entity.data(other);
			this.species = data.species();
			this.partner = data.partner();
			this.name    = data.name   ();
			return this;
		}
	}
}