package some.app.entities._generated._Employee;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Employee;

public interface EmployeeCreator extends Entity.Creator<Employee,EmployeeCreator>
{
	public EmployeeCreator firstName(String value);
	
	public EmployeeCreator lastName(String value);
	
	public EmployeeCreator employer(String value);
		
	
	public static EmployeeCreator New()
	{
		return new EmployeeCreator.Default();
	}
	
	public static EmployeeCreator New(final Employee other)
	{
		return New().entity(other).copy(other);
	}
	
	
	
	public class Default
	extends Entity.Creator.Abstract<Employee,EmployeeCreator>
	implements EmployeeCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private String firstName;
		private String lastName ;
		private String employer ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		
		@Override
		public EmployeeCreator firstName(final String value)
		{
			this.firstName = value;
			return this;
		}

		@Override
		public EmployeeCreator lastName(final String value)
		{
			this.lastName = value;
			return this;
		}

		@Override
		public EmployeeCreator employer(final String value)
		{
			this.employer = value;
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
			return new EmployeeData(entityInstance, this.firstName, this.lastName, this.employer);
		}
		
		@Override
		public EmployeeCreator copy(final Employee other)
		{
			final Employee data = (Employee)other.$data();
			this.firstName = data.firstName();
			this.lastName  = data.lastName() ;
			this.employer  = data.employer() ;
			
			return this;
		}
		
	}
	
}
