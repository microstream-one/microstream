package one.microstream.util.similarity;

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

import one.microstream.X;
import one.microstream.collections.XArrays;
import one.microstream.equality.Equalator;

public interface MultiMatch<E>
{
	public double similarityThreshold();

	public double singletonPrecedenceThreshold();

	public double singletonPrecedenceBonus();

	public double lowestSimilarity();
	
	public double averageSimilarity();
	
	public double highestSimilarity();
	
	public MultiMatchResult<E> result();
	
	public MultiMatchAssembler<E> assembler();
	
	
	public static <E> MultiMatch<E> New(final MultiMatcher<E> matcher, final E[] source, final E[] target)
	{
		return new MultiMatch.Default<>(
			notNull(matcher),
			notNull(source) ,
			notNull(target)
		);
	}
	
	public class Default<E> implements MultiMatch<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static int calculateMatchCount(final int[] s2tMapping)
		{
			int matchCount = 0;
			for(int i = 0; i < s2tMapping.length; i++)
			{
				if(s2tMapping[i] < 0)
				{
					continue;
				}
				matchCount++;
			}
			return matchCount;
		}

		public static double maxTargetQuantifier(final double[] sTargets)
		{
			double maxQuantifier = 0.0;
			for(int t = 0; t < sTargets.length; t++)
			{
				if(sTargets[t] > maxQuantifier)
				{
					maxQuantifier = sTargets[t];
				}
			}
			return maxQuantifier;
		}

		public static double maxSourceQuantifier(final double[][] quantifiers, final int t)
		{
			double maxQuantifier = 0.0;
			for(int s = 0; s < quantifiers.length; s++)
			{
				if(quantifiers[s][t] > maxQuantifier)
				{
					maxQuantifier = quantifiers[s][t];
				}
			}
			return maxQuantifier;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final MultiMatcher<E>           matcher       ;
		private final MatchValidator<? super E> matchValidator;

		// thresholds //
		
		/**
		 * Minimal similarity to be met by a candidate pair in order to be considered for matching (lower bound filter).
		 */
		// double instead of float, since it gets only for direct evaluation value comparison.
		private final double similarityThreshold;
		
		/**
		 * Upward similarity bound from which on a singleton is granted precedence no matter the similarity of the
		 * conflicted candidate pair.<p>
		 * See {@link MultiMatcher#singletonPrecedenceThreshold()}.
		 */
		private final double singletonPrecedenceThreshold;

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
		
		final double[][] matrix;
		final double[]   linkedSourceSimilarities; // doesn't require a target~ pendant as it applies to both

		int    sourceCandidateCount;
		int    targetCandidateCount;
		double averageSimilarity   ;
		double lowestSimilarity    ;
		double highestSimilarity   ;
		int    matchCount          ;

		MultiMatchResult<E> result;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		@SuppressWarnings("unchecked")
		protected Default(final MultiMatcher<E> matcher, final E[] source, final E[] target)
		{
			super();
			this.matcher = matcher;
			this.matchValidator = matcher.validator();

			// configurations
			this.singletonPrecedenceThreshold = matcher.singletonPrecedenceThreshold();
			this.singletonPrecedenceBonus = matcher.singletonPrecedenceBonus();
			this.similarityThreshold = matcher.similarityThreshold();
			this.noiseFactor = matcher.noiseFactor();

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
			this.matrix = new double[source.length][this.inputTarget.length];

			this.linkedSourceSimilarities = new double[source.length];
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public double lowestSimilarity()
		{
			return this.lowestSimilarity;
		}
		
		@Override
		public double highestSimilarity()
		{
			return this.highestSimilarity;
		}

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
			XArrays.fill(this.linkedSourceSimilarities, 0);

			for(int s = 0; s < this.matrix.length; s++)
			{
				final double[] sTargets = this.matrix[s];
				for(int t = 0; t < sTargets.length; t++)
				{
					sTargets[t] = 0.0;
				}
			}
		}

		protected void buildSimilarityMatrix()
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
						this.matrix[s][t] = sim;
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
				this.source[s], this.target[t], this.matrix[s][t], this.srcCandCount[s], this.trgCandCount[t]
			))
			{
				this.removeOne(this.srcCandCount, this.trgCandCount, s, t);
				return;
			}


			this.link(s, t);
			this.linkedSourceSimilarities[s] = this.matrix[s][t];

			final double[] sourceTargets = this.matrix[s];
			for(int i = 0; i < sourceTargets.length; i++)
			{
				if(sourceTargets[i] > 0.0)
				{
					this.removeOne(this.srcCandCount, this.trgCandCount, s, i);
				}
				sourceTargets[i] = 0.0;
			}

			for(int i = 0; i < this.matrix.length; i++)
			{
				if(this.matrix[i][t] > 0.0)
				{
					this.removeOne(this.srcCandCount, this.trgCandCount, i, t);
				}
				this.matrix[i][t] = 0.0;
			}

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
			this.buildSimilarityMatrix();
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
				final double[] sTargets = this.matrix[s];
				for(int t = 0; t < this.target.length; t++)
				{
					if(sTargets[t] != 1.0)
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
					final double[] sTargets = this.matrix[s];
					for(int t = 0; t < sTargets.length; t++)
					{
						if(this.matrix[s][t] == 0 || this.trgCandCount[t] != 1)
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
					if(this.matrix[s][t] == 0)
					{
						continue;
					}
					// search for a better singleton target match and switch to it
					int bsts = s; // best singleton target match source index
					double bestTargetSingletonQnt = this.matrix[s][t];
					for(int i = s; i < this.source.length; i++)
					{
						// starting at s is sufficient because of singleton iteration
						// (04.10.2018 TM)TODO: MultiMatching: But why is it enough? Or is it a bug?
						if(this.srcCandCount[i] == 1 && this.matrix[i][t] > bestTargetSingletonQnt)
						{
							bestTargetSingletonQnt = this.matrix[bsts = i][t];
						}
					}

					// if found target singleton has absolute precedence, link it without looking at overall best at all
					if(this.matrix[bsts][t] >= this.singletonPrecedenceThreshold)
					{
						this.linkOneMatched(bsts, t);
						return true;
					}

					// search for overall best target match
					int bots = bsts; // best overall target match source index
					for(int i = 0; i < this.source.length; i++)
					{
						if(this.matrix[i][t] > bestTargetSingletonQnt)
						{
							bots = i;
						}
					}

					// if best matched singleton is also overall best matched or has relative precedence, then link it
					if(bots == bsts || this.matrix[bsts][t] * this.singletonPrecedenceBonus >= this.matrix[bots][t])
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
					if(this.matrix[s][t] == 0)
					{
						continue;
					}
					// search for a better singleton source match and switch to it
					int bsst = t; // best singleton source match target index
					double bestSourceSingletonQnt = this.matrix[s][t];
					for(int i = t; i < this.target.length; i++)
					{
						// starting at s is sufficient because of singleton iteration
						if(this.trgCandCount[i] == 1 && this.matrix[s][i] > bestSourceSingletonQnt)
						{
							bestSourceSingletonQnt = this.matrix[s][bsst = i];
						}
					}

					// if found source singleton has absolute precedence, link it without looking at overall best at all
					if(this.matrix[s][bsst] >= this.singletonPrecedenceThreshold)
					{
						this.linkOneMatched(s, bsst);
						return true;
					}

					// search for overall best source match
					int bost = bsst; // best overall source match target index
					for(int i = 0; i < this.target.length; i++)
					{
						if(this.matrix[s][i] > bestSourceSingletonQnt)
						{
							bost = i;
						}
					}

					// if best matched singleton is also overall best matched or has relative precedence, then link it
					if(bost == bsst || this.matrix[s][bsst] * this.singletonPrecedenceBonus >= this.matrix[s][bost])
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
			double maxQuantifier = 0.0;

			for(int s = 0; s < this.source.length; s++)
			{
				if(this.srcCandCount[s] == 0.0)
				{
					continue;
				}
				for(int t = 0; t < this.target.length; t++)
				{
					if(this.matrix[s][t] > 0.0 && this.matrix[s][t] > maxQuantifier)
					{
						maxQuantifier = this.matrix[sMax = s][tMax = t];
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
				final double noiseThreshold;
				final double[] sTargets;
				if((noiseThreshold = maxTargetQuantifier(sTargets = this.matrix[s]) * this.noiseFactor) == 0)
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
				final double noiseThreshold;
				if((noiseThreshold = maxSourceQuantifier(this.matrix, t) * this.noiseFactor) == 0)
				{
					continue;
				}
				for(int s = 0; s < this.matrix.length; s++)
				{
					if(this.matrix[s][t] > 0 && this.matrix[s][t] < noiseThreshold)
					{
						this.removeOne(this.srcCandCount, this.trgCandCount, s, t);
						this.matrix[s][t] = 0;
					}
				}
			}
//			this.debug_printState("removed t-noise");
		}

		// public methods //

		protected Default<E> match()
		{
			/* (04.10.2018 TM)TODO: MultiMatching: Consolidate type structure
			 * Currently, the MultiMatcher is more of a factory (configuration holder),
			 * while the MultiMatch contains the actual algorithm and serves as the result instance,
			 * PLUS there is an additonal MultiMatchResult type.
			 * This is all weird and should be consolidated.
			 * 
			 * Also, leaving a potentially gigantic, but totally empty quantifier[][] in the kind-of result instance
			 * at the end of the algorithm is a waste of memory.
			 * All data that is only temporarily required for the algorithm to work on should end with it.
			 * 
			 * This code is basically from my early days in 2012 and while it works quite well, it deserves an
			 * overhault. Maybe even improve on the algorithm itself. Maybe get rid of the precedence stuff.
			 */
			
			this.initializeLinkingArray();

			this.linkAllEquals(this.matcher.equalator());

			if(this.similarityThreshold > 0)
			{
				this.linkAllSimilar(this.matcher.similator());
			}

			this.calculateStatistics();
			
			return this;
		}
		
		private void calculateStatistics()
		{
			this.matchCount = calculateMatchCount(this.srcToTrgMap);
			
			double lowest = Double.MAX_VALUE, highest = 0.0, total = 0.0;
			for(final double linkedSimilarity : this.linkedSourceSimilarities)
			{
				total += linkedSimilarity;
				if(linkedSimilarity < lowest)
				{
					lowest = linkedSimilarity;
				}
				if(linkedSimilarity > highest)
				{
					highest = linkedSimilarity;
				}
			}
			
			this.averageSimilarity = this.matchCount == 0 ? 0 : total / this.matchCount;
			this.lowestSimilarity  = lowest;
			this.highestSimilarity = highest;
		}
		
		private MultiMatchResult<E> buildResult()
		{
			final E[]      source     = this.inputSource;
			final E[]      target     = this.inputTarget;
			final int[]    s2tMapping = this.srcToTrgMap;
			final double[] linkedSims = this.linkedSourceSimilarities;
			
			final Similarity<E>[] matchS = Similarity.Array(source.length);
			final Similarity<E>[] matchT = Similarity.Array(target.length);

			for(int s = 0; s < source.length; s++)
			{
				if(s2tMapping[s] >= 0)
				{
					final Similarity<E> item = Similarity.New(
						source[s],
						linkedSims[s],
						target[s2tMapping[s]]
					);
					matchT[s2tMapping[s]] = matchS[s] = item;
				}
			}

			return new MultiMatchResult.Default<>(
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
			return new MultiMatchAssembler.Default<>(this);
		}

	}

}
