package net.jadoth.persistence.test.setter;

public class Handler extends AbstractHandler
{

	public Handler()
	{
		super();
	}
	
	
	public final void setValues(final long address, final long[] values)
	{
		long a = address;
		for(final long value : values)
		{
			// (13.12.2018 TM)XXX: switch between function reference and direct call
//			XMemory.set_long(a, value);
			this.set_long(a, value);
			a += 8;
		}
	}
	
}
