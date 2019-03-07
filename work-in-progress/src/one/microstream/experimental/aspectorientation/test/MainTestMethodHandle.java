package one.microstream.experimental.aspectorientation.test;


/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTestMethodHandle {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		MyClass mc = new MyClass();
		System.out.println(mc.doStuff(1, 3.14f, "Hallo"));
		System.out.println(mc.doStuff(2, 3.14f, null));
	}

}
