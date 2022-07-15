package one.microstream.hashing;

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
import static one.microstream.math.XMath.notNegative;
import static one.microstream.math.XMath.positive;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingTable;
import one.microstream.math.XMath;

public interface HashStatisticsBucketBased extends HashStatistics
{
	public default double averageBucketLength()
	{
		return (double)this.elementCount() / this.hashLength();
	}
	
	public float hashDensity();
	
	public long highestBucketLength();
	
	public XGettingTable<Long, Long> bucketLengthDistribution();
	
	
	
	public static HashStatisticsBucketBased New(
		final long                      hashLength              ,
		final long                      elementCount            ,
		final float                     hashDensity             ,
		final long                      highestBucketLength     ,
		final XGettingTable<Long, Long> bucketLengthDistribution
	)
	{
		return new HashStatisticsBucketBased.Default(
			   positive(hashLength)              ,
			notNegative(elementCount)            ,
			            hashDensity              ,
			notNegative(highestBucketLength)     ,
			    notNull(bucketLengthDistribution)
		);
	}
	
	public final class Default implements HashStatisticsBucketBased
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long                      hashLength              ;
		private final long                      elementCount            ;
		private final float                     hashDensity             ;
		private final long                      highestBucketLength     ;
		private final XGettingTable<Long, Long> bucketLengthDistribution;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final long                      hashLength              ,
			final long                      elementCount            ,
			final float                     hashDensity             ,
			final long                      highestBucketLength     ,
			final XGettingTable<Long, Long> bucketLengthDistribution
		)
		{
			super();
			this.hashLength               = hashLength              ;
			this.elementCount             = elementCount            ;
			this.hashDensity              = hashDensity             ;
			this.highestBucketLength      = highestBucketLength     ;
			this.bucketLengthDistribution = bucketLengthDistribution;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long hashLength()
		{
			return this.hashLength;
		}
		
		@Override
		public final long elementCount()
		{
			return this.elementCount;
		}
		
		@Override
		public final float hashDensity()
		{
			return this.hashDensity;
		}
		
		@Override
		public final long highestBucketLength()
		{
			return this.highestBucketLength;
		}
		
		@Override
		public final XGettingTable<Long, Long> bucketLengthDistribution()
		{
			return this.bucketLengthDistribution;
		}
		
		@Override
		public final String toString()
		{
			return VarString.New()
				.add("hashLength = ")             .add(this.hashLength).lf()
				.add("elementCount = ")           .add(this.elementCount).lf()
				.add("averageBucketLength = ")    .add(XMath.round2(this.averageBucketLength())).lf()
				.add("hashDensity = ")            .add(this.hashDensity).lf()
				.add("highestBucketLength = ")    .add(this.highestBucketLength).lf()
				.add("bucketLengthDistribution: ").lf()
				.apply(v ->
					this.bucketLengthDistribution.iterate(e ->
						v.add(e.key()).add(": ").add(e.value()).lf()
					)
				)
				.toString()
			;
		}
		
	}
	
}
