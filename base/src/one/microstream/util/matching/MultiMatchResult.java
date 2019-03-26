package one.microstream.util.matching;

import one.microstream.collections.BulkList;
import one.microstream.collections.ConstList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.functional.XFunc;


public interface MultiMatchResult<E>
{
	public int matchCount();

	public XGettingSequence<E> inputSources();

	public XGettingSequence<E> inputTargets();

	public XGettingSequence<? extends Item<E>> matchesInSourceOrder();

	public XGettingSequence<? extends Item<E>> matchesInTargetOrder();

	public XGettingSequence<E> remainingSources();

	public XGettingSequence<E> remainingTargets();

	public XGettingSequence<E> unmatchedSources();

	public XGettingSequence<E> unmatchedTargets();

	public XGettingSequence<? extends Item<E>> sourceMatches();

	public XGettingSequence<? extends Item<E>> targetMatches();

	public XGettingList<E> matchedSources();

	public XGettingList<E> matchedTargets();
	
	
	public interface Item<E>
	{
		public E sourceElement();
		
		public double similarity();
		
		public E targetElement();
		
		
		
		public final class Implementation<E> implements MultiMatchResult.Item<E>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final E      sourceElement;
			private final double similarity   ;
			private final E      targetElement;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(final E sourceElement, final double similarity, final E targetElement)
			{
				super();
				this.sourceElement = sourceElement;
				this.similarity    = similarity   ;
				this.targetElement = targetElement;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final E sourceElement()
			{
				return this.sourceElement;
			}
			
			@Override
			public final double similarity()
			{
				return this.similarity;
			}
			
			@Override
			public final E targetElement()
			{
				return this.targetElement;
			}
			
		}
		
	}
	
	
	
	public class Implementation<E> implements MultiMatchResult<E>
	{
		///////////////////////////////////////////////////////////////////////////
		//  static methods  //
		/////////////////////

		static <T> ConstList<T> collectRemaining(
			final ConstList<T>                 input  ,
			final ConstList<? extends Item<T>> matches
		)
		{
			final BulkList<T> remaining = BulkList.New(input.size());
			input.iterateIndexed((final T e, final long index) ->
			{
				remaining.add(matches.at(index) == null ? e : null);
			});
			
			return remaining.immure();
		}

		static <T> ConstList<T> collectUnmatched(
			final ConstList<T>                 input  ,
			final ConstList<? extends Item<T>> matches
		)
		{
			final BulkList<T> unmatched = BulkList.New(input.size());
			input.iterateIndexed((final T e, final long index) ->
			{
				if(matches.at(index) == null)
				{
					unmatched.add(e);
				}
			});
			
			return unmatched.immure();
		}

		static <T> ConstList<T> collectMatched(
			final ConstList<T>                 input  ,
			final ConstList<? extends Item<T>> matches
		)
		{
			final BulkList<T> matched = BulkList.New(input.size());
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

		final ConstList<? extends Item<E>> matchesInSourceOrder;
		final ConstList<? extends Item<E>> matchesInTargetOrder;

		ConstList<E> remainingSources;
		ConstList<E> remainingTargets;
		ConstList<E> unmatchedSources;
		ConstList<E> unmatchedTargets;
		ConstList<E>   matchedSources;
		ConstList<E>   matchedTargets;

		ConstList<? extends Item<E>> sourceMatches;
		ConstList<? extends Item<E>> targetMatches;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		protected Implementation(
			final int                          matchCount          ,
			final ConstList<E>                 sourceInput         ,
			final ConstList<E>                 targetInput         ,
			final ConstList<? extends Item<E>> matchesInSourceOrder,
			final ConstList<? extends Item<E>> matchesInTargetOrder
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
		public XGettingSequence<? extends Item<E>> matchesInSourceOrder()
		{
			return this.matchesInSourceOrder;
		}

		@Override
		public XGettingSequence<? extends Item<E>> matchesInTargetOrder()
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
		public synchronized XGettingSequence<? extends Item<E>> sourceMatches()
		{
			if(this.sourceMatches == null)
			{
				this.sourceMatches = this.matchesInSourceOrder.filterTo(
					new BulkList<Item<E>>(this.matchCount),
					XFunc.notNull()
				).immure();
			}
			return this.sourceMatches;
		}

		@Override
		public synchronized XGettingSequence<? extends Item<E>> targetMatches()
		{
			if(this.targetMatches == null)
			{
				this.targetMatches = this.matchesInTargetOrder.filterTo(
					new BulkList<Item<E>>(this.matchCount),
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
