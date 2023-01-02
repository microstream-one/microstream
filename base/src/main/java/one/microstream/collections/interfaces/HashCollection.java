package one.microstream.collections.interfaces;

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

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingList;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.KeyValue;

public interface HashCollection<E> extends Sized
{
	public float hashDensity();

	/**
	 * Sets the hash density (1/density) of this hashing collection if applicable.
	 * <p>
	 * If this procedure is not applicable for the hash collection (e.g. an immutable hash collection), calling this
	 * method has no effect.
	 *
	 * @param hashDensity the new hash density to be set.
	 * @throws IllegalArgumentException if the passed value would have an effect but is less than or equal to 0.
	 */
	public void setHashDensity(final float hashDensity);

	public HashEqualator<? super E> hashEquality();

	public Analysis<? extends HashCollection<E>> analyze();

	public boolean hasVolatileHashElements();

	public int hashDistributionRange();

	@Override
	public long size();

	/**
	 * Recalculates the hash value of all entries and reorganizes and optimizes the hash storage accordingly.
	 * This method is meant for cases where a hash collection has to collect its elements before proper hash
	 * values can be derived for them or where hash mutable elements can change their hash-relevant state after
	 * having been added (and hashed) in a hash collection.
	 * Note that depending on the hash-relevant state of elements and their changing of it, it can be possible
	 * that some elements oust others, thus decreasing the collection's size. This behavior depends on the
	 * type of the elements and the used hash logic and cannot be compensated by a general purpose collection
	 * implementation.
	 *
	 * @return the new size of the collection which might be lower than before the call.
	 */
	public int rehash();


	public static final float DEFAULT_HASH_FACTOR = 1.0f;

	public static final int   DEFAULT_HASH_LENGTH = 1;



	public class Analysis<H>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final int MAX_TO_STRING_ELEMENT_COUNT = 32;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final H subject;
		private final int size;
		private final float hashDensity;
		private final int slotCount;
		private final int shortestEntryChainLength;
		private final double averageEntryChainLength;
		private final int longestEntryChainLength;
		private final int distributionRange;
		private final XGettingList<KeyValue<Integer, Integer>> chainLengthDistribution;
		private final double distributionEfficienty;
		private final double storageEfficienty;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Analysis(
			final H subject,
			final int size,
			final float hashDensity,
			final int slotCount,
			final int shortestEntryChainLength,
			final int longestEntryChainLength,
			final int distributionRange,
			final XGettingList<KeyValue<Integer, Integer>> chainLengthDistribution
		)
		{
			super();
			this.subject                  = subject                              ;
			this.size                     = size                                 ;
			this.hashDensity              = hashDensity                          ;
			this.slotCount                = slotCount                            ;
			this.shortestEntryChainLength = shortestEntryChainLength             ;
			this.averageEntryChainLength  = (double)size / slotCount             ;
			this.longestEntryChainLength  = longestEntryChainLength              ;
			this.chainLengthDistribution  = chainLengthDistribution              ;
			this.distributionEfficienty   = 1 / this.averageEntryChainLength     ;
			this.storageEfficienty        = (double)slotCount / distributionRange;
			this.distributionRange        = distributionRange                    ;
		}



		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////

		public H getSubject()
		{
			return this.subject;
		}

		public float getHashDensity()
		{
			return this.hashDensity;
		}

		public int getSlotCount()
		{
			return this.slotCount;
		}

		public int getShortestEntryChainLength()
		{
			return this.shortestEntryChainLength;
		}

		public double getAverageEntryChainLength()
		{
			return this.averageEntryChainLength;
		}

		public int getLongestEntryChainLength()
		{
			return this.longestEntryChainLength;
		}

		public XGettingList<KeyValue<Integer, Integer>> getChainLengthDistribution()
		{
			return this.chainLengthDistribution;
		}

		public int getSize()
		{
			return this.size;
		}

		public double getDistributionEfficienty()
		{
			return this.distributionEfficienty;
		}

		public double getStorageEfficienty()
		{
			return this.storageEfficienty;
		}

		public int getDistributionRange()
		{
			return this.distributionRange;
		}


		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			final VarString vc = VarString.New()
			.add("subject: ").add(this.subject.getClass() + " @" + System.identityHashCode(this.subject)).lf()
			.add("size: ").add(this.size).lf()
			.add("hashDensity: ").add(this.hashDensity).lf()
			.add("slotCount: ").add(this.slotCount).lf()
			.add("shortestEntryChainLength: ").add(this.shortestEntryChainLength).lf()
			.add("averageEntryChainLength: ").add(this.averageEntryChainLength).lf()
			.add("longestEntryChainLength: ").add(this.longestEntryChainLength).lf()
			.add("distributionRange: ").add(this.distributionRange).lf()
			.add("distributionEfficienty: " ).add(this.distributionEfficienty).lf()
			.add("storageEfficienty: ").add(this.storageEfficienty).lf()
			.lf()
			.add("slot occupation: ").lf()
			;

			final int[] a = new int[this.distributionRange + 1];
			this.chainLengthDistribution.iterate(new Consumer<KeyValue<Integer, Integer>>()
			{
				@Override
				public void accept(final KeyValue<Integer, Integer> e)
				{
					vc.add(e.key()).add(": ").add(e.value()).append('\t');
					if(e.value() > MAX_TO_STRING_ELEMENT_COUNT)
					{
						vc.add("[many > 32]");
					}
					else
					{
						vc.repeat(e.value(), '|');
					}
					vc.lf();
					for(int i = e.key() + 1; i-- > 1;)
					{
						a[i] += e.value();
					}
				}
			});
			a[0] = this.chainLengthDistribution.at(0).value();
			vc.lf()
			.add("entry distribution: ").lf();
			for(int i = 0; i < a.length; i++)
			{
				vc.add(i == 0 ? "empty" : "rank" + i).add(": ").add(a[i]).append('\t');
				if(a[i] > MAX_TO_STRING_ELEMENT_COUNT)
				{
					vc.add("[many > 32]");
				}
				else
				{
					vc.repeat(a[i], '|');
				}
				vc.lf();
			}

			return vc.toString();
		}

	}

}
