package net.jadoth.persistence.internal;

import static java.lang.System.identityHashCode;

import java.lang.ref.WeakReference;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.exceptions.NumberRangeException;
import net.jadoth.hashing.HashStatisticsBucketBased;
import net.jadoth.math.XMath;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import net.jadoth.persistence.exceptions.PersistenceExceptionNullObjectId;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceAcceptor;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.persistence.types.PersistencePredicate;

public final class ObjectRegistryGrowingRange2 implements PersistenceObjectRegistry
{
	/* Notes:
	 * - funny find: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451 . welcome to this user code class!
	 * - all methods prefixed with "synch" are only called from inside a synchronized context
	 */

	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/**
	 * Default or minimal size for the registry's hash range (hash table array size).
	 * This is intentionally pretty high, because it has to hold a high amount of references
	 * even without a big data model. It has to hold the following references:
	 * <ul>
	 * <li>More than 1000 cached java primitive wrapper instances</li>
	 * <li>All swizzling-relevant types (including abstract classes and interfaces)</li>
	 * <li>All swizzling-relevant constants</li>
	 * <li>All actual data model (entity) instances to be handled</li>
	 * </ul>
	 * Additionally, as the performance of a general "contain everything" registry like this is more important
	 * than small memory footprint, the hash range is better chosen generously high to have even a large
	 * number if entries spread out well.
	 */
	private static final int   MINIMUM_SLOT_LENGTH  = 1024; // 1 << 10
	private static final float DEFAULT_HASH_DENSITY = 1.0f;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int padCapacity(final int desiredSlotLength)
	{
		if(XMath.isGreaterThanOrEqualHighestPowerOf2(desiredSlotLength))
		{
			return XMath.highestPowerOf2_int();
		}
		int slotCount = MINIMUM_SLOT_LENGTH;
		while(slotCount < desiredSlotLength)
		{
			slotCount <<= 1;
		}
		return slotCount;
	}

	private static float positive(final float value) throws NumberRangeException
	{
		if(value > 0f)
		{
			return value;
		}
		throw new NumberRangeException();
	}
	


	public static ObjectRegistryGrowingRange2 New()
	{
		return New(MINIMUM_SLOT_LENGTH);
	}

	public static ObjectRegistryGrowingRange2 New(final int slotSize)
	{
		return New(slotSize, DEFAULT_HASH_DENSITY);
	}

	public static ObjectRegistryGrowingRange2 New(final float hashDensity)
	{
		return New(MINIMUM_SLOT_LENGTH, hashDensity);
	}

	public static ObjectRegistryGrowingRange2 New(
		final int   slotSize   ,
		final float hashDensity
	)
	{
		return new ObjectRegistryGrowingRange2(
			padCapacity(slotSize),
			positive(hashDensity)
		);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private Entry[][] oidHashedEntries; // "primary" slots. See put() and increaseStorage() methods
	private Entry[][] refHashedEntries;
	private int       size       ;
	private float     hashDensity;
	private long      capacity   ;
	private int       hashRange  ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ObjectRegistryGrowingRange2(final int paddedSlotSize, final float hashDensity)
	{
		super();
		// (01.12.2018 TM)XXX: internalReset
		this.oidHashedEntries = new Entry[paddedSlotSize][];
		this.refHashedEntries = new Entry[paddedSlotSize][];
		this.capacity    = (long)(paddedSlotSize * (this.hashDensity = hashDensity));
		this.size        = 0;
		this.hashRange   = paddedSlotSize - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetRefHashedEntries()
	{
		return this.refHashedEntries;
	}

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetOidHashedEntries()
	{
		return this.oidHashedEntries;
	}

	private synchronized boolean synchronizedPut(final long oid, final Object ref)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectRegistryGrowingRange2#synchronizedPut()
//		/* (01.12.2018 TM)XXX: isn't it sub-optimal to check for orphaned entries twice?
//		 * Shouldn't it be better to lookup an existing entry per oid and then handle accordingly?
//		 */
//		if(alreadyRegisteredReference(this.refHashedEntries[identityHashCode(ref) & this.hashRange], oid, ref))
//		{
//			return false;
//		}
//
//		/* At this point, the reference is definitely not contained in the registry.
//		 * Either because it never has been or because there's an orphaned entry with its oid.
//		 * Still, a check for oid collision and replacing an orphaned entries must be done.
//		 */
//		final Entry entry;
//		if((entry = synchLookupEntryPerOid(this.oidHashedEntries[(int)oid & this.hashRange], oid)) != null)
//		{
//			synchRehash(this.refHashedEntries, entry.set(ref), entry);
//			return false;
//		}
//
//		/* At this point, neither the reference nor a fitting orphaned entry is contained in the registry
//		 * and there is no colliding oid, either.
//		 * So a new Entry is created and inserted.
//		 */
//		this.synchPutNewEntry(new Entry(oid, ref));
//		return true;
	}

	private synchronized Object synchronizedAddRef(final long oid, final Object ref) // dirty flag, bua
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectRegistryGrowingRange2#synchronizedAddRef()
//		if(alreadyRegisteredReference(this.refHashedEntries[identityHashCode(ref) & this.hashRange], oid, ref))
//		{
//			return ref;
//		}
//
//		/* at this point, the reference is definitely not contained in the registry.
//		 * either because it never has been or because there's an orphan weak entry with its id.
//		 * Check for colliding id and orphan case
//		 */
//		final Entry entry;
//		if((entry = synchLookupEntryPerOid(this.oidHashedEntries[(int)oid & this.hashRange], oid)) != null)
//		{
//			final Object current;
//			if((current = entry.get()) != null)
//			{
//				return current; // no matter if current is already the same reference or not, just return it (add logic)
//			}
//			synchRehash(this.refHashedEntries, entry.set(ref), entry);
//			return ref;
//		}
//
//		/* at this point, neither the reference nor an orphan entry is not contained in the registry
//		 * and there is no colliding id either.
//		 * So create a new Entry for it
//		 */
//		this.synchPutNewEntry(new Entry(oid, ref));
//		return ref;
	}

	private void synchPutNewEntry(final Entry entry)
	{
		// at this point, the new entry can and will definitely be added. So increase size here and if necessary slots.
		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		// storage is defintely big enough, size has already been incremented, entry can be simply inserted
		synchInsertEntry(this.oidHashedEntries, this.refHashedEntries, entry);
	}

	private void synchIncreaseStorage()
	{
		/* Notes:
		 * - even here, orphan entries are kept because they still represent an oid->tid mapping
		 * - as a consequence, size can not change here and hence is not recalculated and not re-set.
		 * - slotsPerOid potentially contains more entries than slotsPerRef because of empty oid->tid entries
		 * - it is mathematically impossible that rebuilding would require another increase
		 * - recalculating modulo every time when inserting is inefficent here, but more efficient for single puts.
		 */
		if(XMath.isGreaterThanOrEqualHighestPowerOf2(this.oidHashedEntries.length))
		{
			this.capacity = Integer.MAX_VALUE; // disable special case after running into it once (cool 8-) )
			return;
		}
		this.synchRebuild(this.oidHashedEntries.length << 1);
	}
	
	
	/**
	 * Rebuild with maintained hash size to clean up orphan entries.
	 * 
	 */
	private void synchRebuild()
	{
		this.synchRebuild(this.oidHashedEntries.length);
	}

	private void synchRebuild(final int slotLength)
	{
//		XDebug.debugln("rebuilding to length " + slotLength);
		final Entry[][] newSlotsPerOid = new Entry[slotLength][];
		final Entry[][] newSlotsPerRef = new Entry[slotLength][];

		synchRebuildSlots(this.oidHashedEntries, newSlotsPerOid, newSlotsPerRef);

		this.capacity    = (int)(slotLength * this.hashDensity);
		this.oidHashedEntries = newSlotsPerOid;
		this.refHashedEntries = newSlotsPerRef;
		this.hashRange      = slotLength - 1;
//		XDebug.debugln(" * done. new capacity = " + this.capacity);
	}



	private static long lookupOid(final Entry[] bucketsR, final Object object)
	{
		//even during concurrent put, rebuild, clean, this algorithm is thread safe
		// (01.12.2018 TM)FIXME: why? concurrency-safe stack copy of the array?
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].get() == object)
				{
					return bucketsR[i].oid;
				}
			}
		}
		
		return Persistence.nullId();
	}

	private static Object lookupObject(final Entry[] bucketsI, final long oid)
	{
		/* Notes:
		 * - if thread cache is up to date, the read-only algorithm on the slots array is thread-safe and correct
		 * - array length is intentionally not cached due to normally short array length and maybe JIT optimization
		 * - even during concurrent put, rebuild, clean, this algorithm is thread safe
		 * - oids are assumed to be roughly sequential, hence the lower 32 bit are sufficient for proper distribution
		 */
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					return bucketsI[i].get();
				}
			}
		}
				
		return null;
	}

	private static boolean containsOid(final Entry[] bucketsI, final long oid)
	{
		/* Notes:
		 * - if thread cache is up to date, the read-only algorithm on the slots array is thread-safe and correct
		 * - array length is intentionally not cached due to normally short array length and maybe JIT optimization
		 * - even during concurrent put, rebuild, clean, this algorithm is thread safe
		 * - oids are assumed to be roughly sequential, hence the lower 32 bit are sufficient for proper distribution
		 */
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					return true;
				}
			}
		}
		return false;
	}


	private static void validateEntryOid(final Entry entry, final long oid, final Object ref)
	{
		if(entry.oid != oid)
		{
			throw new PersistenceExceptionConsistencyObjectId(ref, entry.oid, oid);
		}
	}

	private static boolean alreadyRegisteredReference(
		final Entry[] bucketsR,
		final long    oid     ,
		final Object  ref
	)
	{
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].get() == ref)
				{
					validateEntryOid(bucketsR[i], oid, ref);
					return true;
				}
			}
		}
		return false;
	}

	private static Entry synchLookupEntryPerOid(final Entry[] bucketsI, final long oid)
	{
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					return bucketsI[i];
				}
			}
		}
		return null;
	}

	private static Entry[] synchEnlargeBuckets(final Entry[] oldBuckets, final Entry entry)
	{
		/* Notes:
		 * - orphan entries are kept intentionally
		 * - bucket length is very unlikely to grow beyond max pow2 int, so no grow length check is performed
		 */
		final Entry[] newBuckets;
		System.arraycopy(oldBuckets, 0, newBuckets = new Entry[oldBuckets.length << 1], 0, oldBuckets.length);
		newBuckets[oldBuckets.length] = entry;
		return newBuckets;
	}

	private static boolean synchUseEmptyBucket(final Entry[] buckets, final Entry entry)
	{
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] == null)
			{
				buckets[i] = entry;
				return true;
			}
		}
		return false;
	}

	private static void synchRehash(final Entry[][] slotsPerRef, final int oldHash, final Entry entry)
	{
		if(oldHash >= 0)
		{
			if(oldHash > 0)
			{
				synchRemoveEntry(slotsPerRef[oldHash & slotsPerRef.length - 1], entry);
			}
			synchPutEntry(slotsPerRef, entry.hash & slotsPerRef.length - 1, entry);
		}
	}

	private static void synchRemoveEntry(final Entry[] buckets, final Entry entry)
	{
		if(buckets == null)
		{
			// can happen via rebuild (e.g. clean up).
			return;
		}
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] == entry)
			{
				buckets[i] = null;
				return;
			}
		}
	}

	private static void synchInsertEntry(final Entry[][] slotsPerOid, final Entry[][] slotsPerRef, final Entry entry)
	{
		synchPutEntry(slotsPerOid, (int)entry.oid & slotsPerOid.length - 1, entry);
		if(!entry.isEmpty())
		{
			// putting in a ref bucket makes no sense for neither hollow nor orphan (= empty) entries.
			synchPutEntry(slotsPerRef, entry.hash & slotsPerOid.length - 1, entry);
		}
	}

	private static Entry[] synchCreateNewBuckets(final Entry entry)
	{
		/* starting at only one bucket is a little inefficient for enlargement but pretty memory-efficient.
		 * As the goal is to produce as few collisions as possible, single-entry buckets is the common case.
		 */
		return new Entry[]{entry};
	}

	private static void synchPutEntry(final Entry[][] slots, final int idx, final Entry entry)
	{
		if(slots[idx] == null)
		{
			// case 1: slot still empty. Create new buckets array.
			slots[idx] = synchCreateNewBuckets(entry);
		}
		else if(!synchUseEmptyBucket(slots[idx], entry)) // case 2: buckets array still has an empty bucket.
		{
			// case 3: buckets array full. Enlarge and insert.
			slots[idx] = synchEnlargeBuckets(slots[idx], entry);
		}
	}

	private static void synchRebuildSlots(
		final Entry[][] oldSlots      ,
		final Entry[][] newSlotsPerOid,
		final Entry[][] newSlotsPerRef
	)
	{
		for(int i = 0; i < oldSlots.length; i++)
		{
			if(oldSlots[i] != null)
			{
				synchRebuildBuckets(oldSlots[i], newSlotsPerOid, newSlotsPerRef);
			}
		}
	}

	private static void synchRebuildBuckets(
		final Entry[]   buckets       ,
		final Entry[][] newSlotsPerOid,
		final Entry[][] newSlotsPerRef
	)
	{
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] != null)
			{
				synchInsertEntry(newSlotsPerOid, newSlotsPerRef, buckets[i]);
			}
		}
	}

	private static void iterateEntries(final Entry[][] slots, final PersistenceAcceptor acceptor)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null)
					{
						acceptor.accept(buckets[b].oid, buckets[b].get());
					}
				}
			}
		}
	}

	private static void synchClearOrphanEntries(final Entry[][] slots)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null && buckets[b].isEmpty())
					{
						buckets[b] = null;
					}
				}
			}
		}
	}
	
	private static void synchClearEntries(final Entry[][] slots, final PersistencePredicate filter)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null && (buckets[b].isEmpty()
						|| filter.test(buckets[b].oid, buckets[b].get()))
					)
					{
						buckets[b] = null;
					}
				}
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

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
		return this.oidHashedEntries.length;
	}

	@Override
	public float hashDensity()
	{
		return this.hashDensity;
	}
	
	@Override
	public synchronized ObjectRegistryGrowingRange2 setHashDensity(final float hashDensity)
	{
		// (28.11.2018 TM)NOTE: this implementation will be replaced soon. No point in improving it.
		throw new net.jadoth.meta.NotImplementedYetError();
//		this.hashDensity = Hashing.hashDensity(hashDensity);
//		// ToDO: recalculate capacity, check for rebuild
//		return this;
	}

	@Override
	public long capacity()
	{
		return this.capacity;
	}

	@Override
	public synchronized void truncate()
	{
		final Entry[][] slotsI = this.oidHashedEntries, slotsR = this.refHashedEntries;
		for(int i = 0; i < slotsI.length; i++)
		{
			slotsI[i] = slotsR[i] = null;
		}
		this.size = 0;
	}

	@Override
	public boolean containsObjectId(final long oid)
	{
		return containsOid(this.synchronizedGetOidHashedEntries()[(int)oid & this.hashRange], oid);
	}

	@Override
	public long lookupObjectId(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		return lookupOid(this.synchronizedGetRefHashedEntries()[identityHashCode(object) & this.hashRange], object);
	}

	@Override
	public Object lookupObject(final long oid)
	{
		return lookupObject(this.synchronizedGetOidHashedEntries()[(int)oid & this.hashRange], oid);
	}

	@Override
	public boolean registerObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}

		return this.synchronizedPut(oid, object);
	}

	@Override
	public Object optionalRegisterObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}
		return this.synchronizedAddRef(oid, object);
	}

	public final synchronized boolean removeObjectById(final long id)
	{
		if(id == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.oidHashedEntries[(int)id & this.hashRange]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == id)
				{
					synchRemoveEntry(this.refHashedEntries[bucketsI[i].hash & this.hashRange], bucketsI[i]);
					bucketsI[i] = null;
					this.size--;
					return true;
				}
			}
		}
		return false;
	}

	public final synchronized boolean removeObject(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.refHashedEntries[identityHashCode(object) & this.hashRange]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].get() == object)
				{
					synchRemoveEntry(this.oidHashedEntries[(int)bucketsI[i].oid & this.hashRange], bucketsI[i]);
					bucketsI[i] = null;
					this.size--;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		iterateEntries(this.synchronizedGetOidHashedEntries(), acceptor);
		return acceptor;
	}

	public synchronized void shrink()
	{
		final int requiredSlotLength;
		if((requiredSlotLength = padCapacity((int)(this.size / this.hashDensity))) >= this.oidHashedEntries.length)
		{
			return; // can't shrink, abort
		}
		this.synchRebuild(requiredSlotLength);
	}

	@Override
	public synchronized void cleanUp()
	{
		this.clearOrphanEntries();
		this.synchRebuild();
	}

	public synchronized void clearOrphanEntries()
	{
		synchClearOrphanEntries(this.oidHashedEntries);
		synchClearOrphanEntries(this.refHashedEntries);
	}
	
	public final synchronized <P extends PersistencePredicate> P removeObjectsBy(final P filter)
	{
		synchClearEntries(this.oidHashedEntries, filter);
		synchClearEntries(this.refHashedEntries, filter);
		this.synchRebuild(this.oidHashedEntries.length);
		return filter;
	}
	
	@Override
	public final synchronized XGettingTable<String, HashStatisticsBucketBased> createHashStatistics()
	{
		// (28.11.2018 TM)NOTE: this implementation will be replaced soon. No point in improving it.
		throw new net.jadoth.meta.NotImplementedYetError();
	}



	///////////////////////////////////////////////////////////////////////////
	// member types     //
	/////////////////////

	private static final class Entry extends WeakReference<Object>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final long oid ;
		      int  hash; // causes 4 byte alignment overhead with and without coops. => 4 byte to spare

		      
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Entry(final long oid, final Object referent)
		{
			super(referent);
			this.oid  = oid;
			this.hash = identityHashCode(referent);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		/**
		 * @return {@code true} if this entry's referent is {@code null} for any reason, {@code false} otherwise.
		 *
		 * @see #isOrphan()
		 */
		final boolean isEmpty()
		{
			return this.get() == null;
		}
	}

}
