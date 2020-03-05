package one.microstream.reflect;

public class MainTestReflectiveCopier
{
	
	public static void main(final String[] args)
	{
		final TestClass source = new TestClass(
			(byte)127, true, (short)2_000, 'a', 40_000, 3.14f, 800_000L, 9.81, "Hello World", "S2", "trStr"
		);
		final TestClass target = new TestClass();

		System.out.println("Source: " + source);
		System.out.println("Target before copy: " + target);

//		final ReflectiveCopier<TestClass> copier = ReflectiveCopier.New(TestClass.class);
//		copier.copy(source, target);
		
		final ReflectiveCopier<TestClass> copier = ReflectiveCopier.New(source);
		copier.copyTo(target);
		
		System.out.println("Target after copy: " + source);
	}
	
	
	static class TestClass
	{
		byte    y;
		boolean b;
		short   s;
		char    c;
		int     i;
		float   f;
		long    l;
		double  d;
		
		String string1;
		String string2;
		
		transient String transientString;

		TestClass()
		{
			super();
		}
		
		TestClass(final byte y, final boolean b, final short s, final char c, final int i, final float f, final long l, final double d, final String string1, final String string2, final String transientString)
		{
			super();
			this.y = y;
			this.b = b;
			this.s = s;
			this.c = c;
			this.i = i;
			this.f = f;
			this.l = l;
			this.d = d;
			this.string1 = string1;
			this.string2 = string2;
			this.transientString = transientString;
		}

		@Override
		public String toString()
		{
			return "TestClass [y=" + this.y
				+ ", b=" + this.b
				+ ", s=" + this.s
				+ ", c=" + this.c
				+ ", i=" + this.i
				+ ", f=" + this.f
				+ ", l=" + this.l
				+ ", d=" + this.d
				+ ", string1 = " + this.string1
				+ ", string2 = " + this.string2
				+ ", transientString = " + this.transientString
				+ "]"
			;
		}
		
		
		
		
		
		
	}
	
}
