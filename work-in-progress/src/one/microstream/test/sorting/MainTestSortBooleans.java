package one.microstream.test.sorting;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestSortBooleans
{
	static final int SIZE = 200;
	static final boolean[] booleans = new boolean[SIZE];
	static {
		for(int i = 0; i < SIZE; i++)
		{
			booleans[i] = (System.nanoTime() & 1L) > 0;
		}
	}


	public static void main(final String[] args)
	{
		print(booleans);
//		JaSort.quicksort(booleans);
		print(booleans);
	}


	static void print(final boolean[] booleans)
	{
		for(final boolean b : booleans)
		{
			System.out.print(b ? '1' : '0');
		}
		System.out.println();
	}

}
