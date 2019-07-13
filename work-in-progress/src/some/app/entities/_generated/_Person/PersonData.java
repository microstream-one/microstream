package some.app.entities._generated._Person;

import java.util.Objects;

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

	@Override
	public int hashCode()
	{
		return Objects.hash(
			this.firstName(), 
			this.lastName ()
		);
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj
			|| (	obj instanceof PersonData 
				&&  PersonEqualator.INSTANCE.equal(this, (PersonData)obj)
			   );
	}
}
