package wrapping;

public interface MyType
{
	public void methodA(int value);
	
	public String methodB();
	
	public String methodC(long value, String stuff);
	
	// ...
	
	public int methodZ();
	
	
	
	public static MyType New()
	{
		throw new UnsupportedOperationException("TODO");
	}
	

	
}
