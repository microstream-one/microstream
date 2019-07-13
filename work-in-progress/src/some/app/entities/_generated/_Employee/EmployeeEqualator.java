package some.app.entities._generated._Employee;

import one.microstream.X;
import one.microstream.equality.Equalator;
import some.app.entities.Employee;

public interface EmployeeEqualator extends Equalator<Employee>
{
	public static EmployeeEqualator INSTANCE = (e1, e2) -> {
		
		return X.equal(e1.firstName(), e2.firstName()) 
		    && X.equal(e1.lastName (), e2.lastName ()) 
		    && X.equal(e1.employer (), e2.employer ());
	};
}
