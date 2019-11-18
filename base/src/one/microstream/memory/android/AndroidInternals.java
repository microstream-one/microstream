package one.microstream.memory.android;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import one.microstream.functional.DefaultInstantiator;
import one.microstream.memory.DirectBufferDeallocator;
import one.microstream.typing.XTypes;
import sun.misc.Unsafe;


public class AndroidInternals
{
	///////////////////////////////////////////////////////////////////////////
	// system access //
	//////////////////
	
	/*
	 * Potentially used by other classes in other projects but same package, so do not change to private.
	 * Also note: this must be the very first field to be initialized, otherwise other field
	 * initializations using it will fail with an NPE.
	 */
	static final Unsafe VM = getMemoryAccess();
	
	/*
	 * If magic values should be represented by constants and constants should be encapsulated by methods
	 * like instance fields should, then why use the code and memory detour of constants in the first place?
	 * Direct "Constant Methods" are the logical conclusion and they get jitted away, anyway.
	 */
	static final String fieldNameUnsafe()
	{
		return "theUnsafe";
	}
	
	/* (18.11.2019 TM)NOTE:
	 * Basically the same as JdkInternals#getMemoryAccess, but it is a good idea in general to strictly separate
	 * the two platforms.
	 */
	public static final Unsafe getMemoryAccess()
	{
		if(AndroidInternals.class.getClassLoader() == null)
		{
			return Unsafe.getUnsafe(); // Not on bootclasspath
		}
		try
		{
			final Field theUnsafe = Unsafe.class.getDeclaredField(fieldNameUnsafe());
			theUnsafe.setAccessible(true);
			return (Unsafe)theUnsafe.get(null); // static field, no argument needed, may be null (see #get JavaDoc)
		}
		catch(final Exception e)
		{
			throw new Error("Could not obtain access to \"" + fieldNameUnsafe() + "\"", e);
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initialization //
	///////////////////

	/*
	 * See android libcore class DirectByteBuffer, method public final void free().
	 * https://android.googlesource.com/platform/libcore/+/f33eae7e84eb6d3b0f4e86b59605bb3de73009f3/luni/src/main/java/java/nio/DirectByteBuffer.java#280
	 */
	static final String METHOD_NAME_DirectByteBuffer_free = "free"; // just switch to "address" to test with JDK.
	
	static final Method METHOD_DirectByteBuffer_free = tryGetMethod(
		XTypes.directByteBufferClass(),
		METHOD_NAME_DirectByteBuffer_free
	);
	
	static final Method tryGetMethod(final Class<?> type, final String declaredMethodName)
	{
		Method method = null;
		try
		{
			method = type.getDeclaredMethod(declaredMethodName);
			method.setAccessible(true);
		}
		catch(final Exception e)
		{
			// method remains null
		}
		
		return method;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@SuppressWarnings("unchecked") // cast is safe as the passed type IS the type T.
	public static final <T> T instantiateBlank(final Class<T> c)
	{
		/* (18.11.2019 TM)NOTE:
		 * See android libcore class Unsafe, method public final void allocateInstance().
		 * https://android.googlesource.com/platform/libcore/+/9edf43dfcc35c761d97eb9156ac4254152ddbc55/libdvm/src/main/java/sun/misc/Unsafe.java#349
		 * The only difference is that android's variant does not throw a checked InstantiationException.
		 * It declares no thrown exception at all.
		 * So to keep compatibility with a JDK-oriented compiler, just Exception is caught.
		 * (which is a good idea in general).
		 */
		try
		{
			return (T)VM.allocateInstance(c);
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	// "internal" prefixed method that is public, to indicate that it uses VM-internal details.
	public static final void internalDeallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		// better check again in here, in case this method ever gets called from another context, e.g. reflective.
		if(directBuffer == null)
		{
			return;
		}
		
		if(METHOD_DirectByteBuffer_free == null)
		{
			// something went wrong and the method could not have been resolved, so just abort.
			return;
		}
		
		// important to prevent invoking the method on an instance of the wrong class.
		XTypes.guaranteeDirectByteBuffer(directBuffer);
		
		try
		{
			METHOD_DirectByteBuffer_free.invoke(directBuffer);
		}
		catch(final Exception e)
		{
			// something went wrong during the method invokation. Again do nothing.
			return;
		}
	}
	
	public static final DefaultInstantiator InstantiatorBlank()
	{
		return AndroidInstantiatorBlank.New();
	}
	
	public static final DirectBufferDeallocator DirectBufferDeallocator()
	{
		return AndroidDirectBufferDeallocator.New();
	}
	
}
