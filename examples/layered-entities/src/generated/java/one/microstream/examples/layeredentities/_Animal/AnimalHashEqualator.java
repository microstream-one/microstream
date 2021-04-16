package one.microstream.examples.layeredentities._Animal;

import one.microstream.typing.Stateless;
import one.microstream.examples.layeredentities.Animal;
import java.util.Objects;
import one.microstream.X;
import one.microstream.hashing.HashEqualator;


public interface AnimalHashEqualator extends HashEqualator<Animal>
{
	public static AnimalHashEqualator New()
	{
		return new Default();
	}

	public static class Default implements AnimalHashEqualator, Stateless
	{
		public static boolean equals(final Animal animal1, final Animal animal2)
		{
			return X.equal(animal1.species(), animal2.species())
				&& X.equal(animal1.partner(), animal2.partner())
			;
		}

		public static int hashCode(final Animal animal)
		{
			return Objects.hash(
				animal.species(),
				animal.partner()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Animal animal1, final Animal animal2)
		{
			return equals(animal1, animal2);
		}

		@Override
		public int hash(final Animal animal)
		{
			return hashCode(animal);
		}
	}
}