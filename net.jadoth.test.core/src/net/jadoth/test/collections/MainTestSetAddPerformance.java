package net.jadoth.test.collections;

import java.util.Comparator;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.functional.XFunc;
import net.jadoth.typing.XTypes;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestSetAddPerformance
{

	static final int SIZE = 1000*1000;
	static final int LOOPS = 100;
	static final int OFFSET = 9;


	static final Comparator<Integer> SORT_INTEGER = new Comparator<Integer>() {
		@Override public int compare(final Integer o1, final Integer o2) {
			return o1.intValue() < o2.intValue() ?-1 : o1.intValue() == o2.intValue() ?0 :1;
		}
	};


	static Integer[] integers = new Integer[SIZE];
	static {
		for(int i = 0; i < SIZE; i++)
		{
			integers[i] = i;
		}
	}

	public static void main(final String[] args)
	{
//		testCompleteness();
//		testPerformance();
		testIteration();
	}


	static void testIteration()
	{
//		final HashSet<Integer> ints = new HashSet<Integer>(SIZE, 1.00f);
		final EqHashEnum<Integer> ints = EqHashEnum.NewCustom(SIZE, 1.00f);
//		final VarSet<Integer> ints = new VarSet<Integer>(HashingType.IDENTITY, STRONG, SIZE, 1.00f);

		for(int i = 0; i < 10; i++)
		{
			ints.add(i);
		}

		long tStart, tStop;
		long current = 0, sum = 0, min = Integer.MAX_VALUE;
		for(int k = 0; k < LOOPS; k++)
		{
			tStart = System.nanoTime();
			ints.applies(XFunc.any());
			tStop = System.nanoTime();
			current = tStop - tStart;
//			System.out.println("VarSet.newEntry = "+VarSet.newEntry);
//			System.out.println("VarSet.setEntry = "+VarSet.setEntry);
//			System.out.println("VarSet.addEntry = "+VarSet.addEntry);
			System.out.print("\t"+k+ ": Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(current));

//			System.out.println(ints);
//			final Integer[] intArray = ints.toArray(Integer.class);
//			System.out.println("\n"+intArray.length);
//			final HashCollection.Analysis<VarSet<Integer>> analysis = ints.analyze();

			if(k > OFFSET)
			{
				sum += current;
				System.out.println("\tAverage: " + new java.text.DecimalFormat("00,000,000,000").format(sum / (k - OFFSET)));
			}
			else
			{
				System.out.println();
			}

			if(current < min) min = current;
		}
		System.out.println("Average\t: "+new java.text.DecimalFormat("00,000,000,000").format(sum / (LOOPS - OFFSET - 1)));
		System.out.println("Min\t: "+new java.text.DecimalFormat("00,000,000,000").format(min));

	}

	static void testPerformance()
	{
//		final HashSet<Integer> ints = new HashSet<Integer>(SIZE, 1.00f);
		final EqHashEnum<Integer> ints = EqHashEnum.NewCustom(SIZE, 1.00f);
//		final VarSet<Integer> ints = new VarSet<Integer>(HashingType.IDENTITY, STRONG, SIZE, 1.00f);

		long tStart, tStop;
		long current = 0, sum = 0, min = Integer.MAX_VALUE;
		for(int k = 0; k < LOOPS; k++)
		{
			ints.clear();
			tStart = System.nanoTime();
			for(final Integer i : integers)
			{
				ints.add(i);
			}
			tStop = System.nanoTime();
			current = tStop - tStart;
//			System.out.println("VarSet.newEntry = "+VarSet.newEntry);
//			System.out.println("VarSet.setEntry = "+VarSet.setEntry);
//			System.out.println("VarSet.addEntry = "+VarSet.addEntry);
			System.out.print("\t"+k+ ": Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(current));

//			System.out.println(ints);
//			final Integer[] intArray = ints.toArray(Integer.class);
//			System.out.println("\n"+intArray.length);
//			final HashCollection.Analysis<VarSet<Integer>> analysis = ints.analyze();

			if(k > OFFSET)
			{
				sum += current;
				System.out.println("\tAverage: " + new java.text.DecimalFormat("00,000,000,000").format(sum / (k - OFFSET)));
			}
			else
			{
				System.out.println();
			}

			if(current < min) min = current;
		}
		System.out.println("Average\t: "+new java.text.DecimalFormat("00,000,000,000").format(sum / (LOOPS - OFFSET - 1)));
		System.out.println("Min\t: "+new java.text.DecimalFormat("00,000,000,000").format(min));
	}

	static void testCompleteness()
	{
		final EqHashEnum<Integer> ints = EqHashEnum.NewCustom(16, 10.00f);
		for(final Integer i : integers)
		{
			ints.add(i);
		}

		final Integer[] intArray = ints.toArray(Integer.class);
		if(XTypes.to_int(ints.size()) != SIZE)
		{
			throw new RuntimeException("ints.size() "+XTypes.to_int(ints.size())+" != SIZE " + SIZE);
		}
		if(intArray.length != SIZE)
		{
			throw new RuntimeException("intArray.length "+intArray.length+" != SIZE " + SIZE);
		}
//		JaSort.mergesort(intArray, SORT_INTEGER);
		// (09.04.2011)FIXME: mergesort
		if(intArray[0] != 0)
		{
			throw new RuntimeException("intArray[0] "+intArray[0]+" != 0 ");
		}
		if(intArray[intArray.length-1] != SIZE-1)
		{
			throw new RuntimeException("intArray[intArray.length-1] "+intArray[intArray.length-1]+" !=  "+(SIZE-1));
		}

		System.out.println(intArray.length);
		System.out.println(intArray[0]);
		System.out.println(intArray[intArray.length-1]);


//		final LimitList<Integer> intList = new LimitList<Integer>(ints.toArray(Integer.class));
//		intList.sort(SORT_INTEGER);
//		System.out.println(intList.getFirst());
//		System.out.println(intList.getLast());
	}

}
