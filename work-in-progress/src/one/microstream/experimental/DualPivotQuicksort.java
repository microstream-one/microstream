package one.microstream.experimental;

/**
 * Dual Pivot Quicksort
 * source:
 * http://iaroslavski.narod.ru/quicksort/DualPivotQuicksort.java
 * http://article.gmane.org/gmane.comp.java.openjdk.core-libs.devel/2628
 *
 */
public class DualPivotQuicksort
{
	public static void quicksortDualPivot(final int[] values)
	{
		dualPivotQuicksort(values, 0, values.length - 1, true);
	}

	private static void dualPivotQuicksort(final int[] a, final int low, final int high, final boolean leftmost)
	{
		final int length = high - low + 1;

		// Use insertion sort on tiny arrays
		if(length < 32)
		{
			if(!leftmost)
			{
				/*
				 * Every element in adjoining part plays the role
				 * of sentinel, therefore this allows us to avoid
				 * the j >= left check on each iteration.
				 */
				for(int j, i = low + 1; i <= high; i++)
				{
					final int ai = a[i];
					for(j = i - 1; ai < a[j]; j--)
					{
						// assert j >= left;
						a[j + 1] = a[j];
					}
					a[j + 1] = ai;
				}
			} else {
				/*
				 * For case of leftmost part traditional (without a sentinel)
				 * insertion sort, optimized for server JVM, is used.
				 */
				for(int i = low, j = i; i < high; j = ++i)
				{
					final int ai = a[i + 1];
					while(ai < a[j])
					{
						a[j + 1] = a[j];
						if(j-- == low)
						{
							break;
						}
					}
					a[j + 1] = ai;
				}
			}
			return;
		}

		// Inexpensive approximation of length / 7
		final int seventh = (length >>> 3) + (length >>> 6) + 1;

		/*
		 * Sort five evenly spaced elements around (and including) the
		 * center element in the range. These elements will be used for
		 * pivot selection as described below. The choice for spacing
		 * these elements was empirically determined to work well on
		 * a wide variety of inputs.
		 */
		final int e3 = low + high >>> 1; // The midpoint
		final int e2 = e3 - seventh;
		final int e1 = e2 - seventh;
		final int e4 = e3 + seventh;
		final int e5 = e4 + seventh;

		// Sort these elements using insertion sort
		if (a[e2] < a[e1]) { final int t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

		if(a[e3] < a[e2])
		{
			final int t = a[e3]; a[e3] = a[e2]; a[e2] = t;
			if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
		}
		if(a[e4] < a[e3])
		{
			final int t = a[e4]; a[e4] = a[e3]; a[e3] = t;
			if(t < a[e2])
			{
				a[e3] = a[e2]; a[e2] = t;
				if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
			}
		}
		if(a[e5] < a[e4])
		{
			final int t = a[e5]; a[e5] = a[e4]; a[e4] = t;
			if(t < a[e3])
			{
				a[e4] = a[e3]; a[e3] = t;
				if(t < a[e2])
				{
					a[e3] = a[e2]; a[e2] = t;
					if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
				}
			}
		}

		/*
		 * Use the second and fourth of the five sorted elements as pivots.
		 * These values are inexpensive approximations of the first and
		 * second terciles of the array. Note that pivot1 <= pivot2.
		 */
		final int pivot1 = a[e2];
		final int pivot2 = a[e4];

		// Pointers
		int less  = low;  // The index of the first element of center part
		int great = high; // The index before the first element of right part

		if(pivot1 != pivot2)
		{
			/*
			 * The first and the last elements to be sorted are moved to the
			 * locations formerly occupied by the pivots. When partitioning
			 * is complete, the pivots are swapped back into their final
			 * positions, and excluded from subsequent sorting.
			 */
			a[e2] = a[low];
			a[e4] = a[high];

			/*
			 * Skip elements, which are less or greater than pivot values.
			 */
			while (a[++less] < pivot1){/**/}
			while (a[--great] > pivot2){/**/}

			/*
			 * Partitioning:
			 *
			 *   left part           center part                   right part
			 * +--------------------------------------------------------------+
			 * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
			 * +--------------------------------------------------------------+
			 *               ^                          ^       ^
			 *               |                          |       |
			 *              less                        k     great
			 *
			 * Invariants:
			 *
			 *              all in (left, less)   < pivot1
			 *    pivot1 <= all in [less, k)     <= pivot2
			 *              all in (great, right) > pivot2
			 *
			 * Pointer k is the first index of ?-part.
			 */
			outer:
				for(int k = less; k <= great; k++)
				{
					final int ak = a[k];
					if (ak < pivot1) { // Move a[k] to left part
						a[k] = a[less];
						a[less] = ak;
						less++;
					} else if (ak > pivot2) { // Move a[k] to right part
						while(a[great] > pivot2)
						{
							if(great-- == k)
							{
								break outer;
							}
						}
						if(a[great] < pivot1)
						{
							a[k] = a[less];
							a[less] = a[great];
							less++;
						} else { // pivot1 <= a[great] <= pivot2
							a[k] = a[great];
						}
						a[great] = ak;
						great--;
					}
				}

			// Swap pivots into their final positions
			a[low]  = a[less  - 1]; a[less  - 1] = pivot1;
			a[high] = a[great + 1]; a[great + 1] = pivot2;

			// Sort left and right parts recursively, excluding known pivots
			dualPivotQuicksort(a, low, less - 2, leftmost);
			dualPivotQuicksort(a, great + 2, high, false);

			/*
			 * If center part is too large (comprises > 5/7 of the array),
			 * swap internal pivot values to ends.
			 */
			if(less < e1 && e5 < great)
			{
				/*
				 * Skip elements, which are equal to pivot values.
				 */
				while(a[less] == pivot1)
				{
					less++;
				}
				while(a[great] == pivot2)
				{
					great--;
				}

				/*
				 * Partitioning:
				 *
				 *   left part         center part                  right part
				 * +----------------------------------------------------------+
				 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
				 * +----------------------------------------------------------+
				 *              ^                        ^       ^
				 *              |                        |       |
				 *             less                      k     great
				 *
				 * Invariants:
				 *
				 *              all in (*,  less) == pivot1
				 *     pivot1 < all in [less,  k)  < pivot2
				 *              all in (great, *) == pivot2
				 *
				 * Pointer k is the first index of ?-part.
				 */
				outer:
					for(int k = less; k <= great; k++)
					{
						final int ak = a[k];
						if (ak == pivot1) { // Move a[k] to left part
							a[k] = a[less];
							a[less] = ak;
							less++;
						} else if (ak == pivot2) { // Move a[k] to right part
							while(a[great] == pivot2)
							{
								if(great-- == k)
								{
									break outer;
								}
							}
							if(a[great] == pivot1)
							{
								a[k] = a[less];
								/*
								 * Even though a[great] equals to pivot1, the
								 * assignment a[less] = pivot1 may be incorrect,
								 * if a[great] and pivot1 are floating-point zeros
								 * of different signs. Therefore in float and
								 * double sorting methods we have to use more
								 * accurate assignment a[less] = a[great].
								 */
								a[less] = pivot1;
								less++;
							} else { // pivot1 < a[great] < pivot2
								a[k] = a[great];
							}
							a[great] = ak;
							great--;
						}
					}
			}

			// Sort center part recursively
			dualPivotQuicksort(a, less, great, false);

		} else { // Pivots are equal
			/*
			 * Partition degenerates to the traditional 3-way
			 * (or "Dutch National Flag") schema:
			 *
			 *   left part    center part              right part
			 * +-------------------------------------------------+
			 * |  < pivot  |   == pivot   |     ?    |  > pivot  |
			 * +-------------------------------------------------+
			 *              ^              ^        ^
			 *              |              |        |
			 *             less            k      great
			 *
			 * Invariants:
			 *
			 *   all in (left, less)   < pivot
			 *   all in [less, k)     == pivot
			 *   all in (great, right) > pivot
			 *
			 * Pointer k is the first index of ?-part.
			 */
			for(int k = low; k <= great; k++)
			{
				if(a[k] == pivot1)
				{
					continue;
				}
				final int ak = a[k];

				if (ak < pivot1) { // Move a[k] to left part
					a[k] = a[less];
					a[less] = ak;
					less++;
				} else { // a[k] > pivot1 - Move a[k] to right part
					/*
					 * We know that pivot1 == a[e3] == pivot2. Thus, we know
					 * that great will still be >= k when the following loop
					 * terminates, even though we don't test for it explicitly.
					 * In other words, a[e3] acts as a sentinel for great.
					 */
					while(a[great] > pivot1)
					{
						// assert great > k;
						great--;
					}
					if(a[great] < pivot1)
					{
						a[k] = a[less];
						a[less] = a[great];
						less++;
					} else { // a[great] == pivot1
						/*
						 * Even though a[great] equals to pivot1, the
						 * assignment a[k] = pivot1 may be incorrect,
						 * if a[great] and pivot1 are floating-point
						 * zeros of different signs. Therefore in float
						 * and double sorting methods we have to use
						 * more accurate assignment a[k] = a[great].
						 */
						a[k] = pivot1;
					}
					a[great] = ak;
					great--;
				}
			}

			// Sort left and right parts recursively
			dualPivotQuicksort(a, low, less - 1, leftmost);
			dualPivotQuicksort(a, great + 1, high, false);
		}
	}
}
