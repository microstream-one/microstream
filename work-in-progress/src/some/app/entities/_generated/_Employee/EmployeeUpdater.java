package some.app.entities._generated._Employee;

import one.microstream.entity.Entity;
import some.app.entities.Employee;


public interface EmployeeUpdater extends Entity.Updater<Employee, EmployeeUpdater>
{
	public static boolean setFirstName(final Employee employee, final String firstName)
	{
		return New(employee).firstName(firstName).update();
	}

	public static boolean setLastName(final Employee employee, final String lastName)
	{
		return New(employee).lastName(lastName).update();
	}

	public static boolean setEmployer(final Employee employee, final String employer)
	{
		return New(employee).employer(employer).update();
	}

	public EmployeeUpdater firstName(String firstName);

	public EmployeeUpdater lastName(String lastName);

	public EmployeeUpdater employer(String employer);

	public static EmployeeUpdater New(final Employee employee)
	{
		return new Default(employee);
	}

	public class Default
		extends Entity.Updater.Abstract<Employee, EmployeeUpdater>
		implements EmployeeUpdater
	{
		private String firstName;
		private String lastName ;
		private String employer ;

		protected Default(final Employee employee)
		{
			super(employee);
		}

		@Override
		public EmployeeUpdater firstName(final String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		@Override
		public EmployeeUpdater lastName(final String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		@Override
		public EmployeeUpdater employer(final String employer)
		{
			this.employer = employer;
			return this;
		}

		@Override
		public Employee createData(final Employee entityInstance)
		{
			return new EmployeeData(entityInstance,
				this.firstName,
				this.lastName ,
				this.employer );
		}

		@Override
		public EmployeeUpdater copy(final Employee other)
		{
			final Employee data = Entity.data(other);
			this.firstName = data.firstName();
			this.lastName  = data.lastName ();
			this.employer  = data.employer ();
			return this;
		}
	}
}