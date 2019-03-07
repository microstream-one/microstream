package some.app.entities._generated._Person;

import net.jadoth.entity.EntityLayerIdentity;
import some.app.entities.Person;

public final class _Entity extends EntityLayerIdentity<Person> implements Person
{
	_Entity()
	{
		super();
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
