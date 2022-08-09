package one.microstream.collections;

/*-
 * #%L
 * MicroStream Base
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

import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.functional._longIterable;
import one.microstream.functional._longPredicate;
import one.microstream.functional._longProcedure;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;

public interface Set_long extends OptimizableCollection, Composition, _longIterable
{
	public boolean add(long element);

	public boolean contains(long element);

	public void clear();

	public void truncate();

	public Set_long filter(_longPredicate selector);



	public static Set_long New()
	{
		return new Set_long.Default(
			Default.defaultSlotLength(),
			Default.defaultChainLength(),
			Default.defaultChainGrowthFactor()
		);
	}

	public static Set_long New(final int slotSize)
	{
		return new Set_long.Default(
			slotSize,
			Default.defaultChainLength(),
			Default.defaultChainGrowthFactor()
		);
	}

	public static Set_long New(
		final int   slotSize          ,
		final int   chainDefaultLength,
		final float chainGrowthFactor
	)
	{
		return new Set_long.Default(slotSize, chainDefaultLength, chainGrowthFactor);
	}


	public final class Default implements Set_long
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final int defaultSlotLength()
		{
			return 1;
		}

		public static final int defaultChainLength()
		{
			return 1;
		}

		public static final float defaultChainGrowthFactor()
		{
			// grow by 1 for small chains but grow bigger chains by 10%.
			return 1.1f;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long[][] hashSlots;
		private int      hashRange;
		private int      size     ;

		private final int chainInitialLength;
		private final float chainGrowthFactor;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final int slotSize, final int chainDefaultLength, final float chainGrowthFactor)
		{
			super();
			this.hashSlots = new long[(this.hashRange = XMath.pow2BoundCapped(slotSize) - 1) + 1][];
			this.chainInitialLength = XMath.positive(chainDefaultLength);
			this.chainGrowthFactor  = XMath.positive(chainGrowthFactor);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		//////////////////////

		@Override
		public final long size()
		{
			return this.size;
		}

		@Override
		public final boolean isEmpty()
		{
			return this.size == 0;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void rebuild(final int newLength)
		{
			if(this.hashSlots.length >= newLength || newLength <= 0)
			{
				return;
			}

			final int      newRange = newLength - 1;
			final long[][] oldSlots = this.hashSlots;
			final long[][] newSlots = new long[newLength][];

			// chopped nested for loops into separate methods for JIT'ability.
			for(int i = 0; i < oldSlots.length; i++)
			{
				if(oldSlots[i] == null)
				{
					continue;
				}
				this.redistributeElements(newSlots, newRange, oldSlots[i]);
			}

			this.hashSlots = newSlots;
			this.hashRange = newRange;
		}

		private void redistributeElements(final long[][] newSlots, final int newRange, final long[] oldChain)
		{
			for(final long element : oldChain)
			{
				this.addElement(newSlots, hash(element,  newRange), element);
			}
		}

		private static int hash(final long element, final int hashRange)
		{
			return (int)(element & hashRange);
		}

		private boolean addElement(final long[][] hashSlots, final int hashIndex, final long element)
		{
			final long[] chain;
			if((chain = hashSlots[hashIndex]) != null)
			{
				// normal case: search for an empty slot (0L) in an existing chain
				for(int n = 0; n < chain.length; n++)
				{
					if(chain[n] == 0L)
					{
						// found empty slot after the last element
						chain[n] = element;
						return true;
					}
					if(chain[n] == element)
					{
						return false;
					}
				}

				// edge case: chain array is full. Enlarge and add element at the first free index.
				hashSlots[hashIndex] = this.enlargeChain(chain, element);
				return true;
			}

			// corner case: no chain at all, yet
			(hashSlots[hashIndex] = new long[this.chainInitialLength])[0] = element;
			return true;
		}

		private long[] enlargeChain(final long[] array, final long newElement)
		{
			final int newLength = (int)(array.length * this.chainGrowthFactor);

			final long[] newArray = new long[Math.max(newLength, array.length + 1)];
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[array.length] = newElement;

			return newArray;
		}

		@Override
		public final boolean add(final long element)
		{
			if(!this.addElement(this.hashSlots, hash(element, this.hashRange), element))
			{
				return false;
			}

			if(++this.size >= this.hashRange)
			{
				this.rebuild((int)(this.hashSlots.length * 2.0f));
			}

			return true;
		}

		@Override
		public final boolean contains(final long element)
		{
			if(element != 0L)
			{
				for(final long e : this.hashSlots[hash(element, this.hashRange)])
				{
					if(e == element)
					{
						return true;
					}
				}
			}

			return false;
		}

		@Override
		public void iterate(final _longProcedure procedure)
		{
			final long[][] hashSlots = this.hashSlots;
			for(int i = 0; i < hashSlots.length; i++)
			{
				if(hashSlots[i] == null)
				{
					continue;
				}
				for(final long e : hashSlots[i])
				{
					if(e == 0L)
					{
						break;
					}
					procedure.accept(e);
				}
			}
		}

		/**
		 * Optimizes the internal storage and returns the remaining amount of entries.
		 * @return the amount of entries after the optimization is been completed.
		 */
		@Override
		public long optimize()
		{
			this.rebuild(XMath.pow2BoundCapped(this.size));
			return this.size;
		}

		@Override
		public void clear()
		{
			final long[][] slots = this.hashSlots;
			for(int i = 0, len = slots.length; i < len; i++)
			{
				slots[i] = null;
			}
			this.size = 0;
		}

		@Override
		public void truncate()
		{
			this.hashSlots = new long[1][];
			this.size = 0;
		}

		@Override
		public Set_long.Default filter(final _longPredicate selector)
		{
			final Set_long.Default result = new Set_long.Default(1, this.chainInitialLength, this.chainGrowthFactor);

			this.iterate(e ->
			{
				if(selector.test(e))
				{
					result.add(e);
				}
			});

			return result;
		}

	}

}
