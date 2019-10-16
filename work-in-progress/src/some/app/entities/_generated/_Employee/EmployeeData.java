package some.app.entities._generated._Employee;

import one.microstream.entity.EntityData;
import some.app.entities.Employee;


public class EmployeeData extends EntityData implements Employee
{
	private final String firstName;
	private final String lastName ;
	private final String employer ;

	protected EmployeeData(final Employee entity,
		final String firstName,
		final String lastName ,
		final String employer )
	{
		super(entity);

		this.firstName = firstName;
		this.lastName  = lastName ;
		this.employer  = employer ;
	}

	@Override
	public String firstName()
	{
		return this.firstName;
	}

	@Override
	public String lastName()
	{
		return this.lastName;
	}

	@Override
	public String employer()
	{
		return this.employer;
	}
}