package one.microstream.examples.layeredentities._Pet;

import one.microstream.chars.VarString;
import one.microstream.examples.layeredentities.Pet;


public interface PetAppendable extends VarString.Appendable
{
	public static String toString(final Pet pet)
	{
		return New(pet).appendTo(VarString.New()).toString();
	}

	public static PetAppendable New(final Pet pet)
	{
		return new Default(pet);
	}

	public static class Default implements PetAppendable
	{
		private final Pet pet;

		Default(final Pet pet)
		{
			super();

			this.pet = pet;
		}

		@Override
		public VarString appendTo(final VarString vs)
		{
			return vs.add(this.pet.getClass().getSimpleName())
				.add(" [species = ")
				.add(this.pet.species())
				.add(", partner = ")
				.add(this.pet.partner())
				.add(", name = ")
				.add(this.pet.name())
				.add(']');
		}
	}
}