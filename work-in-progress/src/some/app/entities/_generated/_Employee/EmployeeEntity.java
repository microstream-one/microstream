package some.app.entities._generated._Employee;

import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Employee;


public class EmployeeEntity extends EntityLayerIdentity implements Employee
{
	protected EmployeeEntity()
	{
		super();
	}

	@Override
	protected Employee entityData()
	{
		return (Employee)super.entityData();
	}

	@Override
	public final String firstName()
	{
		return this.entityData().firstName();
	}

	@Override
	public final String lastName()
	{
		return this.entityData().lastName();
	}

	@Override
	public final String employer()
	{
		return this.entityData().employer();
	}
}