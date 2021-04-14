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
	public boolean deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		return JdkInternals.internalDeallocateDirectBuffer(directBuffer);
	}
	
}