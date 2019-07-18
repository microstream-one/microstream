package some.app.entities;

import one.microstream.entity.Entity;
import some.app.entities._generated._Person.PersonCreator;


/**
 * This is all that is required to define an entity.
 * Everything else is generated or abstract framework logic.
 * 
 * @author TM
 */
public interface Person extends Entity
{
	public String firstName();
	
	public String lastName();
		
	
	
	
	// -------------------------------------------------------------------//
	
	// (18.07.2019 TM)NOTE: example for an optional convenience setter
	public default Person setFirstName(final String firstName)
	{
		Entity.updateData(this, New(this).lastName(firstName).createData());
		
		return this;
	}
	
	// (18.07.2019 TM)NOTE: example for an optional pseudo-constructor
	public static PersonCreator New(final Person other)
	{
		return PersonCreator.New(other);
	}
	
}
