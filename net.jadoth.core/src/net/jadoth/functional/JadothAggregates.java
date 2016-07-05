package net.jadoth.functional;

import net.jadoth.collections.functions.AggregateCount;


public final class JadothAggregates
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static final Aggregator<Integer, Integer> max(final int initialValue)
	{
		return new MaxInteger(initialValue);
	}

	public static final <E> AggregateCount<E> count()
	{
		return new AggregateCount<>();
	}




	public static final class MaxInteger implements Aggregator<Integer, Integer>
	{
		private int max;

		public MaxInteger(final int max)
		{
			super();
			this.max = max;
		}

		@Override
		public final void accept(final Integer value)
		{
			if(value != null && value > this.max)
			{
				this.max = value;
			}
		}

		@Override
		public final Integer yield()
		{
			return this.max;
		}

	}



	private JadothAggregates()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
