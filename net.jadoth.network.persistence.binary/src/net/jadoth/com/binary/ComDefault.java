package net.jadoth.com.binary;

import java.nio.ByteBuffer;

import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.types.BinaryPersistence;

public class ComDefault
{
	// (12.11.2018 TM)FIXME: JET-43: move that stuff or rename the class
	
	public static int networkChunkHeaderLength()
	{
		/* currently just a plain simple single length value.
		 * Will be more in the future, though. E.g. endianess, protocol version, etc.
		 */
		return BinaryPersistence.lengthLength();
	}
	
	public static long getNetworkChunkHeaderContentLength(final ByteBuffer directByteBuffer)
	{
		return XVM.get_long(XVM.getDirectByteBufferAddress(directByteBuffer));
	}
	
	public static void setNetworkChunkHeaderContentLength(final ByteBuffer directByteBuffer, final long contentLength)
	{
		XVM.set_long(XVM.getDirectByteBufferAddress(directByteBuffer), contentLength);
	}
	
	
		
}
