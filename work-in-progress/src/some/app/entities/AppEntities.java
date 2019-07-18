package some.app.entities;

import some.app.entities._generated._Employee.EmployeeCreator;
import some.app.entities._generated._Person.PersonCreator;

/**
 * Central usability class containing util methods for various entity creator.
 * 
 * @author TM
 *
 */
public class AppEntities
{
	public static PersonCreator Person()
	{
		return PersonCreator.New();
	}
	
	public static PersonCreator Person(final Person other)
	{
		return PersonCreator.New(other);
	}
	
	public static EmployeeCreator Employee()
	{
		return EmployeeCreator.New();
	}
	
	public static EmployeeCreator Employee(final Employee other)
	{
		return EmployeeCreator.New(other);
	}
}
