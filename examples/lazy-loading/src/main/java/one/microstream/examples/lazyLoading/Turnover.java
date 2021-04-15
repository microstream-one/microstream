/**
 *
 */

package one.microstream.examples.lazyLoading;

import java.time.Instant;


/**
 * @author FlorianHabermann
 *
 */
public class Turnover
{
	private final double  amount;
	private final Instant timestamp;
	
	public Turnover(final double amount, final Instant timestamp)
	{
		super();
		this.amount    = amount;
		this.timestamp = timestamp;
	}
	
	public double getAmount()
	{
		return this.amount;
	}

	public Instant getTimestamp()
	{
		return this.timestamp;
	}
}
