package net.jadoth.test.collections;

import static net.jadoth.math.JadothMath.sequence;

import net.jadoth.experimental.RingQueue;

public class MainTestQueue
{
	static final int SIZE = 10;
	static final int[] ints = sequence(SIZE-1);


	public static void main(final String[] args)
	{
		final RingQueue<Integer> q = new RingQueue<>(8);


		for(int i = 0; i < SIZE; i++)
		{
			q.add(i);
			printInts();
			q.printStorage();
			System.out.println();
		}

		System.out.println("------------------------------------------------------------------------------------");

		for(int i = 0; i < SIZE*2; i++)
		{
			System.out.println("retrieved: "+q.poll()+" (g = "+q.getGetIndex()+", s = "+q.getSetIndex()+")");
			printInts();
			q.printStorage();
			System.out.println();
		}

		for(int i = 0; i < SIZE+10; i++)
		{
			q.add(i+10);
			System.out.println("put: "+i+10+" (g = "+q.getGetIndex()+", s = "+q.getSetIndex()+")");
			printInts();
			q.printStorage();
			System.out.println();
		}
	}



	static void printInts()
	{
		for(final int i : ints)
		{
			System.out.print('\t');
			System.out.print(i);
		}
		System.out.println();
	}
}
