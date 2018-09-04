package net.jadoth.util.matching;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.collections.XArrays;
import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.typing.KeyValue;

public interface MultiMatch<E>
{
	public double similarityThreshold();

	public double singletonPrecedenceThreshold();

	public double singletonPrecedenceBonus();

	public double averageSimilarity();

	public double totalSimilarity();
	
	public MultiMatchResult<E> result();
	
	public MultiMatchAssembler<E> assembler();
	
	
	public static <E> MultiMatch<E> New(final MultiMatcher<E> matcher, final E[] source, final E[] target)
	{
		return new MultiMatch.Implementation<>(
			notNull(matcher),
			notNull(source) ,
			notNull(target)
		);
	}
	
	public class Implementation<E> implements MultiMatch<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		// note that similarity values get converted from raw double similarity to int quantifiers to boost performance.
		protected static final int MAX_QUANTIFIER = 1000000000; // nicer to read/debug values and still big enough.



		///////////////////////////////////////////////////////////////////////////
		// static methods   //
		/////////////////////

		static double similarity(final int quantifier)
		{
			return (double)quantifier / MAX_QUANTIFIER;
		}

		static int quantifier(final double similarity)
		{
			return (int)(similarity * MAX_QUANTIFIER);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final MultiMatcher<E>           matcher       ;
		private final MatchValidator<? super E> matchValidator;

		// thresholds //
		
		/**
		 * Minimal similarity to be met by a candidate pair in order to be considered for matching (lower bound filter).
		 */
		private final double similarityThreshold; // stays double as it gets only for direct evaluation value comparison.
		/**
		 * Upward similarity bound from which on a singleton is granted precedence no matter the similarity of the
		 * conflicted candidate pair.
		 */
		private final int singletonPrecedenceThreshold;

		// factors //
		/**
		 * Similarity bonus factor applies to a singleton's similarity when comparing to a conflicted candidate pair.
		 */
		private final double singletonPrecedenceBonus;
		/**
		 * Percentage of the highest similarity in one row (source or target) under which all other candidate pairs
		 * in this row (= noise) get removed.
		 */
		private final double noiseFactor;

		// working variables //

		final E[] inputSource;
		final E[] inputTarget;
		final E[] source;
		final E[] target;
		final E[] linkedTargets;
		final E[] linkedSources;

		final int[] srcToTrgMap;
		final int[] trgToSrcMap;
		final int[] srcCandCount;
		final int[] trgCandCount;
		final int[][] quantifiers;
		final int[] linkedSrcQuantifiers; // doesn't require a target~ pendant as it applies to both

		int    sourceCandidateCount;
		int    targetCandidateCount;
		int    totalQuantifier     ;
		double averageSimilarity   ;
		int    matchCount          ;

		MultiMatchResult<E> result;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		@SuppressWarnings("unchecked")
		protected Implementation(final MultiMatcher<E> matcher, final E[] source, final E[] target)
		{
			super();
			this.matcher = matcher;
			this.matchValidator = matcher.matchCallback();

			// configurations
			this.singletonPrecedenceThreshold = quantifier(matcher.singletonPrecedenceThreshold());
			this.singletonPrecedenceBonus = matcher.singletonPrecedenceBonus();
			this.similarityThreshold = matcher.similarityThreshold();
			this.noiseFactor = matcher.noiseFactor();
//			this.suspicousMatchThreshold = quantifier(matcher.getSuspiciousMatchThreshold());

			this.source = (this.inputSource = source).clone();
			this.target = (this.inputTarget = target).clone();
			this.linkedTargets = (E[])new Object[this.source.length];
			this.linkedSources = (E[])new Object[this.target.length];

			// mapping metadata values
			this.srcCandCount = new int[source.length];
			this.trgCandCount = new int[this.inputTarget.length];
			this.srcToTrgMap = new int[source.length];
			this.trgToSrcMap = new int[this.inputTarget.length];

			// similarity matrix (converted to integer quantifier values)
			this.quantifiers = new int[source.length][this.inputTarget.length];

			this.linkedSrcQuantifiers = new int[source.length];
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public double similarityThreshold()
		{
			return this.similarityThreshold;
		}

		@Override
		public double singletonPrecedenceThreshold()
		{
			return this.singletonPrecedenceThreshold;
		}

		@Override
		public double singletonPrecedenceBonus()
		{
			return this.singletonPrecedenceBonus;
		}

		@Override
		public double averageSimilarity()
		{
			return this.averageSimilarity;
		}

		@Override
		public double totalSimilarity()
		{
			return similarity(this.totalQuantifier);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		// internal methods - linking //

		protected void initializeLinkingArray()
		{
			XArrays.fill(this.srcToTrgMap, -1);
			XArrays.fill(this.trgToSrcMap, -1);
			System.arraycopy(this.inputSource, 0, this.source, 0, this.inputSource.length);
			System.arraycopy(this.inputTarget, 0, this.target, 0, this.inputTarget.length);
		}

		protected void initializeSimilarityArrays()
		{
			XArrays.fill(this.srcCandCount, 0);
			XArrays.fill(this.trgCandCount, 0);
			XArrays.fill(this.linkedSrcQuantifiers, 0);

			for(int s = 0; s < this.quantifiers.length; s++)
			{
				final int[] sTargets = this.quantifiers[s];
				for(int t = 0; t < sTargets.length; t++)
				{
					sTargets[t] = 0;
				}
			}
		}

		protected void buildQuantifiers()
		{
			final MultiMatcher<E> m = this.matcher;
			
			for(int s = 0; s < this.source.length; s++)
			{
				if(this.source[s] == null)
				{
					continue;
				}

				for(int t = 0; t < this.target.length; t++)
				{
					if(this.target[t] == null)
					{
						continue;
					}

					final double sim;
					if((sim = m.similator().evaluate(this.source[s], this.target[t])) >= this.similarityThreshold)
					{
						this.quantifiers[s][t] = quantifier(sim);
						this.srcCandCount[s]++;
						this.trgCandCount[t]++;
					}
				}
			}
		}

		protected void link(final int s, final int t)
		{
			this.linkedTargets[s] = this.inputTarget[this.srcToTrgMap[s] = t];
			this.linkedSources[t] = this.inputSource[this.trgToSrcMap[t] = s];
			this.target[t] = null;
			this.source[s] = null;
		}

		protected void linkOneMatched(final int s, final int t)
		{
			if(this.matchValidator != null && !this.matchValidator.isValidMatch(
				this.source[s], this.target[t], similarity(this.quantifiers[s][t]), this.srcCandCount[s], this.trgCandCount[t]
			))
			{
				this.removeOne(this.srcCandCount, this.trgCandCount, s, t);
				return;
			}

//			final String debug_Title = "Linked " + s + "/" + t + ": "
//				+ XMath.round2(similarity(this.quantifiers[s][t]))
//				+ " " + this.source[s] + " <-> " + this.target[t]
//			;

			this.link(s, t);
			this.totalQuantifier += this.linkedSrcQuantifiers[s] = this.quantifiers[s][t];

			final int[] sourceTargets = this.quantifiers[s];
			for(int i = 0; i < sourceTargets.length; i++)
			{
				if(sourceTargets[i] > 0)
				{
					this.removeOne(this.srcCandCount, this.trgCandCount, s, i);
				}
				sourceTargets[i] = 0;
			}

			for(int i = 0; i < this.quantifiers.length; i++)
			{
				if(this.quantifiers[i][t] > 0)
				{
					this.removeOne(this.srcCandCount, this.trgCandCount, i, t);
				}
				this.quantifiers[i][t] = 0;
			}

//			this.debug_printState(debug_Title);
		}

		protected void linkAllEquals(final Equalator<? super E> equalator)
		{
			if(equalator == null)
			{
				return;
			}

			for(int s = 0; s < this.source.length; s++)
			{
				if(this.source[s] == null)
				{
					continue;
				}

				for(int t = 0; t < this.target.length; t++)
				{
					if(this.target[t] == null)
					{
						continue;
					}

					if(equalator.equal(this.source[s], this.target[t]))
					{
						this.link(s, t);
					}
				}
			}
		}

		protected void linkAllSimilar(final Similator<? super E> similator)
		{
			if(similator == null)
			{
				return;
			}

			this.initializeSimilarityArrays();
			this.buildQuantifiers();
			this.calculateCandidateCount();

			// link all perfect matches once at the beginning
			this.linkAllPerfect();

			if(this.similarityThreshold < this.noiseFactor)
			{
				this.removeNoise();
			}

			// repeatedly link obvious candidates and/or resolve conflicts until all candidates are linked or removed
			while(this.sourceCandidateCount > 0 && this.targetCandidateCount > 0)
			{
				// inlined minimum
				// link all unconflicted candidates (one linked candidate is already sufficient to restart the loop)
				if(this.linkAllUnconflicted())
				{
					continue; // found and linked one or more unconflicted candidates, so restart loop
				}

				// try to link a conflicted source singleton with precedence
				if(this.resolveOneSourceSingleton())
				{
					continue; // removed an entry (link or dropped suspicious), restart loop to check for new unconflicted
				}

				// try to link a conflicted target singleton with precedence
				if(this.resolveOneTargetSingleton())
				{
					continue; // removed an entry (link or dropped suspicious), restart loop to check for new unconflicted
				}

				// link the one candidate with the highest similarity and then restart the loop
				this.linkOneBestMatch();
			}
		}

		protected void linkAllPerfect()
		{
//			this.debug_printState("Before Linking Perfects");

			for(int s = 0; s < this.source.length; s++)
			{
				if(this.srcCandCount[s] == 0)
				{
					continue;
				}
				final int[] sTargets = this.quantifiers[s];
				for(int t = 0; t < this.target.length; t++)
				{
					if(sTargets[t] != MAX_QUANTIFIER)
					{
						continue;
					}
					this.linkOneMatched(s, t);
				}
			}
		}

		protected boolean linkAllUnconflicted()
		{
			boolean hasChanged = false;
			loop: // interesting loop: restart looping over quantifiers until no more unconflicted can be found
			while(true)
			{
				for(int s = 0; s < this.source.length; s++)
				{
					if(this.srcCandCount[s] != 1)
					{
						continue;
					}
					final int[] sTargets = this.quantifiers[s];
					for(int t = 0; t < sTargets.length; t++)
					{
						if(this.quantifiers[s][t] == 0 || this.trgCandCount[t] != 1)
						{
							continue;
						}
						this.linkOneMatched(s, t);
						hasChanged = true;
						continue loop; // restart because previous conflicts may have been resolved
					}
				}
				break;
			}
			return hasChanged;
		}

		protected boolean resolveOneSourceSingleton()
		{
			for(int s = 0; s < this.source.length; s++)
			{
				if(this.srcCandCount[s] != 1)
				{
					continue;
				}
				for(int t = 0; t < this.target.length; t++)
				{
					if(this.quantifiers[s][t] == 0)
					{
						continue;
					}
					// search for a better singleton target match and switch to it
					int bsts = s; // best singleton target match source index
					int bestTargetSingletonQnt = this.quantifiers[s][t];
					for(int i = s; i < this.source.length; i++)
					{
						// starting at s is sufficient because of singleton iteration
						if(this.srcCandCount[i] == 1 && this.quantifiers[i][t] > bestTargetSingletonQnt)
						{
							bestTargetSingletonQnt = this.quantifiers[bsts = i][t];
						}
					}

					// if found target singleton has absolute precedence, link it without looking at overall best at all
					if(this.quantifiers[bsts][t] >= this.singletonPrecedenceThreshold)
					{
						this.linkOneMatched(bsts, t);
						return true;
					}

					// search for overall best target match
					int bots = bsts; // best overall target match source index
					for(int i = 0; i < this.source.length; i++)
					{
						if(this.quantifiers[i][t] > bestTargetSingletonQnt)
						{
							bots = i;
						}
					}

					// if best matched singleton is also overall best matched or has relative precedence, then link it
					if(bots == bsts || this.quantifiers[bsts][t] * this.singletonPrecedenceBonus >= this.quantifiers[bots][t])
					{
						this.linkOneMatched(bsts, t);

						// true means not success, but change. So this is correct even for a dropped suspicious.
						return true;
					}

					// otherwise ignore this singleton for now and continue
				}
			}

			return false;
		}

		protected boolean resolveOneTargetSingleton()
		{
			for(int t = 0; t < this.target.length; t++)
			{
				if(this.trgCandCount[t] != 1)
				{
					continue;
				}
				for(int s = 0; s < this.source.length; s++)
				{
					if(this.quantifiers[s][t] == 0)
					{
						continue;
					}
					// search for a better singleton source match and switch to it
					int bsst = t; // best singleton source match target index
					int bestSourceSingletonQnt = this.quantifiers[s][t];
					for(int i = t; i < this.target.length; i++)
					{
						// starting at s is sufficient because of singleton iteration
						if(this.trgCandCount[i] == 1 && this.quantifiers[s][i] > bestSourceSingletonQnt)
						{
							bestSourceSingletonQnt = this.quantifiers[s][bsst = i];
						}
					}

					// if found source singleton has absolute precedence, link it without looking at overall best at all
					if(this.quantifiers[s][bsst] >= this.singletonPrecedenceThreshold)
					{
						this.linkOneMatched(s, bsst);
						return true;
					}

					// search for overall best source match
					int bost = bsst; // best overall source match target index
					for(int i = 0; i < this.target.length; i++)
					{
						if(this.quantifiers[s][i] > bestSourceSingletonQnt)
						{
							bost = i;
						}
					}

					// if best matched singleton is also overall best matched or has relative precedence, then link it
					if(bost == bsst || this.quantifiers[s][bsst] * this.singletonPrecedenceBonus >= this.quantifiers[s][bost])
					{
						this.linkOneMatched(s, bsst);

						// true means not success, but change. So this is correct even for a dropped suspicious.
						return true;
					}

					// otherwise ignore this singleton for now and continue
				}
			}

			return false;
		}

		protected void linkOneBestMatch()
		{
			int sMax = -1, tMax = -1;
			int maxQuantifier = 0;

			for(int s = 0; s < this.source.length; s++)
			{
				if(this.srcCandCount[s] == 0)
				{
					continue;
				}
				for(int t = 0; t < this.target.length; t++)
				{
					if(this.quantifiers[s][t] > 0 && this.quantifiers[s][t] > maxQuantifier)
					{
						maxQuantifier = this.quantifiers[sMax = s][tMax = t];
					}
				}
			}
			// there MUST be at least one remaining candidate or this method wouldn't have been called in the first place.
			this.linkOneMatched(sMax, tMax);
		}

		// internal methods - utils //

		protected void calculateCandidateCount()
		{
			int sc = 0;
			for(int s = 0; s < this.srcCandCount.length; s++)
			{
				if(this.srcCandCount[s] > 0)
				{
					sc++;
				}
			}
			int tc = 0;
			for(int t = 0; t < this.trgCandCount.length; t++)
			{
				if(this.trgCandCount[t] > 0)
				{
					tc++;
				}
			}
			this.sourceCandidateCount = sc;
			this.targetCandidateCount = tc;
		}

		protected void removeOne(final int[] sCandCount, final int[] tCandCount, final int s, final int t)
		{
			if(--tCandCount[t] == 0)
			{
				this.targetCandidateCount--;
			}
			if(--sCandCount[s] == 0)
			{
				this.sourceCandidateCount--;
			}
		}

		protected void removeNoise()
		{
			// source-wise noise reduction
			for(int s = 0; s < this.srcCandCount.length; s++)
			{
				if(this.srcCandCount[s] == 0)
				{
					continue;
				}
				final int noiseThreshold;
				final int[] sTargets;
				if((noiseThreshold = (int)(MultiMatcher.maxTargetQuantifier(sTargets = this.quantifiers[s]) * this.noiseFactor)) == 0)
				{
					continue;
				}
				for(int t = 0; t < sTargets.length; t++)
				{
					if(sTargets[t] > 0 && sTargets[t] < noiseThreshold)
					{
						this.removeOne(this.srcCandCount, this.trgCandCount, s, t);
						sTargets[t] = 0;
					}
				}
			}
//			this.debug_printState("removed s-noise");

			// target-wise noise reduction
			for(int t = 0; t < this.trgCandCount.length; t++)
			{
				if(this.trgCandCount[t] == 0)
				{
					continue;
				}
				final int noiseThreshold;
				if((noiseThreshold = (int)(MultiMatcher.maxSourceQuantifier(this.quantifiers, t) * this.noiseFactor)) == 0)
				{
					continue;
				}
				for(int s = 0; s < this.quantifiers.length; s++)
				{
					if(this.quantifiers[s][t] > 0 && this.quantifiers[s][t] < noiseThreshold)
					{
						this.removeOne(this.srcCandCount, this.trgCandCount, s, t);
						this.quantifiers[s][t] = 0;
					}
				}
			}
//			this.debug_printState("removed t-noise");
		}

		// public methods //

		protected Implementation<E> match()
		{
			this.initializeLinkingArray();

			this.linkAllEquals(this.matcher.equalator());

			if(this.similarityThreshold > 0)
			{
				this.linkAllSimilar(this.matcher.similator());
			}

			this.averageSimilarity = similarity(this.totalQuantifier)
				/ (this.matchCount = MultiMatcher.calculateMatchCount(this.srcToTrgMap))
			;
			return this;
		}

		@SuppressWarnings("unchecked")
		private MultiMatchResult<E> buildResult()
		{
			final E[] source = this.inputSource, target = this.inputTarget;
			final int[] s2tMapping = this.srcToTrgMap;
			final KeyValue<E, E>[] matchS = new KeyValue[source.length];
			final KeyValue<E, E>[] matchT = new KeyValue[target.length];

			for(int s = 0; s < source.length; s++)
			{
				if(s2tMapping[s] >= 0)
				{
					matchT[s2tMapping[s]] = matchS[s] = X.KeyValue(source[s], target[s2tMapping[s]]);
				}
			}

			return new MultiMatchResult.Implementation<>(
				this.matchCount,
				X.ConstList(source),
				X.ConstList(target),
				X.ConstList(matchS),
				X.ConstList(matchT)
			);
		}


		@Override
		public synchronized MultiMatchResult<E> result()
		{
			if(this.result == null)
			{
				this.result = this.buildResult();

				// only for debugging / testing
//				System.out.println("InputSources        : " + this.result.getInputSources());
//				System.out.println("InputTargets        : " + this.result.getInputTargets());
	//
//				System.out.println("MatchesInSourceOrder: " + this.result.getMatchesInSourceOrder());
//				System.out.println("MatchesInTargetOrder: " + this.result.getMatchesInTargetOrder());
	//
//				System.out.println("SourceMatches       : " + this.result.getSourceMatches());
//				System.out.println("TargetMatches       : " + this.result.getTargetMatches());
	//
//				System.out.println("RemainingSources    : " + this.result.getRemainingSources());
//				System.out.println("RemainingTargets    : " + this.result.getRemainingTargets());
	//
//				System.out.println("UnmatchedSources    : " + this.result.getUnmatchedSources());
//				System.out.println("UnmatchedTargets    : " + this.result.getUnmatchedTargets());
	//
//				System.out.println("MatchedSources      : " + this.result.getMatchedSources());
//				System.out.println("MatchedTargets      : " + this.result.getMatchedTargets());
			}
			
			return this.result;
		}
		
		@Override
		public MultiMatchAssembler<E> assembler()
		{
			return new MultiMatchAssembler.Implementation<>(this);
		}

	}

}