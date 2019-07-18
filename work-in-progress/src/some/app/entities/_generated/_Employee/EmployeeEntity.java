package some.app.entities._generated._Employee;

import some.app.entities.Employee;
import some.app.entities._generated._Person.PersonEntity;

public class EmployeeEntity extends PersonEntity implements Employee
{
	EmployeeEntity()
	{
		super();
	}
	
	@Override
	public Employee $entityData()
	{
		return (Employee)super.$entityData();
	}

	@Override
	public final String employer()
	{
		return this.$entityData().employer();
	}
	
}
