package some.app.entities._generated._Person;

import java.util.Objects;

import one.microstream.X;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Stateless;
import some.app.entities.Person;


// example for a generated HashEqualator
public interface PersonHashEqualator extends HashEqualator<Person>
{
	public static PersonHashEqualator Default()
	{
		return new PersonHashEqualator.Default();
	}
	
	public final class Default implements PersonHashEqualator, Stateless
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static boolean equals(final Person p1, final Person p2)
		{
			return X.equal(p1.firstName(), p2.firstName())
				&& X.equal(p1.lastName (), p2.lastName ())
			;
		}
		
		public static int hashCode(final Person person)
		{
			return Objects.hashCode(person.firstName())
				| Objects.hashCode(person.lastName())
			;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean equal(final Person p1, final Person p2)
		{
			return equals(p1, p2);
		}

		@Override
		public int hash(final Person person)
		{
			return hashCode(person);
		}
		
	}
	
}
