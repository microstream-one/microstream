package one.microstream.memory;

import java.nio.ByteBuffer;

/**
 * The combination of:
 * <ul>
 * <li>Missing proper DirectByteBuffer public API interface type,</li>
 * <li>Missing deallocate() possibility for direct ByteBuffers,</li>
 * <li>sun.misc.Cleaner package change with Java 9,</li>
 * <li>modules with Java 9 AND java.base not exporting its stuff so that halfway proper workarounds
 *     for the JDK design errors could be created using reflection without forcing special vm arguments
 *     on everyone wanting to use the library</li>
 * </ul>
 * leave only one sanity-preserving way to handle explicit deallocation of memory directly allocated by
 * {@link ByteBuffer#allocateDirect(int)}:
 * <p>
 * ---
 * <p>
 * This type handles explicit deallocation of memory allocated by direct ByteBuffers created via
 * {@link ByteBuffer#allocateDirect(int)}.<br>
 *  The default implementation is a no-op to maintain compatibility accross the Java 8-9 transition.<p>
 * The required approach for a functional implementation would be:<p>
 * For JDK 8:
 * <pre>{@code
 * // compensate missing proper typing in JDK
 *if(!(directByteBuffer instanceof sun.nio.ch.DirectBuffer))
 *{
 *	return; // or throw exception
 *}
 *
 *sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer)directByteBuffer).cleaner();
 *
 // cleaner can be null
 *if(cleaner != null)
 *{
 *	cleaner.clean();
 *}
 *                                                                     }</pre>
 * <p>
 * For JDK 9(+)
 * <pre>{@code
 * // compensate missing proper typing in JDK
 *if(!(directByteBuffer instanceof sun.nio.ch.DirectBuffer))
 *{
 *	return; // or throw exception
 *}
 *
 *jdk.internal.ref.Cleaner cleaner = ((sun.nio.ch.DirectBuffer)directByteBuffer).cleaner();
 *
 // cleaner can be null
 *if(cleaner != null)
 *{
 *	cleaner.clean();
 *}
 *                                                                     }</pre>
 * 
 * Also see:
 * http://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us
 * 
 * @author TM
 */
public interface DirectBufferDeallocator
{
	public void deallocateDirectBuffer(ByteBuffer directBuffer);
	
	
	
	public static DirectBufferDeallocator NoOp()
	{
		return new DirectBufferDeallocator.NoOp();
	}
	
	public final class NoOp implements DirectBufferDeallocator
	{
		NoOp()
		{
			// constructors are an implementation detail that are to be hidden just like fields.
			super();
		}
		
		@Override
		public void deallocateDirectBuffer(final ByteBuffer directBuffer)
		{
			// no-op by default because of too many design errors in JDK to provide a sane solution out of the box.
		}
		
	}
	
//	public final class Java8Makeshift implements DirectByteBufferDeallocator
//	{
//		public Java8Makeshift()
//		{
//			super();
//		}
//
//		@Override
//		public void deallocateDirectByteBuffer(final ByteBuffer directBuffer)
//		{
//			// compensate missing proper typing in JDK
//			if(!(directBuffer instanceof sun.nio.ch.DirectBuffer))
//			{
//				return; // or throw exception
//			}
//
//			final sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer)directBuffer).cleaner();
//
//			// cleaner can be null
//			if(cleaner != null)
//			{
//				cleaner.clean();
//			}
//		}
//
//	}
	
//	public final class Java9Makeshift implements DirectByteBufferDeallocator
//	{
//		public Java9Makeshift()
//		{
//			super();
//		}
//
//		@Override
//		public void deallocateDirectByteBuffer(final ByteBuffer directBuffer)
//		{
//			// compensate missing proper typing in JDK
//			if(!(directBuffer instanceof sun.nio.ch.DirectBuffer))
//			{
//				return; // or throw exception
//			}
//
//			final jdk.internal.ref.Cleaner cleaner = ((sun.nio.ch.DirectBuffer)directBuffer).cleaner();
//
//			// cleaner can be null
//			if(cleaner != null)
//			{
//				cleaner.clean();
//			}
//		}
//
//	}
	
}
