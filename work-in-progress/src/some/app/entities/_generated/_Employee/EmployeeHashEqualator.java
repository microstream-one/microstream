package some.app.entities._generated._Employee;

import java.util.Objects;

import one.microstream.X;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Stateless;
import some.app.entities.Employee;
import some.app.entities._generated._Person.PersonHashEqualator;

public interface EmployeeHashEqualator extends HashEqualator<Employee>
{
	public static EmployeeHashEqualator Default()
	{
		return new EmployeeHashEqualator.Default();
	}
	
	public final class Default implements EmployeeHashEqualator, Stateless
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static boolean equals(final Employee e1, final Employee e2)
		{
			return PersonHashEqualator.Default.equals(e1, e2)
				&& X.equal(e1.employer(), e2.employer())
			;
		}
		
		public static int hashCode(final Employee employee)
		{
			return PersonHashEqualator.Default.hashCode(employee)
				| Objects.hashCode(employee.employer())
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
		public boolean equal(final Employee p1, final Employee p2)
		{
			return equals(p1, p2);
		}

		@Override
		public int hash(final Employee person)
		{
			return hashCode(person);
		}
		
	}
	
}
