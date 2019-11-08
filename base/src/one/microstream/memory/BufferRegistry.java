package one.microstream.memory;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import one.microstream.hashing.XHashing;

public class BufferRegistry
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int     hashRange, capacity, shrinkBound, currentLowestFreeIndex, size;
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

	BufferRegistry()
	{
		this(1);
	}
	
	BufferRegistry(final int initialCapacity)
	{
		super();
		this.createArrays(XHashing.padHashLength(initialCapacity));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void createArrays(final int capacity)
	{
		this.update(new Entry[capacity], new Entry[capacity]);
	}
	
	private void update(final Entry[] hashTable, final Entry[] indexTable)
	{
		this.hashTable   = hashTable;
		this.indexTable  = indexTable;
		this.capacity    = hashTable.length;
		this.shrinkBound = hashTable.length / 2;
		this.hashRange   = hashTable.length - 1;
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
		
		if(++this.size >= this.capacity)
		{
			this.checkForIncrementingRebuild();
		}
		
		final int index = this.determineFreeIndex();
		this.indexTable[index] = this.hashTable[this.hash(byteBuffer)] = new Entry(
			byteBuffer,
			index,
			this.hashTable[this.hash(byteBuffer)]
		);
		
		return index;
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
		// (08.11.2019 TM)FIXME: priv#111: incrementingRebuild
		this.resetHollowEncounters();
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
			for(Entry e = hashTable[i], last = null; e != null; e = (last = e).link)
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
						last.link = e.link;
					}
					size--;
				}
			}
		}
		
		// it is probably faster to determine it from scratch and the end instead of updating it several times
		this.currentLowestFreeIndex = determineLowestFreeIndex(indexTable);
		
		// store to heap (always funny)
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
		final Entry[] indexTable = this.indexTable;
		final Entry[] hashTable  = this.hashTable;
		final int     capacity   = hashTable.length;
		
		final int     newCapacity  = this.shrinkBound;
		final int     newHashRange = newCapacity - 1;
		final Entry[] newIndxTable = new Entry[newCapacity];
		final Entry[] newHashTable = new Entry[newCapacity];
		
		// load working copy from heap (always funny)
		int size = this.size;
		
		for(int i = 0; i < capacity; i++)
		{
			for(Entry e = hashTable[i]; e != null; e = e.link)
			{
				final ByteBuffer bb;
				if((bb = e.get()) == null)
				{
					size--;
				}
				else
				{
					final unfug, weil die indices final nach außen fest sind.
					newIndxTable[e.index] = newHashTable[System.identityHashCode(bb) & newHashRange] =
						new Entry(bb, e.index, newHashTable[System.identityHashCode(bb) & newHashRange])
					;
				}
			}
		}
		
		// it is probably faster to determine it from scratch and the end instead of updating it several times
		final int currentLowestFreeIndex = determineLowestFreeIndex(indexTable);
		
		// (08.11.2019 TM)FIXME: priv#111: shrink
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
		throw new Error("Inconsistent byte buffer registry");
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
		for(int i = this.capacity; --i >= shrinkBound;)
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
	
	final void remove(final ByteBuffer byteBuffer)
	{
		for(Entry e = this.hashTable[this.hash(byteBuffer)], last = null; e != null; e = (last = e).link)
		{
			if(e.get() == byteBuffer)
			{
				this.removeEntry(byteBuffer, e, last);
			}
			if(e.isHollow())
			{
				this.hollowEncounters++;
			}
		}
		
		
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
