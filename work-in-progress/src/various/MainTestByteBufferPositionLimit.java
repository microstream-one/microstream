package various;

import java.nio.ByteBuffer;

import one.microstream.memory.XMemory;

public class MainTestByteBufferPositionLimit
{
	
	public static void main(final String[] args)
	{
		final ByteBuffer bb = ByteBuffer.allocate(1000);
		
		bb.position(37).limit(400);
		printPositionLimit("Initial :", bb);
		
		final long poslim = XMemory.getPositionLimit(bb);
		
		bb.position(10).limit(100);
		printPositionLimit("Modified:", bb);
		
		XMemory.setPositionLimit(bb, poslim);
		printPositionLimit("Reset   :", bb);
	}
	
	static void printPositionLimit(final String label, final ByteBuffer bb)
	{
		System.out.println(label + " Position = " + bb.position() + ", Limit = " + bb.limit());
	}
	
}
