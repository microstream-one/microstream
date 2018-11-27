package net.jadoth.persistence.internal;

import static java.lang.System.identityHashCode;
import static net.jadoth.X.notNull;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import net.jadoth.hashing.Hashing;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObject;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectRegistry;

public final class DefaultObjectRegistry implements PersistenceObjectRegistry
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
	/**
	 * Object registries are meant to keep thousands of objects, even in small applications.
	 * So anything below ~1000 is just a redundant excercise in initial array copying, especially
	 * considering the numerous constant instanes that must be registered
	 */
	private static final int MINIMUM_HASH_LENGTH = 1<<10; // 1024
	
	/**
	 * (Technical) magic value. Cannot be higher due to hashing bit arithmetic.
	 */
	private static final int MAXIMUM_HASH_LENGTH = 1<<30; // 1073741824, highest power-of-2 int value
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final int padHashLength(final long desiredHashLength)
	{
		if(desiredHashLength >= MAXIMUM_HASH_LENGTH)
		{
			return MAXIMUM_HASH_LENGTH;
		}
		
		int capacity = MINIMUM_HASH_LENGTH;
		while(capacity < desiredHashLength)
		{
			capacity <<= 1;
		}
		
		return capacity;
	}
	
	public static final float padHashDensity(final float desiredHashDensity)
	{
		if(Hashing.hashDensity(desiredHashDensity) < minimumHashDensity())
		{
			return minimumHashDensity();
		}
		
		// the desired hash density is valid, so it can be used directly
		return desiredHashDensity;
	}
	
	public static final float minimumHashDensity()
	{
		// below that, the overhead for the quad-bucket-arrays does not pay off and performance gain wouldn't be much.
		return 8.0f;
	}
	
	public static final float defaultHashDensity()
	{
		// start low
		return minimumHashDensity();
	}
	
	public static final int minimumHashLength()
	{
		return MINIMUM_HASH_LENGTH;
	}
	
	public static final int defaultHashLength()
	{
		// start low
		return MINIMUM_HASH_LENGTH;
	}
	
	public static final int maximumHashLength()
	{
		return MAXIMUM_HASH_LENGTH;
	}
	
	
	
	public static final DefaultObjectRegistry New()
	{
		return New(defaultHashDensity());
	}
	
	public static final DefaultObjectRegistry New(final float desiredHashDensity)
	{
		// there is no point in supporting a desired initial capacity when there's a capacity low bound for the size.
		
		final float hashDensity  = padHashDensity(desiredHashDensity);
		final int   hashLength   = defaultHashLength();
		final int   hashRange    = hashLength - 1;
		final long  capacityHigh = (long)(hashLength * hashDensity);
		final long  capacityLow  = 0;
		final long  size         = 0;
		
		return new DefaultObjectRegistry(
			new long[hashLength][],
			new Item[hashLength][],
			new Item[hashLength][],
			new long[hashLength][],
			hashLength            ,
			hashDensity           ,
			hashRange             ,
			capacityLow           ,
			capacityHigh          ,
			size
		);
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
	private final float hashDensity;
	
	/**
	 * The current length of the hash table master arrays. All four have always the same length.
	 */
	private int hashLength;
	
	/**
	 * Due to hashing arithmetic, a hash range is always in the form of 2^n - 1, or in shift-writing: (1<<n) - 1.
	 */
	private int hashRange;
		
	/**
	 * The amount of containable entries before a rebuild to increase the storage is required.
	 */
	private long capacityHigh;
	
	/**
	 * The amount of containable entries before a rebuild to decrease the storage is required.
	 */
	private long capacityLow;
	
	/**
	 * The total entry count (including existnig orphan entries)
	 */
	private long size;
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	DefaultObjectRegistry(
		final long[][] oidHashedOidKeysTable,
		final Item[][] oidHashedRefValsTable,
		final Item[][] refHashedRefKeysTable,
		final long[][] refHashedOidValsTable,
		final int      hashLength           ,
		final float    hashDensity          ,
		final int      hashRange            ,
		final long     capacityHigh         ,
		final long     capacityLow          ,
		final long     size
	)
	{
		super();
		this.oidHashedOidKeysTable = oidHashedOidKeysTable;
		this.oidHashedRefValsTable = oidHashedRefValsTable;
		this.refHashedRefKeysTable = refHashedRefKeysTable;
		this.refHashedOidValsTable = refHashedOidValsTable;
		this.hashLength            = hashLength           ;
		this.hashDensity           = hashDensity          ;
		this.hashRange             = hashRange            ;
		this.capacityHigh          = capacityHigh         ;
		this.capacityLow           = capacityLow          ;
		this.size                  = size                 ;
	}

	private int hash(final long objectId)
	{
		/* (27.11.2018 TM)TODO: test and comment hashing performance
		 * - hash(){ (int)objectId & this.hashRange
		 * - hash(){ (int)(objectId & this.hashRange)
		 * - hash(){ (int)(objectId & this.hashRangeLong)
		 * - inlined (int)objectId & this.hashRange
		 * - inlined (int)(objectId & this.hashRange)
		 * - inlined (int)(objectId & this.hashRangeLong)
		 * 
		 * Also cover rehashByObjectId()
		 */
		
		// an sign bit accidentally caused by the cast is irrelevant since the hash range will always suppress it.
		return (int)objectId & this.hashRange;
	}
	
	private int hash(final Object object)
	{
		/* (27.11.2018 TM)TODO: test and comment hashing performance
		 * - hash(){ System.identityHashCode(object) & this.hashRange
		 * - inlined System.identityHashCode(object) & this.hashRange
		 */
		
		// an sign bit accidentally caused by the cast is irrelevant since the hash range will always suppress it.
		return System.identityHashCode(object) & this.hashRange;
	}
	
	@Override
	public long lookupObjectId(final Object object)
	{
		notNull(object);
		
		final Item[] refHashedRefKeys;
		final long[] refHashedOidVals;
		synchronized(this)
		{
			// both must be queried under protection of the same lock to guarantee consistency.
			refHashedRefKeys = this.refHashedRefKeysTable[this.hash(object)];
			if(refHashedRefKeys == null)
			{
				return Persistence.nullId();
			}
			
			refHashedOidVals = this.refHashedOidValsTable[this.hash(object)];
		}
		
		// bucket arrays are effectively immutable, so the rest does not require a lock.
		for(int i = 0; i < refHashedRefKeys.length; i++)
		{
			// can be null for orphan entries
			if(refHashedRefKeys[i].get() == object)
			{
				return refHashedOidVals[i];
			}
		}
		
		// since null can never be contained, returning the null-id signals a miss.
		return Persistence.nullId();
	}
	
	@Override
	public final Object lookupObject(final long objectId)
	{
		final long[] oidHashedOidKeys;
		final Item[] oidHashedRefVals;
		synchronized(this)
		{
			// both must be queried under protection of the same lock to guarantee consistency.
			oidHashedOidKeys = this.oidHashedOidKeysTable[this.hash(objectId)];
			if(oidHashedOidKeys == null)
			{
				return null;
			}
			
			oidHashedRefVals = this.oidHashedRefValsTable[this.hash(objectId)];
		}

		// bucket arrays are effectively immutable, so the rest does not require a lock.
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				return oidHashedRefVals[i].get();
			}
		}

		// since null can never be contained, returning the null signals a miss.
		return null;
	}
	
	@Override
	public boolean containsObjectId(final long objectId)
	{
		final long[] oidHashedOidKeys;
		synchronized(this)
		{
			// must be queried under protection of a lock to guarantee consistency.
			oidHashedOidKeys = this.oidHashedOidKeysTable[this.hash(objectId)];
			if(oidHashedOidKeys == null)
			{
				return false;
			}
		}

		// bucket arrays are effectively immutable, so the rest does not require a lock.
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				return true;
			}
		}

		// signal objectId not found.
		return false;
	}
	
	@Override
	public synchronized <A extends PersistenceObjectRegistry.Acceptor> A iterateEntries(final A acceptor)
	{
		// iterating everything has so many bucket accesses that a total lock is the only viable thing to do.
		
		final long[][] oidHashedOidKeysTable = this.oidHashedOidKeysTable;
		final Item[][] oidHashedRefValsTable = this.oidHashedRefValsTable;
		
		final int oidHashedOidKeysLength = oidHashedOidKeysTable.length;
		
		for(int h = 0; h < oidHashedOidKeysLength; h++)
		{
			final long[] oidHashedOidKeys = oidHashedOidKeysTable[h];
			if(oidHashedOidKeys == null)
			{
				continue;
			}
			
			final Item[] oidHashedRefVals = oidHashedRefValsTable[h];
			
			for(int i = 0; i < oidHashedOidKeys.length; i++)
			{
				// might be an orphan item with a hollow weak reference.
				final Object instance = oidHashedRefVals[i].get();
				if(instance != null)
				{
					acceptor.accept(oidHashedOidKeys[i], instance);
				}
			}
		}
		
		return acceptor;
	}
	
	@Override
	public long size()
	{
		return this.size;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public int hashRange()
	{
		return this.hashRange;
	}

	@Override
	public float hashDensity()
	{
		return this.hashDensity;
	}

	@Override
	public long capacity()
	{
		return this.capacityHigh;
	}
		
	@Override
	public synchronized boolean registerObject(final long objectId, final Object object)
	{
		// both branches must use the SAME Item instance to reduce memory consumption
		final Item newItem = this.synchAddPerObjectId(objectId, object);
		if(newItem == null)
		{
			return false;
		}

		// the second branch must be changed accordingly
		this.synchAddPerObject(objectId, newItem);

		// check for global rebuild after entries have changed (more or even fewer because of removed orphans)
		this.synchCheckForRebuild();
		
		return true;
	}

	@Override
	public Object optionalRegisterObject(final long objectId, final Object object)
	{
		// FIXME DefaultObjectRegistry#optionalRegisterObject()
		throw new net.jadoth.meta.NotImplementedYetError();
	}
	
	@Override
	public final synchronized void cleanUp()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME DefaultObjectRegistry#cleanUp()
	}

	/* (27.11.2018 TM)FIXME: orphan management
	 * - check orphan count (rebuilds if too high)
	 * - quick check orphan count (a few random samples as an estimate)
	 * - there is no explicit orphan removal as this is done implicitely by the rebuild
	 */
	
	@Override
	public final synchronized void clearOrphanEntries()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME DefaultObjectRegistry#clearOrphanEntries()
	}

	@Override
	public final synchronized void shrink()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME DefaultObjectRegistry#shrink()
	}
	
	// removing //
	
	@Override
	public final synchronized boolean removeObjectById(final long id)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME DefaultObjectRegistry#removeObjectById()
	}

	@Override
	public final synchronized boolean removeObject(final Object object)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME DefaultObjectRegistry#removeObject()
	}

	@Override
	public final synchronized void clear()
	{
		this.clearAll();
		// (27.11.2018 TM)FIXME: JET-48: reregister constants
	}
	
	public final synchronized void clearAll()
	{
		// there is no point in keeping the old hash table arrays when there's a capacity low bound.
		final int hashLength = defaultHashLength();
		this.oidHashedOidKeysTable = new long[hashLength][];
		this.oidHashedRefValsTable = new Item[hashLength][];
		this.refHashedRefKeysTable = new Item[hashLength][];
		this.refHashedOidValsTable = new long[hashLength][];
		this.hashLength   = hashLength;
		this.hashRange    = hashLength - 1;
		this.capacityLow  = 0;
		this.capacityHigh = (long)(hashLength * this.hashDensity);
		this.size         = 0;
	}
	
	@Override
	public final synchronized void clear(final Predicate<? super PersistenceObjectRegistry.Entry> filter)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME DefaultObjectRegistry#clear()
	}
		
	
	
	private Item synchAddPerObjectId(final long objectId, final Object object)
	{
		final long[] oldOidKeys = this.oidHashedOidKeysTable[this.hash(objectId)];
		if(oldOidKeys == null)
		{
			return this.synchAddPerObjectIdInNewBucket(objectId, object);
		}
		
		final Item[] oldRefVals = this.oidHashedRefValsTable[this.hash(objectId)];
		final int    oldLength  = oldOidKeys.length;
		
		final long[] newOidKeys = new long[oldLength + 1];
		final Item[] newRefVals = new Item[oldLength + 1];
		
		int t = 0; // target index in the new bucket arrays. Drags behind i for found orphan entries.
		for(int i = 0; i < oldLength; i++)
		{
			if(oldOidKeys[i] == objectId)
			{
				if(oldRefVals[i].get() == object)
				{
					// object is already registered, abort.
					return null;
				}
				
				final Object alreadyRegistered = oldRefVals[i].get();
				if(alreadyRegistered != null)
				{
					throw new PersistenceExceptionConsistencyObject(objectId, alreadyRegistered, object);
				}
				
				// subject orphan entry, discard (can NOT be consolidated with the general case below!)
				continue;
			}
			
			// check for general orphan case
			if(oldRefVals[i].get() == null)
			{
				// non-subject orphan entry, discard.
				continue;
			}
			
			// copy non-orphan non-subject entry to new bucket arrays
			newOidKeys[t] = oldOidKeys[i];
			newRefVals[t] = oldRefVals[i];
			t++;
		}
		
		final Item item = new Item(object);
		
		// if at least one orphan was found, the bucket arrays have to be rebuilt again.
		if(t < oldLength)
		{
			this.addNewEntryPerObjectId(truncatePlus1(newOidKeys, t), truncatePlus1(newRefVals, t), objectId, item);
			
			// size change: orphan count is subtracted, the new entry adds one.
			this.size = this.size - oldLength + t + 1;
		}
		else
		{
			this.addNewEntryPerObjectId(newOidKeys, newRefVals, objectId, item);
			
			// size change: no orphans, so just an increment.
			this.size++;
		}
		
		return item;
	}
	
	private void synchAddPerObject(final long objectId, final Item newItem)
	{
		final int    hashIndex  = this.hash(newItem.get());
		final Item[] oldRefKeys = this.refHashedRefKeysTable[hashIndex];
		if(oldRefKeys == null)
		{
			this.synchAddPerObjectInNewBucket(objectId, newItem, hashIndex);
			return;
		}
		
		final long[] oldOidVals = this.refHashedOidValsTable[hashIndex];
		final int    oldLength  = oldOidVals.length;
		
		final Item[] newRefKeys = new Item[oldLength + 1];
		final long[] newOidVals = new long[oldLength + 1];
		
		int t = 0; // target index in the new bucket arrays. Drags behind i for found orphan entries.
		for(int i = 0; i < oldLength; i++)
		{
			if(oldOidVals[i] == objectId)
			{
				// can only be a subject orphan entry as the other cases would have been handled before entering here.
				continue;
			}
			
			// check for general orphan case
			if(oldRefKeys[i].get() == null)
			{
				// non-subject orphan entry, discard.
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
			this.addNewEntryPerObject(truncatePlus1(newRefKeys, t), truncatePlus1(newOidVals, t), objectId, newItem, hashIndex);
			// size change already done by per-oid adding. Orphans not accounted for there are neglected here.
		}
		else
		{
			this.addNewEntryPerObject(newRefKeys, newOidVals, objectId, newItem, hashIndex);
			// size change already done by per-oid adding
		}
	}
	
	private Item synchAddPerObjectIdInNewBucket(final long objectId, final Object object)
	{
		final Item newItem = new Item(object);
		
		this.oidHashedOidKeysTable[this.hash(objectId)] = new long[]{objectId};
		this.oidHashedRefValsTable[this.hash(objectId)] = new Item[]{newItem};
		this.size++;
		
		return newItem;
	}
	
	private void synchAddPerObjectInNewBucket(final long objectId, final Item newItem, final int hashIndex)
	{
		this.refHashedRefKeysTable[hashIndex] = new Item[]{newItem};
		this.refHashedOidValsTable[hashIndex] = new long[]{objectId};
		// size change already done by per-oid adding
	}
	
	private static void addNewEntry(
		final long[] oidBucket,
		final Item[] refBucket,
		final long   objectId ,
		final Item   item
	)
	{
		// a new entry is always at the last index
		oidBucket[oidBucket.length - 1] = objectId;
		refBucket[refBucket.length - 1] = item;
	}
		
	private void addNewEntryPerObjectId(
		final long[] oidBucket,
		final Item[] refBucket,
		final long   objectId ,
		final Item   object
	)
	{
		addNewEntry(oidBucket, refBucket, objectId, object);
		this.oidHashedOidKeysTable[this.hash(objectId)] = oidBucket;
		this.oidHashedRefValsTable[this.hash(objectId)] = refBucket;
	}
	
	private void addNewEntryPerObject(
		final Item[] refBucket,
		final long[] oidBucket,
		final long   objectId ,
		final Item   item     ,
		final int    hashIndex
	)
	{
		addNewEntry(oidBucket, refBucket, objectId, item);
		this.refHashedRefKeysTable[hashIndex] = refBucket;
		this.refHashedOidValsTable[hashIndex] = oidBucket;
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
	
	private void synchCheckForRebuild()
	{
		if(this.size > this.capacityHigh)
		{
			// increasing rebuild required because the hash table became too small (or entries distribution too dense).
			this.synchRebuild();
		}
		
		if(this.size < this.capacityLow)
		{
			// decreasing rebuild required because the hash table became unnecessarily big.
			this.synchRebuild();
		}
		
		// otherwise, no rebuild is required.
	}
	
	private void synchRebuild()
	{
		// locally cached old references / values.
		final int oldHashLength = this.hashLength;
		final long[][] oldOidKeysTable = this.oidHashedOidKeysTable;
		final Item[][] oldRefValsTable = this.oidHashedRefValsTable;
		
		// locally created new references / values.
		final int newHashLength = padHashLength((long)(this.size / this.hashDensity));
		final int newHashRange  = newHashLength - 1;
		final long newCapacityHigh = this.calculateCapacityFromHashLength(newHashLength);
		final long newCapacityLow  = newHashLength == MINIMUM_HASH_LENGTH
			? 0
			: this.calculateCapacityFromHashLength(newHashLength / 2)
		;
		final long[][] newOidKeysTable = new long[newHashLength][];
		final Item[][] newRefValsTable = new Item[newHashLength][];
		final Item[][] newRefKeysTable = new Item[newHashLength][];
		final long[][] newOidValsTable = new long[newHashLength][];
		
		// this is also the only opportunity to recalculate the actual size
		long size = 0;
		
		// both new branches are populated from the per-oid-branch
		for(int h = 0; h < oldHashLength; h++)
		{
			final long[] oldOidKeys = oldOidKeysTable[h];
			if(oldOidKeys == null)
			{
				continue;
			}
			
			final Item[] oldRefVals = oldRefValsTable[h];
			for(int i = 0; i < oldRefVals.length; i++)
			{
				if(oldRefVals[i].get() == null)
				{
					continue;
				}
				
				final long oid = oldOidKeys[i];
				final Item ref = oldRefVals[i];
				populateByObjectId(newOidKeysTable, newRefValsTable, (int)oid & newHashRange, oid, ref);
				populateByObject(newRefKeysTable, newOidValsTable, identityHashCode(ref.get()) & newHashRange, oid, ref);
				size++;
			}
		}

		// registry state gets switched over from old to new
		this.oidHashedOidKeysTable = newOidKeysTable;
		this.oidHashedRefValsTable = newRefValsTable;
		this.refHashedRefKeysTable = newRefKeysTable;
		this.refHashedOidValsTable = newOidValsTable;
		this.hashLength            = newHashLength  ;
		this.hashRange             = newHashRange   ;
		this.capacityHigh          = newCapacityHigh;
		this.capacityLow           = newCapacityLow ;
		this.size                  = size           ;
		// hash density remains the same
	}
	
	private long calculateCapacityFromHashLength(final int hashLength)
	{
		return (long)(hashLength * this.hashDensity);
	}
	
	private static void populateByObjectId(
		final long[][] oidKeysTable,
		final Item[][] refValsTable,
		final int      hashIndex   ,
		final long     oid         ,
		final Item     item
	)
	{
		oidKeysTable[hashIndex] = newOidsBucket(oidKeysTable[hashIndex], oid);
		refValsTable[hashIndex] = newRefsBucket(refValsTable[hashIndex], item);
	}
	
	private static void populateByObject(
		final Item[][] refKeysTable,
		final long[][] oidValsTable,
		final int      hashIndex   ,
		final long     oid         ,
		final Item     item
	)
	{
		refKeysTable[hashIndex] = newRefsBucket(refKeysTable[hashIndex], item);
		oidValsTable[hashIndex] = newOidsBucket(oidValsTable[hashIndex], oid);
	}
	
	private static final long[] newOidsBucket(final long[] oids, final long oid)
	{
		if(oids == null)
		{
			return new long[]{oid};
		}
		
		final long[] newBucket = new long[oids.length];
		System.arraycopy(oids, 0, newBucket, 0, oids.length);
		newBucket[newBucket.length - 1] = oid;
		return newBucket;
	}
	
	private static final Item[] newRefsBucket(final Item[] refs, final Item item)
	{
		if(refs == null)
		{
			return new Item[]{item};
		}
		
		final Item[] newBucket = new Item[refs.length];
		System.arraycopy(refs, 0, newBucket, 0, refs.length);
		newBucket[newBucket.length - 1] = item;
		return newBucket;
	}
		
		
	
	static final class Item extends WeakReference<Object>
	{
		Item(final Object referent)
		{
			super(referent);
		}
	}
	
}
