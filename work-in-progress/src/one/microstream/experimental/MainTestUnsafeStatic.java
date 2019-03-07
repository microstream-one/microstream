package one.microstream.experimental;

import java.lang.reflect.Field;

import one.microstream.memory.XMemory;
import sun.misc.Unsafe;

public class MainTestUnsafeStatic
{
	static final Unsafe vm = (Unsafe)XMemory.getSystemInstance();

	public static void main(final String[] args) throws Throwable
	{
		final Field fieldValue = Test.class.getDeclaredField("i");
		final Object fieldBase = vm.staticFieldBase(fieldValue);
		System.out.println(fieldBase);
	}
}



final class Test
{
	static int value;
	int i;
}
