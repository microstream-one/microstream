package some.app.entities._generated._Employee;

import java.util.Objects;

import one.microstream.X;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Stateless;
import some.app.entities.Employee;


public interface EmployeeHashEqualator extends HashEqualator<Employee>
{
	public static EmployeeHashEqualator New()
	{
		return new Default();
	}

	public final class Default implements EmployeeHashEqualator, Stateless
	{
		public static boolean equals(final Employee employee1, final Employee employee2)
		{
			return X.equal(employee1.firstName(), employee2.firstName())
				&& X.equal(employee1.lastName (), employee2.lastName ())
				&& X.equal(employee1.employer (), employee2.employer ())
			;
		}

		public static int hashCode(final Employee employee)
		{
			return Objects.hash(
				employee.firstName(),
				employee.lastName (),
				employee.employer ()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Employee employee1, final Employee employee2)
		{
			return equals(employee1, employee2);
		}

		@Override
		public int hash(final Employee employee)
		{
			return hashCode(employee);
		}
	}
}