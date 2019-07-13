package some.app.entities._generated._Person;

import one.microstream.X;
import one.microstream.equality.Equalator;
import some.app.entities.Person;

public interface PersonEqualator extends Equalator<Person>
{
	public static PersonEqualator INSTANCE = (p1, p2) -> {
		
		return X.equal(p1.firstName(), p2.firstName()) 
		    && X.equal(p1.lastName (), p2.lastName ());
	};
}
