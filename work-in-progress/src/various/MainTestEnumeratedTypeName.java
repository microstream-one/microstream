package various;


import java.util.Comparator;

import one.microstream.reflect.XReflect;

public class MainTestEnumeratedTypeName
{
	static Object nested;
	
	public static void main(final String[] args)
	{
		final Comparator<Object> c = new Comparator<Object>()
		{
			final class Nested
			{
				// empty
			}
			
			{
				MainTestEnumeratedTypeName.nested = new Nested();
			}
			
			@Override
			public int compare(final Object o1, final Object o2)
			{
				throw new one.microstream.meta.NotImplementedYetError(); // FIXME Comparator#compare()
			}
		};
		
		test(MainTestEnumeratedTypeName.class);
		test(OuterNested.class);
		test(c.getClass());
		test(nested.getClass());
		test(OuterNested.c.getClass());
	}
	
	
	
	static void test(final Class<?> type)
	{
		System.out.println(type.getName() + " -> " + XReflect.hasEnumeratedTypeName(type));
	}
	
	
	static final class OuterNested
	{
		static final Comparator<Object> c = new Comparator<Object>()
		{
			@Override
			public int compare(final Object o1, final Object o2)
			{
				throw new one.microstream.meta.NotImplementedYetError(); // FIXME Comparator#compare()
			}
		};
	}
}
