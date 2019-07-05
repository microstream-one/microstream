package one.microstream.memory;

import java.nio.ByteBuffer;

import one.microstream.chars.XChars;

public class MainTestPlatformInternals
{
	public static void main(final String[] args) throws Exception
	{
		System.err.println("PlatformInternals status:");
        System.err.println(PlatformInternals.getResolvingStatus());
		System.err.println("-------------------------");
        
		System.err.println("PlatformInternals init warnings:");
		PlatformInternals.printInitializationWarnings(System.err);
		System.err.println("--------------------------------");
        
        final ByteBuffer dbb = ByteBuffer.allocateDirect(1000);

        System.out.println(XChars.systemString(dbb) + "#address = " + PlatformInternals.getDirectBufferAddress(dbb));
        
        System.out.println("Deallocating " + XChars.systemString(dbb));
        PlatformInternals.deallocateDirectBuffer(dbb);
        System.out.println(XChars.systemString(dbb) + "#address = " + PlatformInternals.getDirectBufferAddress(dbb));

	}
}
