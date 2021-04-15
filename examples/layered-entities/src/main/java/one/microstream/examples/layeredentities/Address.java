package one.microstream.examples.layeredentities;

import one.microstream.entity.Entity;

public interface Address extends Entity
{
	public String street();
	
	public String city();
	
	public String zipCode();
}
