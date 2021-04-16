package one.microstream.examples.layeredentities._Animal;

import one.microstream.examples.layeredentities.Animal;
import one.microstream.chars.VarString;


public interface AnimalAppendable extends VarString.Appendable
{
	public static String toString(final Animal animal)
	{
		return New(animal).appendTo(VarString.New()).toString();
	}

	public static AnimalAppendable New(final Animal animal)
	{
		return new Default(animal);
	}

	public static class Default implements AnimalAppendable
	{
		private final Animal animal;

		Default(final Animal animal)
		{
			super();

			this.animal = animal;
		}

		@Override
		public VarString appendTo(final VarString vs)
		{
			return vs.add(this.animal.getClass().getSimpleName())
				.add(" [species = ")
				.add(this.animal.species())
				.add(", partner = ")
				.add(this.animal.partner())
				.add(']');
		}
	}
}