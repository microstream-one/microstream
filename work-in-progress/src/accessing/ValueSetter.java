package accessing;

public interface ValueSetter extends ValueProxy
{
	public void set_int(Object target, int value);
		
	public void set_long(Object target, long value);
		
	public void set(Object target, Object reference);
		
}
