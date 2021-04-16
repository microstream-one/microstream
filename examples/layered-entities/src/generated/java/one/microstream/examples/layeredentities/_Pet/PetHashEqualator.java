package one.microstream.examples.layeredentities._Pet;

import one.microstream.typing.Stateless;
import java.util.Objects;
import one.microstream.X;
import one.microstream.hashing.HashEqualator;
import one.microstream.examples.layeredentities.Pet;


public interface PetHashEqualator extends HashEqualator<Pet>
{
	public static PetHashEqualator New()
	{
		return new Default();
	}

	public static class Default implements PetHashEqualator, Stateless
	{
		public static boolean equals(final Pet pet1, final Pet pet2)
		{
			return X.equal(pet1.species(), pet2.species())
				&& X.equal(pet1.partner(), pet2.partner())
				&& X.equal(pet1.name   (), pet2.name   ())
			;
		}

		public static int hashCode(final Pet pet)
		{
			return Objects.hash(
				pet.species(),
				pet.partner(),
				pet.name   ()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Pet pet1, final Pet pet2)
		{
			return equals(pet1, pet2);
		}

		@Override
		public int hash(final Pet pet)
		{
			return hashCode(pet);
		}
	}
}