/**
 *
 */
package net.jadoth.test.lang;

import java.util.ArrayList;
import java.util.Arrays;

import net.jadoth.Jadoth;
import net.jadoth.reference.LinkReference;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestLinkedReference
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
//		testSimple();
		testPerformance();

	}

	public static <T> LinkReference<T> linkReference(final T object)
	{
		return new LinkReference.Implementation<>(object);
	}

	static void testSimple()
	{
		final LinkReference<String> stringChain1 = linkReference("A");
		stringChain1.link("B").link("C").link("D");
		System.out.println(Arrays.toString(stringChain1.toArray()));

		System.out.println("");
		final LinkReference<String> stringChain2 = Jadoth.chain("1", "2", "3", "4");
		System.out.println(Arrays.toString(stringChain2.toArray()));


		System.out.println("foreach:");
		//cool
		for(final String s : stringChain2)
		{
			System.out.println(s);
		}

		System.out.println("partial foreach:");
		//even cooler
		for(final String s : stringChain2.next())
		{
			System.out.println(s);
		}

		System.out.println("for iteration:");
		//even more cool
		for(LinkReference<String> r = stringChain1; r != null; r = r.next())
		{
			System.out.println(r.get());
		}
	}


	static void testPerformance()
	{
		final int COUNT = 100000;
		int c = COUNT;

		final ArrayList<String> arrayList = new ArrayList<>(c);
		LinkReference<String> chain = linkReference(null);
		LinkReference<String> loopChain = chain;
		while(c --> 0)
		{
			arrayList.add(Integer.toString(c));
			loopChain = loopChain.link(Integer.toString(c));
		}
		chain = chain.next(); //stupid

		String loopString = null;


//		for(int i = 0; i < COUNT; i++)
//		{
//			loopString = arrayList.get(i);
//		}
//		for(LinkedReference<String> i = chain; i != null; i = i.next())
//		{
//			loopString = i.get();
//		}
//


//		for(String s : chain) System.out.println(s);


		long tStart;
		long tStop;


		for(int n = 0; n < 10; n++)
		{
			tStart = System.nanoTime();
			for(LinkReference<String> i = chain; i != null; i = i.next())
			{
				loopString = i.get();
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time (for next): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


			tStart = System.nanoTime();
			for(final String s : chain)
			{
				loopString = s;
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time (for each): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));



			tStart = System.nanoTime();
			for(int i = 0; i < COUNT; i++)
			{
				loopString = arrayList.get(i);
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time (for list): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		}



		System.out.println(loopString);


	}

}
