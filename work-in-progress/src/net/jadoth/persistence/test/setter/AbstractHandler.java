package net.jadoth.persistence.test.setter;

public abstract class AbstractHandler
{
	private final ValueSetter_long valueSetter_long = Setters.getValueSetter_long();
	
	
	protected AbstractHandler()
	{
		super();
	}
	
	protected final void set_long(final long address, final long value)
	{
		this.valueSetter_long.set_long(address, value);
	}
	
}
