package net.jadoth.test.reflection;

import net.jadoth.reflect.JadothReflect;


public class MainTestMostSpecificCommonClass
{
	public static void main(final String[] args)
	{
		testLowestCommonClass();
		testLowestCommonClass(null, null);
		testLowestCommonClass(null, null, 1);
		testLowestCommonClass(1, 2, 3);
		testLowestCommonClass(1, 2L, 3.14);
		testLowestCommonClass(1, 2L, 3.14, "a");
		testLowestCommonClass(1, 2L, 3.14, "a", new Object());
		testLowestCommonClass(1, null, 2L, 3.14, null, "a", new Object());
		testLowestCommonClass(null, 1, null, 2L, 3.14, null, "a", new Object(), null);
		testLowestCommonClass(null, 1, 2L, null, null);
	}

	static void testLowestCommonClass(final Object... objects)
	{
		System.out.println(JadothReflect.determineMostSpecificCommonClass(objects));
	}

}
