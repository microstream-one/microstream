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

public final class ObjectRegistryGrowingRange implements PersistenceObjectRegistry
{
	/* (27.11.2018 TM)TODO: ObjectRegistry housekeeping thread
	 * - optimize() method to trim bucket arrays and random-sample-check for orphans.
	 * - thread with weak back-reference to this registry to make it stop automatically.
	 * - "lastRegister" timestamp to not interrupt registering-heavy phases.
	 * - the usual config values for check intervals etc.
	 * - start() and stop() method in the registry for explicit control.
	 * - a size increase ensures the thread is running, a size of 0 terminates it.
	 */
	
	/* Funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * Welcome to this user code class.
	 */
	
	/* Notes on byte size per entry (+/- COOPS):
	 * - Entry instances occupy 40/64 bytes, including memory padding (12/16 bytes header, 4 references, 1 long, 1 int)
	 * - The hash density specifies the average length of the hash chain in a hash table slot.
	 * - Arrays have a header length of 16/24
	 * - All entries in one hash chain share the bucket array total length and the two hash table slots.
	 * 
	 * Conclusion:
	 * The major memory eater is the rather big JDK WeakReference implementation.
	 * Increasing the hash density up to 16.0 can be reasonable to minimize the implementation's memory consumption.
	 * Blame the rest on the JDK.
	 * 
	 * Hash   |  bytes/entry  | Perf.
	 * density| +COOPS -COOPS |estim.
	 * ------------------------------
	 * ^ anything above is ridiculous
	 *  16,00 |  57,50  84,00 | ~ 55%
	 *   8,00 |  59,00  88,00 | ~ 65%
	 *   4,00 |  62,00  96,00 | ~ 70%
	 *   3,20 |  63,50 100,00 | ~ 75%
	 *   2,00 |  68,00 112,00 | ~ 80%
	 *   1,60 |  71,00 120,00 | ~ 85%
	 *   1,00 |  80,00 144,00 | =100%
	 *   0,80 |  86,00 160,00 | ~105%
	 *   0,50 | 104,00 208,00 | ~110%
	 * v anything below is ridiculous
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
	


	public static ObjectRegistryGrowingRange New()
	{
		return New(MINIMUM_SLOT_LENGTH);
	}

	public static ObjectRegistryGrowingRange New(final int slotSize)
	{
		return New(slotSize, DEFAULT_HASH_DENSITY);
	}

	public static ObjectRegistryGrowingRange New(final float hashDensity)
	{
		return New(MINIMUM_SLOT_LENGTH, hashDensity);
	}

	/**
	 * Note on hashDensity:
	 * Values < 1.0 are hardly worth it.
	 * Values from 1.6 to 8.0 can be a reasonable trade of less memory consumption for acceptably lower performance.
	 * Anything above 16.0 or below 0.5 is ridiculous.
	 * 
	 * @param slotSize
	 * @param hashDensity.
	 * @return
	 */
	public static ObjectRegistryGrowingRange New(
		final int   slotSize   ,
		final float hashDensity
	)
	{
		return new ObjectRegistryGrowingRange(
			padCapacity(slotSize),
			positive(hashDensity)
		);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private Entry[][] oidHashedEntries;
	private Entry[][] refHashedEntries;
	private long      size       ;
	private long      capacity   ;
	private float     hashDensity;
	private int       hashRange  ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ObjectRegistryGrowingRange(final int paddedSlotSize, final float hashDensity)
	{
		super();
		this.oidHashedEntries = new Entry[paddedSlotSize][];
		this.refHashedEntries = new Entry[paddedSlotSize][];
		this.capacity    = (int)(paddedSlotSize * (this.hashDensity = hashDensity));
		this.size        = 0;
		this.hashRange      = paddedSlotSize - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetSlotsPerRef()
	{
		return this.refHashedEntries;
	}

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetSlotsPerOid()
	{
		return this.oidHashedEntries;
	}

	private synchronized boolean synchronizedAdd(final long oid, final Object ref)
	{
		if(this.synchAddCheck(oid, ref))
		{
			return false;
		}

		synchInsertEntry(this.oidHashedEntries, this.refHashedEntries, new Entry(oid, ref), this.hashRange);

		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		return true;
	}
	
	
	private boolean synchAddCheck(final long oid, final Object ref)
	{
		final Entry[] bucketsI;
		if((bucketsI = this.oidHashedEntries[(int)oid & this.hashRange]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].objectId == oid)
				{
					this.synchHandleExisting(i, oid, ref, bucketsI[i]);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void synchHandleExisting(final int oidBucketIndex, final long oid, final Object ref, final Entry entry)
	{
		if(entry.get() == ref)
		{
			return;
		}

		validateReferenceNotYetRegistered(this.refHashedEntries[identityHashCode(ref) & this.hashRange], oid, ref);

		remove(this.refHashedEntries[entry.refHash & this.hashRange], entry);

		final Entry newEntry = this.oidHashedEntries[(int)oid & this.hashRange][oidBucketIndex] = new Entry(oid, ref);
		synchPutEntry(this.refHashedEntries, newEntry.refHash & this.hashRange, newEntry);
	}
	
	private static void validateReferenceNotYetRegistered(
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
					return;
				}
			}
		}
	}
	
	private static void remove(final Entry[] buckets, final Entry entry)
	{
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] == entry)
			{
				buckets[i] = null;
				return;
			}
		}
	}

	private synchronized Object synchronizedAddGet(final long oid, final Object ref) // dirty flag, bua
	{
		final Object alreadyRegistered;
		if((alreadyRegistered = this.synchAddGetCheck(oid, ref)) != null)
		{
			return alreadyRegistered;
		}

		synchInsertEntry(this.oidHashedEntries, this.refHashedEntries, new Entry(oid, ref), this.hashRange);

		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		return ref;
	}
	
	private Object synchAddGetCheck(final long oid, final Object ref)
	{
		final Entry[] bucketsI;
		if((bucketsI = this.oidHashedEntries[(int)oid & this.hashRange]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].objectId == oid)
				{
					final Object object;
					if((object = bucketsI[i].get()) != null)
					{
						return object == ref
							? null
							: object
						;
					}
					this.synchHandleExisting(i, oid, ref, bucketsI[i]);
					return true;
				}
			}
		}
		
		return false;
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
		/* Notes:
		 * - if thread cache is up to date, the read-only algorithm on the slots array is thread-safe and correct
		 * - orphan buckets are intentionally not checked and not removed on read-only lookups
		 * - array length is intentionally not cached due to normally short array length and maybe JIT optimization
		 * - recalculating the modulo here allows the method to be static and skip subject passing
		 * - even during concurrent put, rebuild, clean, this algorithm is thread safe
		 */
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].get() == object)
				{
					return bucketsR[i].objectId;
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
				if(bucketsI[i] != null && bucketsI[i].objectId == oid)
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
				if(bucketsI[i] != null && bucketsI[i].objectId == oid)
				{
					return true;
				}
			}
		}
		return false;
	}


	private static void validateEntryOid(final Entry entry, final long oid, final Object ref)
	{
		if(entry.objectId != oid)
		{
			throw new PersistenceExceptionConsistencyObjectId(ref, entry.objectId, oid);
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
				if(bucketsI[i] != null && bucketsI[i].objectId == oid)
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
		 * - an increment of 4 proved to be highly efficient for reasonable hash densities.
		 */
		final Entry[] newBuckets;
		System.arraycopy(oldBuckets, 0, newBuckets = new Entry[oldBuckets.length + 4], 0, oldBuckets.length);
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
			synchPutEntry(slotsPerRef, entry.refHash & slotsPerRef.length - 1, entry);
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
		synchPutEntry(slotsPerOid, (int)entry.objectId & slotsPerOid.length - 1, entry);
		synchPutEntry(slotsPerRef,     entry.refHash & slotsPerOid.length - 1, entry);
	}
	
	// The parameter order significantly influences performance. Do not change.
	private static void synchInsertEntry(
		final Entry[][] slotsPerOid,
		final Entry[][] slotsPerRef,
		final Entry     entry      ,
		final int       hashRange
	)
	{
		synchPutEntry(slotsPerOid, hashRange & (int)entry.objectId, entry);
		synchPutEntry(slotsPerRef, hashRange &     entry.refHash, entry);
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
			if(buckets[i] != null && !buckets[i].isEmpty())
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
						acceptor.accept(buckets[b].objectId, buckets[b].get());
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
						|| filter.test(buckets[b].objectId, buckets[b].get()))
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
	public synchronized ObjectRegistryGrowingRange setHashDensity(final float hashDensity)
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
		return containsOid(this.synchronizedGetSlotsPerOid()[(int)oid & this.hashRange], oid);
	}

	@Override
	public long lookupObjectId(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		return lookupOid(this.synchronizedGetSlotsPerRef()[identityHashCode(object) & this.hashRange], object);
	}

	@Override
	public Object lookupObject(final long oid)
	{
		return lookupObject(this.synchronizedGetSlotsPerOid()[(int)oid & this.hashRange], oid);
	}

	@Override
	public final boolean registerObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}

		return this.synchronizedAdd(oid, object);
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
		return this.synchronizedAddGet(oid, object);
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
				if(bucketsI[i] != null && bucketsI[i].objectId == id)
				{
					synchRemoveEntry(this.refHashedEntries[bucketsI[i].refHash & this.hashRange], bucketsI[i]);
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
					synchRemoveEntry(this.oidHashedEntries[(int)bucketsI[i].objectId & this.hashRange], bucketsI[i]);
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
		iterateEntries(this.synchronizedGetSlotsPerOid(), acceptor);
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
		
		final long objectId;
		      int  refHash ;

		      
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Entry(final long objectId, final Object referent)
		{
			super(referent);
			this.objectId = objectId;
			this.refHash  = System.identityHashCode(referent);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		/**
		 * @return {@code true} if this entry has lost its referent.
		 *
		 * @see #isEmpty()
		 */
		final boolean isEmpty()
		{
			return this.get() == null;
		}

	}

}
