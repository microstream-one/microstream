package one.microstream.memory;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import one.microstream.X;
import one.microstream.reflect.XReflect;

/**
 * This class provides static utility functionality to access certain required access to internal JDK logic
 * without using JDK-version-specific dependencies. E.g. JDK-internal classes in the "sun.*" package were moved to
 * the "jdk.*" package but access to those is required to compensate for certain shortcomings in the JDK's public
 * API.
 * <p>
 * This class does the magic trick of allowing JDK-specific access without having JDK-specific dependencies in the
 * source code. Congratulations and felicitation always welcomed.
 * <p>
 * In more general terms, this class abstracts platform-version-specific details.
 * 
 * @author TM
 *
 */
public class PlatformInternals
{
	/*
	 * A basic principle of this class is to never throw exceptions if the resolving attempts should fail.
	 * The reason is that doing so would prevent using the library in an absolute fashion, even if the low-level
	 * functionality handled here wouldn't even be used.
	 * Instead, warnings are written to the console (bad per se, but preferable in these cases).
	 * Using parts of the library that require that low-level functionality will fail very fast and thus prevent
	 * any damage / inconsistencies.
	 */
	
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	static final Class<?> CLASS_DirectBuffer = XReflect.tryIterativeResolveType(
		// initial type name
		"sun.nio.ch.DirectBuffer"
		// future changes here ... (maybe other JDKs as well? Android?)
	);
	
	// not needed (yet?)
//	static final Class<?> CLASS_DirectByteBuffer = XReflect.tryIterativeResolveType(
//		// initial type name
//		"java.nio.DirectByteBuffer"
//		// future changes here ... (maybe other JDKs as well? Android?)
//	);
	
	static final Class<?> CLASS_Cleaner = XReflect.tryIterativeResolveType(
		// initial type name
		"sun.misc.Cleaner",
		// Java 9+ type name
		"jdk.internal.ref.Cleaner"
		// future changes here ... (maybe other JDKs as well? Android?)
	);
	
	static final String FIELD_NAME_address  = "address";
	static final String METHOD_NAME_address = "address";
	static final String METHOD_NAME_cleaner = "cleaner";
	static final String METHOD_NAME_clean   = "clean"  ;

	// Note java.nio.Buffer comment: "Used only by direct buffers. Hoisted here for speed in JNI GetDirectBufferAddress"
	static final long   FIELD_OFFSET_Buffer_address = tryGetFieldOffset(Buffer.class, FIELD_NAME_address);
	
	static final Method METHOD_DirectBuffer_address = tryResolveMethod(CLASS_DirectBuffer, METHOD_NAME_address);
	static final Method METHOD_DirectBuffer_cleaner = tryResolveMethod(CLASS_DirectBuffer, METHOD_NAME_cleaner);
	static final Method METHOD_Cleaner_clean        = tryResolveMethod(CLASS_Cleaner, METHOD_NAME_clean);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static fields //
	//////////////////

	private static DirectBufferDeallocator   directBufferDeallocator   = null;
	private static DirectBufferAddressGetter directBufferAddressGetter = null;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initializing methods //
	/////////////////////////
	
	static final Class<?> tryIterativeResolveType(final String... typeNames)
	{
		final Class<?> type = XReflect.tryIterativeResolveType(typeNames);
		if(type != null)
		{
			return type;
		}
		
		System.err.println(
			"Warning. No runtime type could have been found for the given type name list "
			+ Arrays.toString(typeNames)
		);
		
		return null;
	}

	static final long tryGetFieldOffset(final Class<?> type, final String declaredFieldName)
	{
		if(type == null)
		{
			System.err.println(
				"Warning. Cannot resolve declared field \""
				+ declaredFieldName
				+ "\" in an unresolved type."
			);
			return -1;
		}
					
		// minimal algorithm, only for local use
		for(Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
		{
			try
			{
				for(final Field field : c.getDeclaredFields())
				{
					if(field.getName().equals(declaredFieldName))
					{
						return XMemory.objectFieldOffset(field);
					}
				}
			}
			catch(final Exception e)
			{
				// fall through to return
				e.printStackTrace();
			}
		}

		System.err.println(
			"Warning. No declared field with name \""
			+ declaredFieldName
			+ "\" could have been found in the class hierarchy beginning at "
			+ type
		);
		return -1;
	}
	
	static final Method tryResolveMethod(
		final Class<?>    type          ,
		final String      methodName    ,
		final Class<?>... parameterTypes
	)
	{
		if(type == null)
		{
			System.err.println(
				"Warning. Cannot resolve declared method \""
				+ methodName
				+ "\" in an unresolved type."
			);
			return null;
		}
		
		// minimal algorithm, only for local use
		for(Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
		{
			try
			{
				for(final Method method : c.getDeclaredMethods())
				{
					if(method.getName().equals(methodName)
						&& Arrays.equals(method.getParameters(), parameterTypes)
					)
					{
						method.setAccessible(true);
						return method;
					}
				}
			}
			catch(final Exception e)
			{
				// fall through to return
				e.printStackTrace();
			}
		}

		System.err.println(
			"Warning. No declared method with name \""
			+ methodName
			+ "\" could have been found in the class hierarchy beginning at "
			+ type.toString()
		);
		return null;
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// static getters & setters //
	/////////////////////////////
	
	/**
	 * Allows to set the {@link DirectBufferDeallocator} used by
	 * {@link #deallocateDirectBuffer(ByteBuffer)} as an override to the means this class inherently tries to provide.<br>
	 * See {@link DirectBufferDeallocator} for details.
	 * <p>
	 * The passed instance "should" be immutable or better stateless to ensure concurrency-safe usage,
	 * but ultimately, the responsibility resides with the author of the instance's implementation.
	 * <p>
	 * Passing a {@literal null} resets the behavior of {@link #deallocateDirectBuffer(ByteBuffer)} to the inherent logic.
	 * 
	 * @param deallocator the deallocator to be used, potentially {@literal null}.
	 * 
	 * @see DirectBufferDeallocator
	 */
	public static synchronized void setDirectBufferDeallocator(
		final DirectBufferDeallocator deallocator
	)
	{
		directBufferDeallocator = mayNull(deallocator);
	}
	
	public static synchronized DirectBufferDeallocator getDirectBufferDeallocator()
	{
		return directBufferDeallocator;
	}
	
	/**
	 * Allows to set the {@link DirectBufferAddressGetter} used by
	 * {@link #getDirectBufferAddress(ByteBuffer)} as an override to the means this class inherently tries to provide.<br>
	 * See {@link DirectBufferAddressGetter} for details.
	 * <p>
	 * The passed instance "should" be immutable or better stateless to ensure concurrency-safe usage,
	 * but ultimately, the responsibility resides with the author of the instance's implementation.
	 * <p>
	 * Passing a {@literal null} resets the behavior of {@link #getDirectBufferAddress(ByteBuffer)} to the inherent logic.
	 * 
	 * @param addressGetter the addressGetter to be used, potentially {@literal null}.
	 * 
	 * @see DirectBufferDeallocator
	 */
	public static synchronized void setDirectBufferAddressGetter(
		final DirectBufferAddressGetter addressGetter
	)
	{
		directBufferAddressGetter = mayNull(addressGetter);
	}
	
	public static synchronized DirectBufferAddressGetter getDirectBufferAddressGetter()
	{
		return directBufferAddressGetter;
	}
	
	public static Class<?> getClassDirectbuffer()
	{
		return CLASS_DirectBuffer;
	}
	
	public static Class<?> getClassCleaner()
	{
		return CLASS_Cleaner;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static API methods //
	///////////////////////
	
	/**
	 * Guarantees the usability of this class by validating if all functionality is usable.
	 * This does not necessarily mean that all attempts to resolve JDK-internal code structures were successful.
	 * Some have alternative options that are used as a fallback.
	 * 
	 * @throws Error
	 */
	public static void guaranteeUsability()
	{
		if(CLASS_DirectBuffer == null
		&& FIELD_OFFSET_Buffer_address < 0
		&& METHOD_DirectBuffer_address == null
		&& directBufferAddressGetter == null
		)
		{
			throw new Error(
				"No means to obtain the DirectBuffer address value. Use #setDirectBufferAddressGetter."
			);
		}
		
		if(CLASS_DirectBuffer == null
		&& METHOD_DirectBuffer_cleaner == null
		&& METHOD_Cleaner_clean == null
		&& directBufferDeallocator == null
		)
		{
			throw new Error(
				"No means to deallocate the DirectBuffer off-heap memory. Use #setDirectBufferDeallocator."
			);
		}
	}
	
	/**
	 * Just to have all jdk internal types here at one place.
	 * 
	 * @param directBuffer
	 * @return
	 */
	public static final boolean isDirectBuffer(final ByteBuffer directBuffer)
	{
		if(CLASS_DirectBuffer == null)
		{
			throw new Error();
		}
		
		notNull(directBuffer);
		
		return CLASS_DirectBuffer.isInstance(directBuffer);
	}
	
	public static final <DB extends ByteBuffer> DB guaranteeDirectBuffer(final DB directBuffer)
	{
		if(isDirectBuffer(directBuffer))
		{
			return directBuffer;
		}
		
		throw new ClassCastException(
			directBuffer.getClass().getName() + " cannot be cast to " + CLASS_DirectBuffer.getName()
		);
	}

	public static final ByteBuffer ensureDirectBufferCapacity(final ByteBuffer current, final long capacity)
	{
		if(current.capacity() >= capacity)
		{
			return current;
		}
		
		X.checkArrayRange(capacity);
		deallocateDirectBuffer(current);
		
		return ByteBuffer.allocateDirect((int)capacity);
	}
		
	/**
	 * No idea if this method is really (still?) necesssary, but it sounds reasonable.
	 * See
	 * http://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us
	 *
	 * @param directBuffer
	 */
	public static final void deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		if(directBufferDeallocator != null)
		{
			directBufferDeallocator.deallocateDirectBuffer(directBuffer);
			return;
		}
		
		internalDeallocateDirectBuffer(directBuffer);
	}
	
	static final void internalDeallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		if(METHOD_DirectBuffer_cleaner == null || METHOD_Cleaner_clean == null)
		{
			throw new Error(
				"No means to explicitely deallocate a DirectBuffer available."
			);
		}
		
		final Object cleaner;
		try
		{
			cleaner = METHOD_DirectBuffer_cleaner.invoke(directBuffer);
			METHOD_Cleaner_clean.invoke(cleaner);
		}
		catch(final ReflectiveOperationException e)
		{
			throw new Error(e);
		}
	}
	
	public static final long getDirectBufferAddress(final ByteBuffer directBuffer)
	{
		if(directBufferAddressGetter != null)
		{
			return directBufferAddressGetter.getDirectBufferAddress(directBuffer);
		}
		
		return internalGetDirectBufferAddress(directBuffer);
	}
	
	static final long internalGetDirectBufferAddress(final ByteBuffer directBuffer)
	{
		guaranteeDirectBuffer(directBuffer);
		
		if(FIELD_OFFSET_Buffer_address >= 0)
		{
			return XMemory.get_long(directBuffer, FIELD_OFFSET_Buffer_address);
		}
		
		if(METHOD_DirectBuffer_address != null)
		{
			try
			{
				// this variable is intended to emphasize the intermediately created Long instance, which is inefficient.
				final Long addressValue = (Long)METHOD_DirectBuffer_address.invoke(directBuffer);
				return addressValue.longValue();
			}
			catch(final ReflectiveOperationException e)
			{
				throw new Error(e);
			}
		}
		
		throw new Error(
			"No means to access " + CLASS_DirectBuffer.getName() + "." + FIELD_NAME_address + " available."
		);
	}
			
	public static final byte[] directBufferToArray(final ByteBuffer directBuffer)
	{
		final byte[] bytes;
		XMemory.copyRangeToArray(
			getDirectBufferAddress(directBuffer),
			bytes = new byte[directBuffer.limit()]
		);
		return bytes;
	}
	
	public static final String getResolvingStatus()
	{
		return
			"Class DirectBuffer           : " + CLASS_DirectBuffer          + '\n' +
			"Class Cleaner                : " + CLASS_Cleaner               + '\n' +
			"field offset Buffer#address  : " + FIELD_OFFSET_Buffer_address + '\n' +
			"Method DirectBuffer#address(): " + METHOD_DirectBuffer_address + '\n' +
			"Method DirectBuffer#cleaner(): " + METHOD_DirectBuffer_cleaner + '\n' +
			"Method Cleaner#clean()       : " + METHOD_Cleaner_clean        + '\n'
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private PlatformInternals()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
