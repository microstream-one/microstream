package one.microstream.test.corp.main;

import java.lang.reflect.Field;
import java.util.Arrays;

public class MainTestEnumReflection
{
	public static void main(final String[] args)
	{
		printEnumConstants(SimpleEnum.class);
		printEnumConstants(CrazyEnum.class);
	}
	
	
	static void printEnumConstants(final Class<?> c)
	{
		// this includes enum constant fields on the Oracle JVM but not on android
		final Field[] fields = c.getDeclaredFields();
		
		final Object[] enumConstants = c.getEnumConstants();
		
		System.out.println(c);
		System.out.println("Fields   : " + Arrays.toString(fields));
		System.out.println("Constants: " + Arrays.toString(enumConstants));
		System.out.println();
	}
	
}
