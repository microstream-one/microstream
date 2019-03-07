package one.microstream.util.matching;

import one.microstream.collections.BulkList;
import one.microstream.collections.ConstList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.functional.XFunc;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


public interface MultiMatchResult<E>
{
	public int matchCount();

	public XGettingSequence<E> inputSources();

	public XGettingSequence<E> inputTargets();

	public XGettingSequence<KeyValue<E, E>> matchesInSourceOrder();

	public XGettingSequence<KeyValue<E, E>> matchesInTargetOrder();

	public XGettingSequence<E> remainingSources();

	public XGettingSequence<E> remainingTargets();

	public XGettingSequence<E> unmatchedSources();

	public XGettingSequence<E> unmatchedTargets();

	public XGettingSequence<KeyValue<E, E>> sourceMatches();

	public XGettingSequence<KeyValue<E, E>> targetMatches();

	public XGettingList<E> matchedSources();

	public XGettingList<E> matchedTargets();
	
	
	
	public class Implementation<E> implements MultiMatchResult<E>
	{
		///////////////////////////////////////////////////////////////////////////
		//  static methods  //
		/////////////////////

		static <T> ConstList<T> collectRemaining(final ConstList<T> input, final ConstList<KeyValue<T, T>> matches)
		{
			final BulkList<T> remaining = new BulkList<>(XTypes.to_int(input.size()));
			input.iterateIndexed((final T e, final long index) ->
			{
				remaining.add(matches.at(index) == null ? e : null);
			});
			
			return remaining.immure();
		}

		static <T> ConstList<T> collectUnmatched(final ConstList<T> input, final ConstList<KeyValue<T, T>> matches)
		{
			final BulkList<T> unmatched = new BulkList<>(XTypes.to_int(input.size()));
			input.iterateIndexed((final T e, final long index) ->
			{
				if(matches.at(index) == null)
				{
					unmatched.add(e);
				}
			});
			
			return unmatched.immure();
		}

		static <T> ConstList<T> collectMatched(final ConstList<T> input, final ConstList<KeyValue<T, T>> matches)
		{
			final BulkList<T> matched = new BulkList<>(XTypes.to_int(input.size()));
			input.iterateIndexed((final T e, final long index) ->
			{
				if(matches.at(index) != null)
				{
					matched.add(e);
				}
			});
			
			return matched.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final int matchCount;

		final ConstList<E> sources;
		final ConstList<E> targets;

		final ConstList<KeyValue<E, E>> matchesInSourceOrder;
		final ConstList<KeyValue<E, E>> matchesInTargetOrder;

		ConstList<E> remainingSources;
		ConstList<E> remainingTargets;
		ConstList<E> unmatchedSources;
		ConstList<E> unmatchedTargets;
		ConstList<E>   matchedSources;
		ConstList<E>   matchedTargets;

		ConstList<KeyValue<E, E>> sourceMatches;
		ConstList<KeyValue<E, E>> targetMatches;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		protected Implementation(
			final int                       matchCount          ,
			final ConstList<E>              sourceInput         ,
			final ConstList<E>              targetInput         ,
			final ConstList<KeyValue<E, E>> matchesInSourceOrder,
			final ConstList<KeyValue<E, E>> matchesInTargetOrder
		)
		{
			super();
			this.matchCount           = matchCount          ;
			this.sources              = sourceInput         ;
			this.targets              = targetInput         ;
			this.matchesInSourceOrder = matchesInSourceOrder;
			this.matchesInTargetOrder = matchesInTargetOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public int matchCount()
		{
			return this.matchCount;
		}

		@Override
		public XGettingSequence<E> inputSources()
		{
			return this.sources;
		}

		@Override
		public XGettingSequence<E> inputTargets()
		{
			return this.targets;
		}

		@Override
		public XGettingSequence<KeyValue<E, E>> matchesInSourceOrder()
		{
			return this.matchesInSourceOrder;
		}

		@Override
		public XGettingSequence<KeyValue<E, E>> matchesInTargetOrder()
		{
			return this.matchesInTargetOrder;
		}

		@Override
		public synchronized XGettingSequence<E> remainingSources()
		{
			if(this.remainingSources == null)
			{
				this.remainingSources = collectRemaining(this.sources, this.matchesInSourceOrder);
			}
			return this.remainingSources;
		}

		@Override
		public synchronized XGettingSequence<E> remainingTargets()
		{
			if(this.remainingTargets == null)
			{
				this.remainingTargets = collectRemaining(this.targets, this.matchesInTargetOrder);
			}
			return this.remainingTargets;
		}

		@Override
		public synchronized XGettingSequence<E> unmatchedSources()
		{
			if(this.unmatchedSources == null)
			{
				this.unmatchedSources = collectUnmatched(this.sources, this.matchesInSourceOrder);
			}
			return this.unmatchedSources;
		}

		@Override
		public synchronized XGettingSequence<E> unmatchedTargets()
		{
			if(this.unmatchedTargets == null)
			{
				this.unmatchedTargets = collectUnmatched(this.targets, this.matchesInTargetOrder);
			}
			return this.unmatchedTargets;
		}

		@Override
		public synchronized XGettingSequence<KeyValue<E, E>> sourceMatches()
		{
			if(this.sourceMatches == null)
			{
				this.sourceMatches = this.matchesInSourceOrder.filterTo(
					new BulkList<KeyValue<E, E>>(this.matchCount),
					XFunc.notNull()
				).immure();
			}
			return this.sourceMatches;
		}

		@Override
		public synchronized XGettingSequence<KeyValue<E, E>> targetMatches()
		{
			if(this.targetMatches == null)
			{
				this.targetMatches = this.matchesInTargetOrder.filterTo(
					new BulkList<KeyValue<E, E>>(this.matchCount),
					XFunc.notNull()
				).immure();
			}
			return this.targetMatches;
		}

		@Override
		public synchronized ConstList<E> matchedSources()
		{
			if(this.matchedSources == null)
			{
				this.matchedSources = collectMatched(this.sources, this.matchesInSourceOrder);
			}
			return this.matchedSources;
		}

		@Override
		public synchronized ConstList<E> matchedTargets()
		{
			if(this.matchedTargets == null)
			{
				this.matchedTargets = collectMatched(this.targets, this.matchesInTargetOrder);
			}
			return this.matchedTargets;
		}

	}
	
}
