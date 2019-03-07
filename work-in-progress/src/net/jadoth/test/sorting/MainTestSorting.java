package net.jadoth.test.sorting;

import static net.jadoth.test.sorting.SortValue.compare;

import net.jadoth.collections.XSort;




public class MainTestSorting
{
	static final int SIZE = 8192;
	static final int SQRTSIZE = (int)Math.sqrt(SIZE)+1;
	static final SortValue[] staticBuffer = new SortValue[SIZE];

	static final boolean PRINT_INPUT  = false; // false true
	static final boolean PRINT_OUTPUT = false;
	static final boolean PRINT_TIME   = true;

	private static final java.text.DecimalFormat df = new java.text.DecimalFormat("00,000,000,000");

	public static void main(final String[] args)
	{
		System.out.println("SIZE = "+SIZE);

		SortValue[] values = SortValue.createArray(SIZE);
		long tStart, tStop;

		for(int k = 10000; k --> 0;)
		{
//			values = SortValue.alreadySorted(values);
//			values = SortValue.reverseSorted(values);
//			values = SortValue.uniques(values);
			values = SortValue.random(values);
//			values = SortValue.fewUnique(values, 10);
//			values = SortValue.fewUniqueBulk(values, 10);
//			values = SortValue.fewUniqueBulkAlreadySorted(values, 20);
//			values = SortValue.sawtoothForward(values, 10);
//			values = SortValue.sawtoothForwardLifted(values, 10);
//			values = SortValue.nearlySorted(values);
//			values = SortValue.init(6, 5, 1, 6, 3, 6, 1, 2, 6, 3, 5, 2, 9, 4, 4, 1); // (12.04.2011)FIXME: bug in distinctionsort
//			values = SortValue.init(0, 8, 15, 4, 13, 3, 12, 5, 9, 0, 7, 11, 13, 0, 9, 8);

			if(PRINT_INPUT) SortValue.print("input :", values);
			tStart = System.nanoTime();

//			JaSort.quicksort(values, compare);
//			JaSort.quicksortDualPivot(values, compare);

//			Arrays.sort(values, compare);
//			JaSort.mergesort(values, compare);
			XSort.sort(values, compare);
//			JaSort.sort(values, 0, SIZE, compare);
//			JaSort.parallelSort(values, compare);
//			Arrays.sort(values, 1, 998, compare);
//			JaSort.bufferSort(values, staticBuffer, compare);
//			JaSort.bufferSort(values, staticBuffer, 100, 999, compare);
//			JaSort.bufferMergesort(values, staticBuffer, compare);
//			JaSort.bufferDistinctsort(values, staticBuffer, compare);
//			JaSort.distinctsort(values, compare);
//			JaSort.distinctsort10Into(values, staticBuffer, compare);
//			System.arraycopy(staticBuffer, 0, values, 0, values.length);

//			JaSort.insertionsort2(values, compare);

			tStop = System.nanoTime();
			if(PRINT_OUTPUT) SortValue.printStable("output:", values);
			if(PRINT_TIME) System.out.println(k+" Elapsed Time:\t" + df.format(tStop - tStart));
//			if(PRINT_INPUT || PRINT_OUTPUT) System.out.println();
		}
		System.out.println("done");

	}


}
