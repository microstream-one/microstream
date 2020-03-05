
package one.microstream.storage.configuration;

import java.time.Duration;
import java.util.function.Function;

public enum DurationUnit
{
	NS (Duration::ofNanos  ),
	MS (Duration::ofMillis ),
	S  (Duration::ofSeconds),
	M  (Duration::ofMinutes),
	H  (Duration::ofHours  ),
	D  (Duration::ofDays   );
	
	
	private Function<Long, Duration> creator;

	private DurationUnit(final Function<Long, Duration> creator)
	{
		this.creator = creator;
	}
	
	public Duration create(final long amount)
	{
		return this.creator.apply(amount);
	}
}
