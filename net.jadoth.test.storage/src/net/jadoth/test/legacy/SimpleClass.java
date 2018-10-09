package net.jadoth.test.legacy;

import net.jadoth.chars.XChars;

public class SimpleClass
{
	int   first ;
	float second;

	public SimpleClass(final int first, final float second)
	{
		super();
		this.first  = first ;
		this.second = second;
	}
	
	@Override
	public String toString()
	{
		return XChars.systemString(this) + ": first = " + this.first + ", second = " + this.second;
	}
	
	
}
