package one.microstream.memory;

import java.nio.ByteBuffer;

/**
 * Similar to {@link DirectBufferDeallocator} but to obtain the DirectBuffer's address value.
 * 
 * 
 */
public interface DirectBufferAddressGetter
{
	public long getDirectBufferAddress(ByteBuffer directBuffer);
		
	
	
//	public final class Java8Makeshift implements DirectBufferAddressGetter
//	{
//		public Java8Makeshift()
//		{
//			super();
//		}
//
//		@Override
//		public long getDirectBufferAddress(final ByteBuffer directBuffer)
//		{
//			return ((sun.nio.ch.DirectBuffer)directBuffer).address();
//		}
//
//	}
	
}
