package one.microstream.memory.sun;

import java.nio.ByteBuffer;

import one.microstream.memory.DirectBufferDeallocator;

final class JdkDirectBufferDeallocator implements DirectBufferDeallocator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final JdkDirectBufferDeallocator New()
	{
		return new JdkDirectBufferDeallocator();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	JdkDirectBufferDeallocator()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		JdkInternals.internalDeallocateDirectBuffer(directBuffer);
	}
	
}