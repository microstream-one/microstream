package some.app.entities._generated._Employee;

import java.util.Objects;

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
		return Objects.hash(
			this.firstName(), 
			this.lastName (),
			this.employer ()
		);
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj
			|| (	obj instanceof EmployeeData 
				&&  EmployeeEqualator.INSTANCE.equal(this, (EmployeeData)obj)
			   );
	}
}
