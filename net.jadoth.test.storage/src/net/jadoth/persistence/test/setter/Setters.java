package net.jadoth.persistence.test.setter;

import net.jadoth.low.XMemory;

public class Setters
{
	private static final ValueSetter_long SET_LONG_1 = new ValueSetter_long()
	{
		@Override
		public void set_long(final long address, final long value)
		{
			XMemory.set_long(address, value);
		}
	};
	
	// just a second variant to prevent JVM over-optimizing the test.
	private static final ValueSetter_long SET_LONG_2 = new ValueSetter_long()
	{
		@Override
		public void set_long(final long address, final long value)
		{
			XMemory.set_long(address, value + 1);
		}
	};
	
	
	public static final ValueSetter_long getValueSetter_long()
	{
		return System.currentTimeMillis() > 0
			? SET_LONG_1
			: SET_LONG_2
		;
	}
	
	private static Handler handler;
	
	public static synchronized Handler getHandler()
	{
		return Setters.handler;
	}
	
	public static synchronized void setHandler(final Handler handler)
	{
		Setters.handler = handler;
	}
}