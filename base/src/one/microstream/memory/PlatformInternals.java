package one.microstream.memory;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingSequence;
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

	// must be initialized first for the initializing methods to be able to use it.
	static final BulkList<Warning> INITIALIZATION_WARNINGS = BulkList.New();
	
	static final Class<?> CLASS_DirectBuffer = tryIterativeResolveType(
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
	
	static final Class<?> CLASS_Cleaner = tryIterativeResolveType(
		// initial type name
		"sun.misc.Cleaner",
		// Java 9+ type name
		"jdk.internal.ref.Cleaner"
		// future changes here ... (maybe other JDKs as well? Android?)
	);
	
	static final String FIELD_NAME_address  = "address";
	static final String FIELD_NAME_thunk    = "thunk"  ;
	static final String METHOD_NAME_address = "address";
	static final String METHOD_NAME_cleaner = "cleaner";
	static final String METHOD_NAME_clean   = "clean"  ;

	// Note java.nio.Buffer comment: "Used only by direct buffers. Hoisted here for speed in JNI GetDirectBufferAddress"
	static final long   FIELD_OFFSET_Buffer_address = tryGetFieldOffset(Buffer.class, FIELD_NAME_address);
	static final long   FIELD_OFFSET_Cleaner_thunk  = tryGetFieldOffset(CLASS_Cleaner, FIELD_NAME_thunk);
	
	static final Method METHOD_DirectBuffer_address = tryResolveMethod(CLASS_DirectBuffer, METHOD_NAME_address);
	static final Method METHOD_DirectBuffer_cleaner = tryResolveMethod(CLASS_DirectBuffer, METHOD_NAME_cleaner);
	static final Method METHOD_Cleaner_clean        = tryResolveMethod(CLASS_Cleaner, METHOD_NAME_clean);
		
	/*
	 * Note on Java 9:
	 * Cleaner#clean is no longer accessible. Error message:
	 * Unable to make public void jdk.internal.ref.Cleaner.clean() accessible:
	 * module java.base does not "exports jdk.internal.ref" to unnamed module [...]
	 * 
	 * ...
	 * "does not exports"
	 * ...
	 * anyway ...
	 * 
	 * The problem here is that the morons simple don't understand that if you explicitely allocate off-heap memory,
	 * you also have to deallocate it explicitely since the GC does not manage it. Calling the Cleaner upon object
	 * destruction solves "most" cases, but no "all". Every lousy C++ newbie learns that you have to deallocate
	 * the memory you allocated, clean up your mess, care for your memory. But no, not the JDK morons.
	 * 
	 * There are, of course, numerous discussions and adressings of this issue.
	 * See for example:
	 * http://mail.openjdk.java.net/pipermail/core-libs-dev/2016-February/038669.html
	 * https://issues.apache.org/jira/browse/LUCENE-6989
	 * Discussions and "ideas" up and down of maybe making the DirectByteBuffer implement "AutoCloseable" or whatever.
	 * Morons.
	 * If you provide a means to directly allocate memory (which can be necessary in some libraries like this one here),
	 * you also have to provide a means to deallocate it. Otherwise, you're just a newbie lacking basic understand
	 * of (low-level) software development. Morons.
	 * 
	 * There even is a Cleaner$Cleanable in another package, but the DirectByteBuffer Cleaner does not implement it.
	 * Morons.
	 * 
	 * There is the Runnable "thunk" (actually Deallocator) that can actually be called.
	 * It even checks to be executed only once (with the hilarious comment "Paranoia". You newbie.)
	 * This works, but with two potential problems:
	 * 1.) Should that little "if" ever be removed, the Deallocator might be run twice, potentially messing up memory,
	 *     crashing the JVM.
	 * 2.) The code is not synchronized (because newbies don't do that), so it can theoretically happen that
	 *     the code is run twice in a race condition.
	 *     Maybe this can't happen since one call would always come from a non-GC thread and one call from a GC
	 *     thread. However, if someone writes a Finalizer with an explicit deallocation, it's GC-thread vs. GC-thread.
	 *     IF there's only one GC thread, then, it's perfectly safe again.
	 *     But if there are more than one GC threads, it might crash.
	 * 
	 * Conclusion:
	 * Given all that multitudes of moronity in the JDK, the best I can do is "SHOULD work reliable".
	 * It can, however, also potentially crash your whole process.
	 * This can happen if you have to rely on the work of morons who don't understand basic memory management.
	 */
	
	
	
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
		
		addInitializationWarning("No runtime type could have been found for the given type name list "
			+ Arrays.toString(typeNames)
		);
		
		return null;
	}
	
	static final String localWarningHeader()
	{
		return "WARNING (" + PlatformInternals.class.getName()+"): ";
	}

	static final long tryGetFieldOffset(final Class<?> type, final String declaredFieldName)
	{
		if(type == null)
		{
			addInitializationWarning("Cannot resolve declared field \""
				+ declaredFieldName
				+ "\" in an unresolved type."
			);
			return -1;
		}
		
		Throwable cause = null;
					
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
				cause = e;
			}
		}

		addInitializationWarning("No declared field with name \""
			+ declaredFieldName
			+ "\" could have been found in the class hierarchy beginning at "
			+ type,
			cause
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
			addInitializationWarning("Cannot resolve declared method \""
				+ methodName
				+ "\" in an unresolved type."
			);
			return null;
		}
		
		Throwable cause = null;
		
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
				cause = e;
			}
		}

		addInitializationWarning("No (usable) declared method with name \""
			+ methodName
			+ "\" could have been found in the class hierarchy beginning at "
			+ type.toString(),
			cause
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
		if(FIELD_OFFSET_Cleaner_thunk >= 0)
		{
			internalDeallocateDirectBufferByThunk(directBuffer);
			return;
		}
		
		if(METHOD_Cleaner_clean != null)
		{
			internalDeallocateDirectBufferByClean(directBuffer);
			return;
		}
		
		throw new Error("No means to explicitely deallocate a DirectBuffer available.");
	}
	
	static final void internalDeallocateDirectBufferByClean(final ByteBuffer directBuffer)
	{
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
	
	static final void internalDeallocateDirectBufferByThunk(final ByteBuffer directBuffer)
	{
		final Object cleaner;
		try
		{
			cleaner = METHOD_DirectBuffer_cleaner.invoke(directBuffer);
		}
		catch(final ReflectiveOperationException e)
		{
			throw new Error(e);
		}
		
		final Object cleanerThunkDeallocatorRunnable = XMemory.getObject(cleaner, FIELD_OFFSET_Cleaner_thunk);
		
		if(!(cleanerThunkDeallocatorRunnable instanceof Runnable))
		{
			// better to not deallocate and hope the DBB will get cleaned up by the GC instead of an exception
			return;
		}
		
		// at least secure this call externally against race conditions if the geniuses can't do it internally
		synchronized(cleanerThunkDeallocatorRunnable)
		{
			((Runnable)cleanerThunkDeallocatorRunnable).run();
			
			/* must be set explicitely since the deallocator only sets his copy of the address to 0.
			 * It might seem dangerous to zero out the address of a still reachable and potentially used
			 * direct byte buffer, but this logic here is only executed if the DirectByteBuffer is explicitely
			 * deallocated. If it is still used after that, it is simple a programming error, not different
			 * from writing to a wrong memory address.
			 * So zeroing out the address is the correct thing to do to one the one hand keep the state consistent
			 * and on the other hand prevent access to allegedly still allocated memory while in fact, it has
			 * already been deallocated. It is much better to encounter a zero address in such a case than to
			 * chaotically read or even write from/to memory that might already have been allocated for something else.
			 */
			XMemory.set_long(directBuffer, FIELD_OFFSET_Buffer_address, 0);
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
			"field offset Cleaner#thunk   : " + FIELD_OFFSET_Cleaner_thunk  + '\n' +
			"Method DirectBuffer#address(): " + METHOD_DirectBuffer_address + '\n' +
			"Method DirectBuffer#cleaner(): " + METHOD_DirectBuffer_cleaner + '\n' +
			"Method Cleaner#clean()       : " + METHOD_Cleaner_clean        + '\n'
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// warning handling //
	/////////////////////
	
	private static void addInitializationWarning(final String message)
	{
		addInitializationWarning(message, null);
	}
	
	private static void addInitializationWarning(final String message, final Throwable cause)
	{
		INITIALIZATION_WARNINGS.add(new Warning.Default(message, cause));
	}
	
	public static final XGettingSequence<Warning> initializationWarnings()
	{
		return INITIALIZATION_WARNINGS;
	}
	
	public static final void printInitializationWarnings(final PrintStream printStream)
	{
		for(final Warning warning : INITIALIZATION_WARNINGS)
		{
			warning.print(printStream);
			printStream.println();
		}
	}
	
	public interface Warning
	{
		public String message();
		
		public Throwable cause();
		
		public void print(PrintStream printStream);
		
		
		
		final class Default implements Warning
		{
			final String    message;
			final Throwable cause  ;
			
			Default(final String message, final Throwable cause)
			{
				super();
				this.message = message;
				this.cause = cause;
			}

			@Override
			public String message()
			{
				return this.message;
			}

			@Override
			public Throwable cause()
			{
				return this.cause;
			}
			
			@Override
			public final void print(final PrintStream printStream)
			{
				final String printMessage = localWarningHeader() + coalesce(this.message, "");
				printStream.println(printMessage);
				if(this.cause != null)
				{
					this.cause.printStackTrace(printStream);
				}
			}
			
		}
		
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
	
	// all that makeshift code just because they don't understand basic memory management ...
	
}
