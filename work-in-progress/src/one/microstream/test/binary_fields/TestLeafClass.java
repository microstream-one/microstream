package one.microstream.test.binary_fields;

public class TestLeafClass extends TestBaseClass
{
	int    leafValue_int  ;
	float  leafValue_float;
	String leafReference  ;
	
	
	public int leafValue_int()
	{
		return this.leafValue_int;
	}
	
	public void setLeafValue_int(final int leafValue_int)
	{
		this.leafValue_int = leafValue_int;
	}
}
