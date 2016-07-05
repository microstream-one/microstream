package net.jadoth.test.reflection.label;

import java.util.Arrays;

import net.jadoth.reflect.JadothReflect;
import net.jadoth.reflect.Label;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestLabel
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		System.out.println(Arrays.toString(
			JadothReflect.getMembersByLabel(TestClass.A, TestClass.class.getDeclaredFields())
		));
		System.out.println(JadothReflect.getMemberByLabel(TestClass.A,  TestClass.class.getDeclaredFields()));
		System.out.println(JadothReflect.getMemberByLabel(TestClass.A1, TestClass.class.getDeclaredFields()));
		System.out.println(JadothReflect.getMemberByLabel(TestClass.A2, TestClass.class.getDeclaredFields()));
	}





	public static class TestClass
	{
		public static final String A = "A";
		public static final String A1 = "A1";
		public static final String A2 = "A2";

		@Label(A)
		private String privateString;

		@Label({A, A1})
		protected String protectedString;

		@Label({A, A2})
		public String publicString;
	}

}
