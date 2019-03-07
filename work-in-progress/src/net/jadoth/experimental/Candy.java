/**
 * 
 */
package net.jadoth.experimental;

import net.jadoth.math.XMath;



/**
 * @author Thomas Muenz
 *
 */
public class Candy
{
	// Broken - uses floating point for monetary calculation!
	public static void main(final String[] args) 
	{
		double funds = 1.00;
		int itemsBought = 0;
		for(double price = 0.10; funds >= price; price += 0.10)
		{
			funds -= price;
			funds = XMath.round(funds, 2); //patch :-D
			itemsBought++;
		}		
		System.out.println(itemsBought + " items bought.");
		System.out.println("Change: $" + funds);
	}
}
