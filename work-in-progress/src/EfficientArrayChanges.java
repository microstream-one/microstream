public final class EfficientArrayChanges
{
	private static int newCapacity(final int oldCapacity)
	{
		return oldCapacity < 0
			? newSmallerCapacity(-oldCapacity)
			: newBiggerCapacity(oldCapacity)
		;
		
		// fancy log() algorithm with 111 iterations
//		final double sign = Math.signum(oldCapacity);
//		final double oldCapacityAbsVal = Math.abs(oldCapacity);
//		final double log10 = Math.log10(oldCapacityAbsVal);
//		final double change = sign * Math.max(oldCapacityAbsVal / Math.max(log10, 1.0), 1.0);
//		final double newCapacityDecimal = Math.max(oldCapacityAbsVal + change, 1.0);
//		return (int)newCapacityDecimal;
	}
		
	private static int newBiggerCapacity(final int oldCapacity)
	{
		return oldCapacity >= 2_000_000_000
			? Integer.MAX_VALUE
			: oldCapacity >= 200
				? oldCapacity + (oldCapacity >> 4)
				: oldCapacity + 10
		;
	}
	
	private static int newSmallerCapacity(final int oldCapacity)
	{
		return oldCapacity < 11
			? 0
			: oldCapacity >= 180
				? oldCapacity - (oldCapacity >> 4)
				: oldCapacity - 10
		;
	}
	
	public static void main(final String[] args)
	{
		int capacity = 0;
		for(int gen = 0; capacity < Integer.MAX_VALUE;)
		{
			final int newCapacity = newCapacity(capacity);
			System.out.println((++gen) + ": " + capacity + "\t" + newCapacity + "\t(" + (newCapacity - capacity) + ")");
			capacity = newCapacity;
		}
		for(int gen = 0; capacity > 1;)
		{
			final int newCapacity = newCapacity(-capacity);
			System.err.println((++gen) + ": " + capacity + "\t" + newCapacity + "\t(" + (newCapacity - capacity) + ")");
			capacity = newCapacity;
		}
		
//		printNewCapacity(1);
//		printNewCapacity(7);
//		printNewCapacity(10);
//		printNewCapacity(68);
//		printNewCapacity(100);
//		printNewCapacity(1_000);
//		printNewCapacity(10_000);
//		printNewCapacity(100_000);
//		printNewCapacity(1_000_000);
//		printNewCapacity(2_000_000);
//		printNewCapacity(Integer.MAX_VALUE);
//		printNewCapacity(Integer.MAX_VALUE*2);
//
//		printNewCapacity(-1);
//		printNewCapacity(-7);
//		printNewCapacity(-10);
//		printNewCapacity(-68);
//		printNewCapacity(-100);
//		printNewCapacity(-1_000);
//		printNewCapacity(-10_000);
//		printNewCapacity(-100_000);
//		printNewCapacity(-1_000_000);
//		printNewCapacity(-2_000_000);
//		printNewCapacity(-Integer.MAX_VALUE);
//		printNewCapacity(-Integer.MAX_VALUE*2);
	}
	
	private static void printNewCapacity(final int oldCapacity)
	{
		System.out.println(oldCapacity + "\t" + newCapacity(oldCapacity));
	}
	
}
