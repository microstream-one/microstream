package net.jadoth.test;

import java.nio.ByteBuffer;

import net.jadoth.memory.Memory;
import net.jadoth.util.XVM;
import sun.misc.Unsafe;

public class MainTestUnsafeWrappingPerformance
{
	private static final long SIZE = 1_000_000;
	private static final int RUNS = 100;
	private static final long[] TIMES = new long[RUNS];

	private static final Unsafe vm = (Unsafe)XVM.getSystemInstance();



	public static void main(final String[] args)
	{
		final ByteBuffer bb = ByteBuffer.allocateDirect((int)(SIZE * 4));
//		final ByteBuffer bb = ByteBuffer.allocateDirect(SIZE * 4);
		final long address = Memory.getDirectByteBufferAddress(bb);


		for(int r = 0; r < RUNS; r++)
		{
			final long tStart = System.nanoTime();
			for(int s = 0; s < SIZE; s++)
			{
				vm.putInt(address + s, s);
//				Memory.set_int(address + s, s);
			}
			TIMES[r] = System.nanoTime() - tStart;
		}
		double sum = 0.0;
		for(int r = 1; r < RUNS; r++)
		{
			sum += TIMES[r];
		}
		System.out.println("Average = " + new java.text.DecimalFormat("00,000,000,000").format(sum / (RUNS - 1)));
	}
}
