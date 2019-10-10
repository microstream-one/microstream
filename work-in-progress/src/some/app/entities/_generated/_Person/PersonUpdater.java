package some.app.entities._generated._Person;

import one.microstream.entity.Entity;
import some.app.entities.Person;


public class PersonUpdater
{
	public static boolean setFirstName(final Person person, final String firstName)
	{
		return Entity.updateData(
			person,
			PersonCreator.New(person).firstName(firstName).createData());
	}

	public static boolean setLastName(final Person person, final String lastName)
	{
		return Entity.updateData(
			person,
			PersonCreator.New(person).lastName(lastName).createData());
	}

	protected PersonUpdater()
	{
		super();
	}
}