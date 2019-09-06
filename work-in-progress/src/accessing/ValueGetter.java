package accessing;

public interface ValueGetter extends ValueProxy
{
	public int get_int(Object source);
	
	public Object get(Object source);
	
	public long as_long(Object source);
	
	public Object as_Object(Object source);
	
	
	// direct copying logic: does no transformation, target setter must match perfectly
	
	public void copyTo(Object source, ValueSetter setter, Object target);
	
	
	// copying logic with transformation, e.g. int to double or char to Character.
	
	public void transformTo(Object source, ValueSetter setter, Object target);
	
}
