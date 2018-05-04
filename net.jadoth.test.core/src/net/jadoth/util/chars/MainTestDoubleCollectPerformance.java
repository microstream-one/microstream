package net.jadoth.util.chars;

import net.jadoth.chars.JadothChars;
import net.jadoth.chars.VarString;

public class MainTestDoubleCollectPerformance
{
	private static final int SIZE = 1_000_000;
	private static final int RUNS = 20;


	public static void main(final String[] args)
	{
		System.out.println(Boolean.FALSE);

		final double[] values = new double[SIZE];
		for(int i = 0; i < values.length; i++)
		{
//			values[i] = Math.random() * 1_000_000;
//			values[i] = Math.random() * 5_000;
			values[i] = Math.random();
//			values[i] = JaMath.round(Math.random(), 3);
//			values[i] = JaMath.round(Math.random(), 6);
//			values[i] = JaMath.round(Math.random(), 8);
//			values[i] = JaMath.round(Math.random(), 9);
//			values[i] = Math.random() * 5E200;
//			values[i] = Math.random() / 100;
//			values[i] = Math.random() / 1000;
//			values[i] = Math.random() / 1E290;
		}

		final VarString vs = VarString.New(JadothChars.maxCharCount_double() * SIZE);

		System.out.println("-----------JaChars.putValue(double)--------------");
		for(int r = RUNS; r --> 0;)
		{
			vs.reset();
			final long tStart = System.nanoTime();
			for(int i = 0; i < values.length; i++)
			{
				vs.add(values[i]);
			}
			final long tStop = System.nanoTime();
			System.out.println(vs.last()+" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

		System.out.println("-----------Double.toString(double)--------------");
		for(int r = RUNS; r --> 0;)
		{
			vs.reset();
			final long tStart = System.nanoTime();
			for(int i = 0; i < values.length; i++)
			{
				vs.add(Double.toString(values[i]));
			}
			final long tStop = System.nanoTime();
			System.out.println(vs.last()+" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

	}
}
