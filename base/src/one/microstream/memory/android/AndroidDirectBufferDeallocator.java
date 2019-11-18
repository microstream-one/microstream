package one.microstream.memory.android;

import java.nio.ByteBuffer;

import one.microstream.memory.DirectBufferDeallocator;


public final class AndroidDirectBufferDeallocator implements DirectBufferDeallocator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final AndroidDirectBufferDeallocator New()
	{
		return new AndroidDirectBufferDeallocator();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AndroidDirectBufferDeallocator()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		AndroidInternals.internalDeallocateDirectBuffer(directBuffer);
	}
	
}
