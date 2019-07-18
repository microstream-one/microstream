package some.app.entities._generated._Person;

import one.microstream.entity.EntityData;
import some.app.entities.Person;

public class PersonData extends EntityData implements Person
{
	private final String firstName;
	private final String lastName ;

	protected PersonData(final Person entity, final String firstName, final String lastName)
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
	
	
	// (18.07.2019 TM)FIXME: see explanation in EntityData

	@Override
	public int hashCode()
	{
		return PersonHashEqualator.Default.hashCode(this);
	}

	@Override
	public boolean equals(final Object obj)
	{
		return this == obj
			|| obj instanceof PersonData
			&&  PersonHashEqualator.Default.equals(this, (PersonData)obj)
		;
	}
	
}
