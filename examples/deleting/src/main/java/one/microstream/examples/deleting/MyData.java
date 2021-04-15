
package one.microstream.examples.deleting;

public class MyData
{
	private String name;
	private int    intValue;
	
	public MyData(final String name, final int value)
	{
		super();
		this.name     = name;
		this.intValue = value;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(final String name)
	{
		this.name = name;
	}
	
	public int getIntegerValue()
	{
		return this.intValue;
	}
	
	public void setIntValue(final int integerValue)
	{
		this.intValue = integerValue;
	}
	
	@Override
	public String toString()
	{
		return this.name + " value: " + this.intValue;
	}
	
}
