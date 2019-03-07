package one.microstream.persistence.test;

import java.util.Random;

public class MainTestStuff
{
	private static final int RUNS = 1_000_000;
	private static final int SIZE = 1_000_000_000;

	public static final MainTestStuff INSTANCE = new MainTestStuff();

	public int value = 0;

	public void setValue(final int value)
	{
		this.value = value;
	}

	public static void main(final String[] args)
	{
		test(SIZE + new Random().nextInt(100), new int[SIZE + new Random().nextInt(100)]);
	}

	public static MainTestStuff get()
	{
		return INSTANCE;
	}


	private static void test(final int size, final int[] array)
	{
//		INSTANCE.value = new Random().nextInt(100);

		final Adder adder = new Adder(){
			@Override
			public int add4(final int a, final int b)
			{
				return a + b;
			}

		};

//		final int len = array.length;
		for(int r = RUNS; r --> 0;)
		{
			long tStart, tStop;
			tStart = System.nanoTime();
			doit(adder);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}

	public static int doit(final Adder instance)
	{
		int val = 0;
//		final int val2 = 5;
//		final int val3 = 3453;
//		final int val4 = 2345654;
//		final int val5 = 4564;

		for(int i = 0; i < SIZE; i++)
		{
//			val = val + i;
//			val = val + i;
//			val = val + i;
//			val = val + i;
//			val = val + i;
//			val2 = val2 + i;
//			val3 = val3 + i;
//			val4 = val4 + i;
//			val5 = val5 + i;
//			instance.value = i;
//			val = add1(val, i);
//			val = INSTANCE.add4(val, i + i + i + i);
//			val = instance.add4(val, instance.add4(val, instance.add4(val, instance.add4(val, i))));
			val = instance.add4(val, i);
//			val = instance.add4(val, i);
//			val = instance.add4(val, i);
//			val = instance.add4(val, i);
		}
		return val;
	}


	static int add1(final int val, final int i)
	{
		return val + i;
	}

	static int add2(final int val, final int i)
	{
		return val + i;
	}

	public static int add3(final int val, final int i)
	{
		return val + i;
	}

	int add4(final int val, final int i)
	{
		return val + i;
	}

	interface Adder { int add4(int a, int b); }

}
