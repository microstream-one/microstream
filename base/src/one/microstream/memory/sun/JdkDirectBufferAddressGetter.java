package one.microstream.memory.sun;

import java.nio.ByteBuffer;

import one.microstream.memory.DirectBufferAddressGetter;

final class JdkDirectBufferAddressGetter implements DirectBufferAddressGetter
{

	JdkDirectBufferAddressGetter()
	{
		super();
	}

	@Override
	public final long getDirectBufferAddress(final ByteBuffer directBuffer)
	{
		return JdkInternals.internalGetDirectByteBufferAddress(directBuffer);
	}
	
}