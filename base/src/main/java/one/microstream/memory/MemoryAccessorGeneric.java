package one.microstream.memory;

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

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.exceptions.MemoryException;
import one.microstream.functional.DefaultInstantiator;
import one.microstream.reflect.XReflect;
import one.microstream.typing.XTypes;


public final class MemoryAccessorGeneric implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// address packing //
	////////////////////
	
	// 10 + 20 = 30 bits. The sign (32nd) is ignored as it would ruin address arithmetic. 31th is small/big switch.
	static final int
	
		SMALL_CHUNK_MAX_SLOT_COUNT = Byte.MAX_VALUE,
	
		SMALL_CHUNK_SIZE_BITCOUNT  = 10,
		SMALL_CHUNK_CHAIN_BITCOUNT = 20,
				
		// the amount of bits to left shift a size value to pad it into position (12+7=19)
		SMALL_CHUNK_SIZE_BITSHIFT_COUNT = SMALL_CHUNK_CHAIN_BITCOUNT,
		
		// the amount of bits to left shift a chain index value to pad it into position (7)
		SMALL_CHUNK_CHAIN_BITSHIFT_COUNT = 0,
		
		// the bit mask to stance out the size part (the left most 12 bits save the sign bit)
		SMALL_CHUNK_SIZE_BITMASK  = ~(Integer.MAX_VALUE << SMALL_CHUNK_SIZE_BITCOUNT ) << SMALL_CHUNK_SIZE_BITSHIFT_COUNT ,
		
		// the bit mask to stance out the chain position part (the 12 bits between the size and chunks bits)
		SMALL_CHUNK_CHAIN_BITMASK = ~(Integer.MAX_VALUE << SMALL_CHUNK_CHAIN_BITCOUNT) << SMALL_CHUNK_CHAIN_BITSHIFT_COUNT,
		
		SMALL_CHUNK_MAX_BUFFER_SIZE = 4096,
		
		SMALL_CHUNK_MAX_SIZE         = 1 << SMALL_CHUNK_SIZE_BITCOUNT ,
		SMALL_CHUNK_MAX_CHAIN_LENGTH = 1 << SMALL_CHUNK_CHAIN_BITCOUNT,
		
		SMALL_CHUNK_SIZE_4_SLOTS = 1 + SMALL_CHUNK_MAX_BUFFER_SIZE / 5                         ,
		SMALL_CHUNK_SIZE_5_SLOTS = 1 + SMALL_CHUNK_MAX_BUFFER_SIZE / 6                         ,
		SMALL_CHUNK_SIZE_6_SLOTS = 1 + SMALL_CHUNK_MAX_BUFFER_SIZE / 7                         ,
		SMALL_CHUNK_SIZE_7_SLOTS = 1 + SMALL_CHUNK_MAX_BUFFER_SIZE / 8                         ,
		SMALL_CHUNK_SIZE_8_SLOTS = 1 + SMALL_CHUNK_MAX_BUFFER_SIZE / 9                         ,
		SMALL_CHUNK_SIZE_M_SLOTS = 1 + SMALL_CHUNK_MAX_BUFFER_SIZE / SMALL_CHUNK_MAX_SLOT_COUNT,
		
		SMALL_CHUNK_CHAIN_INCREMENT     = 8,
		SMALL_CHUNK_CHAIN_INITIAL_INDEX = 0,
		SMALL_CHUNK_SLOTS_INITIAL_INDEX = 0,
		SMALL_CHUNK_LOWEST_VALID_SIZE   = 1
	;
	
	static final long
		IDENTIFIER_BITSHIFT_COUNT = Integer.SIZE,
		
		SMALL_CHUNK_SIZE_BOUND = SMALL_CHUNK_MAX_SIZE + 1,
		BIG_CHUNK_SIZE_BOUND   = 1L + Integer.MAX_VALUE,
		
		SIZE_TYPE_FLAG = 1L << IDENTIFIER_BITSHIFT_COUNT + SMALL_CHUNK_SIZE_BITCOUNT + SMALL_CHUNK_CHAIN_BITCOUNT,
		REGISTERED_FLAG = SIZE_TYPE_FLAG >>> 1,

		LOWEST_VALID_ADDRESS = 1,

		BIG_CHUNK_TABLE_MAX_LENGTH = (Long.MAX_VALUE ^ SIZE_TYPE_FLAG ^ REGISTERED_FLAG) >>> IDENTIFIER_BITSHIFT_COUNT
	;
	
	// note: increment must be rather large to avoid constant huge array copying for relatively small de/allocations.
	static final int
		BIG_CHUNK_TABLE_INCREMENT    = 64,
		BIG_CHUNK_NO_FREE_SLOT_INDEX = Integer.MAX_VALUE, // important for deallocation logic! See there!
		REGISTERED_MAXIMUM_CAPACITY  = (int)(REGISTERED_FLAG >>> Integer.SIZE)
	;

	
	
	private static int calculateSlotCount(final int chunkSize)
	{
		if(chunkSize >= SMALL_CHUNK_SIZE_8_SLOTS)
		{
			if(chunkSize >= SMALL_CHUNK_SIZE_4_SLOTS)
			{
				return 4;
			}
			if(chunkSize >= SMALL_CHUNK_SIZE_5_SLOTS)
			{
				return 5;
			}
			if(chunkSize >= SMALL_CHUNK_SIZE_6_SLOTS)
			{
				return 6;
			}
			if(chunkSize >= SMALL_CHUNK_SIZE_7_SLOTS)
			{
				return 7;
			}

			return 8;
		}
		if(chunkSize < SMALL_CHUNK_SIZE_M_SLOTS)
		{
			return SMALL_CHUNK_MAX_SLOT_COUNT;
		}
		
		return SMALL_CHUNK_MAX_BUFFER_SIZE / chunkSize;
	}
	
	private static long packSmallChunkAddress(
		final int chunkSizeIndex,
		final int chainIndex    ,
		final int slotIndex
	)
	{
		// offset is the lower 4 bytes plus the packed identifier as the upper 4 bytes
		return SIZE_TYPE_FLAG |
			((long)packSmallChunkIdentifier(chunkSizeIndex, chainIndex) << IDENTIFIER_BITSHIFT_COUNT)
			+ slotIndex * toChunkSize(chunkSizeIndex)
		;
	}
	
	private static int packSmallChunkIdentifier(final int chunkSizeIndex, final int chainIndex)
	{
		return chunkSizeIndex << SMALL_CHUNK_SIZE_BITSHIFT_COUNT
			 | chainIndex << SMALL_CHUNK_CHAIN_BITSHIFT_COUNT
		;
	}
	
	private static int unpackSmallChunkSizeIndex(final int packedSmallChunkIdentifier)
	{
		// SMALL_CHUNK_SIZE_BITMASK is actually not required since they are the left most bits and sign bit is always 0.
		return packedSmallChunkIdentifier >>> SMALL_CHUNK_SIZE_BITSHIFT_COUNT;
	}
	
	private static int unpackSmallChunkChainIndex(final int packedSmallChunkIdentifier)
	{
		return (packedSmallChunkIdentifier & SMALL_CHUNK_CHAIN_BITMASK) >>> SMALL_CHUNK_CHAIN_BITSHIFT_COUNT;
	}
	
	private static long packBigChunkAddress(
		final int bigChunkIndex
	)
	{
		// offset is the lower 4 bytes plus the identifier as the upper 4 bytes. +1 is to avoid 0-address für index 0.
		return (long)(bigChunkIndex + 1) << IDENTIFIER_BITSHIFT_COUNT;
	}
	
	private static long packRegisteredAddress(
		final int registeredIndex
	)
	{
//		// offset is the lower 4 bytes plus the identifier as the upper 4 bytes. Plus "registered" indicator flag.
		return (long)registeredIndex << IDENTIFIER_BITSHIFT_COUNT | REGISTERED_FLAG;
	}
		
	private static int unpackBufferPosition(final long packedAddress)
	{
		// basically just cutting off the upper 4 bytes.
		return (int)packedAddress;
	}
	
	private static int unpackBufferLimit(final long packedAddress)
	{
		/*
		 * < 0 means do not set limit.
		 * Applies to big chunks (always stays at capacity)
		 * and registered buffers (don't touch navigational state at all)
		 */
		return isSmallChunkAddress(packedAddress)
			? unpackSmallChunkBufferLimit(packedAddress)
			: -1
		;
	}
	
	private static int unpackSmallChunkBufferLimit(final long smallChunkPackedAddress)
	{
		final int smallIdentifier = unpackSmallChunkBufferIdentifier(smallChunkPackedAddress);
		final int bufferPosition  = unpackBufferPosition(smallChunkPackedAddress);
		final int chunkSize       = unpackSmallChunkSizeIndex(smallIdentifier) + 1;
		final int slotIndex       = bufferPosition / chunkSize;
		
		return (slotIndex + 1) * chunkSize;
	}
	
	private static boolean isSmallChunkAddress(final long packedAddress)
	{
		return (packedAddress & SIZE_TYPE_FLAG) != 0;
	}
	
	private static boolean isRegisteredAddress(final long packedAddress)
	{
		return (packedAddress & REGISTERED_FLAG) != 0;
	}
	
	private static int unpackBigChunkBufferIndex(final long packedAddress)
	{
		// -1 reverts the +1, which is to avoid 0-address für index 0.
		return (int)(packedAddress >>> IDENTIFIER_BITSHIFT_COUNT) - 1;
	}
	
	private static int unpackRegisteredBufferIndex(final long packedAddress)
	{
		return (int)((packedAddress ^ REGISTERED_FLAG) >>> IDENTIFIER_BITSHIFT_COUNT);
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
	
	public static MemoryAccessorGeneric New(
		final DefaultInstantiator defaultInstantiator
	)
	{
		return New(
			notNull(defaultInstantiator),
			DirectBufferDeallocator.NoOp()
		);
	}
	
	public static MemoryAccessorGeneric New(
		final DirectBufferDeallocator directBufferDeallocator
	)
	{
		return New(
			DefaultInstantiator.Default(),
			notNull(directBufferDeallocator)
		);
	}
	
	public static MemoryAccessorGeneric New(
		final DefaultInstantiator     defaultInstantiator    ,
		final DirectBufferDeallocator directBufferDeallocator
	)
	{
		return new MemoryAccessorGeneric(
			notNull(defaultInstantiator),
			notNull(directBufferDeallocator)
		);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	// instantiating is a very special case and thus must be handleable separately.
	private final DefaultInstantiator     defaultInstantiator    ;
	private final DirectBufferDeallocator directBufferDeallocator;
	
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

	private ByteBuffer[] bigChunkBuffers = new ByteBuffer[BIG_CHUNK_TABLE_INCREMENT];
	private int          firstFreeBigChunkBufferIndex = 0;
	
	private final BufferRegistry bufferRegistry = new BufferRegistry(REGISTERED_MAXIMUM_CAPACITY);
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	MemoryAccessorGeneric(
		final DefaultInstantiator     defaultInstantiator    ,
		final DirectBufferDeallocator directBufferDeallocator
	)
	{
		super();
		this.defaultInstantiator     = defaultInstantiator    ;
		this.directBufferDeallocator = directBufferDeallocator;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	private static ByteBuffer createBuffer(final int capacity)
	{
		return XMemory.allocateDirectNative(capacity);
	}
		
	private long initializeSmallChunkBufferChain(final int chunkSize)
	{
		// local shortcuts for constants
		final int chainLength  = SMALL_CHUNK_CHAIN_INCREMENT;
		final int initChainIdx = SMALL_CHUNK_CHAIN_INITIAL_INDEX;
		final int initSlotIdx  = SMALL_CHUNK_SLOTS_INITIAL_INDEX;
		final int slotCount    = calculateSlotCount(chunkSize);
		final int chunkIdx     = toChunkSizeIndex(chunkSize);
		
		// newly instantiated parts
		final byte[]       chainSizes  = new byte[chainLength];
		final boolean[][]  slotsChains = new boolean[chainLength][];
		final boolean[]    slots       = new boolean[slotCount];
		final ByteBuffer[] bufferChain = new ByteBuffer[chainLength];
		final ByteBuffer   buffer      = createBuffer(slotCount * chunkSize);
		
		// setting up references and values for the first chunk
		bufferChain[initChainIdx] = buffer;
		chainSizes [initChainIdx] = 1;
		slotsChains[initChainIdx] = slots;
		slots[initSlotIdx] = true;
		
		// registering the new parts in the master tables
		this.smallChunkBufferChains     [chunkIdx] = bufferChain;
		this.smallChunkBufferChainSizes [chunkIdx] = chainSizes;
		this.smallChunkBufferSlotsChains[chunkIdx] = slotsChains;
		
		// returning a packed address representing the registered chunk so that it can be addressed.
		return packSmallChunkAddress(chunkIdx, initChainIdx, initSlotIdx);
	}
	
	private static int toChunkSizeIndex(final int chunkSize)
	{
		// size of 0 does not need to be handled, but 2^10 should be. So 1 gets subtracted from the desired size.
		return chunkSize - 1;
	}
	
	private static int toChunkSize(final int chunkSizeIndex)
	{
		return chunkSizeIndex + 1;
	}
	
	private static void validateChunkSize(final int chunkSize)
	{
		// covers negative size and size 0.
		if(chunkSize < 0)
		{
			throw new MemoryException("Invalid memory range: " + chunkSize);
		}
	}
	
	private long allocateMemorySmall(final int chunkSize)
	{

		validateChunkSize(chunkSize);
		
		// consistent with sun.misc.Unsafe#allocateMemory behavior
		if(chunkSize == 0)
		{
			return 0;
		}
				
		final byte[] index = this.smallChunkBufferChainSizes[toChunkSizeIndex(chunkSize)];
		
		// case 1: no chunks buffer chain at all for the specified chunk size
		if(index == null)
		{
			return this.initializeSmallChunkBufferChain(chunkSize);
		}
		
		final int slotCount = calculateSlotCount(chunkSize);
		
		// case 2: scanning for an existing chunks buffer with a free slot
		for(int i = 0; i < index.length; i++)
		{
			if(index[i] < slotCount)
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
		if(this.smallChunkBufferChainSizes[toChunkSizeIndex(chunkSize)][chainPosition] == 0)
		{
			final int chunkSizeIdx = toChunkSizeIndex(chunkSize);
			final int slotCount    = calculateSlotCount(chunkSize);
			this.smallChunkBufferChains     [chunkSizeIdx][chainPosition] = createBuffer(slotCount * chunkSize);
			this.smallChunkBufferSlotsChains[chunkSizeIdx][chainPosition] = new boolean[slotCount];
		}
	}
	
	// only called from code that has ensured that there is space for at least one chunk.
	private long addSmallChunk(final int chunkSize, final int chainPosition)
	{
		this.ensureBuffer(chunkSize, chainPosition);

		final int chunkSizeIdx = toChunkSizeIndex(chunkSize);
		final boolean[]  slots = this.smallChunkBufferSlotsChains[chunkSizeIdx][chainPosition];
		
		int slotIndex = 0;
		for(int i = 0; i < slots.length; i++)
		{
			if(!slots[i])
			{
				slots[slotIndex = i] = true;
				break;
			}
		}
		
		this.smallChunkBufferChainSizes[chunkSizeIdx][chainPosition]++;
		
		return packSmallChunkAddress(chunkSizeIdx, chainPosition, slotIndex);
	}
		
	private static int calculateIncreasedSmallChunkChainLength(final int oldChainLength)
	{
		// this is important in order to not ruin the identifier part of the packed address!
		if(oldChainLength + SMALL_CHUNK_CHAIN_INCREMENT >= SMALL_CHUNK_MAX_CHAIN_LENGTH)
		{
			throw new MemoryException("Memory allocation capacity exceeded.");
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
	
	private static int calculateDecreasedSmallChunkChainLength(
		final int oldChainLength          ,
		final int trailingFreeElementCount
	)
	{
		final int alignedDecrease =
			trailingFreeElementCount / SMALL_CHUNK_CHAIN_INCREMENT * SMALL_CHUNK_CHAIN_INCREMENT
		;
		
		final int newLength = oldChainLength - alignedDecrease;
		
		return newLength;
	}
		
	private void enlargeSmallChunkBufferChain(final int chunkSize)
	{
		final int chunkSizeIndex = toChunkSizeIndex(chunkSize);

		// size increment is very conservative: a small constant amount more to conserve occupied memory
		final int newLength = calculateIncreasedSmallChunkChainLength(this.smallChunkBufferChains[chunkSizeIndex].length);
		this.rebuildSmallChunkChains(chunkSizeIndex, newLength);
	}
	
	private void rebuildSmallChunkChains(final int chunkSizeIndex, final int newLength)
	{
		this.smallChunkBufferChains[chunkSizeIndex] = XArrays.rebuild(
			this.smallChunkBufferChains[chunkSizeIndex], newLength
		);
		this.smallChunkBufferChainSizes[chunkSizeIndex] = XArrays.rebuild(
			this.smallChunkBufferChainSizes[chunkSizeIndex], newLength
		);
		this.smallChunkBufferSlotsChains[chunkSizeIndex] = XArrays.rebuild(
			this.smallChunkBufferSlotsChains[chunkSizeIndex], newLength
		);
	}
	
	private void clearSmallChunkChains(final int chunkSizeIndex)
	{
		this.smallChunkBufferChains     [chunkSizeIndex] = null;
		this.smallChunkBufferChainSizes [chunkSizeIndex] = null;
		this.smallChunkBufferSlotsChains[chunkSizeIndex] = null;
	}
	
	private long allocateMemoryBig(final int chunkSize)
	{

		return this.firstFreeBigChunkBufferIndex < BIG_CHUNK_NO_FREE_SLOT_INDEX
			? this.registerBigChunkBuffer(chunkSize, this.firstFreeBigChunkBufferIndex)
			: this.enlargeBigChunkBufferTableAndRegister(chunkSize)
		;
	}
	
	private long registerBigChunkBuffer(final int chunkSize, final int bigChunkIndex)
	{
		this.bigChunkBuffers[bigChunkIndex] = createBuffer(chunkSize);
		
		this.seekNextFreeBigChunkIndex(bigChunkIndex);
		
		return packBigChunkAddress(bigChunkIndex);
	}
	
	private void seekNextFreeBigChunkIndex(final int usedBigChunkIndex)
	{
		final ByteBuffer[] bigChunkBuffers = this.bigChunkBuffers;
		
		int freeIndex = BIG_CHUNK_NO_FREE_SLOT_INDEX;
		for(int i = usedBigChunkIndex + 1; i < bigChunkBuffers.length; i++)
		{
			if(bigChunkBuffers[i] == null)
			{
				freeIndex = i;
				break;
			}
		}
		
		/*
		 * If there currently is no free slot, the table is not enlarged right away since that is rather costly.
		 * Instead, a marker index is set.
		 * If a deallocation happens before the next allocation, that free index will be used.
		 * It not, meaning the table definitely has to be enlarged, it will be done. But not before.
		 */
		this.firstFreeBigChunkBufferIndex = freeIndex;
	}
	
	private long enlargeBigChunkBufferTableAndRegister(final int chunkSize)
	{
		final int bigChunkIndex = this.bigChunkBuffers.length;
		final int newLength     = calculateIncreasedBigChunkTableLength(this.bigChunkBuffers.length);

		this.bigChunkBuffers                = XArrays.rebuild(this.bigChunkBuffers, newLength);
		this.bigChunkBuffers[bigChunkIndex] = createBuffer(chunkSize);
		this.firstFreeBigChunkBufferIndex   = bigChunkIndex + 1;

		return packBigChunkAddress(bigChunkIndex);
	}
	
	private static int calculateIncreasedBigChunkTableLength(final int oldTableLength)
	{
		// this is important in order to not ruin the identifier part of the packed address!
		if(oldTableLength + BIG_CHUNK_TABLE_INCREMENT >= BIG_CHUNK_TABLE_MAX_LENGTH)
		{
			throw new MemoryException("Memory allocation capacity exceeded.");
		}
		
		return oldTableLength + BIG_CHUNK_TABLE_INCREMENT;
	}
	
	private static int calculateDecreasedBigChunkTableLength(
		final int oldTableLength          ,
		final int trailingFreeElementCount
	)
	{
		final int alignedDecrease =
			trailingFreeElementCount / BIG_CHUNK_TABLE_INCREMENT * BIG_CHUNK_TABLE_INCREMENT
		;
		
		final int newLength = oldTableLength - alignedDecrease;
		
		return Math.max(newLength, BIG_CHUNK_TABLE_INCREMENT);
	}
	
	private static void validateAddress(final long address)
	{
		if(address < LOWEST_VALID_ADDRESS)
		{
			if(address == 0)
			{
				// the famous one
				throw new NullPointerException();
			}
			
			throw new MemoryException("Invalid address: " + address);
		}
	}
	
	private ByteBuffer getBuffer(final long address)
	{
		validateAddress(address);
		
		return isSmallChunkAddress(address)
			? this.getSmallChunkBuffer(unpackSmallChunkBufferIdentifier(address))
			: isRegisteredAddress(address)
				? this.getRegisteredBuffer(unpackRegisteredBufferIndex(address))
				: this.getBigChunkBuffer(unpackBigChunkBufferIndex(address))
		;
	}
	
	private ByteBuffer getSmallChunkBuffer(final int identifier)
	{
		return this.smallChunkBufferChains
			[unpackSmallChunkSizeIndex(identifier)]
			[unpackSmallChunkChainIndex(identifier)]
		;
	}
	
	private ByteBuffer getBigChunkBuffer(final int identifier)
	{
		return this.bigChunkBuffers[identifier];
	}
	
	private ByteBuffer getRegisteredBuffer(final int identifier)
	{
		final ByteBuffer buffer;
		if((buffer = this.bufferRegistry.lookupBuffer(identifier)) != null)
		{
			return buffer;
		}
		
		throw new RuntimeException("Buffer not or no longer registered: " + identifier);
	}
	
	public final boolean systemDeallocateDirectByteBuffer(final ByteBuffer directByteBuffer)
	{
		XTypes.guaranteeDirectByteBuffer(directByteBuffer);
		
		return this.directBufferDeallocator.deallocateDirectBuffer(directByteBuffer);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// API methods //
	////////////////
	
	@Override
	public final void guaranteeUsability()
	{
		// nothing to do here since the construction logic already ensures that.
	}
	
	
	
	// direct byte buffer handling //
	
	@Override
	public final synchronized long getDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		final int registeredIndex = this.bufferRegistry.ensureRegistered(directBuffer);
		
		return packRegisteredAddress(registeredIndex);
	}

	@Override
	public final synchronized boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer)
	{
		if(directBuffer == null)
		{
			// spare the hassle.
			return false;
		}
		
		this.bufferRegistry.ensureRemoved(directBuffer);
		
		return this.systemDeallocateDirectByteBuffer(directBuffer);
	}

	@Override
	public final boolean isDirectByteBuffer(final ByteBuffer byteBuffer)
	{
		return XTypes.isDirectByteBuffer(byteBuffer);
	}
	
	@Override
	public final ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return XTypes.guaranteeDirectByteBuffer(directBuffer);
	}
	
	

	// memory allocation //

	@Override
	public final synchronized long allocateMemory(final long bytes)
	{
		// "<" and ">=" are faster than "<= " and ">".
		if(bytes < SMALL_CHUNK_SIZE_BOUND)
		{
			// checks for case 0 inside.
			return this.allocateMemorySmall((int)bytes);
		}

		// "<" and ">=" are faster than "<= " and ">".
		if(bytes < BIG_CHUNK_SIZE_BOUND)
		{
			return this.allocateMemoryBig((int)bytes);
		}
		
		throw new MemoryException(
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
		if(address < LOWEST_VALID_ADDRESS)
		{
			// no-op is consistent with sun.misc.Unsafe#freeMemory behavior
			if(address == 0)
			{
				return;
			}
			
			// sun.misc.Unsafe#freeMemory ignores negative addresses graciously, which is weird.
			
			throw new MemoryException("Invalid address: " + address);
		}
		
		if(isSmallChunkAddress(address))
		{
			this.freeSmallChunkMemory(address);
		}
		else if(isRegisteredAddress(address))
		{
			this.freeRegisteredBuffer(address);
		}
		else
		{
			this.freeBigChunkMemory(address);
		}
	}
	
	private static void validateProperSmallChunkAddressId(final long address, final int chunkOffset)
	{
		
		/*
		 * This could be ignored, but the base address of an allocated memory range is kind of its identity.
		 * So trying to deallocate anything but a "clear" memory range identity must be a bug.
		 * At least this doesn't just end the process without even a JVM crash output like Unsafe does ]:->.
		 */
		if(chunkOffset != 0)
		{
			throw new MemoryException(
				"Not the base address of an allocated memory range: " + address + " (offset = " + chunkOffset + ")"
			);
		}
	}
	
	private void freeSmallChunkMemory(final long address)
	{
		final int identifier     = unpackSmallChunkBufferIdentifier(address);
		final int bufferPosition = unpackBufferPosition(address);
		final int chunkSizeIndex = unpackSmallChunkSizeIndex(identifier);
		final int chunkSize      = chunkSizeIndex + 1;
		final int chainIndex     = unpackSmallChunkChainIndex(identifier);
		final int slotIndex      = bufferPosition / chunkSize;
		final int chunkOffset    = bufferPosition - slotIndex * chunkSize;
		
		validateProperSmallChunkAddressId(address, chunkOffset);
		
		// apart from actually creating/removing a buffer, the managing algorithms never touch them.
		final boolean[] slots      = this.lookupSlotsNonEmpty(chunkSizeIndex, chainIndex, slotIndex);
		final byte[]    chainSizes = this.lookupChainSizesNonZero(chunkSizeIndex, chainIndex);
		
		if(--chainSizes[chainIndex] == 0)
		{
			this.removeSmallChunkBuffer(chunkSizeIndex, chainIndex);
		}
		else
		{
			slots[slotIndex] = false;
			// no changes to the buffer itself
		}
	}
	
	private void removeSmallChunkBuffer(final int chunkSizeIndex, final int chainIndex)
	{
		this.smallChunkBufferChains     [chunkSizeIndex][chainIndex] = null;
		this.smallChunkBufferSlotsChains[chunkSizeIndex][chainIndex] = null;
		// smallChunkBufferChainSizes needs no modification since that (decrement) causes this method to be called.
		
		this.checkForSmallChunkBufferChainShrinking(chunkSizeIndex, chainIndex);
	}
	
	private void checkForSmallChunkBufferChainShrinking(final int chunkSizeIndex, final int chainIndex)
	{
		final ByteBuffer[] bufferChain = this.smallChunkBufferChains[chunkSizeIndex];
		for(int i = chainIndex; i < bufferChain.length; i++)
		{
			if(bufferChain[i] != null)
			{
				// any occupied higher slot in the chain array means there can be no shrinking
				return;
			}
		}
		
		this.optimizeSmallChunkBufferChain(chunkSizeIndex, chainIndex);
	}
	
	private void optimizeSmallChunkBufferChain(final int chunkSizeIndex, final int chainIndex)
	{
		final ByteBuffer[] bufferChain = this.smallChunkBufferChains[chunkSizeIndex];
		
		int highestNonNullIndex = -1;
		for(int i = chainIndex - 1; i >= 0; i--)
		{
			if(bufferChain[i] != null)
			{
				highestNonNullIndex = i;
				break;
			}
		}
		
		final int freeElementCount = bufferChain.length - (highestNonNullIndex + 1);
		final int newLength = calculateDecreasedSmallChunkChainLength(bufferChain.length, freeElementCount);
		if(newLength < bufferChain.length)
		{
			if(newLength == 0)
			{
				this.clearSmallChunkChains(chunkSizeIndex);
			}
			else
			{
				this.rebuildSmallChunkChains(chunkSizeIndex, newLength);
			}
		}
	}

	public ByteBuffer[] lookupChainNonNull(final int chunkSizeIndex, final int chainIndex)
	{
		final ByteBuffer[] bufferChain = this.lookupChain(chunkSizeIndex);
		if(bufferChain[chainIndex] == null)
		{
			throw new MemoryException("Inconsistency for chunkSize " + toChunkSize(chunkSizeIndex));
		}
		
		return bufferChain;
	}
	
	public ByteBuffer[] lookupChain(final int chunkSizeIndex)
	{
		final ByteBuffer[] bufferChain = this.smallChunkBufferChains[chunkSizeIndex];
		if(bufferChain == null)
		{
			throw new MemoryException("Inconsistency for chunkSize " + toChunkSize(chunkSizeIndex));
		}
		
		return bufferChain;
	}

	private byte[] lookupChainSizesNonZero(final int chunkSizeIndex, final int chainIndex)
	{
		final byte[] chainSizes = this.lookupChainSizes(chunkSizeIndex);
		if(chainSizes[chainIndex] == 0)
		{
			throw new MemoryException("Inconsistency for chunkSize " + toChunkSize(chunkSizeIndex));
		}
		
		return chainSizes;
	}
	
	private byte[] lookupChainSizes(final int chunkSizeIndex)
	{
		final byte[] chainSizes = this.smallChunkBufferChainSizes[chunkSizeIndex];
		if(chainSizes == null)
		{
			throw new MemoryException("Inconsistency for chunkSize " + toChunkSize(chunkSizeIndex));
		}
		
		return chainSizes;
	}
	
	private boolean[] lookupSlots(final int chunkSizeIndex, final int chunkChainIndex)
	{
		final boolean[][] slotsChain = this.smallChunkBufferSlotsChains[chunkSizeIndex];
		if(slotsChain == null)
		{
			throw new MemoryException("Invalid address");
		}
		
		final boolean[] slots = slotsChain[chunkChainIndex];
		if(slots == null)
		{
			throw new MemoryException("Invalid address");
		}
		
		return slots;
	}

	private boolean[] lookupSlotsNonEmpty(
		final int chunkSizeIndex ,
		final int chunkChainIndex,
		final int slotIndex
	)
	{
		final boolean[] slots = this.lookupSlots(chunkSizeIndex, chunkChainIndex);
		if(slots[slotIndex] == false)
		{
			throw new MemoryException("Invalid address");
		}
		
		return slots;
	}

	private void freeRegisteredBuffer(final long address)
	{
		final ByteBuffer buffer = this.bufferRegistry.lookupBuffer(unpackRegisteredBufferIndex(address));
		
		this.deallocateDirectByteBuffer(buffer);
	}
	
	private void freeBigChunkMemory(final long address)
	{
		final ByteBuffer[] bigChunkBuffers = this.bigChunkBuffers;
		
		final int index = unpackBigChunkBufferIndex(address);
		if(index < 0 || index >= bigChunkBuffers.length || bigChunkBuffers[index] == null)
		{
			throw new MemoryException("Invalid address");
		}
		
		// that's it :-D
		bigChunkBuffers[index] = null;
		
		if(index < this.firstFreeBigChunkBufferIndex)
		{
			this.firstFreeBigChunkBufferIndex = index;
		}
		
		this.checkForBigChunkTableDecrease(index);
	}
	
	private void checkForBigChunkTableDecrease(final int currentIndex)
	{
		final ByteBuffer[] bigChunkBuffers = this.bigChunkBuffers;
		
		int freeTrailingElementCount = 0;
		for(int i = currentIndex; i < bigChunkBuffers.length; i++)
		{
			if(bigChunkBuffers[i] != null)
			{
				return;
			}
			freeTrailingElementCount++;
		}
		for(int i = currentIndex ; --i >= 0;)
		{
			if(bigChunkBuffers[i] != null)
			{
				break;
			}
			freeTrailingElementCount++;
		}
		
		final int newLength = calculateDecreasedBigChunkTableLength(bigChunkBuffers.length, freeTrailingElementCount);
		if(newLength != bigChunkBuffers.length)
		{
			this.bigChunkBuffers = XArrays.rebuild(this.bigChunkBuffers, newLength);
		}
	}
	
	@Override
	public final synchronized void fillMemory(final long targetAddress, final long length, final byte value)
	{
		final int intLength = X.checkArrayRange(length);
		
		final int targetPosition = unpackBufferPosition(targetAddress);
		final int targetBound    = unpackBufferLimit(targetAddress);
		
		final ByteBuffer targetBuffer = this.getBuffer(targetAddress);
		
		final int targetLimit = determineActualCopyLimit(targetBound, targetBuffer);
		
		XArrays.validateRange0toUpperBound(targetLimit, targetPosition, intLength);

		// store both buffer current navigational state to restore them later (badly designed concerns mix in BBs)
		final long targetCurrentPosLim = XMemory.getPositionLimit(targetBuffer);

		// prepare buffer navigational states
		XMemory.setPositionLimit(targetBuffer, targetPosition, targetPosition + intLength);
		
		final int bound = targetPosition + intLength;
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = targetPosition; i < bound; i++)
		{
			targetBuffer.put(i, value);
		}

		// restore buffer navigational states in case they are used by external logic (badly designed concerns mix in BBs)
		XMemory.setPositionLimit(targetBuffer, targetCurrentPosLim);
	}
	
	
	
	// address-based getters for primitive values //
	
	@Override
	public final synchronized byte get_byte(final long address)
	{
		return this.getBuffer(address).get(unpackBufferPosition(address));
	}

	@Override
	public final synchronized boolean get_boolean(final long address)
	{
		// because providing a ByteBuffer#get/putBoolean was too hard to do for them...
		return XTypes.to_boolean(this.get_byte(address));
	}

	@Override
	public final synchronized short get_short(final long address)
	{
		return this.getBuffer(address).getShort(unpackBufferPosition(address));
	}

	@Override
	public final synchronized char get_char(final long address)
	{
		return this.getBuffer(address).getChar(unpackBufferPosition(address));
	}

	@Override
	public final synchronized int get_int(final long address)
	{
		return this.getBuffer(address).getInt(unpackBufferPosition(address));
	}

	@Override
	public final synchronized float get_float(final long address)
	{
		return this.getBuffer(address).getFloat(unpackBufferPosition(address));
	}

	@Override
	public final synchronized long get_long(final long address)
	{
		return this.getBuffer(address).getLong(unpackBufferPosition(address));
	}

	@Override
	public final synchronized double get_double(final long address)
	{
		return this.getBuffer(address).getDouble(unpackBufferPosition(address));
	}

	// note: getting a pointer from a non-Object-relative address makes no sense.
	
	
	
	// object-based getters for primitive values and references //
	
	@Override
	public final synchronized byte get_byte(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getByte(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized boolean get_boolean(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getBoolean(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized short get_short(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getShort(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized char get_char(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getChar(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized int get_int(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getInt(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized float get_float(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getFloat(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized long get_long(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getLong(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized double get_double(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getDouble(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized Object getObject(final Object instance, final long offset)
	{
		try
		{
			return this.objectField(instance.getClass(), (int)offset).get(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}
	
	
	
	// address-based setters for primitive values //
	
	@Override
	public final synchronized void set_byte(final long address, final byte value)
	{
		this.getBuffer(address).put(unpackBufferPosition(address), value);
	}

	@Override
	public final synchronized void set_boolean(final long address, final boolean value)
	{
		// because providing a ByteBuffer#get/putBoolean was too hard to do for them...
		this.getBuffer(address).put(unpackBufferPosition(address), XTypes.to_byte(value));
	}

	@Override
	public final synchronized void set_short(final long address, final short value)
	{
		this.getBuffer(address).putShort(unpackBufferPosition(address), value);
	}

	@Override
	public final synchronized void set_char(final long address, final char value)
	{
		this.getBuffer(address).putChar(unpackBufferPosition(address), value);
	}

	@Override
	public final synchronized void set_int(final long address, final int value)
	{
		this.getBuffer(address).putInt(unpackBufferPosition(address), value);
	}

	@Override
	public final synchronized void set_float(final long address, final float value)
	{
		this.getBuffer(address).putFloat(unpackBufferPosition(address), value);
	}

	@Override
	public final synchronized void set_long(final long address, final long value)
	{
		this.getBuffer(address).putLong(unpackBufferPosition(address), value);
	}

	@Override
	public final synchronized void set_double(final long address, final double value)
	{
		this.getBuffer(address).putDouble(unpackBufferPosition(address), value);
	}

	// note: setting a pointer to a non-Object-relative address makes no sense.
	
	
	// object-based setters for primitive values and references //
	
	@Override
	public final synchronized void set_byte(final Object instance, final long offset, final byte value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setByte(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_boolean(final Object instance, final long offset, final boolean value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setBoolean(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_short(final Object instance, final long offset, final short value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setShort(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_char(final Object instance, final long offset, final char value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setChar(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_int(final Object instance, final long offset, final int value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setInt(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_float(final Object instance, final long offset, final float value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setFloat(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_long(final Object instance, final long offset, final long value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setLong(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void set_double(final Object instance, final long offset, final double value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).setDouble(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public final synchronized void setObject(final Object instance, final long offset, final Object value)
	{
		try
		{
			this.objectField(instance.getClass(), (int)offset).set(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

		
	
	// transformative byte array primitive value setters //
	
	@Override
	public final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		XArrays.set_byteInBytes(bytes, index, value);
	}
	
	@Override
	public final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		XArrays.set_booleanInBytes(bytes, index, value);
	}

	@Override
	public final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_shortInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Short.reverseBytes(value) : value);
	}

	@Override
	public final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_charInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Character.reverseBytes(value) : value);
	}

	@Override
	public final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_intInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Integer.reverseBytes(value) : value);
	}

	@Override
	public final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		// byte order check inside
		this.set_intInBytes(bytes, index, Float.floatToRawIntBits(value));
	}

	@Override
	public final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_longInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Long.reverseBytes(value) : value);
	}

	@Override
	public final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		// byte order check inside
		this.set_longInBytes(bytes, index, Double.doubleToRawLongBits(value));
	}

	
	
	// generic variable-length range copying //
	
	@Override
	public final synchronized void copyRange(
		final long sourceAddress,
		final long targetAddress,
		final long length
	)
	{
		final int intLength = X.checkArrayRange(length);
		
		final int sourcePosition = unpackBufferPosition(sourceAddress);
		final int targetPosition = unpackBufferPosition(targetAddress);
		final int sourceBound    = unpackBufferLimit(sourceAddress);
		final int targetBound    = unpackBufferLimit(targetAddress);
		
		final ByteBuffer sourceBuffer = this.getBuffer(sourceAddress);
		final ByteBuffer targetBuffer = this.getBuffer(targetAddress);
		
		final int sourceLimit = determineActualCopyLimit(sourceBound, sourceBuffer);
		final int targetLimit = determineActualCopyLimit(targetBound, targetBuffer);
		
		XArrays.validateRange0toUpperBound(sourceLimit, sourcePosition, intLength);
		XArrays.validateRange0toUpperBound(targetLimit, targetPosition, intLength);
		
		if(sourceBuffer == targetBuffer)
		{
			// sometimes, I really hate them for not providing basic functionality. How to do a trivial SHIFT in a BB?
			this.copyRangeSameBuffer(sourcePosition, targetPosition, sourceLimit, targetLimit, intLength, sourceBuffer);
			return;
		}
		
		// store both buffer current navigational state to restore them later (badly designed concerns mix in BBs)
		final long sourceCurrentPosLim = XMemory.getPositionLimit(sourceBuffer);
		final long targetCurrentPosLim = XMemory.getPositionLimit(targetBuffer);

		// prepare buffer navigational states
		XMemory.setPositionLimit(sourceBuffer, sourcePosition, sourcePosition + intLength);
		XMemory.setPositionLimit(targetBuffer, targetPosition, targetPosition + intLength);
				
		// the actual copying with properly validated and setup navigational states
		targetBuffer.put(sourceBuffer);
		
		// restore buffer navigational states in case they are used by external logic (badly designed concerns mix in BBs)
		XMemory.setPositionLimit(sourceBuffer, sourceCurrentPosLim);
		XMemory.setPositionLimit(targetBuffer, targetCurrentPosLim);
	}
	
	private void copyRangeSameBuffer(
		final int        sourcePosition,
		final int        targetPosition,
		final int        sourceLimit   ,
		final int        targetLimit   ,
		final int        intLength     ,
		final ByteBuffer sameBuffer
	)
	{
		// store both buffer current navigational state to restore them later (badly designed concerns mix in BBs)
		final long currentPosLim = XMemory.getPositionLimit(sameBuffer);
		
		XMemory.setPositionLimit(sameBuffer, sourcePosition, sourcePosition + intLength);
		
		/*
		 * "will be BIG_ENDIAN". Just like that. Slice a LE buffer, you get a BE. Naturally.
		 * 
		 */
		final ByteBuffer slice = sameBuffer.slice().order(sameBuffer.order());

		XMemory.setPositionLimit(sameBuffer, targetPosition, targetPosition + intLength);
		
		// the actual copying with properly validated and setup navigational states
		sameBuffer.put(slice);

		// restore buffer navigational states in case they are used by external logic (badly designed concerns mix in BBs)
		XMemory.setPositionLimit(sameBuffer, currentPosLim);
	}
	
	private static int determineActualCopyLimit(final int definedLimit, final ByteBuffer bb)
	{
		return definedLimit >= 0
			? definedLimit
			: bb.capacity()
		;
	}

	
	
	// address-to-array range copying //
	
	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		final int sourcePosition = unpackBufferPosition(sourceAddress);
		final int sourceBound    = unpackBufferLimit(sourceAddress);
		
		final ByteBuffer sourceBuffer = this.getBuffer(sourceAddress);
		
		final int sourceLimit = determineActualCopyLimit(sourceBound, sourceBuffer);
		
		XArrays.validateRange0toUpperBound(sourceLimit, sourcePosition, target.length);
		
		// store both buffer current navigational state to restore them later (badly designed concerns mix in BBs)
		final long sourceCurrentPosLim = XMemory.getPositionLimit(sourceBuffer);

		// prepare buffer navigational state
		XMemory.setPositionLimit(sourceBuffer, sourcePosition, sourcePosition + target.length);
		
		// the actual copying with properly validated and setup navigational state
		sourceBuffer.get(target);
		
		// restore buffer navigational states in case they are used by external logic (badly designed concerns mix in BBs)
		XMemory.setPositionLimit(sourceBuffer, sourceCurrentPosLim);
	}
	
	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = XTypes.to_boolean(buffer.get(position + i));
		}
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = buffer.getShort(position + i * Short.BYTES);
		}
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = buffer.getChar(position + i * Character.BYTES);
		}
	}
	
	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = buffer.getInt(position + i * Integer.BYTES);
		}
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = buffer.getFloat(position + i * Float.BYTES);
		}
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = buffer.getLong(position + i * Long.BYTES);
		}
	}

	@Override
	public final synchronized void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		final ByteBuffer buffer   = this.getBuffer(sourceAddress);
		final int        position = unpackBufferPosition(sourceAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < target.length; i++)
		{
			target[i] = buffer.getDouble(position + i * Double.BYTES);
		}
	}

	
	
	// array-to-address range copying //
	
	@Override
	public final synchronized void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		final int targetPosition = unpackBufferPosition(targetAddress);
		final int targetBound    = unpackBufferLimit(targetAddress);
		
		final ByteBuffer targetBuffer = this.getBuffer(targetAddress);
		
		final int targetLimit = determineActualCopyLimit(targetBound, targetBuffer);
		
		XArrays.validateRange0toUpperBound(targetLimit, targetPosition, array.length);

		// store both buffer current navigational state to restore them later (badly designed concerns mix in BBs)
		final long targetCurrentPosLim = XMemory.getPositionLimit(targetBuffer);

		// prepare buffer navigational state
		XMemory.setPositionLimit(targetBuffer, targetPosition, targetPosition + array.length);
		
		// the actual copying with properly validated and setup navigational state
		targetBuffer.put(array);
		
		// restore buffer navigational states in case they are used by external logic (badly designed concerns mix in BBs)
		XMemory.setPositionLimit(targetBuffer, targetCurrentPosLim);
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.put(position + i, XTypes.to_byte(array[i]));
		}
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.putShort(position + i * Short.BYTES, array[i]);
		}
	}

	@Override
	public final synchronized void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.putChar(position + i * Character.BYTES, array[i]);
		}
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.putInt(position + i * Integer.BYTES, array[i]);
		}
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.putFloat(position + i * Float.BYTES, array[i]);
		}
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.putLong(position + i * Long.BYTES, array[i]);
		}
	}
	
	@Override
	public final synchronized void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		final ByteBuffer buffer   = this.getBuffer(targetAddress);
		final int        position = unpackBufferPosition(targetAddress);
		
		// thanks to incredibly insufficient API design of ByteBuffer, there is no properly efficient way to do it.
		for(int i = 0; i < array.length; i++)
		{
			buffer.putDouble(position + i * Double.BYTES, array[i]);
		}
	}
	
	
	
	// conversion to byte array //
	
	// (uses interface default implementations)
	
	
	
	// field offset abstraction //
	
	/**
	 * Returns an unspecified, abstract "offset" of the passed {@link Field} to specify generic access of the
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
	
	public static final Class<?> determineMostSpecificDeclaringClass(final Field[] fields)
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
		final Class<?> mostSpecificDeclaringClass = determineMostSpecificDeclaringClass(fields);
		
		return this.objectFieldOffsets(mostSpecificDeclaringClass, fields);
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
		
		return this.registerCollectedObjectFields(objectClass);
	}
		
	private Field[] ensureRegisteredObjectFields(final Class<?> objectClass, final Field... fields)
	{
		final Field[] objectFields = this.objectFieldsRegistry.get(objectClass);
		if(objectFields != null)
		{
			if(fields != null && !Arrays.equals(fields, objectFields))
			{
				throw new MemoryException("Inconsistent object class fields");
			}
			
			return objectFields;
		}
		
		return fields != null
			? this.registerObjectFields(objectClass, fields)
			: this.registerCollectedObjectFields(objectClass)
		;
	}
	
	private Field[] registerCollectedObjectFields(final Class<?> objectClass)
	{
		/*
		 * Note on algorithm:
		 * Each class in a class hierarchy gets its own registry entry, even if that means redundancy.
		 * This is necessary to make the offset-to-field lookup quick
		 */
		final Field[] fields = XReflect.collectInstanceFields(objectClass);

		this.registerObjectFields(objectClass, fields);
		
		return fields;
	}
	
	private Field[] registerObjectFields(final Class<?> objectClass, final Field[] fields)
	{
		for(final Field field : fields)
		{
			XReflect.setAccessible(objectClass, field);
		}
				
		if(!this.objectFieldsRegistry.add(objectClass, fields))
		{
			throw new MemoryException("Object fields already registered for " + objectClass);
		}
		
		return fields;
	}
	
	private Field objectField(final Class<?> c, final int offset)
	{
		final Field[] objectFields = this.objectFieldsRegistry.get(c);
		validateObjectFieldsNotNull(objectFields, c);

		if(offset >= 0 && offset < objectFields.length)
		{
			return objectFields[offset];
		}

		throw createInvalidOffsetException(objectFields, c, offset);
	}
	
	
	private static void validateObjectFieldsNotNull(final Field[] objectFields, final Class<?> c)
	{
		if(objectFields == null)
		{
			throw new MemoryException("No object fields registered for " + c + ".");
		}
	}
	
	private static RuntimeException createInvalidOffsetException(
		final Field[]  objectFields,
		final Class<?> c           ,
		final int      offset
	)
	{
		return new MemoryException("Unknown object field offset " + offset + " for " + c + ".");
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
		
		throw new MemoryException(
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
		 * actual class initialization. There, it ensures the field base which is needed to calculate the
		 * field offsets. Here, it ensures that the object fields are registered which are required to
		 * determine the abstract pseudo-offset. Nice :-).
		 */
		this.ensureRegisteredObjectFields(c);
	}
	
	@Override
	public final synchronized void ensureClassInitialized(final Class<?> c, final Iterable<Field> usedFields)
	{
		final Field[] fields = BulkList.New(usedFields).toArray(Field.class);
		
		this.ensureRegisteredObjectFields(c, fields);
	}
	
	@Override
	public final synchronized <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return this.defaultInstantiator.instantiate(c);
	}

	
	// memory statistics creation //
	
	@Override
	public final synchronized MemoryStatistics createHeapMemoryStatistics()
	{
		final Runtime runtime = Runtime.getRuntime() ;
		final long    max     = runtime.maxMemory()  ;
		final long    total   = runtime.totalMemory(); // ^= committed
		final long    free    = runtime.freeMemory() ;
		final long    used    = total - free         ;
		
		return MemoryStatistics.New(max, total, used);
	}
	
	@Override
	public final synchronized MemoryStatistics createNonHeapMemoryStatistics()
	{
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public final synchronized MemoryAccessor toReversing()
	{
		return this.reversing;
	}

}
