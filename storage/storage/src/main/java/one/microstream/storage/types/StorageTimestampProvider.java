package one.microstream.storage.types;

import java.util.concurrent.atomic.AtomicLong;

/*-
 * #%L
 * microstream-storage
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

public interface StorageTimestampProvider
{
	/**
	 * Provides the current timestamp in nanosecond precision but not necessarily in nanosecond accuracy.
	 * However, it is guaranteed that subsequent calls of this method never return an equal or lower value.
	 * 
	 * @return a strictly monotone increasing timestamp with nanosecond precision.
	 */
	public long currentNanoTimestamp();
	
	/**
	 * Set the base value used to create the timestamps
	 * Implementations are allowed to ignore it.
	 * 
	 * @param base base value for timestamp creation.
	 * @return base value for timestamp creation.
	 */
	public long set(long base);
	
	
	public final class Default implements StorageTimestampProvider
	{
		private long lastTimeMillis, currentOffset;

		@Override
		public synchronized long currentNanoTimestamp()
		{
			final long currentTimeMillis;
			if((currentTimeMillis = System.currentTimeMillis()) == this.lastTimeMillis)
			{
				return Storage.millisecondsToNanoseconds(currentTimeMillis) + ++this.currentOffset;
			}
			// a read and check every time is faster than an (almost always unnecessary) write every time.
			if(this.currentOffset != 0)
			{
				this.currentOffset = 0;
			}
			return Storage.millisecondsToNanoseconds(this.lastTimeMillis = currentTimeMillis);
		}

		
		/**
		 * This implementation ignores the offset.
		 * 
		 * @param offset ignored by this implementation.
		 * @return always zero.
		 */
		@Override
		public long set(final long offset)
		{
			return 0;
		}
		
	}
	
	/**
	 * Returns a timestamp based upon the current system time.
	 * If the current system time is less or equal to the last returned value
	 * the last returned timestamp plus 1 nanosecond will be returned to archive
	 * a strictly monotone behavior.
	 */
	public final class MonotonicTime implements StorageTimestampProvider
	{
		private long lastTimeNanos;
		
		/**
		 * Returns a timestamp based upon the current system time.
		 * If the current system time is less or equal to the last returned value
		 * the last returned timestamp plus 1 nanosecond will be returned to archive
		 * a strictly monotone behavior.
		 */
		@Override
		public synchronized long currentNanoTimestamp()
		{
			final long currentTimeNanos = Storage.millisecondsToNanoseconds(System.currentTimeMillis());
			
			if(currentTimeNanos <= this.lastTimeNanos)
			{
				return ++this.lastTimeNanos;
			}
			return this.lastTimeNanos = currentTimeNanos;
		}
		
		/**
		 * Set to new base value only if the new value is larger then the current one.
		 */
		@Override
		public synchronized long set(final long timeNs)
		{
			if(timeNs > this.lastTimeNanos)
			{
				this.lastTimeNanos = timeNs;
			}
			return this.lastTimeNanos;
		}
	}
	
	/**
	 * An implementation of {@link StorageTimestampProvider} that provides an strictly monotonic increasing
	 * long value instead of a time value. This implementation does not rely on any time based value
	 * that might be affected by changes of the system clock.
	 * 
	 */
	public final class MonotonicCounter implements StorageTimestampProvider {
		
		private final AtomicLong lastValue = new AtomicLong();
		
		/**
		 * Provides an strictly monotonic increasing
		 * long value starting from the set base value {@link #set(long)} instead of a time based value.
		 * 
		 * @return a strictly monotone increasing long value.
		 */
		@Override
		public long currentNanoTimestamp()
		{
			return this.lastValue.incrementAndGet();
		}

		/**
		 * Set to new base value only if the new value is larger then the current one.
		 */
		@Override
		public long set(final long base)
		{
			return this.lastValue.updateAndGet((v) -> (base > v) ? base : v);
		}
		
	}
	
}
