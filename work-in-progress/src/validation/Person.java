package validation;

import one.microstream.X;

public class Person
{
	@Validated(Check.class) // optional
	String firstname;

	@Validated // optional
	String lastname;
	
	
	
	public void setFirstname(final String firstname)
	{
		// setter uses centralized checking logic for validation
		this.firstname = X.validate(firstname, Check::firstname);
	}
	
	public void setLastname(final String lastname)
	{
		// setter uses centralized checking logic for validation
		this.lastname = X.validate(lastname, Check::lastname);
	}
	
	
	// centralizing validation logic for both class itself and external reconstruction solutions
	@Validations // optional
	static class Check
	{
		static boolean firstname(final String value)
		{
			// arbitrary logic here. Trivial null-check for simplicity's sake of the example
			return value != null;
		}
		
		static boolean lastname(final String value)
		{
			// arbitrary logic here. Trivial null-check for simplicity's sake of the example
			return value != null;
		}
	}
	
}
