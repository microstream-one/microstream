

public class MainTestArrayListAdd
{
	public static void main(final String[] args)
	{
		// setup values
		final int size = 1000 * 1000;
		final String[] values = new String[size];
		for(int i = 0; i < size; i++)
		{
			values[i] = "value "+i;
		}

		// setup list
		final java.util.ArrayList<String> list = new java.util.ArrayList<>(size);

		// execute runs with time measuring
		final int runs = 10 * 1000;
		for(int r = 0; r < runs; r++)
		{
			list.clear();
			long tStart, tStop;
			tStart = System.nanoTime();

			// beginning of actual work
			for(int i = 0; i < size; i++)
			{
				list.add(values[i]);
			}
			// end of actual work

			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)+" @ run "+r);
		}
	}
}
