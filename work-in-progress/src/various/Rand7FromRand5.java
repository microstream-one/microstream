package various;


import one.microstream.math.XMath;

public class Rand7FromRand5
{
	static final int SIZE = 70000;
	
	public static void main(final String[] args)
	{
		test();
//		ref();
	}	
	
	static void test()
	{
		final int[] counts = new int[7];
		for(int i = SIZE; i --> 0;)
		{
			counts[rand7()]++;
		}
		print(counts);
	}
	
	static void ref()
	{
		final int[] refs = new int[7];
		for(int i = SIZE; i --> 0;)
		{
			refs[XMath.random(7)]++;
		}
		print(refs);
	}
	
	
	static int rand5()
	{
		return XMath.random(5);
	}
	
	static int rand7()
	{
		return (7*(rand5()+1) + rand5() - 5) / 5;
	}
	
	
	
	
	static void print(final int[] counts)
	{
		System.out.println();
		for(int i = 0; i < counts.length; i++)
		{
			System.out.println(i+"\t"+counts[i]);
		}
	}
	
}
