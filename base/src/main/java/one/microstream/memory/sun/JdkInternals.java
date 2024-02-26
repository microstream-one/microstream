package one.microstream.memory.sun;

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

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.DefaultInstantiator;
import one.microstream.memory.DirectBufferAddressGetter;
import one.microstream.memory.DirectBufferDeallocator;
import one.microstream.memory.MemoryStatistics;
import one.microstream.memory.XMemory;
import one.microstream.reflect.XReflect;
import one.microstream.typing.XTypes;
import sun.misc.Unsafe;

public final class JdkInternals
{
	///////////////////////////////////////////////////////////////////////////
	// system access //
	//////////////////

	/*
	 * This must be the very first field to be initialized, otherwise other field
	 * initializations using it will fail with an NPE.
	 */
	private static final Unsafe VM = getMemoryAccess();
	
	public static Unsafe VM()
	{
		return VM;
	}

	/*
	 * If magic values should be represented by constants and constants should be encapsulated by methods
	 * like instance fields should, then why use the code and memory detour of constants in the first place?
	 * Direct "Constant Methods" are the logical conclusion and they get jitted away, anyway.
	 */
	static final String fieldNameUnsafe()
	{
		return "theUnsafe";
	}

	public static final Unsafe getMemoryAccess()
	{
		if(JdkInternals.class.getClassLoader() == null)
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
	// constants //
	//////////////

	// better calculate it instead of making wild assumptions that can change (e.g. 64 bit coops has only 12 byte)
	private static final int BYTE_SIZE_OBJECT_HEADER = calculateByteSizeObjectHeader();

	// According to tests and investigation, memory alignment is always 8 bytes, even for 32 bit JVMs.
	private static final int
		MEMORY_ALIGNMENT_FACTOR =                           8,
		MEMORY_ALIGNMENT_MODULO = MEMORY_ALIGNMENT_FACTOR - 1,
		MEMORY_ALIGNMENT_MASK   = ~MEMORY_ALIGNMENT_MODULO
	;

	/*
	 * Rationale for these local constants:
	 * For Unsafe putting methods like Unsafe#putInt etc, there were two versions before Java 9:
	 * One with an int offset (deprecated) and one with a long offset.
	 * The base offset constants are ints, so they have to be casted for the compiler to select the correct
	 * method option.
	 * However, in Java 9, the int variant disappeared. That now causes an "unnecessary cast" warning.
	 * But removing it would mean in Java 8 and below, the int variant would be chosen and a deprecation warning would
	 * be displayed.
	 * So the only way to use those methods without warnings in either version is to have a constant that is
	 * naturally of type long.
	 */
	private static final long
		ARRAY_BYTE_BASE_OFFSET    = Unsafe.ARRAY_BYTE_BASE_OFFSET   ,
		ARRAY_BOOLEAN_BASE_OFFSET = Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
		ARRAY_SHORT_BASE_OFFSET   = Unsafe.ARRAY_SHORT_BASE_OFFSET  ,
		ARRAY_CHAR_BASE_OFFSET    = Unsafe.ARRAY_CHAR_BASE_OFFSET   ,
		ARRAY_INT_BASE_OFFSET     = Unsafe.ARRAY_INT_BASE_OFFSET    ,
		ARRAY_FLOAT_BASE_OFFSET   = Unsafe.ARRAY_FLOAT_BASE_OFFSET  ,
		ARRAY_LONG_BASE_OFFSET    = Unsafe.ARRAY_LONG_BASE_OFFSET   ,
		ARRAY_DOUBLE_BASE_OFFSET  = Unsafe.ARRAY_DOUBLE_BASE_OFFSET ,
		ARRAY_OBJECT_BASE_OFFSET  = Unsafe.ARRAY_OBJECT_BASE_OFFSET
	;



	///////////////////////////////////////////////////////////////////////////
	// initialization //
	///////////////////

	// direct byte buffer handling //

	/*
	 * A basic principle of this handling logic is to never throw exceptions if the resolving attempts should fail.
	 * The reason is that doing so would prevent using the library in an absolute fashion, even if the low-level
	 * functionality handled here wouldn't even be used.
	 * Instead, warnings are written to the console (bad per se, but preferable in these cases).
	 * Using parts of the library that require that low-level functionality will fail very fast and thus prevent
	 * any damage / inconsistencies.
	 */

	// must be initialized first for the initializing methods to be able to use it.
	static final ArrayList<Warning> INITIALIZATION_WARNINGS = new ArrayList<>();

	/*
	 * See
	 * http://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us
	 */
	static final Class<?> CLASS_Cleaner = tryIterativeResolveType(
		// initial type name
		"sun.misc.Cleaner",
		// Java 9+ type name
		"jdk.internal.ref.Cleaner"
		// future changes here ... (maybe other JDKs as well? Android?)
	);

	static final String FIELD_NAME_address  = "address";
	static final String FIELD_NAME_cleaner  = "cleaner";
	static final String FIELD_NAME_thunk    = "thunk"  ;

	// Note java.nio.Buffer comment: "Used only by direct buffers. Hoisted here for speed in JNI GetDirectBufferAddress"
	static final long FIELD_OFFSET_Buffer_address           = tryGetFieldOffset(Buffer.class, FIELD_NAME_address);
	static final long FIELD_OFFSET_DirectByteBuffer_cleaner = tryGetFieldOffset(XTypes.directByteBufferClass(), FIELD_NAME_cleaner);
	static final long FIELD_OFFSET_Cleaner_thunk            = tryGetFieldOffset(CLASS_Cleaner, FIELD_NAME_thunk);

	static final Class<?> tryIterativeResolveType(final String... typeNames)
	{
		// intentionally SystemClassLoader since all the types are system types.
		final Class<?> type = XReflect.tryIterativeResolveType(ClassLoader.getSystemClassLoader(), typeNames);
		if(type != null)
		{
			return type;
		}

		addInitializationWarning("No runtime type could have been found for the given type name list "
			+ Arrays.toString(typeNames)
		);

		return null;
	}

	/**
	 * Guarantees the full usability of this class by validating if all functionality is usable.
	 *
	 * @throws Error if not all functionality is usable
	 */
	public static void guaranteeUsability()
	{
		if(directBufferAddressGetter == null)
		{
			throw new Error(
				"No means to obtain the DirectByteBuffer address value. Use #setDirectBufferAddressGetter."
			);
		}

		if(directBufferDeallocator == null)
		{
			throw new Error(
				"No means to deallocate the DirectBuffer off-heap memory. Use #setDirectBufferDeallocator."
			);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// warning handling //
	/////////////////////

	static final String localWarningHeader()
	{
		return "WARNING (" + JdkInternals.class.getName()+"): ";
	}

	private static void addInitializationWarning(final String message)
	{
		addInitializationWarning(message, null);
	}

	private static void addInitializationWarning(final String message, final Throwable cause)
	{
		INITIALIZATION_WARNINGS.add(new Warning.Default(message, cause));
	}

	public static final List<Warning> initializationWarnings()
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

	private static DirectBufferDeallocator   directBufferDeallocator   = initializeDirectBufferDeallocator();
	private static DirectBufferAddressGetter directBufferAddressGetter = initializeDirectBufferAddressGetter();

	private static DirectBufferAddressGetter initializeDirectBufferAddressGetter()
	{
		if(FIELD_OFFSET_Buffer_address < 0)
		{
			// address getter is left null and must be provided/set explicitely by the user
			return null;
		}

		return new JdkDirectBufferAddressGetter();
	}

	private static DirectBufferDeallocator initializeDirectBufferDeallocator()
	{
		if(FIELD_OFFSET_DirectByteBuffer_cleaner < 0
		|| FIELD_OFFSET_Cleaner_thunk < 0
		|| FIELD_OFFSET_Buffer_address < 0
		)
		{
			// deallocator is left as NoOp and must be provided/set explicitly by the user
			return DirectBufferDeallocator.NoOp();
		}

		return new JdkDirectBufferDeallocator();
	}

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
		directBufferDeallocator = notNull(deallocator);
	}

	public static synchronized DirectBufferDeallocator getDirectBufferDeallocator()
	{
		return directBufferDeallocator;
	}

	/**
	 * Allows to set the {@link DirectBufferAddressGetter} used by
	 * {@link #getDirectByteBufferAddress(ByteBuffer)} as an override to the means this class inherently tries to provide.<br>
	 * See {@link DirectBufferAddressGetter} for details.
	 * <p>
	 * The passed instance "should" be immutable or better stateless to ensure concurrency-safe usage,
	 * but ultimately, the responsibility resides with the author of the instance's implementation.
	 * <p>
	 * Passing a {@literal null} resets the behavior of {@link #getDirectByteBufferAddress(ByteBuffer)} to the inherent logic.
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

	// "internal" prefixed method that is public, to indicate that it uses JDK-internal details.
	public static final long internalGetDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		XTypes.guaranteeDirectByteBuffer(directBuffer);
		return JdkInternals.get_long(directBuffer, JdkInternals.FIELD_OFFSET_Buffer_address);
	}

	public static final long getDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		if(directBufferAddressGetter == null)
		{
			throw new Error("No means to get a DirectByteBuffer's address available.");
		}
		return directBufferAddressGetter.getDirectBufferAddress(directBuffer);
	}

	// "internal" prefixed method that is public, to indicate that it uses VM-internal details.
	public static final boolean internalDeallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		// better check again in here, in case this method ever gets called from another context, e.g. reflective.
		if(directBuffer == null)
		{
			return false;
		}

		XTypes.guaranteeDirectByteBuffer(directBuffer);

		VM.invokeCleaner(directBuffer);

		return true;
	}

	public static final boolean deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		// If no buffer to be deallocated is passed, no code is executed at all.
		if(directBuffer == null)
		{
			return false;
		}

		return directBufferDeallocator.deallocateDirectBuffer(directBuffer);
	}


	// memory allocation //

	public static final long allocateMemory(final long bytes)
	{
		return VM.allocateMemory(bytes);
	}

	public static final long reallocateMemory(final long address, final long bytes)
	{
		return VM.reallocateMemory(address, bytes);
	}

	public static final void freeMemory(final long address)
	{
		VM.freeMemory(address);
	}

	public static final void fillMemory(final long address, final long length, final byte value)
	{
		VM.setMemory(address, length, value);
	}



	// address-based getters for primitive values //

	public static final byte get_byte(final long address)
	{
		return VM.getByte(address);
	}

	public static final boolean get_boolean(final long address)
	{
		return VM.getBoolean(null, address);
	}

	public static final short get_short(final long address)
	{
		return VM.getShort(address);
	}

	public static final char get_char(final long address)
	{
		return VM.getChar(address);
	}

	public static final int get_int(final long address)
	{
		return VM.getInt(address);
	}

	public static final float get_float(final long address)
	{
		return VM.getFloat(address);
	}

	public static final long get_long(final long address)
	{
		return VM.getLong(address);
	}

	public static final double get_double(final long address)
	{
		return VM.getDouble(address);
	}

	// note: getting a pointer from a non-Object-relative address makes no sense.



	// object-based getters for primitive values and references //

	public static byte get_byte(final Object instance, final long offset)
	{
		return VM.getByte(instance, offset);
	}

	public static boolean get_boolean(final Object instance, final long offset)
	{
		return VM.getBoolean(instance, offset);
	}

	public static short get_short(final Object instance, final long offset)
	{
		return VM.getShort(instance, offset);
	}

	public static char get_char(final Object instance, final long offset)
	{
		return VM.getChar(instance, offset);
	}

	public static int get_int(final Object instance, final long offset)
	{
		return VM.getInt(instance, offset);
	}

	public static float get_float(final Object instance, final long offset)
	{
		return VM.getFloat(instance, offset);
	}

	public static long get_long(final Object instance, final long offset)
	{
		return VM.getLong(instance, offset);
	}

	public static double get_double(final Object instance, final long offset)
	{
		return VM.getDouble(instance, offset);
	}

	public static Object getObject(final Object instance, final long offset)
	{
		return VM.getObject(instance, offset);
	}



	// address-based setters for primitive values //

	public static void set_byte(final long address, final byte value)
	{
		VM.putByte(address, value);
	}

	public static void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		VM.putBoolean(null, address, value);
	}

	public static void set_short(final long address, final short value)
	{
		VM.putShort(address, value);
	}

	public static void set_char(final long address, final char value)
	{
		VM.putChar(address, value);
	}

	public static void set_int(final long address, final int value)
	{
		VM.putInt(address, value);
	}

	public static void set_float(final long address, final float value)
	{
		VM.putFloat(address, value);
	}

	public static void set_long(final long address, final long value)
	{
		VM.putLong(address, value);
	}

	public static void set_double(final long address, final double value)
	{
		VM.putDouble(address, value);
	}

	// note: setting a pointer to a non-Object-relative address makes no sense.



	// object-based setters for primitive values and references //

	public static final void set_byte(final Object instance, final long offset, final byte value)
	{
		VM.putByte(instance, offset, value);
	}

	public static final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		VM.putBoolean(instance, offset, value);
	}

	public static void set_short(final Object instance, final long offset, final short value)
	{
		VM.putShort(instance, offset, value);
	}

	public static final void set_char(final Object instance, final long offset, final char value)
	{
		VM.putChar(instance, offset, value);
	}

	public static final void set_int(final Object instance, final long offset, final int value)
	{
		VM.putInt(instance, offset, value);
	}

	public static final void set_float(final Object instance, final long offset, final float value)
	{
		VM.putFloat(instance, offset, value);
	}

	public static final void set_long(final Object instance, final long offset, final long value)
	{
		VM.putLong(instance, offset, value);
	}

	public static final void set_double(final Object instance, final long offset, final double value)
	{
		VM.putDouble(instance, offset, value);
	}

	public static final void setObject(final Object instance, final long offset, final Object value)
	{
		VM.putObject(instance, offset, value);
	}



	// transformative byte array primitive value setters //

	public static final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		VM.putByte(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		VM.putBoolean(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		VM.putShort(bytes, ARRAY_BYTE_BASE_OFFSET+ index, value);
	}

	public static final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		VM.putChar(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		VM.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		VM.putFloat(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		VM.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		VM.putDouble(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}



	// generic variable-length range copying //

	public static final void copyRange(
		final long sourceAddress,
		final long targetAddress,
		final long length
	)
	{
		VM.copyMemory(sourceAddress, targetAddress, length);
	}

	public static final void copyRange(
		final Object source      ,
		final long   sourceOffset,
		final Object target      ,
		final long   targetOffset,
		final long   length
	)
	{
		VM.copyMemory(source, sourceOffset, target, targetOffset, length);
	}



	// address-to-array range copying //

	public static final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BYTE_BASE_OFFSET, target.length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BOOLEAN_BASE_OFFSET, target.length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_SHORT_BASE_OFFSET, (long)target.length * Short.BYTES);
	}

	public static final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_CHAR_BASE_OFFSET, (long)target.length * Character.BYTES);
	}

	public static final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_INT_BASE_OFFSET, (long)target.length * Integer.BYTES);
	}

	public static final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_FLOAT_BASE_OFFSET, (long)target.length * Float.BYTES);
	}

	public static final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_LONG_BASE_OFFSET, (long)target.length * Long.BYTES);
	}

	public static final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_DOUBLE_BASE_OFFSET, (long)target.length * Double.BYTES);
	}



	// array-to-address range copying //

	public static final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_BYTE_BASE_OFFSET, null, targetAddress, array.length);
	}

	public static final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_BOOLEAN_BASE_OFFSET, null, targetAddress, array.length);
	}

	public static final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_SHORT_BASE_OFFSET, null, targetAddress, (long)array.length * Short.BYTES);
	}

	public static final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_CHAR_BASE_OFFSET, null, targetAddress, (long)array.length * Character.BYTES);
	}

	public static final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_INT_BASE_OFFSET, null, targetAddress, (long)array.length * Integer.BYTES);
	}

	public static final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_FLOAT_BASE_OFFSET, null, targetAddress, (long)array.length * Float.BYTES);
	}

	public static final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_LONG_BASE_OFFSET, null, targetAddress, (long)array.length * Long.BYTES);
	}

	public static final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_DOUBLE_BASE_OFFSET, null, targetAddress, (long)array.length * Double.BYTES);
	}



	// conversion to byte array //

	public static final byte[] asByteArray(final long[] longArray)
	{
		final byte[] bytes = new byte[checkArrayRange((long)longArray.length * Long.BYTES)];
		VM.copyMemory(longArray, ARRAY_LONG_BASE_OFFSET, bytes, ARRAY_BYTE_BASE_OFFSET, bytes.length);
		return bytes;
	}

	public static final byte[] asByteArray(final long value)
	{
		final byte[] bytes = new byte[XMemory.byteSize_long()];
		set_long(bytes, ARRAY_BYTE_BASE_OFFSET, value);
		return bytes;
	}



	// field offset abstraction //

	/*
	 * Object field offset access is prohibited in sun.misc.Unsafe for records in Java >= 16.
	 * As a workaround jdk.internal.misc.Unsafe is used.
	 * For this to work the VM has to be started with
	 * --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
	 */
	private static Object internalVM;
	private static Method internalObjectFieldOffsetMethod;

	private static Object internalVM()
	{
		Object internalVM = JdkInternals.internalVM;
		if(internalVM == null)
		{
			synchronized(JdkInternals.class)
			{
				if((internalVM = JdkInternals.internalVM) == null)
				{
					internalVM = JdkInternals.internalVM = createInternalVM();
				}
			}
		}
		return internalVM;
	}

	private static Object createInternalVM()
	{
		try
		{
			return Class.forName("jdk.internal.misc.Unsafe")
				.getMethod("getUnsafe")
				.invoke(null)
			;
		}
		catch(IllegalAccessException
			| IllegalArgumentException
			| InvocationTargetException
			| NoSuchMethodException
			| SecurityException
			| ClassNotFoundException e
		)
		{
			throw new Error(
				"Could not obtain access to \"jdk.internal.misc.Unsafe\", " +
				"please start the VM with --add-exports java.base/jdk.internal.misc=ALL-UNNAMED",
				e
			);
		}
	}

	private static Method internalObjectFieldOffsetMethod()
	{
		Method internalObjectFieldOffsetMethod = JdkInternals.internalObjectFieldOffsetMethod;
		if(internalObjectFieldOffsetMethod == null)
		{
			synchronized(JdkInternals.class)
			{
				if((internalObjectFieldOffsetMethod = JdkInternals.internalObjectFieldOffsetMethod) == null)
				{
					internalObjectFieldOffsetMethod = JdkInternals.internalObjectFieldOffsetMethod = lookupInternalObjectFieldOffsetMethod();
				}
			}
		}
		return internalObjectFieldOffsetMethod;
	}

	private static Method lookupInternalObjectFieldOffsetMethod()
	{
		try
		{
			return internalVM().getClass()
				.getMethod("objectFieldOffset", Field.class)
			;
		}
		catch(NoSuchMethodException | SecurityException e
		)
		{
			throw new Error("Could not obtain access to \"jdk.internal.misc.Unsafe#objectFieldOffset\"", e);
		}
	}

	/**
	 * Return the field value's arithmetic memory offset relative to the object base offset.
	 *
	 * @param field the field to get the offset for
	 * @return the field value's memory offset.
	 */
	public static final long objectFieldOffset(final Field field)
	{
		try
		{
			return VM.objectFieldOffset(field);
		}
		catch(final UnsupportedOperationException uoe)
		{
			try
			{
				return (long) internalObjectFieldOffsetMethod().invoke(
					internalVM(),
					field
				);
			}
			catch(final IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				throw new Error("Error invoking \"jdk.internal.misc.Unsafe#objectFieldOffset\"", e);
			}
		}
	}

	/**
	 * Array alias vor #objectFieldOffset(Field).
	 * 
	 * @param fields the fields to get the offset for
	 * @return the fields values' memory offsets.
	 */
	public static final long[] objectFieldOffsets(final Field... fields)
	{
		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not an object field: " + fields[i]);
			}
			offsets[i] = objectFieldOffset(fields[i]);
		}

		return offsets;
	}



	// special system methods, not really memory-related //

	public static final void ensureClassInitialized(final Class<?> c)
	{
		VM.ensureClassInitialized(c);
	}

	public static final void ensureClassInitialized(final Class<?>... classes)
	{
		for(final Class<?> c : classes)
		{
			ensureClassInitialized(c);
		}
	}

	@SuppressWarnings("unchecked")
	public static final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		try
		{
			return (T)VM.allocateInstance(c);
		}
		catch(final InstantiationException e)
		{
			throw new InstantiationRuntimeException(e);
		}
	}

	public static final DefaultInstantiator InstantiatorBlank()
	{
		return JdkInstantiatorBlank.New();
	}

	public static final DirectBufferDeallocator DirectBufferDeallocator()
	{
		return new JdkDirectBufferDeallocator();
	}



	// memory size querying logic //

	public static int pageSize()
	{
		return VM.pageSize();
	}

	public static final int byteSizeReference()
	{
		return Unsafe.ARRAY_OBJECT_INDEX_SCALE;
	}

	public static int byteSizeInstance(final Class<?> type)
	{
		if(type.isPrimitive())
		{
			throw new IllegalArgumentException();
		}
		if(type.isArray())
		{
			// instance byte size accounts only array header (object header plus length field plus overhead)
			return VM.arrayBaseOffset(type);
		}
		if(type == Object.class)
		{
			// required because Object's super class is null (see below)
			return byteSizeObjectHeader();
		}

		// declared fields suffice as all super class fields are positioned before them
		final Field[] declaredFields = type.getDeclaredFields();
		long maxInstanceFieldOffset = 0;
		Field maxInstanceField = null;
		for(int i = 0; i < declaredFields.length; i++)
		{
			if(Modifier.isStatic(declaredFields[i].getModifiers()))
			{
				continue;
			}
			final long fieldOffset = VM.objectFieldOffset(declaredFields[i]);
			if(fieldOffset >= maxInstanceFieldOffset)
			{
				maxInstanceField = declaredFields[i];
				maxInstanceFieldOffset = fieldOffset;
			}
		}

		// no declared instance field at all, fall back to super class fields recursively
		if(maxInstanceField == null)
		{
			return byteSizeInstance(type.getSuperclass());
		}

		// memory alignment is a wild assumption at this point. Hopefully it will always be true. Otherwise it's a bug.
		return (int)alignAddress(maxInstanceFieldOffset + byteSizeFieldValue(maxInstanceField.getType()));
	}

	public static final int byteSizeObjectHeader()
	{
		return BYTE_SIZE_OBJECT_HEADER;
	}

	private static final int calculateByteSizeObjectHeader()
	{
		// min logic should be unnecessary but better exclude any source for potential errors
		long minOffset = Long.MAX_VALUE;
		final Field[] declaredFields = ObjectHeaderSizeDummy.class.getDeclaredFields();
		for(final Field field : declaredFields)
		{
			// just in case
			if(Modifier.isStatic(field.getModifiers()))
			{
				continue;
			}

			// requires the dummy field calculateByteSizeObjectHeaderFieldOffsetDummy
			if(VM.objectFieldOffset(field) < minOffset)
			{
				minOffset = VM.objectFieldOffset(field);
			}
		}
		if(minOffset == Long.MAX_VALUE)
		{
			throw new Error("Could not find object header dummy field in class " + ObjectHeaderSizeDummy.class);
		}

		return (int)minOffset; // offset of first instance field is guaranteed to be in int range ^^.
	}

	public static final int byteSizeFieldValue(final Class<?> type)
	{
		return type.isPrimitive()
			? XMemory.byteSizePrimitive(type)
			: byteSizeReference()
		;
	}

	public static final long byteSizeArray_byte(final long elementCount)
	{
		return ARRAY_BYTE_BASE_OFFSET + elementCount;
	}

	public static final long byteSizeArray_boolean(final long elementCount)
	{
		return ARRAY_BOOLEAN_BASE_OFFSET + elementCount;
	}

	public static final long byteSizeArray_short(final long elementCount)
	{
		return ARRAY_SHORT_BASE_OFFSET + elementCount * Short.BYTES;
	}

	public static final long byteSizeArray_char(final long elementCount)
	{
		return ARRAY_CHAR_BASE_OFFSET + elementCount * Character.BYTES;
	}

	public static final long byteSizeArray_int(final long elementCount)
	{
		return ARRAY_INT_BASE_OFFSET + elementCount * Integer.BYTES;
	}

	public static final long byteSizeArray_float(final long elementCount)
	{
		return ARRAY_FLOAT_BASE_OFFSET + elementCount * Float.BYTES;
	}

	public static final long byteSizeArray_long(final long elementCount)
	{
		return ARRAY_LONG_BASE_OFFSET + elementCount * Long.BYTES;
	}

	public static final long byteSizeArray_double(final long elementCount)
	{
		return ARRAY_DOUBLE_BASE_OFFSET + elementCount * Double.BYTES;
	}

	public static final long byteSizeArrayObject(final long elementCount)
	{
		return ARRAY_OBJECT_BASE_OFFSET + elementCount * byteSizeReference();
	}



	///////////////////////////////////////////////////////////////////////////
	// SUN-specific low-level logic //
	/////////////////////////////////

	// unchecked throwing magic //

	public static final void throwUnchecked(final Throwable t) // magically throws Throwable
	{
		// magic!
		VM.throwException(t);
	}



	// compare and swap //

	public static final boolean compareAndSwap_int(
		final Object subject    ,
		final long   offset     ,
		final int    expected   ,
		final int    replacement
	)
	{
		return VM.compareAndSwapInt(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwap_long(
		final Object subject    ,
		final long   offset     ,
		final long   expected   ,
		final long   replacement
	)
	{
		return VM.compareAndSwapLong(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwapObject(
		final Object subject    ,
		final long   offset     ,
		final Object expected   ,
		final Object replacement
	)
	{
		return VM.compareAndSwapObject(subject, offset, expected, replacement);
	}



	// memory aligning arithmetic //

	public static final long alignAddress(final long address)
	{
		if((address & MEMORY_ALIGNMENT_MODULO) == 0)
		{
			return address; // already aligned
		}
		// According to tests and investigation, memory alignment is always 8 bytes, even for 32 bit JVMs.
		return (address & MEMORY_ALIGNMENT_MASK) + MEMORY_ALIGNMENT_FACTOR;
	}



	// static field base and offsets //

	public static Object getStaticFieldBase(final Field field)
	{
		return VM.staticFieldBase(notNull(field)); // throws IllegalArgumentException, so no need to check here
	}

	public static long[] getStaticFieldOffsets(final Field[] fields)
	{
		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(!Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not a static field: " + fields[i]);
			}
			offsets[i] = (int)VM.staticFieldOffset(fields[i]);
		}
		return offsets;
	}


	// memory statistics creation //

	private static MemoryStatistics createMemoryStatistics(final MemoryUsage usage)
	{
		return MemoryStatistics.New(
			usage.getMax()      ,
			usage.getCommitted(),
			usage.getUsed()
		);
	}

	public static MemoryStatistics createHeapMemoryStatistics()
	{
		return createMemoryStatistics(
			ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
		);
	}

	public static MemoryStatistics createNonHeapMemoryStatistics()
	{
		return createMemoryStatistics(
			ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()
		);
	}



	////////////////////////////////////////////////////////
	// copies of general logic to eliminate dependencies //
	//////////////////////////////////////////////////////

	private static final int checkArrayRange(final long capacity)
	{
		if(capacity > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("Invalid array length: " + capacity);
		}

		return (int)capacity;
	}

	private static final <T> T notNull(final T object) throws NullPointerException
	{
		if(object == null)
		{
			throw new NullPointerException();
		}

		return object;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	private JdkInternals()
	{
		// static only
		throw new UnsupportedOperationException();
	}



	// extra class to keep MemoryAccessorSun instances stateless
	static final class ObjectHeaderSizeDummy
	{
		// implicitly used in #calculateByteSizeObjectHeader
		Object calculateByteSizeObjectHeaderFieldOffsetDummy;
	}

}
