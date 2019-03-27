package one.microstream.test.legacy;

public class Person
{
	// old
	int    kundennummer; // -> pin
	String firstname   ; // -> firstName
	String surname     ; // -> lastName
	String comment     ; // discarded, NOT new commerceId
	float newStuff;
	
	// new
//	Integer pin       ; // kundennummer ->
//	String  firstName ; // firstname    ->
//	String  lastName  ; // surname      ->
//	String  commerceId; // new, NOT old comment
//	Address address   ; // new

}

class Address
{
	// dummy
}
