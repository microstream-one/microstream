package net.jadoth.test.collections;

import net.jadoth.collections.ConstList;
import net.jadoth.collections.EqHashEnum;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestConstListvsVarSetContains
{
	static final int LOOPS = 100;
	static final int OFFSET = 9;



	static final int SIZE = 10;
	static final String[] STRING_ARRAY = new String[SIZE];
	static{
		for(int i = 0; i < SIZE; i++)
		{
			STRING_ARRAY[i] = Integer.toString(i);
		}
	}




	public static void main(final String[] args)
	{
		final ConstList<String> STRING_LIST = new ConstList<>(STRING_ARRAY);
		final EqHashEnum<String> STRING_SET = EqHashEnum.New(STRING_ARRAY);
		final String searchString = STRING_ARRAY[SIZE/2];

		long tStart, tStop;
		long current = 0, sum = 0, min = Integer.MAX_VALUE;
		long currentS = 0, sumS = 0, minS = Integer.MAX_VALUE;

		for(int k = 0; k < LOOPS; k++)
		{
			tStart = System.nanoTime();
			STRING_LIST.contains(searchString);
			tStop = System.nanoTime();
			current = tStop - tStart;
			System.out.println("\t"+k+ " Const:\t" + new java.text.DecimalFormat("00,000,000,000").format(current));
			if(k > OFFSET)
			{
				sum += current;
			}
			if(current < min) min = current;

			tStart = System.nanoTime();
			STRING_SET.contains(searchString);
			tStop = System.nanoTime();
			currentS = tStop - tStart;
			System.out.println("\t"+k+ " Set  :\t" + new java.text.DecimalFormat("00,000,000,000").format(currentS));
			if(k > OFFSET)
			{
				sumS += currentS;
			}
			if(currentS < minS) minS = currentS;

			tStart = System.nanoTime();
			STRING_LIST.contains(searchString);
			tStop = System.nanoTime();
			current = tStop - tStart;
			System.out.print("\t"+k+ " Const:\t" + new java.text.DecimalFormat("00,000,000,000").format(current));
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

			tStart = System.nanoTime();
			STRING_SET.contains(searchString);
			tStop = System.nanoTime();
			currentS = tStop - tStart;
			System.out.print("\t"+k+ " Set  :\t" + new java.text.DecimalFormat("00,000,000,000").format(currentS));
			if(k > OFFSET)
			{
				sumS += currentS;
				System.out.println("\tAverage: " + new java.text.DecimalFormat("00,000,000,000").format(sumS / (k - OFFSET)));
			}
			else
			{
				System.out.println();
			}
			if(currentS < minS) minS = currentS;



			System.out.println();
		}
		System.out.println("Const Average\t: "+new java.text.DecimalFormat("00,000,000,000").format(sum / (LOOPS - OFFSET - 1)));
		System.out.println("Const Min\t: "+new java.text.DecimalFormat("00,000,000,000").format(min));
		System.out.println();
		System.out.println("Set   Average\t: "+new java.text.DecimalFormat("00,000,000,000").format(sumS / (LOOPS - OFFSET - 1)));
		System.out.println("Set   Min\t: "+new java.text.DecimalFormat("00,000,000,000").format(minS));
	}
}
