package net.jadoth.test.collections;


import net.jadoth.collections.BulkList;
import net.jadoth.collections.JadothSort;
import net.jadoth.experimental.SortedList;

/**
 * @author Thomas Muenz
 *
 */
@SuppressWarnings("deprecation")
public class MainTestSortedList
{

	static final SortedList<Integer> sList = new SortedList<>(new BulkList<Integer>(), JadothSort::compare);



	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		add(4);
		add(7);
		add(1);
		add(2);
		add(8);
		add(1);
		add(4);
		add(5);
		add(17);
	}



	static void add(final Integer i)
	{
		System.out.println("Adding "+i+":");
		sList.add(i);
		System.out.println(sList);

	}

}
