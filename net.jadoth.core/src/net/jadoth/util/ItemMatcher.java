package net.jadoth.util;

import java.text.DecimalFormat;

import net.jadoth.collections.KeyValue;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.JadothEqualators;
import net.jadoth.math.JadothMath;
import net.jadoth.util.chars.JadothChars;
import net.jadoth.util.chars.VarString;


/**
 * Logic for bidirectionally and exclusively linking all matching items from two collections according
 * to equality and/or sufficient similarity.
 * <p>
 * Exclusviely means each item in both collections can at most be linked with one item from the other collection.
 * Bidirectionally means the link between two items always has two directions. If item A is linked to item B, item B
 * is inherently linked to item A as well.
 * <p>
 * Equality and similarity are defined by {@link Equalator} and {@link Similator} functions that can be passed at
 * creation time. All values controlling the matching algorithm can be optionally configured in the factory class
 * if the default configuration is not desired. Additionally, a callback function for deciding found matches with
 * questionable similarity can be injected.
 * <p>
 * This is a powerful general purpose means of building associations of two sets of similar but not equal elements.<br>
 * A very simple use case is the formal recognition of a changed table column structure (for which this class
 * was originally developed).
 * <p>
 * For example given the following two hypothetical definitions (old and new) of column names:<br>
 * <br>
 * <u><b>Old</b></u>:
 * <ul>
 * <li>Name</li>
 * <li>Firstname</li>
 * <li>Age</li>
 * <li>Address</li>
 * <li>Freetext</li>
 * <li>Email</li>
 * <li>OtherAddress</li>
 * </ul>
 * and<br>
 * <br>
 * <u><b>New</b></u>:
 * <ul>
 * <li>firstname</li>
 * <li>lastname</li>
 * <li>age</li>
 * <li>emailAddress</li>
 * <li>postalAddress</li>
 * <li>noteLink</li>
 * <li>newColumn1</li>
 * <li>someMiscAddress</li>
 * </ul>
 * When using a case insensitive modified Levenshtein {@link Similator}
 * (see {@link JadothChars#levenshteinSubstringSimilarity}) the algorithm produces the following associations:
 * <pre>
 * firstname       < - 1.00-> Firstname
 * lastname        <-0.75-> Name
 * age             < - 1.00-> Age
 * emailAddress    <-0.71-> Email
 * postalAddress   <-0.77-> Address
 * noteLink        +
 * newColumn1      +
 * someMiscAddress <-0.56-> OtherAddress
 *                        x Freetext
 * </pre>
 *
 * @author Thomas Muenz
 *
 * @param <T> the type of the items being matched.
 */
public interface ItemMatcher<E>
{
	// (02.08.2011 TM)NOTE: I'd really like to have true properties in the language :-(

	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public double getSimilarityThreshold();
	public double getSingletonPrecedenceThreshold();
	public double getSingletonPrecedenceBonus();
	public double getNoiseFactor();

	public Equalator<? super E> equalator();
	public Similator<? super E> similator();
	public MatchCallback<? super E> matchCallback();



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	public ItemMatcher<E> setSimilarityThreshold(double similarityThreshold);
	public ItemMatcher<E> setSingletonPrecedenceThreshold(double singletonPrecedenceThreshold);
	public ItemMatcher<E> setSingletonPrecedenceBonus(double singletonPrecedenceBonus);
	public ItemMatcher<E> setNoisefactor(double noiseFactor);

	public ItemMatcher<E> setSimilator(Similator<? super E> similator);
	public ItemMatcher<E> setEqualator(Equalator<? super E> equalator);
	public ItemMatcher<E> setMatchCallback(MatchCallback<? super E> decisionCallback);



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public ItemMatch<E> match(XGettingCollection<? extends E> source, XGettingCollection<? extends E> target);



	public final class Static
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		// thresholds //
		static final double DEFAULT_SIMILARITY_THRESHOLD           = 0.50D;
		static final double DEFAULT_SINGLETON_PRECEDENCE_THRESHOLD = 0.75D;

		// factors //
		static final double DEFAULT_SINGLETON_PRECEDENCE_BONUS = 1.25D;
		static final double DEFAULT_NOISE_FACTOR               = 0.50;

		static final DecimalFormat SIM_FORMAT = JadothChars.createDecimalFormatter("0.00", java.util.Locale.ENGLISH);



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static int calcMatchCount(final int[] s2tMapping)
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

		static int maxTargetQuantifier(final int[] sTargets)
		{
			int maxQuantifier = 0;
			for(int t = 0; t < sTargets.length; t++)
			{
				if(sTargets[t] > maxQuantifier)
				{
					maxQuantifier = sTargets[t];
				}
			}
			return maxQuantifier;
		}

		static int maxSourceQuantifier(final int[][] quantifiers, final int t)
		{
			int maxQuantifier = 0;
			for(int s = 0; s < quantifiers.length; s++)
			{
				if(quantifiers[s][t] > maxQuantifier)
				{
					maxQuantifier = quantifiers[s][t];
				}
			}
			return maxQuantifier;
		}

		protected static <E> VarString assembleState(
			final ItemMatch<E> im,
			final VarString vc,
			final String title,
			final BiProcedure<VarString, ? super E> appender
		)
		{
			final int mc = calcMatchCount(im.s2tMapping);

			vc
			.add(title).lf()
			.add("[candidateCount = " + Math.min(im.sourceCandidateCount, im.targetCandidateCount) + "]")
			.add("[totalSimilarity = " + JadothMath.round3(im.getTotalSimilarity()) + "]")
			.lf()
			.add("[matchCount = " + mc + "]")
			.add("[averageSimilarity = " + (mc == 0 ? 0 : JadothMath.round3(im.getTotalSimilarity() / mc)) + "]")
			.lf()
			;

			vc.add("s\\t").tab();
			final int tLength = im.quantifiers[0].length;
			for(int t = 0; t < tLength; t++)
			{
				vc.add(t).tab();
			}
			vc.add("s2t").lf();

			for(int s = 0; s < im.quantifiers.length; s++)
			{
				final int[] sTargets = im.quantifiers[s];
				vc.add(s).tab();
				for(int t = 0; t < sTargets.length; t++)
				{
					if(sTargets[t] > 0)
					{
						vc.add(SIM_FORMAT.format(ItemMatch.similarity(sTargets[t])));
					}
					vc.tab();
				}
				if(im.s2tMapping[s] >= 0)
				{
					vc.add("#" + im.s2tMapping[s]).tab();
					appender.accept(vc, im.source[s]);
					vc.tab();
					appender.accept(vc, im.target[im.s2tMapping[s]]);
				}
				else if(im.sCandCount[s] > 0)
				{
					vc.add("[" + im.sCandCount[s] + "]");
				}
				else
				{
					vc.tab();
					appender.accept(vc, im.source[s]);
					vc.tab();
				}
				vc.lf();
			}

			vc.add("t2s").tab();
			for(int t = 0; t < tLength; t++)
			{
				if(im.t2sMapping[t] >= 0)
				{
					vc.add("#" + im.t2sMapping[t]);
				}
				else if(im.tCandCount[t] > 0)
				{
					vc.add("[" + im.tCandCount[t] + "]");
				}
				vc.tab();
			}
			vc.lf();
			vc.lf();
			vc.lf();
			return vc;
		}

		public static <E> VarString assembleMappingSchemeHorizontal(
			final ItemMatchResult<E> im,
			final VarString vc,
			final BiProcedure<VarString, ? super E> appender
		)
		{
			final VarString
				line2 = VarString.New(),
				line3 = VarString.New(),
				line4 = VarString.New()
			;

			for(final E e : im.getInputSources())
			{
				appender.accept(vc, e);
				vc.tab();
			}

			for(final KeyValue<E, E> e : im.getMatchesInSourceOrder())
			{
				if(e != null)
				{
					line2.append('|');
					line3.append('|');
					appender.accept(line4, e.value());
				}
				else
				{
					line2.append('+');
				}
				line2.tab();
				line3.tab();
				line4.tab();
			}

			for(final E e : im.getUnmatchedTargets())
			{
				line3.append('-').tab();
				appender.accept(line4, e);
				line4.tab();
			}

			return vc.lf().add(line2).lf().add(line3).lf().add(line4);
		}

		public static <E> VarString assembleMappingSchemeVertical(
			final ItemMatch<E> im,
			final VarString vc,
			final BiProcedure<VarString, ? super E> appender
		)
		{
			for(int s = 0 ; s < im.source.length; s++)
			{
				appender.accept(vc, im.source[s]);
				if(im.linkedTargets[s] != null)
				{
					vc.add("\t<-");
					vc.add(SIM_FORMAT.format(ItemMatch.similarity(im.linkedSrcQuantifiers[s])));
					vc.add("->\t");
					appender.accept(vc, im.linkedTargets[s]);
				}
				else
				{
					vc.add("\t + ");
				}
				vc.lf();
			}
			for(int t = 0 ; t < im.trg.length; t++)
			{
				if(im.trg[t] != null)
				{
					vc.add("\t       x\t");
					appender.accept(vc, im.trg[t]);
					vc.lf();
				}
			}

			return vc;
		}

		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}

	@FunctionalInterface
	public interface MatchCallback<E>
	{
		public boolean isValidMatch(
			E      sourceItem          ,
			E      targetItem          ,
			double similarity          ,
			int    sourceCandidateCount,
			int    targetCandidateCount
		);
	}



	public class Implementation<E> implements ItemMatcher<E>
	{
		// (04.08.2011 TM)TODO: JavaDoc
		// (05.08.2011)XXX: find better names for "source" and "target"




		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private Equalator<? super E>     equalator              = JadothEqualators.value();
		private Similator<? super E>     similator             ;
		private MatchCallback<? super E> suspiciousMatchDecider;

		private double similarityThreshold          = ItemMatcher.Static.DEFAULT_SIMILARITY_THRESHOLD;
		private double singletonPrecedenceThreshold = ItemMatcher.Static.DEFAULT_SINGLETON_PRECEDENCE_THRESHOLD;
//		private double suspiciousMatchThreshold     = ItemMatcher.Static.DEFAULT_SUBSPICIOUS_MATCH_THRESHOLD;
		private double singletonPrecedenceBonus     = ItemMatcher.Static.DEFAULT_SINGLETON_PRECEDENCE_BONUS;
		private double noiseFactor                  = ItemMatcher.Static.DEFAULT_NOISE_FACTOR;



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public double getSimilarityThreshold()
		{
			return this.similarityThreshold;
		}

		@Override
		public double getSingletonPrecedenceThreshold()
		{
			return this.singletonPrecedenceThreshold;
		}

		@Override
		public double getSingletonPrecedenceBonus()
		{
			return this.singletonPrecedenceBonus;
		}

		@Override
		public double getNoiseFactor()
		{
			return this.noiseFactor;
		}
//		@Override
//		public double getSuspiciousMatchThreshold()
//		{
//			return this.suspiciousMatchThreshold;
//		}

		@Override
		public Equalator<? super E> equalator()
		{
			return this.equalator;
		}

		@Override
		public Similator<? super E> similator()
		{
			return this.similator;
		}

		@Override
		public MatchCallback<? super E> matchCallback()
		{
			return this.suspiciousMatchDecider;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		@Override
		public ItemMatcher<E> setSimilarityThreshold(final double similarityThreshold)
		{
			this.similarityThreshold = similarityThreshold;
			return this;
		}

		@Override
		public ItemMatcher<E> setSingletonPrecedenceThreshold(final double singletonPrecedenceThreshold)
		{
			this.singletonPrecedenceThreshold = singletonPrecedenceThreshold;
			return this;
		}

		@Override
		public ItemMatcher<E> setSingletonPrecedenceBonus(final double singletonPrecedenceBonus)
		{
			this.singletonPrecedenceBonus = singletonPrecedenceBonus;
			return this;
		}

		@Override
		public ItemMatcher<E> setNoisefactor(final double noiseFactor)
		{
			this.noiseFactor = noiseFactor;
			return this;
		}

		@Override
		public ItemMatcher<E> setSimilator(final Similator<? super E> similator)
		{
			this.similator = similator;
			return this;
		}

		@Override
		public ItemMatcher<E> setEqualator(final Equalator<? super E> equalator)
		{
			this.equalator = equalator;
			return this;
		}

		@Override
		public ItemMatcher<E> setMatchCallback(final MatchCallback<? super E> suspiciousMatchDecider)
		{
			this.suspiciousMatchDecider = suspiciousMatchDecider;
			return this;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@SuppressWarnings("unchecked")
		@Override
		public ItemMatch<E> match(
			final XGettingCollection<? extends E> source,
			final XGettingCollection<? extends E> target
		)
		{
			return new ItemMatch<>(this, (E[])source.toArray(), (E[])target.toArray()).match();
		}
	}

}
