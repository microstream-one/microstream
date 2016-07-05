
public class MainTestPerformance
{
	private static final int   RUNS   = 10000;   // runs show the JIT improvment over time and even out spikes of a single run
	private static final int   SIZE   = 1_000; // the larger the size is, the more statistically accurate the result becomes
	private static final int[] VALUES = sequence(SIZE - 1); // sequence values from 0 to SIZE -1



	public static void main(final String[] args)
	{
		for(int r = 0; r < RUNS; r++)
		{
			int value = 0; // setup starting state for current run (note: BEFORE time measurment starts)

			// do actual testing
			final long tStart = System.nanoTime();
			for(int i = 0; i < VALUES.length; i++)
			{
				value += VALUES[i];
			}
			final long tStop = System.nanoTime(); // note that variable declaration costs no time (= only a cpu register label)

			 // use value to guarantee that JVM doesn't remove the actually no-op code
			print(r, value, tStop - tStart); // note: the performance required to print does not influence measuring
		}
	}


	// helper method for readability
	public static int[] sequence(final int lastValue)
	{
		final int[] sequence = new int[lastValue + 1];
		for(int i = 0; i <= lastValue; i++)
		{
			sequence[i] = i;
		}
		return sequence;
	}

	// helper method for readability and keeping main() bytecode size low
	private static void print(final int run, final int value, final long elapsedTime)
	{
		System.out.println(run+"\t"+value+"\tElapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(elapsedTime));
	}

}