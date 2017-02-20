import net.jadoth.collections.AbstractArrayStorage;


public class MainTestAASIteration
{
	private static final int   RUNS   = 10000;
	private static final int   SIZE   = 1000000;
	private static final String[] VALUES = new String[SIZE];
	static {
		for(int i = 0; i < VALUES.length - 1; i++)
		{
			VALUES[i] = Integer.toString(i);
		}
	}



	public static void main(final String[] args)
	{
		for(int r = 0; r < RUNS; r++)
		{
			final long tStart = System.nanoTime();
			final boolean value = AbstractArrayStorage.rangedContainsNull(VALUES, VALUES.length, 0, VALUES.length);
			final long tStop = System.nanoTime();
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
	private static void print(final int run, final boolean value, final long elapsedTime)
	{
		System.out.println(run+"\t"+value+"\tElapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(elapsedTime));
	}

}