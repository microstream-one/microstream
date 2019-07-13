package some.app.entities._generated._Person;

import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Person;

public class PersonEntity extends EntityLayerIdentity implements Person
{
	protected PersonEntity()
	{
		super();
	}
	
	@Override
	public Person $data()
	{
		return (Person)super.$data();
	}

	@Override
	public final String firstName()
	{
		return this.$data().firstName();
	}

	@Override
	public final String lastName()
	{
		return this.$data().lastName();
	}	
}
