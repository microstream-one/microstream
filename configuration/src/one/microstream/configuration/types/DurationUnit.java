
package one.microstream.configuration.types;

import java.time.Duration;
import java.util.function.Function;

/**
 * Enumeration of time durations at a given unit of granularity.
 * 
 *
 */
public enum DurationUnit
{
	/**
	 * Nanoseconds
	 */
	NS (Duration::ofNanos  ),
	
	/**
	 * Milliseconds
	 */
	MS (Duration::ofMillis ),
	
	/**
	 * Seconds
	 */
	S  (Duration::ofSeconds),
	
	/**
	 * Minutes
	 */
	M  (Duration::ofMinutes),
	
	/**
	 * Hours
	 */
	H  (Duration::ofHours  ),
	
	/**
	 * Days
	 */
	D  (Duration::ofDays   );
	
	
	private Function<Long, Duration> creator;

	private DurationUnit(
		final Function<Long, Duration> creator
	)
	{
		this.creator = creator;
	}
	
	public Duration create(final long amount)
	{
		return this.creator.apply(amount);
	}
	
}
