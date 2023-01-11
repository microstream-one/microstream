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

import one.microstream.chars.Levenshtein;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.equality.Equalator;


/**
 * Logic for bidirectionally and exclusively linking all matching elements from two collections according
 * to equality and/or sufficient similarity.
 * <p>
 * Exclusively means each element in both collections can at most be linked with one element from the other collection.
 * Bidirectionally means the link between two elements always has two directions. If element A is linked to element B,
 * element B is inherently linked to element A as well.
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
 * (see {@link Levenshtein#substringSimilarity}) the algorithm produces the following associations:
 * <pre>
 * firstname       -1.00- Firstname
 * lastname        -0.75- Name
 * age             -1.00- Age
 * emailAddress    -0.71- Email
 * postalAddress   -0.77- Address
 * noteLink        [new]
 * newColumn1      [new]
 * someMiscAddress -0.56- OtherAddress
 *                      X Freetext
 * </pre>
 *
 * 
 *
 * @param <E> the type of the elements being matched.
 */
public interface MultiMatcher<E>
{
	public double similarityThreshold();

	/**
	 * This is a measure of how "eager" the algorithm is to find as many matches as possible.
	 * The lower this threshold is, the more "single potential match" items will be preferred over actually
	 * better matching pairs just to not leave them unmatched.
	 * To deactivate this special casing, set the threshold to 1.0, meaning only items that fit perfectly anyway
	 * take precedence over others.
	 * 
	 * @return the singleton precedence threshold
	 */
	public double singletonPrecedenceThreshold();
	
	public double singletonPrecedenceBonus();
	
	public double noiseFactor();

	public Equalator<? super E> equalator();
	
	public Similator<? super E> similator();
	
	public MatchValidator<? super E> validator();
	

	public MultiMatcher<E> setSimilarityThreshold(double similarityThreshold);
	
	public MultiMatcher<E> setSingletonPrecedenceThreshold(double singletonPrecedenceThreshold);
	
	public MultiMatcher<E> setSingletonPrecedenceBonus(double singletonPrecedenceBonus);
	
	public MultiMatcher<E> setNoisefactor(double noiseFactor);

	public MultiMatcher<E> setSimilator(Similator<? super E> similator);
	
	public MultiMatcher<E> setEqualator(Equalator<? super E> equalator);
	
	public MultiMatcher<E> setValidator(MatchValidator<? super E> validator);

	public MultiMatch<E> match(XGettingCollection<? extends E> source, XGettingCollection<? extends E> target);


	
	public static double defaultSimilarityThreshold()
	{
		return 0.50;
	}
	
	public static double defaultSingletonPrecedenceThreshold()
	{
		/* see #singletonPrecedenceThreshold.
		 * This should be really high by default to avoid overly enthusiastic matching.
		 */
		return 0.90;
	}
	
	public static double defaultSingletonPrecedenceBonus()
	{
		/* (04.10.2018 TM)TODO: MultiMatching: Why is a "bonus" factor below 1.0?
		 * This makes it a malus, not a bonus.
		 */
		return 0.75;
	}
	
	public static double defaultNoiseFactor()
	{
		return 0.50;
	}
	
	
	
	public static <E> MultiMatcher<E> New()
	{
		return new MultiMatcher.Default<>();
	}

	public class Default<E> implements MultiMatcher<E>
	{
		/* (04.08.2011 TM)TOD0: JavaDoc
		 */

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private Equalator<? super E>      equalator;
		private Similator<? super E>      similator;
		private MatchValidator<? super E> validator;

		private double similarityThreshold          = defaultSimilarityThreshold();
		private double singletonPrecedenceThreshold = defaultSingletonPrecedenceThreshold();
		private double singletonPrecedenceBonus     = defaultSingletonPrecedenceBonus();
		private double noiseFactor                  = defaultNoiseFactor();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// getters //
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
		public double noiseFactor()
		{
			return this.noiseFactor;
		}

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
		public MatchValidator<? super E> validator()
		{
			return this.validator;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////

		@Override
		public MultiMatcher<E> setSimilarityThreshold(final double similarityThreshold)
		{
			this.similarityThreshold = similarityThreshold;
			return this;
		}

		@Override
		public MultiMatcher<E> setSingletonPrecedenceThreshold(final double singletonPrecedenceThreshold)
		{
			this.singletonPrecedenceThreshold = singletonPrecedenceThreshold;
			return this;
		}

		@Override
		public MultiMatcher<E> setSingletonPrecedenceBonus(final double singletonPrecedenceBonus)
		{
			this.singletonPrecedenceBonus = singletonPrecedenceBonus;
			return this;
		}

		@Override
		public MultiMatcher<E> setNoisefactor(final double noiseFactor)
		{
			this.noiseFactor = noiseFactor;
			return this;
		}

		@Override
		public MultiMatcher<E> setSimilator(final Similator<? super E> similator)
		{
			this.similator = similator;
			return this;
		}

		@Override
		public MultiMatcher<E> setEqualator(final Equalator<? super E> equalator)
		{
			this.equalator = equalator;
			return this;
		}

		@Override
		public MultiMatcher<E> setValidator(final MatchValidator<? super E> validator)
		{
			this.validator = validator;
			return this;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@SuppressWarnings("unchecked")
		@Override
		public MultiMatch<E> match(
			final XGettingCollection<? extends E> source,
			final XGettingCollection<? extends E> target
		)
		{
			return new MultiMatch.Default<>(
				this,
				(E[])source.toArray(),
				(E[])target.toArray()
			).match();
		}
		
	}

}
