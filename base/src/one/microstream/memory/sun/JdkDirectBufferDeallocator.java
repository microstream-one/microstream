package one.microstream.memory.sun;

import java.nio.ByteBuffer;

import one.microstream.memory.DirectBufferDeallocator;

final class JdkDirectBufferDeallocator implements DirectBufferDeallocator
{
	JdkDirectBufferDeallocator()
	{
		super();
	}
	
	@Override
	public void deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		JdkInternals.internalDeallocateDirectBuffer(directBuffer);
	}
	
}