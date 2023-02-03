
package one.microstream.configuration.types;

/*-
 * #%L
 * microstream-configuration
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.time.Duration;
import java.util.function.Function;

/**
 * Enumeration of time durations at a given unit of granularity.
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
