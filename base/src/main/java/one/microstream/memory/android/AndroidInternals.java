package one.microstream.memory.android;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
	 * 
	 * (22.11.2019 TM)NOTE:
	 * This only works on certain Android versions. In others, a "Cleaner" instance similar to the JDK
	 * implementation is used. I currently don't even know which ones are newer and which are older.
	 * A proper research and case distinction implementation would be required here.
	 * However, since the whole reason for the explicit deallocation logic is more based on a server use case
	 * and might not ever happen on Android at all AND given that it works (should work?) at least on some android
	 * versions and given the very low priority that results from all that, this distinction is currently not made.
	 * 
	 * See
	 * http://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us
	 * In short:
	 * A server application with very low heap consumption but very high off-heap consumption could run for hours without
	 * a JVM GC ever being called. So the DBBs would never get collected, their allocated off-heap memory never freed.
	 * The JDK simply forgot the most basic rule of memory handling: if you can allocate memory explicitly,
	 * you MUST have a way to deallocate it explicitly, as well.
	 * But for an Android app, such a scenario is very unlikely, so it is hardly important.
	 * 
	 * The lookup and usage of the #free method is done in such a fashion that they never throw an exception.
	 * So if the #free method is not present on a Android version, the deallocation method simply does nothing.
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
	public static final boolean internalDeallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		// better check again in here, in case this method ever gets called from another context, e.g. reflective.
		if(directBuffer == null)
		{
			return false;
		}
		
		if(METHOD_DirectByteBuffer_free == null)
		{
			// something went wrong and the method could not have been resolved, so just abort.
			return false;
		}
		
		// important to prevent invoking the method on an instance of the wrong class.
		XTypes.guaranteeDirectByteBuffer(directBuffer);
		
		try
		{
			METHOD_DirectByteBuffer_free.invoke(directBuffer);
		}
		catch(final Exception e)
		{
			// something went wrong during the method invocation. Again do nothing.
			return false;
		}
		
		return true;
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
