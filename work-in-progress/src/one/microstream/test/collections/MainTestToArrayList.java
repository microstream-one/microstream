package one.microstream.test.collections;

import static one.microstream.math.XMath.sequence;

import java.util.ArrayList;
import java.util.List;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.collections.types.XList;
import one.microstream.typing.XTypes;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestToArrayList
{
	static final int SIZE = 100;
	static final Integer[] intArray = sequence((Integer)(SIZE-1));

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final XList<Integer> ints = X.List(intArray);
		System.out.println(XTypes.to_int(ints.size()));
		System.out.println();

		long tStart;
		long tStop;

		for(int k = 20; k --> 0;)
		{
			tStart = System.nanoTime();
			List<Integer> intArrayList = OldCollections.ArrayList(ints);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


			tStart = System.nanoTime();
			intArrayList = new ArrayList<>(intArrayList);
			tStop = System.nanoTime();
			System.out.println("* Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.out.println(intArrayList.size());
		}

	}

}
