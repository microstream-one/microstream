package one.microstream.android;

import java.nio.ByteBuffer;

import one.microstream.memory.DirectBufferDeallocator;
import one.microstream.memory.XMemory;
import one.microstream.memory.android.AndroidInternals;

public class MainTestAndroidInternals
{
	
	public static void main(final String[] args)
	{
		final ByteBuffer bb = XMemory.allocateDirectNativeDefault();
		
		final DirectBufferDeallocator dbd = AndroidInternals.DirectBufferDeallocator();
		dbd.deallocateDirectBuffer(bb);
	}
	
}
