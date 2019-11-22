package one.microstream.typing;

public class MainTest_boolean
{
	
	public static void main(final String[] args)
	{
		printTest_boolean(true);
		printTest_boolean(false);
	}
	
	public static void printTest_boolean(final boolean value)
	{
		System.out.println("original boolean value: " + value);
		final byte byteValue = XTypes.to_byte(value);
		System.out.println("converted byte value  : " + byteValue);
		final boolean restoredBoolean =  XTypes.to_boolean(byteValue);
		System.out.println("restored boolean value: " + restoredBoolean);
	}
	
}
