import net.jadoth.collections.JadothSort;
import net.jadoth.math.JadothMath;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestQuicksort
{
	public static void main(final String[] args)
	{
		final int size = 19;
//		final int size = 8;



//		final int[] ints = new int[size];
//		for(int i = size; i --> 0;)
//		{
//			ints[i] = (int)(Math.random()* 100);
//		}
//		System.out.println(Arrays.toString(ints));
//		quicksort(ints);
//		System.out.println(Arrays.toString(ints));


//		boolean equal = true;
//		int k = 0;
//		while(equal)
//		{
//			final int[] ints1 = new int[size];
//			for(int i = size; i --> 0;)
//			{
//				ints1[i] = (int)(Math.random()* Integer.MAX_VALUE);
//			}
//			final int[] ints2 = Arrays.copyOf(ints1, ints1.length);
//
//			Arrays.sort(ints2);
//			Quicksort.quicksort(ints1, 0, ints1.length-1);
//
//			equal = Arrays.equals(ints1, ints2);
//			System.out.println(++k);
//		}



//		long tStart, tStop;
//		for(int n = 10; n --> 0;)
//		{
//			final int[] ints1 = new int[size];
//			for(int i = size; i --> 0;)
//			{
//				ints1[i] = (int)(Math.random()* Integer.MAX_VALUE);
//			}
//			final int[] ints2 = Arrays.copyOf(ints1, ints1.length);
//
//			tStart = System.nanoTime();
//			JaSort.quicksort(ints1, 0, ints1.length-1);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (1): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  (1): ["+ints1[0]+"; "+ints1[ints1.length-1]+"]"
//
//			);
//
//			tStart = System.nanoTime();
//			Arrays.sort(ints2);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (2): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  (2): ["+ints2[0]+"; "+ints2[ints2.length-1]+"]"
//			);
//
//			System.out.println("Equals: "+Arrays.equals(ints1, ints2));
//
//			tStart = System.nanoTime();
//			JaSort.quicksort(ints1, 0, ints1.length-1);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (1#): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  (1): ["+ints1[0]+"; "+ints1[ints1.length-1]+"]"
//
//			);
//			tStart = System.nanoTime();
//			Arrays.sort(ints2);
//			tStop = System.nanoTime();
//			System.out.println(
//				"Elapsed Time (2#): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
//				+"  (2): ["+ints2[0]+"; "+ints2[ints2.length-1]+"]"
//			);
//
//			System.out.println("Equals: "+Arrays.equals(ints1, ints2));
//		}



		long currentMin = Long.MAX_VALUE;
		long tStart, tStop;
		for(int n = 100; n --> 0;)
		{
			final int[] ints1 = new int[size];
			for(int i = size; i --> 0;)
			{
				ints1[i] = JadothMath.random(Integer.MAX_VALUE);
			}
			tStart = System.nanoTime();
			JadothSort.sort(ints1, 0, ints1.length-1);
			tStop = System.nanoTime();
			if(tStop - tStart < currentMin) currentMin = tStop - tStart;
		}
		System.out.println("Min: "+new java.text.DecimalFormat("00,000,000,000").format(currentMin));



//		long tStart, tStop;
//		for(int n = 20; n --> 0;)
//		{
//			final int[] ints1 = new int[size];
//			for(int i = size; i --> 0;)
//			{
//				ints1[i] = (int)(Math.random()* Integer.MAX_VALUE);
//			}
//
//			JaSort.swapCount = 0;
//			tStart = System.nanoTime();
//			JaSort.quicksort(ints1, 0, ints1.length-1);
//			tStop = System.nanoTime();
//			System.out.println(JaSort.swapCount+" Elapsed Time: "+new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//
//			JaSort.swapCount = 0;
//			tStart = System.nanoTime();
//			JaSort.quicksort(ints1, 0, ints1.length-1);
//			tStop = System.nanoTime();
//			System.out.println(JaSort.swapCount+" Elapsed Time: "+new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}


	}
}