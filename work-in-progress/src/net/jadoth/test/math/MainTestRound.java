package net.jadoth.test.math;

public class MainTestRound
{
	public static void main(final String[] args)
	{
		System.out.println(round(1.580000001, 3));
	}

	public static final double round(final double value, int decimals)
	{
		if(decimals < 0)
		{
			throw new IllegalArgumentException("No negative values allowed for decimals: "+decimals);
		}

	    // no idea if 323 is the best choice, tbh. At least it's a check for decimals values like million etc.
	    if(decimals > 323)
	    {
	        throw new IllegalArgumentException("decimals value out of range: "+decimals);
	    }

		// inlined pow(double, int) without checks
		double factor = 1.0d;
		while(decimals --> 0)
		{
			factor *= 10.0d;
		}
		return StrictMath.floor(value*factor + 0.5d)/factor;
	}

}
