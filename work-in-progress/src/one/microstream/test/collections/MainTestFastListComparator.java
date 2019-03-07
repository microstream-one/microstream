package one.microstream.test.collections;

import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.functional.XFunc;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestFastListComparator
{
	private static final int SIZE = 1000;




	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final BulkList<String> ints = new BulkList<>(SIZE);
		for(int i = 0; i < SIZE; i++)
		{
			ints.add(Integer.toString(i));
		}

//		final Predicate<String> isLast = new IsLast();
//		class IsLast implements Predicate<String>
//		{
//			@Override public boolean apply(final String t)
//			{
//				return t.equals("999");
//			}
//		}

		final Predicate<String> isLast = new Predicate<String>(){
			@Override public final boolean test(final String t){
				return t.equals("999");
			}
		};



		final String LAST = "999";
		long indexOfLast = 0;


		long tStart;
		long tStop;


		for(int i = 10; i --> 0;)
		{
			tStart = System.nanoTime();
			indexOfLast = ints.indexBy(XFunc.isEqualTo(LAST));
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		System.out.println(indexOfLast);




		for(int i = 10; i --> 0;)
		{
			tStart = System.nanoTime();
			indexOfLast = ints.indexBy(isLast);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		System.out.println(indexOfLast);




	}

}
