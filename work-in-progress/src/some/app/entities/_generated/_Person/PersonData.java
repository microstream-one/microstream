package some.app.entities._generated._Person;

import one.microstream.entity.EntityData;
import some.app.entities.Person;


public class PersonData extends EntityData implements Person
{
	private final String firstName;
	private final String lastName ;

	protected PersonData(final Person entity,
		final String firstName,
		final String lastName )
	{
		super(entity);

		this.firstName = firstName;
		this.lastName  = lastName ;
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
}