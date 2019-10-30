package one.microstream.memory;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.DefaultInstantiator;
import one.microstream.reflect.XReflect;

public final class MemoryAccessorGeneric implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// address packing //
	////////////////////
	
	// 12 + 12 + 7 = 31 bits. The 32nd (sign bit) is the small/big switch.
	static final int
	
		SMALL_CHUNK_SIZE_BITCOUNT  = 12,
		SMALL_CHUNK_CHAIN_BITCOUNT = 12,
		SMALL_CHUNK_SLOT_BITCOUNT  =  7,
		
		// the amount of bits to left shift a size value to pad it into position (12+7=19)
		SMALL_CHUNK_SIZE_BITSHIFT_COUNT = SMALL_CHUNK_CHAIN_BITCOUNT + SMALL_CHUNK_SLOT_BITCOUNT,
		
		// the amount of bits to left shift a chain index value to pad it into position (7)
		SMALL_CHUNK_CHAIN_BITSHIFT_COUNT = SMALL_CHUNK_SLOT_BITCOUNT,
		
		// the amount of bits to left shift a slot index value to pad it into position (0)
		SMALL_CHUNK_SLOT_BITSHIFT_COUNT = 0,
		
		// the bit mask to stance out the size part (the left most 12 bits save the sign bit)
		SMALL_CHUNK_SIZE_BITMASK  = ~(Integer.MAX_VALUE << SMALL_CHUNK_SIZE_BITCOUNT ) << SMALL_CHUNK_SIZE_BITSHIFT_COUNT ,
		
		// the bit mask to stance out the chain position part (the 12 bits between the size and chunks bits)
		SMALL_CHUNK_CHAIN_BITMASK = ~(Integer.MAX_VALUE << SMALL_CHUNK_CHAIN_BITCOUNT) << SMALL_CHUNK_CHAIN_BITSHIFT_COUNT,
		
		// the bit mask to stance out the slot index part (simply the right most 7 bits)
		SMALL_CHUNK_SLOT_BITMASK = ~(Integer.MAX_VALUE << SMALL_CHUNK_SLOT_BITCOUNT) << SMALL_CHUNK_SLOT_BITSHIFT_COUNT,
		
		SMALL_CHUNK_MAX_SIZE         = 1 << SMALL_CHUNK_SIZE_BITCOUNT ,
		SMALL_CHUNK_MAX_CHAIN_LENGTH = 1 << SMALL_CHUNK_CHAIN_BITCOUNT,
		SMALL_CHUNK_MAX_SLOT_COUNT   = 1 << SMALL_CHUNK_SLOT_BITCOUNT ,
				
		SMALL_CHUNK_BUFFER_POSITION_SLOTS_TABLE  = 0,
		SMALL_CHUNK_BUFFER_POSITION_SLOTS_START = SMALL_CHUNK_BUFFER_POSITION_SLOTS_TABLE + SMALL_CHUNK_MAX_SLOT_COUNT,
		
		BIG_CHUNK_BUFFER_POSITION_CHUNK_START  = 0,
		
		SMALL_CHUNK_INITIAL_BUFFER_CHAIN_POSITION = 0
	;
	
	static final long
		SIGN                      = Long.MIN_VALUE,
		IDENTIFIER_BITSHIFT_COUNT = Integer.SIZE
	;
	
	private static long packSmallChunkAddress(
		final int chunkSize ,
		final int chainIndex,
		final int slotIndex ,
		final int offset
	)
	{
		// offset is the lower 4 bytes plus the packed identifier as the upper 4 bytes
		return offset +
			((long)packSmallChunkIdentifier(chunkSize, chainIndex, slotIndex) << IDENTIFIER_BITSHIFT_COUNT)
		;
	}
	
	private static int packSmallChunkIdentifier(final int chunkSize, final int chainIndex, final int slotIndex)
	{
		return chunkSize  << SMALL_CHUNK_SIZE_BITSHIFT_COUNT
		     | chainIndex << SMALL_CHUNK_CHAIN_BITSHIFT_COUNT
		     | slotIndex  << SMALL_CHUNK_SLOT_BITSHIFT_COUNT
		;
	}
	
	private static int unpackSmallChunkSize(final int packedSmallChunkIdentifier)
	{
		// SMALL_CHUNK_SIZE_BITMASK is actually not required since they are the left most bits and sign bit is always 0.
		return packedSmallChunkIdentifier >>> SMALL_CHUNK_SIZE_BITSHIFT_COUNT;
	}
	
	private static int unpackSmallChunkChainIndex(final int packedSmallChunkIdentifier)
	{
		return (packedSmallChunkIdentifier & SMALL_CHUNK_CHAIN_BITMASK) >>> SMALL_CHUNK_CHAIN_BITSHIFT_COUNT;
	}
	
	private static int unpackSmallChunkSlotIndex(final int packedSmallChunkIdentifier)
	{
		// SMALL_CHUNK_SLOT_BITSHIFT_COUNT is actually not required since they are the right most bits.
		return packedSmallChunkIdentifier & SMALL_CHUNK_SLOT_BITMASK;
	}
	
	private static long packBigChunkAddress(
		final int bigChunkIdentifier,
		final int offset
	)
	{
		// offset is the lower 4 bytes plus the identifier as the upper 4 bytes, plus the sign bit.
		return SIGN | offset + ((long)bigChunkIdentifier << IDENTIFIER_BITSHIFT_COUNT);
	}
	
	private static int unpackBigChunkIdentifier(
		final long packedBigChunkAddress
	)
	{
		return (int)((SIGN ^ packedBigChunkAddress) >> IDENTIFIER_BITSHIFT_COUNT);
	}
	
	private static int unpackOffset(final long packedAddress)
	{
		// basically just cutting off the upper 4 bytes.
		return (int)packedAddress;
	}
	
	private static boolean isBigChunkAddress(final long packedAddress)
	{
		return packedAddress < 0;
	}
	
	private static boolean isSmallChunkAddress(final long packedAddress)
	{
		// (30.10.2019 TM)FIXME: how to detect null pointers if 0 is a valid address? maybe small must be negative?
		return packedAddress > 0;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static MemoryAccessorGeneric New()
	{
		return New(
			DefaultInstantiator.Default()
		);
	}
	
	public static MemoryAccessorGeneric New(final DefaultInstantiator defaultInstantiator)
	{
		return new MemoryAccessorGeneric(
			notNull(defaultInstantiator)
		);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	// instantiating is a very special case and thus must be handleable separately.
	private final DefaultInstantiator defaultInstantiator;
	
	// since this implementation isn't stateless anyway, it might as well cache the reversing instance.
	private final MemoryAccessorReversing reversing = new MemoryAccessorReversing(this);
	
	private final HashTable<Class<?>, Field[]> objectFieldsRegistry = HashTable.New();
	
	private final ByteBuffer[][] smallChunkBuffers = new ByteBuffer[SMALL_CHUNK_MAX_SIZE][];
	private final byte[][]       smallChunkChains  = new byte      [SMALL_CHUNK_MAX_SIZE][];
	private final boolean[][][]  smallChunkSlots   = new boolean   [SMALL_CHUNK_MAX_SIZE][][];

	private final ByteBuffer[] bigChunkBuffers = new ByteBuffer[0];
	private final int          firstFreeBugChunkBufferIndex = -1;
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	MemoryAccessorGeneric(final DefaultInstantiator defaultInstantiator)
	{
		super();
		this.defaultInstantiator = defaultInstantiator;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static ByteBuffer createSmallChunksBuffer(final int chunkSize, final int chainPosition)
	{
		return createBuffer(SMALL_CHUNK_BUFFER_POSITION_SLOTS_START + chunkSize * SMALL_CHUNK_MAX_SIZE);
	}
	
	private static ByteBuffer createBuffer(final int size)
	{
		// always native order to minimize the clumsily designed performance overhead to the minimum.
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}
		
	private long initializeSmallChunkBufferChain(final int chunkSize)
	{
		final int length = XArrays.smoothCapacityIncrease(0);
		
		final byte[]       chainIndex = new byte[length];
		final boolean[][]  slotsIndex = new boolean[length][];
		final boolean[]    slots      = new boolean[SMALL_CHUNK_MAX_SLOT_COUNT];
		final ByteBuffer[] buffers    = new ByteBuffer[length];
		
		final ByteBuffer buffer = createSmallChunksBuffer(chunkSize, SMALL_CHUNK_INITIAL_BUFFER_CHAIN_POSITION);
		(this.smallChunkBuffers[chunkSize] = buffers)  [SMALL_CHUNK_INITIAL_BUFFER_CHAIN_POSITION] = buffer;
		(this.smallChunkChains[chunkSize] = chainIndex)[SMALL_CHUNK_INITIAL_BUFFER_CHAIN_POSITION] = 1;
		(this.smallChunkSlots[chunkSize] = slotsIndex) [SMALL_CHUNK_INITIAL_BUFFER_CHAIN_POSITION] = slots;
		slots[0] = true;
		
		return packSmallChunkAddress(chunkSize, SMALL_CHUNK_INITIAL_BUFFER_CHAIN_POSITION, 0, 0);
	}
	
	private long allocateMemorySmall(final int chunkSize)
	{
		final byte[] index = this.smallChunkChains[chunkSize];
		
		// case 1: no chunks buffer chain at all for the specified chunk size
		if(index == null)
		{
			return this.initializeSmallChunkBufferChain(chunkSize);
		}
		
		// case 2: scanning for an existing chunks buffer with a free slot
		for(int i = 0; i < index.length; i++)
		{
			if(index[i] < SMALL_CHUNK_MAX_SLOT_COUNT)
			{
				return this.addSmallChunk(chunkSize, i);
			}
		}
		
		// case 3: existing chunks buffer chain is completely full and has to be enlarged
		this.enlargeSmallChunkBufferChain(chunkSize);
		
		return this.addSmallChunk(chunkSize, index.length);
	}
	
	private long addSmallChunk(final int chunkSize, final int chainPosition)
	{
		// (30.10.2019 TM)FIXME: priv#111: allocate chunk in buffer [bytes][i]
		
		return packSmallChunkAddress(chunkSize, chainPosition, 0, 0);
	}
	
	private void enlargeSmallChunkBufferChain(final int chunkSize)
	{
		// (30.10.2019 TM)FIXME: priv#111: increaseSmallChunkBufferChain
	}
	
	private long allocateMemoryBig(final int chunkSize)
	{
		// (30.10.2019 TM)FIXME: priv#111: MemoryAccessorGeneric#allocateMemoryBig()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	private ByteBuffer getBuffer(final long address)
	{
		if(address < 0)
		{
			
		}
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// API methods //
	////////////////

	// memory allocation //

	@Override
	public final synchronized long allocateMemory(final long bytes)
	{
		if(bytes <= SMALL_CHUNK_MAX_SIZE)
		{
			return this.allocateMemorySmall((int)bytes);
		}
		
		if(bytes <= Integer.MAX_VALUE)
		{
			return this.allocateMemoryBig((int)bytes);
		}
		
		// (30.10.2019 TM)EXCP: proper exception
		throw new RuntimeException(
			"Desired memory range to be allocated of " + bytes
			+ " exceeds technical limit of " + Integer.MAX_VALUE
		);
	}

	@Override
	public final synchronized long reallocateMemory(final long address, final long bytes)
	{
		// no no-op detection since detecting the crazy corner case is not worth slowing down the normal case.
		
		this.freeMemory(address);
		
		return this.allocateMemory(bytes);
	}

	@Override
	public final synchronized void freeMemory(final long address)
	{
		
	}
	
	@Override
	public final synchronized void fillMemory(final long address, final long length, final byte value)
	{
		
	}
	
	
	
	// address-based getters for primitive values //
	
	@Override
	public final synchronized byte get_byte(final long address)
	{
		
	}

	@Override
	public final synchronized boolean get_boolean(final long address)
	{
		
	}

	@Override
	public final synchronized short get_short(final long address)
	{
		
	}

	@Override
	public final synchronized char get_char(final long address)
	{
		
	}

	@Override
	public final synchronized int get_int(final long address)
	{
		
	}

	@Override
	public final synchronized float get_float(final long address)
	{
		
	}

	@Override
	public final synchronized long get_long(final long address)
	{
		
	}

	@Override
	public final synchronized double get_double(final long address)
	{
		
	}

	// note: getting a pointer from a non-Object-relative address makes no sense.
	
	
	
	// object-based getters for primitive values and references //
	
	@Override
	public final synchronized byte get_byte(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized boolean get_boolean(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized short get_short(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized char get_char(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized int get_int(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized float get_float(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized long get_long(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized double get_double(final Object instance, final long offset)
	{
		
	}

	@Override
	public final synchronized Object getObject(final Object instance, final long offset)
	{
		
	}
	
	
	
	// address-based setters for primitive values //
	
	@Override
	public final synchronized void set_byte(final long address, final byte value)
	{
		
	}

	@Override
	public final synchronized void set_boolean(final long address, final boolean value)
	{
		
	}

	@Override
	public final synchronized void set_short(final long address, final short value)
	{
		
	}

	@Override
	public final synchronized void set_char(final long address, final char value)
	{
		
	}

	@Override
	public final synchronized void set_int(final long address, final int value)
	{
		
	}

	@Override
	public final synchronized void set_float(final long address, final float value)
	{
		
	}

	@Override
	public final synchronized void set_long(final long address, final long value)
	{
		
	}

	@Override
	public final synchronized void set_double(final long address, final double value)
	{
		
	}

	// note: setting a pointer to a non-Object-relative address makes no sense.
	
	
	// object-based setters for primitive values and references //
	
	@Override
	public final synchronized void set_byte(final Object instance, final long offset, final byte value)
	{
		
	}

	@Override
	public final synchronized void set_boolean(final Object instance, final long offset, final boolean value)
	{
		
	}

	@Override
	public final synchronized void set_short(final Object instance, final long offset, final short value)
	{
		
	}

	@Override
	public final synchronized void set_char(final Object instance, final long offset, final char value)
	{
		
	}

	@Override
	public final synchronized void set_int(final Object instance, final long offset, final int value)
	{
		
	}

	@Override
	public final synchronized void set_float(final Object instance, final long offset, final float value)
	{
		
	}

	@Override
	public final synchronized void set_long(final Object instance, final long offset, final long value)
	{
		
	}

	@Override
	public final synchronized void set_double(final Object instance, final long offset, final double value)
	{
		
	}

	@Override
	public final synchronized void setObject(final Object instance, final long offset, final Object value)
	{
		
	}

		
	
	// transformative byte array primitive value setters //
	
	@Override
	public final synchronized void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		
	}
	
	@Override
	public final synchronized void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		
	}

	@Override
	public final synchronized void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		
	}

	@Override
	public final synchronized void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		
	}

	@Override
	public final synchronized void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		
	}

	@Override
	public final synchronized void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		
	}

	@Override
	public final synchronized void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		
	}

	@Override
	public final synchronized void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		
	}

	
	
	// generic variable-length range copying //
	
	@Override
	public final synchronized void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		
	}

	@Override
	public final synchronized void copyRange(final Object source, final long sourceOffset, final Object target, final long targetOffset, final long length)
	{
		
	}

	
	
	// address-to-array range copying //
	
	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		
	}
	
	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		
	}
	
	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		
	}

	
	
	// array-to-address range copying //
	
	@Override
	public final synchronized void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		
	}

	@Override
	public final synchronized void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		
	}
	
	
	
	// conversion to byte array //
	
	// (uses interface default implementations)
	
	
	
	// field offset abstraction //
	
	/**
	 * Returns an unspecified, abstract "offset" of the passed {@link Field} to specify a generic access of the
	 * field's value for an instance of its declaring class that can be used with object-based methods like
	 * {@link #set_int(Object, long, int)}. Whether that offset is an actual low-level memory offset relative
	 * to an instance' field offset base or simply an index of the passed field in its declaring class' list
	 * of fields, is implementation-specific.
	 * 
	 * @param field the {@link Field} whose abstract offset shall be determined.
	 * 
	 * @return the passed {@link Field}'s abstract offset.
	 */
	@Override
	public final synchronized long objectFieldOffset(final Field field)
	{
		return this.objectFieldOffset(field.getDeclaringClass(), field);
	}
	
	public static final Class<?> determineMostSpecificClass(final Field[] fields)
	{
		if(XArrays.hasNoContent(fields))
		{
			return null;
		}
		
		Class<?> c = fields[0].getDeclaringClass();
		for(int i = 1; i < fields.length; i++)
		{
			// if the current declaring class is not c, but c is a super class, then the current must be more specific.
			if(fields[i].getDeclaringClass() != c && c.isAssignableFrom(fields[i].getDeclaringClass()))
			{
				c = fields[i].getDeclaringClass();
			}
		}
		
		// at this point, c point to the most specific ("most childish"? :D) class of all fields' declaring classes.
		return c;
	}
	
	/**
	 * Array alias vor #objectFieldOffset(Field).
	 */
	@Override
	public final synchronized long[] objectFieldOffsets(final Field... fields)
	{
		final Class<?> mostSpecificClass = determineMostSpecificClass(fields);
		
		return this.objectFieldOffsets(mostSpecificClass, fields);
	}

	@Override
	public final synchronized long objectFieldOffset(final Class<?> objectClass, final Field field)
	{
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		return objectFieldOffset(objectFields, field);
	}
	
	private Field[] ensureRegisteredObjectFields(final Class<?> objectClass)
	{
		final Field[] objectFields = this.objectFieldsRegistry.get(objectClass);
		if(objectFields != null)
		{
			return objectFields;
		}
		
		return this.registerObjectFields(objectClass);
	}
	
	private Field[] registerObjectFields(final Class<?> objectClass)
	{
		/*
		 * Note on algorithm:
		 * Each class in a class hierarchy gets its own registry entry, even if that means redundancy.
		 * This is necessary to make the offset-to-field lookup quick
		 */
		
		final BulkList<Field> objectFields = BulkList.New(20);
		XReflect.iterateDeclaredFieldsUpwards(objectClass, field ->
		{
			// non-instance fields are always discarded
			if(!XReflect.isInstanceField(field))
			{
				return;
			}
			
			objectFields.add(field);
		});
		
		final Field[] array = XArrays.reverse(objectFields.toArray(Field.class));
		
		if(!this.objectFieldsRegistry.add(objectClass, array))
		{
			// (29.10.2019 TM)EXCP: proper exception
			throw new RuntimeException("Object fields already registered for " + objectClass);
		}
		
		return array;
	}
	
	final static long objectFieldOffset(final Field[] objectFields, final Field field)
	{
		final Class<?> declaringClass = field.getDeclaringClass();
		final String   fieldName      = field.getName();
		
		for(int i = 0; i < objectFields.length; i++)
		{
			if(objectFields[i].getDeclaringClass() == declaringClass && objectFields[i].getName().equals(fieldName))
			{
				return i;
			}
		}
		
		// (29.10.2019 TM)EXCP: proper exception
		throw new RuntimeException(
			"Inconsistent object fields registration for " + declaringClass.getName() + "#" + fieldName
		);
	}
	
	@Override
	public final synchronized long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields)
	{
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not an object field: " + fields[i]);
			}
			offsets[i] = objectFieldOffset(objectFields, fields[i]);
		}
		
		return offsets;
	}
	
	

	// special system methods, not really memory-related //
	
	@Override
	public final synchronized void ensureClassInitialized(final Class<?> c)
	{
		/*
		 * This is the equivalent (as far as the MemoryAccessor functionality is concerned) to the Unsafe's
		 * actual class initialization. There, it ensure the field base which is needed to calculate the
		 * field offsets. Here, it ensures that the object fields are registered which are required to
		 * determine the abstract pseudo-offset. Nice :-).
		 */
		this.ensureRegisteredObjectFields(c);
	}
	
	@Override
	public final synchronized <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return this.defaultInstantiator.instantiate(c);
	}
	
	@Override
	public final synchronized MemoryAccessor toReversing()
	{
		return this.reversing;
	}
	
	
	
	
	
	
	// (30.10.2019 TM)FIXME: priv#111: remove testing code
	
	public static void main(final String[] args)
	{
		print(SMALL_CHUNK_SIZE_BITMASK);
		print(SMALL_CHUNK_CHAIN_BITMASK);
		print(SMALL_CHUNK_SIZE_BITMASK);
		
		final int packedSmallRangeIdentifier = packSmallChunkIdentifier(15, 7, 3);
		
		print(packedSmallRangeIdentifier);
		print(unpackSmallChunkSize(packedSmallRangeIdentifier));
		print(unpackSmallChunkChainIndex(packedSmallRangeIdentifier));
		print(unpackSmallChunkSlotIndex(packedSmallRangeIdentifier));
		
		print(packSmallChunkAddress(15, 7, 3, 31));
		print(unpackOffset(packSmallChunkAddress(15, 7, 3, 31)));
		
		print(packBigChunkAddress(15, 31));
		print(unpackBigChunkIdentifier(packBigChunkAddress(15, 31)));
	}
	
	static void print(final int value)
	{
		System.out.println(value);
		System.out.println(VarString.New().padLeft(Integer.toBinaryString(value), Integer.SIZE, '0'));
		System.out.println("---");
	}
	
	static void print(final long value)
	{
		System.out.println(value);
		System.out.println(VarString.New().padLeft(Long.toBinaryString(value), Long.SIZE, '0'));
		System.out.println("---");
	}
	
}
