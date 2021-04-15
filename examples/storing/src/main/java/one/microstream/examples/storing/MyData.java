
package one.microstream.examples.storing;

public class MyData
{
	private String name;
	private int    intValue;
	
	public MyData(final String content)
	{
		super();
		this.name = content;
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
