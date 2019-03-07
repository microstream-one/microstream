package one.microstream.test.math;

import static one.microstream.math.XMath.round;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestDoubleRoundCandy
{

	public static void main(final String[] args)
	{
//		blochsMain(args); // Broken - uses floating point for monetary calculation!
		fixedMain(args); // Fixed - used round() in each iteration for normalization!
	}


	// Fixed - used round() in each iteration for normalization!
	public static void fixedMain(final String[] args)
	{
		double funds = 1.00;
		int itemsBought = 0;
		for(double price = .10; funds >= price; price += .10)
		{
			funds -= price;
			funds = round(funds, 2);
			itemsBought++;
		}
		System.out.println(itemsBought + " items bought.");
		System.out.println("Change: $" + funds);
	}



	// Broken - uses floating point for monetary calculation!
	public static void blochsMain(final String[] args)
	{
		double funds = 1.00;
		int itemsBought = 0;
		for(double price = .10; funds >= price; price += .10)
		{
			funds -= price;
			itemsBought++;
		}
		System.out.println(itemsBought + " items bought.");
		System.out.println("Change: $" + funds);
	}

}
