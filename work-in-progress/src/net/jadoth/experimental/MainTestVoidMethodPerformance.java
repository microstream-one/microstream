package net.jadoth.experimental;

/**
 * @author Thomas Muenz
 *
 */
public final class MainTestVoidMethodPerformance
{

	private final int value;

	public MainTestVoidMethodPerformance(final int value)
	{
		super();
		this.value = value;
	}



	public void doStuff1()
	{
		if(this.value < 10)
		{
			return;
		}
		return;
	}

	public MainTestVoidMethodPerformance doStuff2()
	{
		if(this.value < 10)
		{
			return null;
		}
		return this;
	}


	static final int SIZE = 10*1000*1000;

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final MainTestVoidMethodPerformance[] array = new MainTestVoidMethodPerformance[SIZE];

		for(int i = 0; i < SIZE; i++)
		{
			array[i] = new MainTestVoidMethodPerformance(i);
		}

		MainTestVoidMethodPerformance r = null;

		for(MainTestVoidMethodPerformance t : array)
		{
			t.doStuff1();
		}
		for(MainTestVoidMethodPerformance t : array)
		{
			t.doStuff2();
		}
		for(MainTestVoidMethodPerformance t : array)
		{
			r = t.doStuff2();
		}

		long tStart;
		long tStop;
		r = null;

		// 1
		try
		{
			tStart = System.nanoTime();
			for(MainTestVoidMethodPerformance t : array)
			{
				r = t.doStuff2();
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		catch(Exception e)
		{
			// just to be sure variable can't be optimized away or idk.
		}
		System.out.println(r);


		// 2
		try
		{
			tStart = System.nanoTime();
			for(MainTestVoidMethodPerformance t : array)
			{
				t.doStuff1();
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		catch(Exception e)
		{
			// just to be sure variable can't be optimized away or idk.
		}


		// 3
		try
		{
			tStart = System.nanoTime();
			for(MainTestVoidMethodPerformance t : array)
			{
				t.doStuff2();
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		catch(Exception e)
		{
			// just to be sure variable can't be optimized away or idk.
		}



		///////////////////////////////////////////////////////////////////////////
		// conclusion       //
		/////////////////////
		/*
		 * Iteration performance depends more on order of tests than on used code.
		 * Hence all three alternatives can be seen as equally fast.
		 *
		 */

	}




}
