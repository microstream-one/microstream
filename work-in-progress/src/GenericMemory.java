import one.microstream.memory.XMemory;

public final class GenericMemory
{
	// (04.07.2019 TM)TODO: test and comment if static final arrays yield better performance
		
	private static byte[][] slots = new byte[1000][];
	
	private static int[] bufferedFreeSlotIndices = new int[1000];
	
	/**
	 * The sum of all bytes allocated.
	 */
	private static long totalAllocatedMemory;
	
	/**
	 * The totalAllocatedMemory plus all overhead required to manage it.
	 */
	private static long totalConsumedMemory ;
	
	
	private static void increaseMemoryStatistics(final int byteCount)
	{
		totalAllocatedMemory += byteCount;
		totalConsumedMemory  += XMemory.byteSizeArray_byte(byteCount);
	}
	
	
//	public static final long allocate(final long byteCount)
//	{
//
//	}
//
//	public static final long reallocate(final long address, final long byteCount)
//	{
//
//	}
	
	private static void increaseSlots()
	{
		
	}
	
	private static int slotIndex(final long address)
	{
		return (int)(address >>> Integer.SIZE);
	}
	
	private static int offset(final long address)
	{
		return (int)(address & Integer.MAX_VALUE);
	}
	
	public static final void free(final long address)
	{
		// bounds check done by JVM
		final byte[] slot = slots[slotIndex(address)];
		if(slot == null)
		{
			// (04.07.2019 TM)FIXME: ignore or exception? Check with Unsafe.
		}
		
		if(offset(address) != 0)
		{
			// cannot deallocate an allocated memory range partially, meaning offset must be 0.
			
			 // (04.07.2019 TM)EXCP: proper Error
			throw new Error();
		}
		
		final int slotIndex = (int)(address >>> Integer.SIZE);
		final int offset    = (int)(address & Integer.MAX_VALUE);
	}
	
}
