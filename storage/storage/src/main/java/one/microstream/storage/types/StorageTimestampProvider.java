package one.microstream.storage.types;

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
		
	}
}
