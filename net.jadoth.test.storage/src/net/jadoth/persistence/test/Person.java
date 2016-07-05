package net.jadoth.persistence.test;

public class Person
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	
	public static Person New(final int id)
	{
		return new Person(id, "P_"+id, Person.class.getSimpleName()+"_"+id, null);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	int id;
	String firstName, lastName;
	Person friend;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	Person(final int id, final String firstName, final String lastName, final Person friend)
	{
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.friend = friend;
	}
	
}
