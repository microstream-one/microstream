package some.app.entities._generated._Person;

import java.util.Objects;

import one.microstream.X;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Stateless;
import some.app.entities.Person;


public interface PersonHashEqualator extends HashEqualator<Person>
{
	public static PersonHashEqualator New()
	{
		return new Default();
	}

	public final class Default implements PersonHashEqualator, Stateless
	{
		public static boolean equals(final Person person1, final Person person2)
		{
			return X.equal(person1.firstName(), person2.firstName())
				&& X.equal(person1.lastName (), person2.lastName ())
			;
		}

		public static int hashCode(final Person person)
		{
			return Objects.hash(
				person.firstName(),
				person.lastName ()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Person person1, final Person person2)
		{
			return equals(person1, person2);
		}

		@Override
		public int hash(final Person person)
		{
			return hashCode(person);
		}
	}
}