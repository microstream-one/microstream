package one.microstream.collections;

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

import java.util.HashMap;
import java.util.Map;

import one.microstream.X;
import one.microstream.collections.interfaces.HashCollection;
import one.microstream.collections.interfaces.HashCollection.Analysis;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


public abstract class AbstractChainEntryLinked<E, K, V, EN extends AbstractChainEntryLinked<E, K, V, EN>>
extends AbstractChainEntry<E, K, V, EN>
{
	public static <E, K, V, C extends HashCollection<K>, EN extends AbstractChainEntryLinked<E, K, V, EN>>
	Analysis<C> analyzeSlots(final C hashCollection, final EN[] slots)
	{
		final HashMap<Integer, int[]> distribution = new HashMap<>();

		int emptySlotCount = 0;
		for(EN entry : slots)
		{
			if(entry == null)
			{
				emptySlotCount++;
				continue;
			}

			int chainLength = 1;
			for(entry = entry.link; entry != null; entry = entry.link)
			{
				chainLength++;
			}
			// intentionally dirty int reference hack (local implementation detail)
			final int[] count = distribution.get(chainLength);
			if(count == null)
			{
				distribution.put(chainLength, X.ints(1));
			}
			else
			{
				count[0]++;
			}
		}
		distribution.put(0, X.ints(emptySlotCount));

		final int distRange = distribution.size();
		final LimitList<KeyValue<Integer, Integer>> result = new LimitList<>(distRange);

		int shortestEntryChainLength = Integer.MAX_VALUE;
		int longestEntryChainLength = 0;
		for(final Map.Entry<Integer, int[]> e : distribution.entrySet())
		{
			final int chainLength = e.getKey();
			if(chainLength > 0)
			{
				if(chainLength < shortestEntryChainLength)
				{
					shortestEntryChainLength = chainLength;
				}
				else if(chainLength > longestEntryChainLength)
				{
					longestEntryChainLength = chainLength;
				}
			}
			result.add(X.KeyValue(e.getKey(), e.getValue()[0]));
		}

		// sort by chain length
		XSort.valueSort(
			result.internalGetStorageArray(),
			(kv1, kv2) -> kv1.key().intValue() - kv2.key().intValue()
		);


		return new HashCollection.Analysis<>(
			hashCollection,
			XTypes.to_int(hashCollection.size()),
			hashCollection.hashDensity(),
			slots.length,
			shortestEntryChainLength,
			longestEntryChainLength,
			distRange,
			result.immure()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	EN link; // the next (linked) entry in the hash chain (null for last in hash chain).



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractChainEntryLinked(final EN link)
	{
		super();
		this.link = link;
	}

}
