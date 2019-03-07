

import one.microstream.chars.VarString;



public class MaintestStringPerformance
{
	static final int SIZE = 10000;
	static final int LEN = 100;

	public static void main(final String[] args)
	{
//		StringBuilder vc = new StringBuilder(LEN);
		VarString vc = VarString.New(LEN);
		final char ch = 'a';


		long tStart;
		long tStop;

		for(int k = 1000; k-->0;k++)
		{
			tStart = System.nanoTime();
			for(int j = 0; j < SIZE; j++)
			{
//				vc = new StringBuilder(LEN);
//				vc.clear();
				vc = VarString.New(LEN);
				for(int i = 0; i < LEN; i++)
				{
					vc.append(ch);
				}
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}
}
