package one.microstream.entity;

import one.microstream.entity.Entity;

public interface Customer extends Entity
{
	public String firstName();
	
	public String lastName();
	
	public Address address();
}
