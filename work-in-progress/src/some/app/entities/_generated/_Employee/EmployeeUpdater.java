package some.app.entities._generated._Employee;

import one.microstream.entity.Entity;
import some.app.entities.Employee;


public class EmployeeUpdater
{
	public static boolean setFirstName(final Employee employee, final String firstName)
	{
		return Entity.updateData(
			employee,
			EmployeeCreator.New(employee).firstName(firstName).createData());
	}

	public static boolean setLastName(final Employee employee, final String lastName)
	{
		return Entity.updateData(
			employee,
			EmployeeCreator.New(employee).lastName(lastName).createData());
	}

	public static boolean setEmployer(final Employee employee, final String employer)
	{
		return Entity.updateData(
			employee,
			EmployeeCreator.New(employee).employer(employer).createData());
	}

	protected EmployeeUpdater()
	{
		super();
	}
}