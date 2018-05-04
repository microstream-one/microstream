package net.jadoth.functional;

import net.jadoth.math.XMath;

public final class MainTestFunc
{
	static final int SIZE = 1_000_000;
	
	public static void main(final String[] args)
	{
		while(true)
		{
			doit();
			System.gc();
		}
	}
	
	
	static final void doit()
	{
		final Object[] obs = new Object[SIZE];
		
		final long tStart = System.nanoTime();
		for(int i = 0; i < SIZE; i++)
		{
//			obs[i] = JadothFunctional.all();
			obs[i] = XFunc.passThrough();
		}
		final long tStop = System.nanoTime();
		System.out.println(new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart) + " " + obs[XMath.random(SIZE)]);
	}
}
