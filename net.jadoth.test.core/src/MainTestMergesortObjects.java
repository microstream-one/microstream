import java.util.Arrays;
import java.util.Comparator;

import net.jadoth.collections.JadothSort;
import net.jadoth.math.JadothMath;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestMergesortObjects
{
	static final Comparator<Age> COMPARE_AGE = new Comparator<Age>(){
		@Override public int compare(final Age o1, final Age o2){
			return o1.value - o2.value;
		}
	};


	private static final int SIZE = 1000000;
	private static final int VALUE_OFF = 0;
	private static final int VALUE_MAX = Integer.MAX_VALUE;

	private static Age[] createArray()
	{
		final Age[] ages = new Age[SIZE];
		for(int i = 0; i < SIZE; i++)
		{
			ages[i] = new Age(VALUE_OFF + JadothMath.random(VALUE_MAX));
		}
		return ages;
	}


	@SuppressWarnings("all")
	static void testSimpleDisplay()
	{
//		JaSort.stackStats.clear();
		final Age[] ages = createArray();
		if(SIZE < 25) System.out.println(Arrays.toString(ages));
		JadothSort.mergesort(ages, COMPARE_AGE);
		if(SIZE < 25) System.out.println(Arrays.toString(ages));

//		for(final Integer i : JaSort.stackStats.keySet())
//		{
//			System.out.println(i+"\t"+JaSort.stackStats.get(i));
//		}
	}


	static void testCorrectness()
	{
		boolean equal = true;
		int k = 0;
		while(equal)
		{
			final Age[] ages = createArray();
			final Age[] ages2 = Arrays.copyOf(ages, ages.length);

			Arrays.sort(ages2, COMPARE_AGE);
			JadothSort.mergesort(ages, COMPARE_AGE);

			if(equal = Arrays.equals(ages, ages2))
			{
				System.out.println(++k);
			}
			else
			{
				System.out.println("failed:");
				System.out.println(Arrays.toString(ages));
				System.out.println(Arrays.toString(ages2));
			}
		}
	}


	static void testCompareToJDK()
	{
		long tStart, tStop;
		for(int n = 10; n --> 0;)
		{
			final Age[] ages = createArray();
//			final Age[] ages2 = Arrays.copyOf(ages, ages.length);
//			final Age[] ages2 = ages;
//			final Age[] ages3 = Arrays.copyOf(ages, ages.length);


			tStart = System.nanoTime();
			JadothSort.mergesort(ages, COMPARE_AGE);
			tStop = System.nanoTime();
			System.out.println(
				"Elapsed Time ( 1 ): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
				+"  ( 1 ): ["+ages[0]+"; "+ages[ages.length-1]+"]"

			);

//			tStart = System.nanoTime();
//			Arrays.sort(ages2, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time ( 2 ): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  ( 2 ): ["+ages2[0]+"; "+ages2[ages2.length-1]+"]"
//			);





//			System.out.println("Equals: "+Arrays.equals(ages, ages2));

//			tStart = System.nanoTime();
//			TimSort.sort(ages3, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (Tim): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  (Tim): ["+ages3[0]+"; "+ages3[ages3.length-1]+"]"
//			);

//			System.out.println("Tim Equals: "+Arrays.equals(ages3, ages2));
			System.out.println("");

//			tStart = System.nanoTime();
//			JaSort.mergesort(ages, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (# 1 ): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  ( 1 ): ["+ages[0]+"; "+ages[ages.length-1]+"]"
//
//			);
//			tStart = System.nanoTime();
//			Arrays.sort(ages2, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (# 2 ): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  ( 2 ): ["+ages2[0]+"; "+ages2[ages2.length-1]+"]"
//			);
//			tStart = System.nanoTime();
//			TimSort.sort(ages3, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (#Tim): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  (Tim): ["+ages3[0]+"; "+ages3[ages3.length-1]+"]"
//			);
//			System.out.println("");

//			System.out.println("Equals: "+Arrays.equals(ages, ages2));
		}
	}



	static void testMinimum()
	{
		long currentMin = Long.MAX_VALUE;
		long tStart, tStop;
		for(int n = 10; n --> 0;)
		{
			final Age[] ages = createArray();
			tStart = System.nanoTime();
			JadothSort.mergesort(ages, COMPARE_AGE);
			tStop = System.nanoTime();
			if(tStop - tStart < currentMin) currentMin = tStop - tStart;
		}
		System.out.println("Min: "+new java.text.DecimalFormat("00,000,000,000").format(currentMin));
	}


	static void testPresorted()
	{
//		long tStart, tStop;
		for(int n = 20; n --> 0;)
		{
			final Age[] ages = createArray();

//			JaSort.swapCount = 0;
//			tStart = System.nanoTime();
			JadothSort.mergesort(ages, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(JaSort.swapCount+" Elapsed Time: "+new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

//			JaSort.swapCount = 0;
//			tStart = System.nanoTime();
			JadothSort.mergesort(ages, COMPARE_AGE);
//			tStop = System.nanoTime();
//			System.out.println(JaSort.swapCount+" Elapsed Time: "+new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}

	public static void main(final String[] args)
	{
//		testSimpleDisplay();
//		testCorrectness();
//		testCompareToJDK();
		testMinimum();
//		testPresorted();
	}
}



//class Age
//{
//	final int value;
//
//	public Age(final int value)
//	{
//		super();
//		this.value = value;
//	}
//
//	@Override
//	public String toString()
//	{
//		return Integer.toString(this.value);
//	}
//
//	/**
//	 * @param obj
//	 * @return
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(final Object obj)
//	{
//		return this.value == ((Age)obj).value;
//	}
//
//}