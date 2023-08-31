package one.microstream.memory;

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

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import one.microstream.exceptions.BufferRegistryException;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;

// note: does not have to be synchronized since it is only used privately in a synchronized parent instance.
public class BufferRegistry
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// crucial since address packing occupies some bits, so the maximum capacity is lower than technically possible.
	private final int maximumCapacityBound;
	
	// "increaseBound" is capacity + 1. "shrinkBound" accordingly.
	private int     hashRange, increaseBound, shrinkBound, currentLowestFreeIndex, size;
	private Entry[] hashTable, indexTable;
	
	/*
	 * Funny thing about this counter:
	 * It's not the actual amount of hollow entries that counts, but the times a hollow entry is encountered,
	 * no matter if it is the same one or a different one. Every hollow encounter means unnecessary effort.
	 * So it is the correct thing to do to count those encounters. Once that number is high enough
	 * (meaning the instance/thread/process has been "bugged out"), a cleanup is appropriate.
	 */
	private long hollowEncounters;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BufferRegistry(final int maximumCapacityBound)
	{
		this(maximumCapacityBound, 1);
	}
	
	BufferRegistry(final int maximumCapacityBound, final int initialCapacityBound)
	{
		super();
		this.maximumCapacityBound = maximumCapacityBound;
		this.createArrays(XHashing.padHashLength(initialCapacityBound));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void createArrays(final int capacity)
	{
		this.updateState(new Entry[capacity], new Entry[capacity], 0);
	}
	
	private void updateState(
		final Entry[] hashTable ,
		final Entry[] indexTable,
		final int     size
	)
	{
		this.hashTable     = hashTable;
		this.indexTable    = indexTable;
		this.increaseBound = hashTable.length;
		this.shrinkBound   = hashTable.length / 2;
		this.hashRange     = hashTable.length - 1;
		this.size          = size;
		this.updateLowestFreeIndex();
	}
	
	private void updateLowestFreeIndex()
	{
		// it is probably faster to determine it from scratch and the end instead of updating it several times
		this.currentLowestFreeIndex = determineLowestFreeIndex(this.indexTable);
	}
	
	private int hash(final ByteBuffer byteBuffer)
	{
		return System.identityHashCode(byteBuffer) & this.hashRange;
	}
	
	private void updateCurrentLowestFreeIndex(final int freeIndex)
	{
		if(freeIndex >= this.currentLowestFreeIndex)
		{
			return;
		}

		this.currentLowestFreeIndex = freeIndex;
	}
	
	private void resetHollowEncounters()
	{
		this.hollowEncounters = 0;
	}
	
	private boolean hasHollowEncounters()
	{
		return this.hollowEncounters > 0;
	}
	
	final int ensureRegistered(final ByteBuffer byteBuffer)
	{
		for(Entry e = this.hashTable[this.hash(byteBuffer)]; e != null; e = e.link)
		{
			if(e.get() == byteBuffer)
			{
				return e.index;
			}
			if(e.isHollow())
			{
				this.hollowEncounters++;
			}
		}
		
		if(++this.size >= this.increaseBound)
		{
			this.checkForIncrementingRebuild();
		}

//		XDebug.println("Registering " + XChars.systemString(byteBuffer) + " of size "+ byteBuffer.capacity());
		
		final int index = this.determineFreeIndex();
		this.indexTable[index] = this.hashTable[this.hash(byteBuffer)] = new Entry(
			byteBuffer,
			index,
			this.hashTable[this.hash(byteBuffer)]
		);
		
		return index;
	}
	
	final ByteBuffer lookupBuffer(final int index)
	{
		return this.indexTable[index] != null
			? this.indexTable[index].get()
			: null
		;
	}
	
	private void checkForIncrementingRebuild()
	{
		if(this.hasHollowEncounters())
		{
			// just clean up the garbage to make at least 1 more space instead of a costly table increase
			this.cleanUp();
		}
		else
		{
			// tables really need to be increased
			this.incrementingRebuild();
		}
	}
	
	private void incrementingRebuild()
	{
		if(this.increaseBound >= this.maximumCapacityBound)
		{
			// rollback preliminary size incrementation to guarantee a consistent state (e.g. for debugging)
			this.size--;

			throw new BufferRegistryException(
				"Buffer registry cannot be increased beyond the specified maximum capacity of "
				+ this.maximumCapacityBound
			);
		}
		
		if(this.increaseBound >= XMath.highestPowerOf2_int())
		{
			// rollback preliminary size incrementation to guarantee a consistent state (e.g. for debugging)
			this.size--;
			
			throw new BufferRegistryException(
				"Buffer registry cannot be increased beyond the technical maximum capacity of "
				+ XMath.highestPowerOf2_int()
			);
			
			/* Note:
			 * It would be possible to leave the hashTable at 2^30 and increase the index table to 2^31-1,
			 * but that would cause considerable code complication for a case that will probably never occur.
			 * If it does, it can be somewhat fixed by implementing that extension.
			 * Of course that only bushes the limit to the int max but does not remove the int limit itself.
			 * It's simply a shame that arrays can't be addressed with longs.
			 */
		}

		// with the corner case out of the way, a simple * 2 suffices.
		this.rebuild(this.increaseBound * 2);
	}
	
	private void cleanUp()
	{
		final Entry[] indexTable = this.indexTable;
		final Entry[] hashTable  = this.hashTable;
		final int     capacity   = hashTable.length;
		
		// load working copy from heap (always funny)
		int size = this.size;
		
		for(int i = 0; i < capacity; i++)
		{
			for(Entry e = hashTable[i], last = null; e != null; e = e.link)
			{
				if(e.isHollow())
				{
					// kick it out consistently (currentLowestFreeIndex NOT updated! See below)
					indexTable[e.index] = null;
					if(last == null)
					{
						hashTable[i] = e.link;
					}
					else
					{
						// sets the link of the same last instance multiple times in case of consecutive hollow entries.
						last.link = e.link;
					}
					size--;
										
					// last remains the same since e gets kicked out
				}
				else
				{
					// only non-hollow entries are accepted as last because hollow ones get kicked out.
					last = e;
				}
			}
		}

		this.updateLowestFreeIndex();
		
		// store to heap
		this.size = size;
		
		// since all hollow entries have been kicked out, the counter must be reset
		this.resetHollowEncounters();
	}
	
	private static int determineLowestFreeIndex(final Entry[] indexTable)
	{
		for(int i = 0; i < indexTable.length; i++)
		{
			if(indexTable[i] == null)
			{
				return i;
			}
		}
		
		// should never be reached
		throw new Error();
	}
	
	private void shrink()
	{
		this.rebuild(this.shrinkBound);
	}
	
	private void rebuild(final int newCapacity)
	{
		final Entry[] oldHashTable = this.hashTable;
		final int     oldCapacity  = oldHashTable.length;
		
		final int     newHashRange = newCapacity - 1;
		final Entry[] newIndxTable = new Entry[newCapacity];
		final Entry[] newHashTable = new Entry[newCapacity];
				
		// load working copy from heap (always funny)
		int size = this.size;
		
		for(int i = 0; i < oldCapacity; i++)
		{
			for(Entry e = oldHashTable[i]; e != null; e = e.link)
			{
				final ByteBuffer bb;
				if((bb = e.get()) == null)
				{
					size--;
				}
				else
				{
					newIndxTable[e.index] = newHashTable[System.identityHashCode(bb) & newHashRange] =
						new Entry(bb, e.index, newHashTable[System.identityHashCode(bb) & newHashRange])
					;
				}
			}
		}
		
		// update instance state
		this.updateState(newHashTable, newIndxTable, size);
		this.resetHollowEncounters();
	}

	private int determineFreeIndex()
	{
		final int currentFreeIndex = this.currentLowestFreeIndex;
		
		final Entry[] indexTable = this.indexTable;
		for(int i = currentFreeIndex + 1; i < indexTable.length; i++)
		{
			if(indexTable[i] == null)
			{
				this.currentLowestFreeIndex = i;
				return currentFreeIndex;
			}
		}
		
		/*
		 * This should never be reached. The reason is a little tricky:
		 * This method is only called in registration logic.
		 * The registration logic ensures sufficient capacity for at least one new element.
		 * So given the currentLowestFreeIndex is consistent, the loop above WILL find
		 * at least one free array slot and never reach here.
		 */
		throw new Error(
			"Inconsistent byte buffer registry: currentLowestFreeIndex = " + this.currentLowestFreeIndex
			+ ", indexTable.length = " + this.indexTable.length + "."
			+ " No free index found." + " Size = " + this.size
		);
	}
	
	private void removeEntry(
		final ByteBuffer byteBuffer,
		final Entry      entry     ,
		final Entry      last
	)
	{
		if(last == null)
		{
			this.hashTable[System.identityHashCode(byteBuffer) & this.hashRange] = entry.link;
		}
		else
		{
			last.link = entry.link;
		}
		
		this.clearIndex(entry.index);
		
		if(--this.size < this.shrinkBound)
		{
			this.checkShrink();
		}
	}
	
	private void checkShrink()
	{
		final Entry[] indexTable = this.indexTable;
		
		final int shrinkBound = this.shrinkBound;
		for(int i = this.increaseBound; --i >= shrinkBound;)
		{
			if(indexTable[i] != null)
			{
				return;
			}
		}
		
		/*
		 * At this point, the upper half of the index table is unused AND the size is less than half the capacity.
		 * This means both tables can safely be shrunk:
		 * - the index table can be replaced by one half its size that can still hold all entries true to their index.
		 * - the hash table can be replaced by one half its size without causing too many collisions.
		 */
		this.shrink();
	}
	
	private void clearIndex(final int index)
	{
		this.indexTable[index] = null;
		this.updateCurrentLowestFreeIndex(index);
	}
	
	final ByteBuffer ensureRemoved(final ByteBuffer byteBuffer)
	{
		for(Entry e = this.hashTable[this.hash(byteBuffer)], last = null; e != null; e = (last = e).link)
		{
			if(e.get() == byteBuffer)
			{
				this.removeEntry(byteBuffer, e, last);
				return byteBuffer;
			}
			if(e.isHollow())
			{
				this.hollowEncounters++;
			}
		}
		
		// ignore not found buffer. Should be no harm.
		return null;
	}
	
	
	
	
	static final class Entry extends WeakReference<ByteBuffer>
	{
		final int index;
		
		Entry link;

		Entry(final ByteBuffer referent, final int index, final Entry nextHashed)
		{
			super(referent);
			this.index = index     ;
			this.link  = nextHashed;
		}
		
		final boolean isHollow()
		{
			return this.get() == null;
		}
		
	}
	
}
