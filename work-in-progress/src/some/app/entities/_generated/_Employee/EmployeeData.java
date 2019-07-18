package some.app.entities._generated._Employee;

import some.app.entities.Employee;
import some.app.entities._generated._Person.PersonData;

public class EmployeeData extends PersonData implements Employee
{
	private final String employer;

	protected EmployeeData(final Employee entity, final String firstName, final String lastName, final String employer)
	{
		super(entity, firstName, lastName);
		
		this.employer = employer;
	}

	@Override
	public String employer()
	{
		return this.employer;
	}

	@Override
	public int hashCode()
	{
		return EmployeeHashEqualator.Default.hashCode(this);
	}

	@Override
	public boolean equals(final Object obj)
	{
		return this == obj
			|| obj instanceof EmployeeData
			&&  EmployeeHashEqualator.Default.equals(this, (EmployeeData)obj);
	}
}
