package one.microstream.memory;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class MainTestUnsafeAccess
{
	// instance state to test Unsafe#allocateInstance
	
	private final int neverZero;
	
	public MainTestUnsafeAccess()
	{
		super();
		this.neverZero = 5; // or is it?
	}
	
	
	
	// main method with two basic sun.misc.Unsafe tests

	public static void main(final String[] args) throws Throwable
	{
		final Unsafe unsafe = getUnsafe();
		
		System.out.println(
			"Address size in this Java process is "
			+ unsafe.addressSize() + " bytes."
		);
		
		System.out.println(
			"\"neverZero\" value of a blank instance of \"" + MainTestUnsafeAccess.class + "\": "
			+ ((MainTestUnsafeAccess)unsafe.allocateInstance(MainTestUnsafeAccess.class)).neverZero
		);
	}
	
	
	
	// sneaky hacky way to get access to sun.misc.Unsafe .

	public static final Unsafe getUnsafe()
	{
		if(MainTestUnsafeAccess.class.getClassLoader() == null)
		{
			return Unsafe.getUnsafe(); // Not on bootclasspath
		}
		try
		{
			final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe)theUnsafe.get(null); // static field, no argument needed, may be null (see #get JavaDoc)
		}
		catch(final Exception e)
		{
			throw new Error("Could not obtain access to \"theUnsafe\" field.", e);
		}
	}

}
