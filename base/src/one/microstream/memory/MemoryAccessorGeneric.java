package one.microstream.memory;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import one.microstream.X;
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
	
		SMALL_CHUNK_MAX_SLOT_COUNT = Byte.MAX_VALUE,
	
		SMALL_CHUNK_SIZE_BITCOUNT  = 12,
		SMALL_CHUNK_CHAIN_BITCOUNT = 18,
				
		// the amount of bits to left shift a size value to pad it into position (12+7=19)
		SMALL_CHUNK_SIZE_BITSHIFT_COUNT = SMALL_CHUNK_CHAIN_BITCOUNT,
		
		// the amount of bits to left shift a chain index value to pad it into position (7)
		SMALL_CHUNK_CHAIN_BITSHIFT_COUNT = 0,
		
		// the bit mask to stance out the size part (the left most 12 bits save the sign bit)
		SMALL_CHUNK_SIZE_BITMASK  = ~(Integer.MAX_VALUE << SMALL_CHUNK_SIZE_BITCOUNT ) << SMALL_CHUNK_SIZE_BITSHIFT_COUNT ,
		
		// the bit mask to stance out the chain position part (the 12 bits between the size and chunks bits)
		SMALL_CHUNK_CHAIN_BITMASK = ~(Integer.MAX_VALUE << SMALL_CHUNK_CHAIN_BITCOUNT) << SMALL_CHUNK_CHAIN_BITSHIFT_COUNT,
		
		SMALL_CHUNK_MAX_SIZE         = 1 << SMALL_CHUNK_SIZE_BITCOUNT ,
		SMALL_CHUNK_MAX_CHAIN_LENGTH = 1 << SMALL_CHUNK_CHAIN_BITCOUNT,
		
		SMALL_CHUNK_SIZE_1_SLOT  = 1 + SMALL_CHUNK_MAX_SIZE / 2                         ,
		SMALL_CHUNK_SIZE_2_SLOTS = 1 + SMALL_CHUNK_MAX_SIZE / 3                         ,
		SMALL_CHUNK_SIZE_3_SLOTS = 1 + SMALL_CHUNK_MAX_SIZE / 4                         ,
		SMALL_CHUNK_SIZE_4_SLOTS = 1 + SMALL_CHUNK_MAX_SIZE / 5                         ,
		SMALL_CHUNK_SIZE_M_SLOTS = 1 + SMALL_CHUNK_MAX_SIZE / SMALL_CHUNK_MAX_SLOT_COUNT,
		
		SMALL_CHUNK_CHAIN_INCREMENT     = 8,
		SMALL_CHUNK_CHAIN_INITIAL_INDEX = 0,
		SMALL_CHUNK_SLOTS_INITIAL_INDEX = 0
	;
	
	static final long
		IDENTIFIER_BITSHIFT_COUNT = Integer.SIZE,
		SIZE_TYPE_FLAG            = 1L << IDENTIFIER_BITSHIFT_COUNT + SMALL_CHUNK_SIZE_BITCOUNT + SMALL_CHUNK_CHAIN_BITCOUNT
	;
	
	private static int calculateSlotCount(final int chunkSize)
	{
		if(chunkSize >= SMALL_CHUNK_SIZE_4_SLOTS)
		{
			if(chunkSize >= SMALL_CHUNK_SIZE_1_SLOT)
			{
				return 1;
			}
			if(chunkSize >= SMALL_CHUNK_SIZE_2_SLOTS)
			{
				return 2;
			}
			if(chunkSize >= SMALL_CHUNK_SIZE_3_SLOTS)
			{
				return 3;
			}

			return 4;
		}
		if(chunkSize < SMALL_CHUNK_SIZE_M_SLOTS)
		{
			return SMALL_CHUNK_MAX_SLOT_COUNT;
		}
		
		return SMALL_CHUNK_MAX_SIZE / chunkSize;
	}
	
	private static long packSmallChunkAddress(
		final int chunkSize ,
		final int chainIndex,
		final int slotIndex
	)
	{
		// offset is the lower 4 bytes plus the packed identifier as the upper 4 bytes
		return SIZE_TYPE_FLAG |
			((long)packSmallChunkIdentifier(chunkSize, chainIndex) << IDENTIFIER_BITSHIFT_COUNT)
			+ slotIndex * chunkSize
		;
	}
	
	private static int packSmallChunkIdentifier(final int chunkSize, final int chainIndex)
	{
		return chunkSize << SMALL_CHUNK_SIZE_BITSHIFT_COUNT | chainIndex << SMALL_CHUNK_CHAIN_BITSHIFT_COUNT;
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
	
	private static long packBigChunkAddress(
		final int bigChunkIdentifier,
		final int offset
	)
	{
		// offset is the lower 4 bytes plus the identifier as the upper 4 bytes, plus the sign bit.
		return offset + ((long)bigChunkIdentifier << IDENTIFIER_BITSHIFT_COUNT);
	}
	
	private static int unpackBigChunkIdentifier(
		final long packedBigChunkAddress
	)
	{
		return (int)(packedBigChunkAddress >> IDENTIFIER_BITSHIFT_COUNT);
	}
	
	private static int unpackBufferPosition(final long packedAddress)
	{
		// basically just cutting off the upper 4 bytes.
		return (int)packedAddress;
	}
	
	private static boolean isBigChunkAddress(final long packedAddress)
	{
		return (packedAddress & SIZE_TYPE_FLAG) == 0;
	}
	
	private static boolean isSmallChunkAddress(final long packedAddress)
	{
		return (packedAddress & SIZE_TYPE_FLAG) != 0;
	}
	
	private static int unpackBigChunkBufferIdentifier(final long packedAddress)
	{
		return (int)(packedAddress >>> IDENTIFIER_BITSHIFT_COUNT);
	}
	
	private static int unpackSmallChunkBufferIdentifier(final long packedAddress)
	{
		return (int)((packedAddress ^ SIZE_TYPE_FLAG) >>> IDENTIFIER_BITSHIFT_COUNT);
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
	
	/*
	 * 3 dimensions:
	 * 1: master table with the chunk size as the index
	 * 2: a chain of buffers, all of them containing chunks of the specified size.
	 * 3: every buffer is segmented into 1 to 127 subsequent chunks. Each is called a "slot".
	 * 
	 * The buffer table is only 2D because the slots are embedded in its allocated memory
	 * The sizes table is only 2D because every size value is an accumulated representation of occupied slots.
	 * The slots chain table (to mark occupied slots) is the only actual 3D-table
	 */
	private final ByteBuffer[][] smallChunkBufferChains      = new ByteBuffer[SMALL_CHUNK_MAX_SIZE][];
	private final byte[][]       smallChunkBufferChainSizes  = new byte      [SMALL_CHUNK_MAX_SIZE][];
	private final boolean[][][]  smallChunkBufferSlotsChains = new boolean   [SMALL_CHUNK_MAX_SIZE][][];

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
	
	private static ByteBuffer createSmallChunksBuffer()
	{
		return createBuffer(SMALL_CHUNK_MAX_SIZE);
	}
	
	private static ByteBuffer createBuffer(final int size)
	{
		// always native order to minimize the clumsily designed performance overhead to the minimum.
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}
		
	private long initializeSmallChunkBufferChain(final int chunkSize)
	{
		// local shortcuts for constants
		final int chainLength  = SMALL_CHUNK_CHAIN_INCREMENT;
		final int initChainIdx = SMALL_CHUNK_CHAIN_INITIAL_INDEX;
		final int initSlotIdx  = SMALL_CHUNK_SLOTS_INITIAL_INDEX;
		
		// newly instantiated parts
		final byte[]       chainSizes  = new byte[chainLength];
		final boolean[][]  slotsChains = new boolean[chainLength][];
		final boolean[]    slots       = new boolean[SMALL_CHUNK_MAX_SLOT_COUNT];
		final ByteBuffer[] bufferChain = new ByteBuffer[chainLength];
		final ByteBuffer   buffer      = createSmallChunksBuffer();
		
		// setting up references and values for the first chunk
		bufferChain[initChainIdx] = buffer;
		chainSizes [initChainIdx] = 1;
		slotsChains[initChainIdx] = slots;
		slots[initSlotIdx] = true;
		
		// registering the new parts in the master tables
		this.smallChunkBufferChains     [chunkSize] = bufferChain;
		this.smallChunkBufferChainSizes [chunkSize] = chainSizes;
		this.smallChunkBufferSlotsChains[chunkSize] = slotsChains;
		
		// returning a packed address representing the registered chunk so that it can be addressed.
		return packSmallChunkAddress(chunkSize, initChainIdx, initSlotIdx);
	}
	
	private long allocateMemorySmall(final int chunkSize)
	{
		final byte[] index = this.smallChunkBufferChainSizes[chunkSize];
		
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
	
	private void ensureBuffer(final int chunkSize, final int chainPosition)
	{
		if(this.smallChunkBufferChainSizes[chunkSize][chainPosition] == 0)
		{
			this.smallChunkBufferChains[chunkSize][chainPosition] = createSmallChunksBuffer();
			this.smallChunkBufferSlotsChains[chunkSize][chainPosition] = new boolean[calculateSlotCount(chunkSize)];
		}
	}
	
	// only called from code that has ensured that there is space for at least one chunk.
	private long addSmallChunk(final int chunkSize, final int chainPosition)
	{
		this.ensureBuffer(chunkSize, chainPosition);
		
		final boolean[] slots = this.smallChunkBufferSlotsChains[chunkSize][chainPosition];
		
		int slotIndex = 0;
		for(int i = 0; i < slots.length; i++)
		{
			if(!slots[i])
			{
				slots[slotIndex = i] = true;
				break;
			}
		}
		
		this.smallChunkBufferChainSizes[chunkSize][chainPosition]++;
		
		return packSmallChunkAddress(chunkSize, chainPosition, slotIndex);
	}
	
	private static int calculateIncreaseChainLength(final int oldChainLength)
	{
		// this is important in order to not ruin the identifier part of the packed address!
		if(oldChainLength + SMALL_CHUNK_CHAIN_INCREMENT >= SMALL_CHUNK_MAX_CHAIN_LENGTH)
		{
			// (31.10.2019 TM)EXCP: proper exception
			throw new RuntimeException("Memory allocation capacity exceeded.");
		}
		
		/* (31.10.2019 TM)NOTE:
		 * This exception could be prevented by all kinds of fallback strategies:
		 * - allocate a small chunk of 1 size more.
		 * - allocate it as a single-buffer "big" ("normal") chunk, even with the memory overhead.
		 * - use some of the 12 unused bits in the lower 4 bytes to extend the chain index number range.
		 * All of these options could be combined to make such an exception pretty much impossible.
		 * However, that is something for a future improvement. For now and the initial version,
		 * the exception is acceptable.
		 */
		
		return oldChainLength + SMALL_CHUNK_CHAIN_INCREMENT;
	}
	
	private static ByteBuffer[] increase(final ByteBuffer[] oldArray, final int newLength)
	{
		final ByteBuffer[] newChain = new ByteBuffer[newLength];
		System.arraycopy(oldArray, 0, newChain, 0, oldArray.length);
		return newChain;
	}
	
	private void enlargeSmallChunkBufferChain(final int chunkSize)
	{
		final ByteBuffer[] oldChain = this.smallChunkBufferChains[chunkSize];
				
		// size increment is very conservative: a small constant amount more to conserve occupied memory
		this.smallChunkBufferChains[chunkSize] = increase(oldChain, calculateIncreaseChainLength(oldChain.length));
	}
	
	private long allocateMemoryBig(final int chunkSize)
	{
		// (30.10.2019 TM)FIXME: priv#111: MemoryAccessorGeneric#allocateMemoryBig()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	private ByteBuffer getBuffer(final long address)
	{
		if(address == 0)
		{
			// the famous one
			throw new NullPointerException();
		}
		if(address < 0)
		{
			// (31.10.2019 TM)EXCP: proper exception
			throw new RuntimeException("Invalid address: " + address);
		}
		
		return isBigChunkAddress(address)
			? this.getBigChunkBuffer(unpackBigChunkBufferIdentifier(address))
			: this.getSmallChunkBuffer(unpackSmallChunkBufferIdentifier(address))
		;
	}
	
	private ByteBuffer getSmallChunkBuffer(final int identifier)
	{
		return this.smallChunkBufferChains
			[unpackSmallChunkSize(identifier)]
			[unpackSmallChunkChainIndex(identifier)]
		;
	}
	
	private ByteBuffer getBigChunkBuffer(final int identifier)
	{
		return this.bigChunkBuffers[identifier];
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
//		testAddressPacking();
//		testSlotCount();
		testAllocation();
	}
	
	static void print32BitHeader()
	{
		System.out.println("33322222222221111111111000000000");
		System.out.println("21_987654321_987654321_987654321");
	}
	
	static void print64BitHeader()
	{
		System.out.println("6666655555555554444444444333333333322222222221111111111000000000");
		System.out.println("4321_987654321_987654321_987654321_987654321_987654321_987654321");
	}
	
	static void testAddressPacking()
	{
		System.out.println("21_987654321_987654321_987654321");
		print("SMALL_CHUNK_SIZE_BITMASK", SMALL_CHUNK_SIZE_BITMASK);
		print("SMALL_CHUNK_CHAIN_BITMASK", SMALL_CHUNK_CHAIN_BITMASK);
		
		final int packedSmallRangeIdentifier = packSmallChunkIdentifier(15, 7);
		print("packSmallChunkIdentifier(15, 7)", packedSmallRangeIdentifier);
		print("unpackSmallChunkSize", unpackSmallChunkSize(packedSmallRangeIdentifier));
		print("unpackSmallChunkChainIndex", unpackSmallChunkChainIndex(packedSmallRangeIdentifier));

		System.out.println("4321_987654321_987654321_987654321_987654321_987654321_987654321");
		print("SIZE_TYPE_FLAG", SIZE_TYPE_FLAG);
		System.out.println("\n\n--Small Chunk Address--");
		print("packSmallChunkAddress(15, 7, 3, 31)", packSmallChunkAddress(15, 7, 3));
		print("unpackSmallChunkBufferIdentifier", unpackSmallChunkBufferIdentifier(packSmallChunkAddress(15, 7, 3)));
		print("unpackBufferPosition", unpackBufferPosition(packSmallChunkAddress(15, 7, 3)));

//		System.out.println("\n\n--BIG Chunk Address--");
//		print(packBigChunkAddress(15, 31));
//		print(unpackBigChunkIdentifier(packBigChunkAddress(15, 31)));
	}
	
	static void testSlotCount()
	{
		// testing calculateSlotCount
		X.repeat(1, 4096, (final int chunkSize) ->
		{
			final int slotCount = MemoryAccessorGeneric.calculateSlotCount(chunkSize);
			System.out.println(chunkSize + " -> " + slotCount + " (total = " + slotCount * chunkSize + ")");
		});
	}
	
	static void testAllocation()
	{
		final MemoryAccessorGeneric memory = MemoryAccessorGeneric.New();
		final long address = memory.allocateMemory(321);
		print64BitHeader();
		print("address for allocated 321 bytes", address);
		final ByteBuffer bb = memory.getBuffer(address);
		System.out.println(bb.capacity());
	}
	
	static void print(final String label, final int value)
	{
		System.out.println((label == null ? "" : label + ": ") + value);
		System.out.println(VarString.New().padLeft(Integer.toBinaryString(value), Integer.SIZE, '0'));
		System.out.println("---");
	}
	
	static void print(final int value)
	{
		print(null, value);
	}
	
	static void print(final String label, final long value)
	{
		System.out.println((label == null ? "" : label + ": ") + value);
		System.out.println(VarString.New().padLeft(Long.toBinaryString(value), Long.SIZE, '0'));
		System.out.println("---");
	}
	
	static void print(final long value)
	{
		print(null, value);
	}
	
}
