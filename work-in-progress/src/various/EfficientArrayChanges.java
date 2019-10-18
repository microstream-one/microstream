package various;
import java.io.PrintStream;

public final class EfficientArrayChanges
{
	public static void main(final String[] args)
	{
		runUp();
		runDown();
//		printIncrease(2_021_161_080);
	}
	
	static int increaseCapacity(final int oldCapacity)
	{
		// 280 steps. Threshold 333 is the best value to smooth the highest increase when starting at 0.
		// Also interesting: increment/threshold of 8/172 and 10/220
		return oldCapacity < 333
			? oldCapacity + 16
			: oldCapacity < 2_021_161_081 // 2021161080 * 1,0625 = 2147483647
				? oldCapacity + (oldCapacity >> 4)
				: Integer.MAX_VALUE
		;
	}
	
	static int decreaseCapacity(final int oldCapacity)
	{
		// 264 steps. Threshold 333 is the best value to smooth the lowest decrease when starting at max value.
		// Also interesting: increment/threshold of 8/161 and 10/180
		return oldCapacity >= 333
			? oldCapacity - (oldCapacity >> 4)
			: oldCapacity >= 16 //
				? oldCapacity - 16
				: 0
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// utility stuff //
	//////////////////
		
	static void runUp(final int start)
	{
		for(int gen = 0, capacity = start; capacity < Integer.MAX_VALUE;)
		{
			capacity = printIncrease(++gen, capacity);
		}
	}
	
	static void runDown(final int start)
	{
		for(int gen = 0, capacity = start; capacity > 1;)
		{
			capacity = printDecrease(++gen, capacity);
		}
	}
	
	static void printIncrease(final int capacity)
	{
		printIncrease(1, capacity);
	}
	
	static void printDecrease(final int capacity)
	{
		printDecrease(1, capacity);
	}
	
	static int printIncrease(final int i, final int capacity)
	{
		final int newCapacity = increaseCapacity(capacity);
		print(System.out, i, capacity, newCapacity);
		return newCapacity;
	}
	
	static int printDecrease(final int i, final int capacity)
	{
		final int newCapacity = decreaseCapacity(capacity);
		print(System.err, i, capacity, newCapacity);
		return newCapacity;
	}
	
	static void print(final PrintStream s, final int i, final int capacity, final int newCapacity)
	{
		s.println(i + ": " + capacity + "\t" + newCapacity + "\t(" + (newCapacity - capacity) + ")");
	}
	
	static void runUp()
	{
		runUp(0);
	}
	
	static void runDown()
	{
		runDown(Integer.MAX_VALUE);
	}
	
	
	// fancy log() algorithm with 111 iterations
//	final double sign = Math.signum(oldCapacity);
//	final double oldCapacityAbsVal = Math.abs(oldCapacity);
//	final double log10 = Math.log10(oldCapacityAbsVal);
//	final double change = sign * Math.max(oldCapacityAbsVal / Math.max(log10, 1.0), 1.0);
//	final double newCapacityDecimal = Math.max(oldCapacityAbsVal + change, 1.0);
//	return (int)newCapacityDecimal;
	
}
