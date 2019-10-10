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
	protected Person $entityData()
	{
		return (Person)super.$entityData();
	}

	@Override
	public final String firstName()
	{
		return this.$entityData().firstName();
	}

	@Override
	public final String lastName()
	{
		return this.$entityData().lastName();
	}
}