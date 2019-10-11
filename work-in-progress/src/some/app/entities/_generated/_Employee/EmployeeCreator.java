package some.app.entities._generated._Employee;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Employee;


public interface EmployeeCreator extends Entity.Creator<Employee, EmployeeCreator>
{
	public EmployeeCreator firstName(String firstName);

	public EmployeeCreator lastName(String lastName);

	public EmployeeCreator employer(String employer);

	public static EmployeeCreator New()
	{
		return new Default();
	}

	public static EmployeeCreator New(final Employee other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Employee, EmployeeCreator>
		implements EmployeeCreator
	{
		private String firstName;
		private String lastName ;
		private String employer ;

		protected Default()
		{
			super();
		}

		@Override
		public EmployeeCreator firstName(final String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		@Override
		public EmployeeCreator lastName(final String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		@Override
		public EmployeeCreator employer(final String employer)
		{
			this.employer = employer;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new EmployeeEntity();
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
		public EmployeeCreator copy(final Employee other)
		{
			final Employee data = Entity.data(other);
			this.firstName = data.firstName();
			this.lastName  = data.lastName ();
			this.employer  = data.employer ();
			return this;
		}
	}
}