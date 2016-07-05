package net.jadoth.test.collections;

import net.jadoth.collections.BulkList;
import net.jadoth.functional.JadothEqualators;
import net.jadoth.util.Equalator;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestFastListEquality
{
//	private static final Comparator<String> IS_EQUAL = new Comparator<String>(){
//		public int compare(final String o1, final String o2) {
//			if(o1 == null)
//			{
//				return o2 == null ?0 :-1;
//			}
//			return o1.equals(o2) ?0 :1;
//		}
//	};

	static final Equalator<String> EQUAL_STRING = JadothEqualators.equality(String.class);


	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final BulkList<String> strings1 = BulkList.New("A", "B", "C");
		final BulkList<String> strings2 = BulkList.New("A", "B", "C");

		System.out.println(strings1.equalsContent(strings2, EQUAL_STRING));

	}

}
