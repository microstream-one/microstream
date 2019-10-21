package one.microstream.persistence.test;

import one.microstream.chars.XChars;
import one.microstream.memory.sun.MemoryAccessorSun;
import sun.misc.Unsafe;

public class MainReadObjectHeader
{
	static final long OFFSET_HEADER = 0L;
	static final long OFFSET_CLASS  = 8L;

	static final Object o = new Object();
	static final Unsafe vm = (Unsafe)MemoryAccessorSun.getSystemInstance();

	public static void main(final String[] args) throws Throwable
	{
		class c {
//			final Class<?> ref = Object.class;
		}

		System.out.println("pointer to Object.class:");
		printPadded(Long.toBinaryString(vm.getLong(new c(), vm.objectFieldOffset(c.class.getDeclaredField("ref")))), "Object.class");

		System.out.println("initial:");
		printObjectHeader(o);

		System.out.println("with hashcode:");
		printIdentityHashCode(o);
		printObjectHeader(o);

		System.out.println("with lock:");
		synchronized(o) {
			printObjectHeader(o);
		}

		System.out.println("after lock:");
		printObjectHeader(o);

		System.out.println("some other Object instances:");
		printObjectHeader(new Object());
		printObjectHeader(new Object());

		System.out.println("pointer to Object.class again:");
		printPadded(Long.toBinaryString(vm.getLong(new c(), vm.objectFieldOffset(c.class.getDeclaredField("ref")))), "Object.class");
	}



	static final String obviousIdentityHashCodeOffset = "________";
	static void printIdentityHashCode(final Object o)
	{
		printPadded(Integer.toBinaryString(o.hashCode())+obviousIdentityHashCodeOffset, "hashcode");
	}
	static void printObjectHeader(final Object o)
	{
		printPadded(Long.toBinaryString(vm.getLong(o, 0L)), "first");
		printPadded(Long.toBinaryString(vm.getLong(o, 8L)), "second");
	}
	static void printPadded(final String bits, final String label)
	{
		System.out.println(XChars.padLeft0(bits, 64)+" "+label);
	}
}
