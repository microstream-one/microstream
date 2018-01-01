package some.app.entities;

import some.app.entities._generated._Person.Creator_Person;

/**
 * Central usability class containing util methods for various entity creator.
 * 
 * @author TM
 *
 */
public class AppEntities
{
	public static Creator_Person Person()
	{
		return Creator_Person.New();
	}
	
	public static Creator_Person Person(final Person other)
	{
		return Creator_Person.New(other);
	}
}
