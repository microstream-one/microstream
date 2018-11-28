package net.jadoth.hashing;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.notNegative;
import static net.jadoth.math.XMath.positive;

import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.math.XMath;

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
		return new HashStatisticsBucketBased.Implementation(
			   positive(hashLength)              ,
			notNegative(elementCount)            ,
			            hashDensity              ,
			notNegative(highestBucketLength)     ,
			    notNull(bucketLengthDistribution)
		);
	}
	
	public final class Implementation implements HashStatisticsBucketBased
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
		
		Implementation(
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
