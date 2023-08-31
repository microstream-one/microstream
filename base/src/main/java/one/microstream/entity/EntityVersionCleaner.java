
package one.microstream.entity;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.X.notNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

import one.microstream.collections.types.XTable;

@FunctionalInterface
public interface EntityVersionCleaner<K>
{
	public void cleanVersions(XTable<K, Entity> versions);
	
	
	public static <K extends Comparable<? super K>> EntityVersionCleaner<K> AmountPreserving(
		final long maxPreservedVersions
	)
	{
		return new AmountPreserving<>(maxPreservedVersions, Comparator.naturalOrder());
	}
	
	public static <K> EntityVersionCleaner<K> AmountPreserving(
		final long          maxPreservedVersions,
		final Comparator<K> comparator
	)
	{
		return new AmountPreserving<>(maxPreservedVersions, comparator);
	}
	
	public static EntityVersionCleaner<Long> AgePreservingSystemTimeMillis(
		final long preservedAgeMillis
	)
	{
		return new AgePreservingSystemTimeMillis(preservedAgeMillis);
	}
	
	public static EntityVersionCleaner<Long> AgePreservingSystemNanoTime(
		final long preservedAgeNanos
	)
	{
		return new AgePreservingSystemNanoTime(preservedAgeNanos);
	}
	
	public static EntityVersionCleaner<Instant> AgePreservingInstant(
		final Duration preservedAge
	)
	{
		return new AgePreservingInstant(preservedAge);
	}
	
	
	public static class AmountPreserving<K> implements EntityVersionCleaner<K>
	{
		private final long                  maxPreservedVersions;
		private final Comparator<? super K> comparator;
		
		protected AmountPreserving(
			final long                  maxPreservedVersions,
			final Comparator<? super K> comparator
		)
		{
			super();
			
			this.maxPreservedVersions = maxPreservedVersions;
			this.comparator           = notNull(comparator);
			
		}
		
		@Override
		public void cleanVersions(final XTable<K, Entity> versions)
		{
			if(versions.size() > this.maxPreservedVersions)
			{
				versions.sort((kv1, kv2) -> this.comparator.compare(kv1.key(), kv2.key()));
				do
				{
					versions.pinch();
				}
				while(versions.size() > this.maxPreservedVersions);
			}
		}
		
	}
	
	public static class AgePreservingSystemTimeMillis implements EntityVersionCleaner<Long>
	{
		private final long preservedAgeMillis;
		
		protected AgePreservingSystemTimeMillis(final long preservedAgeMillis)
		{
			super();
			this.preservedAgeMillis = preservedAgeMillis;
		}
		
		@Override
		public void cleanVersions(final XTable<Long, Entity> versions)
		{
			final long minAge = System.currentTimeMillis() - this.preservedAgeMillis;
			versions.removeBy(kv -> kv.key() < minAge);
		}
		
	}
	
	public static class AgePreservingSystemNanoTime implements EntityVersionCleaner<Long>
	{
		private final long preservedAgeNanos;
		
		protected AgePreservingSystemNanoTime(final long preservedAgeNanos)
		{
			super();
			this.preservedAgeNanos = preservedAgeNanos;
		}
		
		@Override
		public void cleanVersions(final XTable<Long, Entity> versions)
		{
			final long minAge = System.nanoTime() - this.preservedAgeNanos;
			versions.removeBy(kv -> kv.key() < minAge);
		}
		
	}
	
	public static class AgePreservingInstant implements EntityVersionCleaner<Instant>
	{
		private final Duration preservedAge;
		
		protected AgePreservingInstant(final Duration preservedAge)
		{
			super();
			this.preservedAge = preservedAge;
		}
		
		@Override
		public void cleanVersions(final XTable<Instant, Entity> versions)
		{
			final Instant minAge = Instant.now().minus(this.preservedAge);
			versions.removeBy(kv -> kv.key().isBefore(minAge));
		}
		
	}
	
}
