package various;


import java.util.Comparator;

import one.microstream.X;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XList;

public class MainTestSortMulti
{
	static final String[] STRINGS = {
		"hallo"  ,
		"String" ,
		"main"   ,
		"Strings",
		"BAAAB"   ,
		"AAAAA"   ,
		"CAAAC"   ,
		"AAACA"
		
	};
	
	public static void main(final String[] args)
	{
		final XList<String> strings1 = X.List(STRINGS);
		
		System.out.println(strings1);
		System.out.println(
			strings1.copy().sort(
				(s1, s2) -> Integer.compare(s1.length(), s2.length())
			)
		);
		System.out.println(
			strings1.copy().sort(
				(s1, s2) -> Character.compare(s1.charAt(0), s2.charAt(0))
			)
		);
		System.out.println(
			strings1.copy().sort(
				XSort.chain(
					(s1, s2) -> Integer.compare(s1.length(), s2.length()),
					(s1, s2) -> Character.compare(s1.charAt(0), s2.charAt(0))
				)
			)
		);
		System.out.println(
			strings1.copy().sort(
				(s1, s2) ->
				evaluateComparisons(
					Integer.compare(s1.length(), s2.length()),
					Character.compare(s1.charAt(0), s2.charAt(0))
				)
			)
		);
		
		
	}
	
	/**
	 * (17.01.2018 TM)NOTE: This method has been removed because it is clumsy to say the least.
	 * Instead of just evaluating as many comparisons as needed, it always evaluates ALL comparisons, then
	 * packages the results in a new int[] instance which is then evaluated as far as needed.
	 * That might not sound like a big deal, but it is. sorting is a n*log(n) effort, meaning really big stuff
	 * takes overproportionally long to sort. Doing more comparisons as needed MILLIONS of times is a significant
	 * overhead that can simply be avoided with a smarter strategy.
	 * The method is kept here as a legacy reference, but deleted from the core project.
	 * ---
	 * 
	 * Sorting order evaluation helper method for source code simplification where performance is not a (big) concern.
	 * <p>
	 * Each value in the passed array represents a single comparison value (like returned by {@link Comparator}).
	 * The lower the index, the higher the sorting priority.
	 * <p>
	 * Example:
	 * <pre>
	 * return XSort.evaluateComparisons(
	 *     entity1.valueA().compareTo(entity2.valueA()),
	 *     entity1.valueB().compareTo(entity2.valueB()),
	 *     entity1.valueC().compareTo(entity2.valueC())
	 * );
	 * </pre>
	 * This can be compared to the SQL construct "ORDER BY valueA, valueB, valueC"
	 * <p>
	 * Note that with this approach, every value comparison computed is computed on every call and an additional
	 * int array is allocated, while a subsequent comparison of values aborts as soon as a non-zero value is encountered
	 * and does not allocate an additional array. Therefore this method is only viable to simplify source code if
	 * performance is no big concern (e.g. not many elements to sort, not many values to compare, fast value comparisons)
	 *
	 * @param comparisons the array containing the partial comparisons.
	 * @return the first encountered non-zero value while iterating the array starting upwards from index 0.
	 */
	public static final int evaluateComparisons(final int... comparisons)
	{
		if(comparisons != null)
		{
			for(final int i : comparisons)
			{
				if(i != 0)
				{
					return i;
				}
			}
		}
		return 0;
	}
	
}
