package net.jadoth.test.reflection;

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
		System.out.println(determineMostSpecificCommonClass(objects));
	}
	
	@SuppressWarnings("null")
	public static Class<?> determineMostSpecificCommonClass(final Object[] objects)
	{
		/* lowestCommonClass can only stay null if the whole array consists only of nulls or is empty.
		 * In which case it is correct. This cant't cause an NPE later on because this special case
		 * is tied to i reaching the array bounds, in which case the second loop is a no-op.
		 *
		 */
		Class<?> lowestCommonClass = null;
		int i = -1;

		// leading nulls special case handling loop
		while(++i < objects.length)
		{
			if(objects[i] != null)
			{
				lowestCommonClass = objects[i].getClass();
				break;
			}
		}

		// main logic loop
		while(++i < objects.length)
		{
			if(objects[i] == null)
			{
				continue;
			}
			// will never throw a NPE here, despite IDE warning (IDE can't figure out the array bounds dependancy)
			while(!lowestCommonClass.isInstance(objects[i]))
			{
				// automatically aborts at Object.class
				lowestCommonClass = lowestCommonClass.getSuperclass();
			}
		}
		return lowestCommonClass;
	}

}
