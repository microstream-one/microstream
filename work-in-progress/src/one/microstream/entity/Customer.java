package one.microstream.entity;

public interface Customer extends Entity
{
	public String firstName();
	
	public String lastName();
	
	public Address address();
}
