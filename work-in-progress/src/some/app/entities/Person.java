package some.app.entities;

import net.jadoth.entity.Entity;


/**
 * This is all that is required to define an entity.
 * Everything else is generated or abstract framework logic.
 * 
 * @author TM
 */
public interface Person extends Entity<Person>
{
	String firstName();
	
	String lastName();
}
