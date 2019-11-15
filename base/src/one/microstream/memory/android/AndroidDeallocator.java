package one.microstream.memory.android;

import java.nio.ByteBuffer;

import one.microstream.memory.DirectBufferDeallocator;


public final class AndroidDeallocator implements DirectBufferDeallocator
{
	@Override
	public final void deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		/*(15.11.2019 TM)FIXME: AndroidDeallocator
		 * This cast probably won't work since DirectByteBuffer is not public. Maybe reflection has to be used.
		 * For now, the code is simply deactivated since perfect deallocation is not crucial for an initial test.
		 */
//		((DirectByteBuffer)directBuffer).free();
	}
	
}