package net.jadoth.persistence.internal;

import static java.lang.System.identityHashCode;
import static net.jadoth.X.notNull;

import java.lang.ref.WeakReference;

import net.jadoth.persistence.exceptions.PersistenceExceptionConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObject;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectRegistry;

public final class BetterObjectRegistry
{
	public static final int minimumBucketLength()
	{
		// below that, the overhead for the quad-bucket-arrays does not pay off and performance gain wouldn't be much.
		return 8;
	}
	
	public static final float defaultHashDensity()
	{
		// start low
		return minimumBucketLength();
	}
	
	public static final int minimumHashRange()
	{
		// Object registries are meant to keep thousands of objects, even in small applications.
		return (1<<10) - 1;
	}
	
	public static final float defaultHashRange()
	{
		// start low
		return minimumHashRange();
	}
	
	public static final int maximumHashRange()
	{
		// technical limitation of Java arrays
		return Integer.MAX_VALUE;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private long[][] oidHashedOidKeysTable;
	private Item[][] oidHashedRefValsTable;
	private Item[][] refHashedRefKeysTable;
	private long[][] refHashedOidValsTable;
	
	/**
	 * A measurement of how "dense" entries are packed in the hashing structure.<br>
	 * Higher density means less memory consumption but also lower performance.<br>
	 * See {@link #minimumHashDensity()}.
	 */
	private float hashDensity;
	
	/**
	 * Due to hashing arithmetic, a hash range is always in the form of 2^n - 1, or in shift-writing: (1<<n) - 1.
	 */
	private int hashRange;
	
	// (27.11.2018 TM)FIXME: not sure its worth it. Rethink.
//	/**
//	 * The array length at which buckets become suspiciously long. Correlates to {@link #hashDensity}.
//	 */
//	private int bucketLengthThreshold;
//	private int currentHighestBucketLength;
	
	/**
	 * The amount of conainable entries before a rebuild is required
	 */
	private long capacity;
	
	/**
	 * The total entry count (including existnig orphan entries)
	 */
	private long size;
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
//	private void updateCurrentHighestBucketLength(final int bucketLength)
//	{
//		if(bucketLength > this.currentHighestBucketLength)
//		{
//			this.currentHighestBucketLength = bucketLength;
//		}
//	}

	public long lookupObjectId(final Object object)
	{
		notNull(object);
		
		// the global lock is held as short as possible while still guaranteeing consistency.
		final Item[] refHashedRefKeys;
		final long[] refHashedOidVals;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			refHashedRefKeys = this.refHashedRefKeysTable[identityHashCode(object) & this.hashRange];
			refHashedOidVals = this.refHashedOidValsTable[identityHashCode(object) & this.hashRange];
		}
		
		// (26.11.2018 TM)NOTE: see "local locking required?" in #lookupObject
		
		for(int i = 0; i < refHashedRefKeys.length; i++)
		{
			// can be null for orphan entries
			if(refHashedRefKeys[i].get() == object)
			{
				return refHashedOidVals[i];
			}
		}
		
		return Persistence.nullId();
	}
	
	public final Object lookupObject(final long objectId)
	{
		// the global lock is held as short as possible while still guaranteeing consistency.
		final long[] oidHashedOidKeys;
		final Item[] oidHashedRefVals;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			oidHashedOidKeys = this.oidHashedOidKeysTable[(int)objectId & this.hashRange];
			oidHashedRefVals = this.oidHashedRefValsTable[(int)objectId & this.hashRange];
		}
		
		/* (26.11.2018 TM)TODO: local locking required?
		 * The key bucket instances could be used as bucket-local locking subjects,
		 * but I am honestly not sure down to the last detail if that is necessary at all.
		 * Aren't the bucket arrays at this point thread-local copies on the stack anyway?
		 * Not getting a state mutation - or preventing one before the read is done - would
		 * be the same thing that a thread local stack-located copy is, anyway: it has
		 * the state at the time of the lock-protected query. A simple happens-before relation
		 * regarding any mutations that occur afterwards.
		 * Or is this wrong? Can the retrieved bucket array here be concurrency changed by another
		 * thread despite being purely stack-based from here on? Maybe caused by an OS interrupt or whatever?
		 * If there turn out to be concurrency issues with this implementation, these details should be clarified
		 */
		
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				return oidHashedRefVals[i].get();
			}
		}
		
		return null;
	}
	
	public boolean containsObjectId(final long objectId)
	{
		// the global lock is held as short as possible while still guaranteeing consistency.
		final long[] oidHashedOidKeys;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			oidHashedOidKeys = this.oidHashedOidKeysTable[(int)objectId & this.hashRange];
		}
		
		// (26.11.2018 TM)NOTE: see "local locking required?" in #lookupObject
		
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public synchronized <A extends PersistenceObjectRegistry.Acceptor> A iterateEntries(final A acceptor)
	{
		// iterating everything has so many accesses that a total lock is the only consistent thing to do.
		
		final long[][] oidHashedOidKeysTable = this.oidHashedOidKeysTable;
		final Item[][] oidHashedRefValsTable = this.oidHashedRefValsTable;
		
		final int oidHashedOidKeysLength = oidHashedOidKeysTable.length;
		
		for(int t = 0; t < oidHashedOidKeysLength; t++)
		{
			final long[] oidHashedOidKeys = oidHashedOidKeysTable[t];
			final Item[] oidHashedRefVals = oidHashedRefValsTable[t];
			
			for(int i = 0; i < oidHashedOidKeys.length; i++)
			{
				final Object instance = oidHashedRefVals[i].get();
				if(instance != null)
				{
					acceptor.accept(oidHashedOidKeys[i], instance);
				}
			}
		}
		
		return acceptor;
	}
	
	public boolean registerObject(final long objectId, final Object object)
	{
		final long[] oidHashedOidKeys;
		final Item[] oidHashedRefVals;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			oidHashedOidKeys = this.oidHashedOidKeysTable[(int)objectId & this.hashRange];
			oidHashedRefVals = this.oidHashedRefValsTable[(int)objectId & this.hashRange];
		}
		
		// quick check for already contained or colliding entry.
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				if(oidHashedRefVals[i].get() == object)
				{
					return false;
				}
				
				final Object alreadyRegistered = oidHashedRefVals[i].get();
				if(alreadyRegistered != null)
				{
					throw new PersistenceExceptionConsistencyObject(objectId, alreadyRegistered, object);
				}

				// Orphan entry. To be handled as if the object is not registered, yet.
			}
		}
		
		// also does a re-check during bucket rebuild
		this.synchronizedRegister(objectId, object);
		
		// check for global rebuild
		this.synchCheckForRebuild();
		
		return true;
	}
	
	private void synchCheckForRebuild()
	{
		// even if some buckets are too long
		if(this.size <= this.capacity)
		{
			return;
		}
		
		this.synchRebuild();
	}
	
	private void synchRebuild()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME BetterObjectRegistry#synchRebuild()
	}
	
	
	private synchronized boolean synchronizedRegister(final long objectId, final Object object)
	{
		if(this.addPerObjectId(objectId, object))
		{
			this.addPerObject(objectId, object);
			return true;
		}
		
		return false;
	}
	
	
	private boolean addPerObjectId(final long objectId, final Object object)
	{
		final long[] oldOidKeys = this.oidHashedOidKeysTable[(int)objectId & this.hashRange];
		final Item[] oldRefVals = this.oidHashedRefValsTable[(int)objectId & this.hashRange];
		final int    oldLength  = oldOidKeys.length;
		
		final long[] newOidKeys = new long[oldLength + 1];
		final Item[] newRefVals = new Item[oldLength + 1];
		
		int t = 0; // target index in the new bucket arrays. Drags behind i for orphan entries.
		for(int i = 0; i < oldLength; i++)
		{
			if(oldOidKeys[i] == objectId)
			{
				if(oldRefVals[i].get() == object)
				{
					// object has been concurrently registered in the meantime, abort.
					return false;
				}
				
				final Object alreadyRegistered = oldRefVals[i].get();
				if(alreadyRegistered != null)
				{
					throw new PersistenceExceptionConsistencyObject(objectId, alreadyRegistered, object);
				}
				
				// subject orphan entry, note and discard (can NOT be consolidated with the general case below!)
				continue;
			}
			
			// check for general orphan case
			if(oldRefVals[i].get() == null)
			{
				// non-subject orphan entry, note and discard.
				continue;
			}
			
			// copy non-orphan non-subject entry to new bucket arrays
			newOidKeys[t] = oldOidKeys[i];
			newRefVals[t] = oldRefVals[i];
			t++;
		}
		
		// if at least orphan was found, the bucket arrays have to be rebuilt again.
		if(t < oldLength)
		{
			this.addNewEntryPerObjectId(truncatePlus1(newOidKeys, t), truncatePlus1(newRefVals, t), objectId, object);
			
			// size change: subtract orphan count, add one for the new entry
			this.size = this.size - oldLength + t + 1;
		}
		else
		{
			this.addNewEntryPerObjectId(newOidKeys, newRefVals, objectId, object);
			
			// size change: no orphans, so just an increment.
			this.size++;
		}
		
		return true;
	}
	
	private void addPerObject(final long objectId, final Object object)
	{
		final Item[] oldRefKeys = this.refHashedRefKeysTable[identityHashCode(object) & this.hashRange];
		final long[] oldOidVals = this.refHashedOidValsTable[identityHashCode(object) & this.hashRange];
		final int    oldLength  = oldRefKeys.length;
		
		final Item[] newRefKeys = new Item[oldLength + 1];
		final long[] newOidVals = new long[oldLength + 1];
		
		int t = 0; // target index in the new bucket arrays. Drags behind i for orphan entries.
		for(int i = 0; i < oldLength; i++)
		{
			if(oldOidVals[i] == objectId)
			{
				if(oldRefKeys[i].get() != null)
				{
					// this may never happen as it means the registry has become inherently inconsistent.
					// (27.11.2018 TM)EXCP: proper exception
					throw new PersistenceExceptionConsistency("Object registry inconsistency for objectId " + objectId);
				}
				
				// subject orphan entry, note and discard (can NOT be consolidated with the general case below!)
				continue;
			}
			
			// check for general orphan case
			if(oldRefKeys[i].get() == null)
			{
				// non-subject orphan entry, note and discard.
				continue;
			}
			
			// copy non-orphan non-subject entry to new bucket arrays
			newRefKeys[t] = oldRefKeys[i];
			newOidVals[t] = oldOidVals[i];
			t++;
		}
		
		// if at least orphan was found, the bucket arrays have to be rebuilt again.
		if(t < oldLength)
		{
			this.addNewEntryPerObject(truncatePlus1(newRefKeys, t), truncatePlus1(newOidVals, t), objectId, object);
			// size change already done by per-oid adding
		}
		else
		{
			this.addNewEntryPerObject(newRefKeys, newOidVals, objectId, object);
			// size change already done by per-oid adding
		}
	}
	
	private static void addNewEntry(
		final long[] oidBucket,
		final Item[] refBucket,
		final long   objectId  ,
		final Object object
	)
	{
		// a new entry is always at the last index
		oidBucket[oidBucket.length - 1] = objectId;
		refBucket[refBucket.length - 1] = new ObjectEntry(object);
	}
	
	private void addNewEntryPerObjectId(
		final long[] oidBucket,
		final Item[] refBucket,
		final long   objectId ,
		final Object object
	)
	{
		addNewEntry(oidBucket, refBucket, objectId, object);
		this.oidHashedOidKeysTable[(int)objectId & this.hashRange] = oidBucket;
		this.oidHashedRefValsTable[(int)objectId & this.hashRange] = refBucket;
	}
	
	private void addNewEntryPerObject(
		final Item[] refBucket,
		final long[] oidBucket,
		final long   objectId ,
		final Object object
	)
	{
		addNewEntry(oidBucket, refBucket, objectId, object);
		
		this.refHashedRefKeysTable[identityHashCode(object) & this.hashRange] = refBucket;
		this.refHashedOidValsTable[identityHashCode(object) & this.hashRange] = oidBucket;
	}
	
	private static long[] truncatePlus1(final long[] array, final int elementCount)
	{
		final long[] newArray = new long[elementCount + 1];
		System.arraycopy(array, 0, newArray, 0, elementCount);
		return newArray;
	}
	
	private static Item[] truncatePlus1(final Item[] array, final int elementCount)
	{
		final Item[] newArray = new Item[elementCount + 1];
		System.arraycopy(array, 0, newArray, 0, elementCount);
		return newArray;
	}
	
	
	
	public interface Item
	{
		public Object get();
		
		public default boolean isOrphan()
		{
			return this.get() == null;
		}
	}
	
	
	static final class ObjectEntry extends WeakReference<Object> implements Item
	{
		ObjectEntry(final Object referent)
		{
			super(referent);
		}
	}
	
	static final class ConstantEntry implements Item
	{
		private final Object referent;
	
		ConstantEntry(final Object referent)
		{
			super();
			this.referent = referent;
		}
		
		@Override
		public final Object get()
		{
			return this.referent;
		}
	}
	
}
