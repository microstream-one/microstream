package net.jadoth.test.sorting;

import net.jadoth.collections.JadothSort;

public class MainTestDistinctionSort
{
	static final int SIZE = 1000;
	static final int THRESHOLD = (int)Math.sqrt(SIZE)+1;



	static void testDistinctionSort()
	{
		System.out.println("THRESHOLD = "+THRESHOLD);

//		final int[] values = {2,2,2,2, 3,3,3,3, 1,1,1,1, 4,4,4,4, 5,5,5,5};
//		final int[] values = {2, 2, 1, 2, 2, 1, 3, 2, 2, 1, 4, 2, 3, 1, 3, 1};
//		final int[] values = {3, 3, 2, 1, 2, 3, 1, 1, 2, 1};
//		final int[] values = {1, 1, 1, 2, 2, 2, 0, 0, 0, 1};
//		final int[] values = {3, 4, 1, 1, 2, 4, 4, 4, 2, 2};

//		final int[] values = Sort.fewUnique(SIZE, THRESHOLD);
//		final int[] values = Sort.randomUniques(SIZE);
//		Sort.print("input :", values);
		System.out.println();

		System.out.println("sorting:");
//		System.out.println(DistinctionSort.distinctionsort(values, SIZE));

		for(int k = 100; k --> 0;)
		{
			final int[] values = Sort.fewUnique(SIZE, THRESHOLD);
//			Sort.print("input :", values);
			JadothSort.bufferDistinctsort(values, new int[values.length]);
//			JaSort.bufferDistinctSortOld(values, new int[values.length], values.length);
//			JaSort.bufferDistinctSort(values, new int[values.length], values.length);
//			JaSort.bufferDistinctSort(values, new int[values.length]);
			Sort.print("output:", values);
		}

//		JaSort.bufferDistinctSort(values, new int[values.length]);
//		Sort.print("output:", values);
		System.out.println();

//		System.out.println("resorting:");
//		JaSort.distinctionsort(values, SIZE);
//		Sort.print("output: ", values);

//		System.out.println("insertion resort:");
//		JaSort.insertionsortC(values);
//		Sort.print("output: ", values);
	}


	public static void main(final String[] args)
	{
		testDistinctionSort();

	}

}
