package one.microstream.memory;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import one.microstream.chars.XChars;

public class Maintest
{
	public static void main(final String[] args) throws Exception
	{
        final ByteBuffer dbb = ByteBuffer.allocateDirect(1000);
        final Class<?> classDirectBuffer = Class.forName("sun.nio.ch.DirectBuffer");
        final Method methodAddress = classDirectBuffer.getDeclaredMethod("address");
        methodAddress.setAccessible(true);
        final Long address = (Long)methodAddress.invoke(dbb);
        System.out.println(XChars.systemString(dbb) + ".address() == " + address);

	}
}
