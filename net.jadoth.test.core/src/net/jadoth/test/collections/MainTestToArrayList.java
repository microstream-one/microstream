package net.jadoth.test.collections;

import static net.jadoth.collections.JadothCollections.ArrayList;
import static net.jadoth.math.JadothMath.sequence;

import java.util.ArrayList;
import java.util.List;

import net.jadoth.X;
import net.jadoth.collections.types.XList;
import net.jadoth.util.JadothTypes;

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
		System.out.println(JadothTypes.to_int(ints.size()));
		System.out.println();

		long tStart;
		long tStop;

		for(int k = 20; k --> 0;)
		{
			tStart = System.nanoTime();
			List<Integer> intArrayList = ArrayList(ints);
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
