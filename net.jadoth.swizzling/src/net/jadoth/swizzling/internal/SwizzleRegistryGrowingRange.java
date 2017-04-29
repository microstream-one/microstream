package net.jadoth.swizzling.internal;

import static java.lang.System.identityHashCode;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import net.jadoth.collections.HashMapIdId;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.exceptions.NumberRangeException;
import net.jadoth.math.JadothMath;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyInvalidTypeId;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyObject;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyObjectId;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyTid;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyUnknownMapping;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyUnknownType;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyWrongType;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyWrongTypeId;
import net.jadoth.swizzling.exceptions.SwizzleExceptionNullObjectId;
import net.jadoth.swizzling.exceptions.SwizzleExceptionNullTypeId;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.util.KeyValue;

public final class SwizzleRegistryGrowingRange implements SwizzleRegistry
{
	/* Notes:
	 * - funny find: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451 . welcome to this user code class!
	 * - all methods prefixed with "synch" are only called from inside a synchronized context
	 */

	/* Memory demands per entry:
	 * A) 64 bit coops
	 *  08 2*4 byte in hashing arrays
	 *  08 2*4 byte in buckets arrays
	 *  08 entry instance object header
	 *  04 int hash
	 *  08 long oid
	 *  08 long tid
	 *  04 coop ref (to WeakReference instance)
	 *  04 alignment overhead
	 *  32 WeakReference instance (08 + 4*coop + alignment overhead 4)
	 * ---------------------------------
	 *  80 total size
	 *
	 * B) 64 bit full oops
	 *  16 2*8 byte in hashing arrays
	 *  16 2*8 byte in buckets arrays
	 *  16 entry instance object header
	 *  08 long oid
	 *  08 long tid
	 *  04 int hash
	 *  04 primitive alignment overhead
	 *  08 oop ref (to WeakReference instance)
	 *  48 WeakReference instance (16 + 4*oop + no alignment overhead)
	 * ---------------------------------
	 * 128 total size
	 *
	 * Note: in both modes, there are 4 bytes to spare per entry. Maybe useful for future flags etc.
	 */

	/* maybe future to-do: SwizzleRegistryFixedRange
	 * Once stable, derive a SwizzleRegistryFixedRange implementation from this class.
	 * Hash range (slots length) is set once in the constructor and is then unchangeable (final arrays).
	 * Advantage: avoids unnecessary increase checks, probably easier (= faster) to get it thread safe.
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
		if(JadothMath.isGreaterThanOrEqualHighestPowerOf2Integer(desiredSlotLength))
		{
			// (16.04.2016)TODO: why isn't this max integer? See general purpose hash collections
			return JadothMath.highestPowerOf2Integer();
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



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry[][] slotsPerOid; // "primary" slots. See put() and increaseStorage() methods
	private Entry[][] slotsPerRef;
	private int       size       ;
	private float     hashDensity;
	private int       capacity   ;
	private int       modulo     ; // shortcut for "slots.length - 1" (yields around 3% performance in put tests)
	
	private long      typeCount; // can never decrease as runtime types never vanish.

	private final Consumer<SwizzleTypeLink<?>> typeExistsValidator = new Consumer<SwizzleTypeLink<?>>()
	{
		@Override
		public void accept(final SwizzleTypeLink<?> e)
		{
			SwizzleRegistryGrowingRange.this.validateExistingMapping(e.type(), e.typeId());
		}
	};

	private final Consumer<SwizzleTypeLink<?>> typePossibleValidator = new Consumer<SwizzleTypeLink<?>>()
	{
		@Override
		public void accept(final SwizzleTypeLink<?> e)
		{
			SwizzleRegistryGrowingRange.this.validatePossibleMapping(e.type(), e.typeId());
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SwizzleRegistryGrowingRange()
	{
		this(MINIMUM_SLOT_LENGTH);
	}

	public SwizzleRegistryGrowingRange(final int slotSize)
	{
		this(slotSize, DEFAULT_HASH_DENSITY);
	}

	public SwizzleRegistryGrowingRange(final float hashDensity)
	{
		this(MINIMUM_SLOT_LENGTH, hashDensity);
	}

	public SwizzleRegistryGrowingRange(final int slotSize, final float hashDensity)
	{
		super();
		final int paddedSlotSize = padCapacity(slotSize);
		this.slotsPerOid = new Entry[paddedSlotSize][];
		this.slotsPerRef = new Entry[paddedSlotSize][];
		this.capacity    = (int)(paddedSlotSize * (this.hashDensity = positive(hashDensity)));
		this.size        = 0;
		this.modulo      = paddedSlotSize - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetSlotsPerRef()
	{
		return this.slotsPerRef;
	}

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetSlotsPerOid()
	{
		return this.slotsPerOid;
	}

	private synchronized boolean synchronizedPut(final long oid, final long tid, final Object ref)
	{
		if(alreadyRegisteredReference(this.slotsPerRef[identityHashCode(ref) & this.modulo], oid, tid, ref))
		{
			return false;
		}

		/*
		 * at this point, the reference is definitely not contained in the registry.
		 * Either because it never has been or because there's an orphan weak entry with its oid.
		 * Check for colliding id and orphan case
		 */
		final Entry entry;
		if((entry = synchLookupEntryPerOid(this.slotsPerOid[(int)oid & this.modulo], oid)) != null)
		{
			synchRehash(this.slotsPerRef, entry.set(tid, ref), entry);
			return false;
		}

		/*
		 * at this point, neither the reference nor a fitting orphan entry is contained in the registry
		 * and there is no colliding oid, either. So create and put a new Entry for it.
		 */
		this.synchPutNewEntry(new Entry(oid, tid, ref));
		
		if(tid == Swizzle.classTypeId())
		{
			this.typeCount++;
		}
		
		return true;
	}

	private synchronized Object synchronizedAddRef(final long oid, final long tid, final Object ref) // dirty flag, bua
	{
		if(alreadyRegisteredReference(this.slotsPerRef[identityHashCode(ref) & this.modulo], oid, tid, ref))
		{
			return ref;
		}

		/* at this point, the reference is definitely not contained in the registry.
		 * either because it never has been or because there's an orphan weak entry with its id.
		 * Check for colliding id and orphan case
		 */
		final Entry entry;
		if((entry = synchLookupEntryPerOid(this.slotsPerOid[(int)oid & this.modulo], oid)) != null)
		{
			final Object current;
			if((current = entry.ref.get()) != null)
			{
				return current; // no matter if current is already the same reference or not, just return it (add logic)
			}
			synchRehash(this.slotsPerRef, entry.set(tid, ref), entry);
			return ref;
		}

		/* at this point, neither the reference nor an orphan entry is not contained in the registry
		 * and there is no colliding id either.
		 * So create a new Entry for it
		 */
		this.synchPutNewEntry(new Entry(oid, tid, ref));
		return ref;
	}

	private synchronized Object synchronizedPutIds(final long oid, final long tid)
	{
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerOid[(int)oid & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					if(bucketsI[i].tid != tid)
					{
						throw new SwizzleExceptionConsistencyTid(oid, bucketsI[i].tid, tid, null);
					}
					return bucketsI[i].ref.get();
				}
			}
		}

		// intentionally no registration at all in the perReference slots because there is no reference (yet)

		this.synchPutNewEntry(new Entry(oid, tid));
		return null;
	}

	private void synchPutNewEntry(final Entry entry)
	{
		// at this point, the new entry can and will definitely be added. So increase size here and if necessary slots.
		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		// storage is defintely big enough, size has already been incremented, entry can be simply inserted
		synchInsertEntry(this.slotsPerOid, this.slotsPerRef, entry);
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
		if(JadothMath.isGreaterThanOrEqualHighestPowerOf2Integer(this.slotsPerOid.length))
		{
			this.capacity = Integer.MAX_VALUE; // disable special case after running into it once (cool 8-) )
			return;
		}
		this.synchRebuild(this.slotsPerOid.length << 1);
	}

	private void synchRebuild(final int slotLength)
	{
//		JadothConsole.debugln("rebuilding to length " + slotLength);
		final Entry[][] newSlotsPerOid = new Entry[slotLength][];
		final Entry[][] newSlotsPerRef = new Entry[slotLength][];

		synchRebuildSlots(this.slotsPerOid, newSlotsPerOid, newSlotsPerRef);

		this.capacity    = (int)(slotLength * this.hashDensity);
		this.slotsPerOid = newSlotsPerOid;
		this.slotsPerRef = newSlotsPerRef;
		this.modulo      = slotLength - 1;
//		JadothConsole.debugln(" * done. new capacity = " + this.capacity);
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
				if(bucketsR[i] != null && bucketsR[i].ref.get() == object)
				{
					return bucketsR[i].oid;
				}
			}
		}
		return 0L;
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
					return bucketsI[i].ref.get();
				}
			}
		}
		return null;
	}

	private static long lookupTid(final Entry[] bucketsI, final long oid)
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
					return bucketsI[i].tid;
				}
			}
		}
		return 0L;
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


	private static void validateEntryOidTid(final Entry entry, final long oid, final long tid, final Object ref)
	{
		if(entry.oid != oid)
		{
			throw new SwizzleExceptionConsistencyObjectId(ref, entry.oid, oid);
		}
		if(entry.tid != tid)
		{
			// this check is almost superfluous here. Maybe remove.
			throw new SwizzleExceptionConsistencyTid(oid, entry.tid, tid, ref);
		}
	}

	private static boolean alreadyRegisteredReference(
		final Entry[] bucketsR,
		final long    oid     ,
		final long    tid     ,
		final Object  ref
	)
	{
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].ref.get() == ref)
				{
					validateEntryOidTid(bucketsR[i], oid, tid, ref);
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

	private static void validateExistingTypeForTypeId(final Entry[] bucketsI, final long tid, final Class<?> type)
	{
		if(isConsistentRegisteredTypeForTypeId(bucketsI, tid, type))
		{
			return;
		}
		throw new SwizzleExceptionConsistencyUnknownMapping(tid, type);
	}

	private static void validateExistingTypeIdForType(final Entry[] bucketsR, final long tid, final Class<?> type)
	{
		if(isConsistentRegisteredTypeIdForType(bucketsR, tid, type))
		{
			return;
		}
		throw new SwizzleExceptionConsistencyUnknownMapping(tid, type);
	}

	private static boolean isConsistentRegisteredTypeForTypeId(
		final Entry[]  bucketsI,
		final long     tid     ,
		final Class<?> type
	)
	{
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == tid)
				{
					// tid == oid for types
					if(bucketsI[i].ref.get() == type)
					{
						return true;
					}
					throw new SwizzleExceptionConsistencyWrongType(tid, (Class<?>)bucketsI[i].ref.get(), type);
				}
			}
		}
		return false;
	}

	private static boolean isConsistentRegisteredTypeIdForType(
		final Entry[]  bucketsR,
		final long     tid     ,
		final Class<?> type
	)
	{
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].oid == tid)
				{
					// tid == oid for types
					if(bucketsR[i].oid == tid)
					{
						// getting here actually means the registry is inconsistent. Maybe throw exception etc.
						return true;
					}
					throw new SwizzleExceptionConsistencyWrongTypeId(type, bucketsR[i].oid, tid);
				}
			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void iterateTypes(final Entry[][] slots, final Consumer<KeyValue<Long, Class<?>>> iterator)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null && buckets[b].ref.get() instanceof Class<?>)
					{
						// no idea how to do it otherwise (and don't want to wrap the procedure in a relay procedure :P)
						iterator.accept((KeyValue)buckets[b]);
					}
				}
			}
		}
	}

	private static void iterateEntries(final Entry[][] slots, final Consumer<KeyValue<Long, Object>> iterator)
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
						iterator.accept(buckets[b]);
					}
				}
			}
		}
	}

	private static void synchClearOrphanEntries(final Entry[][] slots, final HashMapIdId orphanage)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null && buckets[b].isOrphan())
					{
						if(orphanage != null)
						{
							orphanage.add(buckets[b].oid, buckets[b].tid);
						}
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
	public long typeCount()
	{
		return this.typeCount;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public int getHashRange()
	{
		return this.slotsPerOid.length;
	}

	@Override
	public float getHashDensity()
	{
		return this.hashDensity;
	}

	@Override
	public int capacity()
	{
		return this.capacity;
	}

	@Override
	public synchronized void clear()
	{
		final Entry[][] slotsI = this.slotsPerOid, slotsR = this.slotsPerRef;
		for(int i = 0; i < slotsI.length; i++)
		{
			slotsI[i] = slotsR[i] = null;
		}
		this.size = 0;
	}

	@Override
	public long lookupTypeIdForObjectId(final long oid)
	{
		return lookupTid(this.synchronizedGetSlotsPerOid()[(int)oid & this.modulo], oid);
	}

	@Override
	public boolean containsObjectId(final long oid)
	{
		return containsOid(this.synchronizedGetSlotsPerOid()[(int)oid & this.modulo], oid);
	}

	@Override
	public long lookupObjectId(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		return lookupOid(this.synchronizedGetSlotsPerRef()[identityHashCode(object) & this.modulo], object);
	}

	@Override
	public long lookupTypeId(final Class<?> type)
	{
		if(type == null)
		{
			throw new NullPointerException();
		}
		return lookupOid(this.synchronizedGetSlotsPerRef()[identityHashCode(type) & this.modulo], type);
	}

	@Override
	public Object lookupObject(final long oid)
	{
		return lookupObject(this.synchronizedGetSlotsPerOid()[(int)oid & this.modulo], oid);
	}

	@SuppressWarnings("unchecked") // safety of cast guaranteed by logic
	@Override
	public <T> Class<T> lookupType(final long tid)
	{
		return (Class<T>)lookupObject(this.synchronizedGetSlotsPerOid()[(int)tid & this.modulo], tid);
	}

	@Override
	public Object registerTypeIdForObjectId(final long oid, final long tid)
	{
		if(oid == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		if(tid == 0L)
		{
			throw new SwizzleExceptionNullTypeId();
		}
		return this.synchronizedPutIds(oid, tid);
	}

	void validateExistingMapping(final Class<?> type, final long typeId)
	{
		// don't know if this method's synchronization pattern is worth much performance, but it's funny to use it
		final Entry[][] slotsPerOid, slotsPerRef;
		final int modulo;
		synchronized(this)
		{
			slotsPerOid = this.slotsPerOid;
			slotsPerRef = this.slotsPerRef;
			modulo      = this.modulo;
		}

		// don't lock out other threads while doing mere non-writing validation work
		validateExistingTypeForTypeId(slotsPerOid[(int)(typeId & modulo)         ], typeId, type);
		validateExistingTypeIdForType(slotsPerRef[identityHashCode(type) & modulo], typeId, type);
	}

	void validatePossibleMapping(final Class<?> type, final long typeId)
	{
		// don't know if this method's synchronization pattern is worth much performance, but it's funny to use it
		final Entry[][] slotsPerOid, slotsPerRef;
		final int modulo;
		synchronized(this)
		{
			slotsPerOid = this.slotsPerOid;
			slotsPerRef = this.slotsPerRef;
			modulo      = this.modulo;
		}

		// don't lock out other threads while doing mere non-writing validation work
		// only use for consistency check. Wether type is registered or unknown is irrelevant here
		isConsistentRegisteredTypeForTypeId(slotsPerOid[(int)(typeId & modulo)         ], typeId, type);
		isConsistentRegisteredTypeIdForType(slotsPerRef[identityHashCode(type) & modulo], typeId, type);
	}

	@Override
	public synchronized void validateExistingTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		mappings.iterate(this.typeExistsValidator);
	}

	@Override
	public synchronized void validatePossibleTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		mappings.iterate(this.typePossibleValidator);
	}

	@Override
	public boolean registerType(final long tid, final Class<?> type)
	{
		if(type == null)
		{
			throw new NullPointerException();
		}
		if(tid == 0L)
		{
			throw new SwizzleExceptionNullTypeId();
		}
//		JadothConsole.debugln(Jadoth.systemString(this) + " registering " + tid + " <-> " + type);
		return this.synchronizedPut(tid, Swizzle.classTypeId(), type);
	}

	@Override
	public boolean registerObject(final long oid, final long tid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		if(tid == 0L)
		{
			throw new SwizzleExceptionNullTypeId();
		}
		return this.synchronizedPut(oid, tid, object);
	}


	private synchronized boolean lookupTidAndPut(final long oid, final Object obj)
	{
		// (13.08.2012)NOTE: this method could probably be optimized
		final long tid;
		if((tid = lookupOid(this.slotsPerRef[identityHashCode(obj.getClass()) & this.modulo], obj.getClass())) == 0L)
		{
			throw new SwizzleExceptionConsistencyUnknownType(obj.getClass());
		}
		return this.synchronizedPut(oid, tid, obj);
	}

	@Override
	public boolean registerObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		return this.lookupTidAndPut(oid, object);
	}

	@Override
	public Object optionalRegisterObject(final long oid, final long tid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		if(tid == 0L)
		{
			throw new SwizzleExceptionNullTypeId();
		}
		return this.synchronizedAddRef(oid, tid, object);
	}

	private synchronized Object lookupTidAndAdd(final long oid, final Object obj)
	{
		// (13.08.2012)NOTE: this method could probably be optimized
		final long tid;
		if((tid = lookupOid(this.slotsPerRef[identityHashCode(obj.getClass()) & this.modulo], obj.getClass())) == 0L)
		{
			throw new SwizzleExceptionConsistencyUnknownType(obj.getClass());
		}
		return this.synchronizedAddRef(oid, tid, obj);
	}

	@Override
	public Object optionalRegisterObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		return this.lookupTidAndAdd(oid, object);
	}

	@Override
	public synchronized Object retrieveByOid(final long oid)
	{
		if(oid == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerOid[(int)oid & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					final Entry entry = bucketsI[i];
					synchRemoveEntry(this.slotsPerRef[entry.hash & this.modulo], entry);
					bucketsI[i] = null;
					this.size--;
					return entry.ref.get();
				}
			}
		}
		return null;
	}

	@Override
	public synchronized long retrieveByObject(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerRef[identityHashCode(object) & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].ref.get() == object)
				{
					final Entry entry = bucketsI[i];
					synchRemoveEntry(this.slotsPerOid[(int)entry.oid & this.modulo], entry);
					bucketsI[i] = null;
					this.size--;
					return entry.oid;
				}
			}
		}
		return 0L;
	}

	@Override
	public synchronized Class<?> retrieveByTid(final long tid)
	{
		if(tid == 0L)
		{
			throw new SwizzleExceptionNullTypeId();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerOid[(int)tid & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == tid)
				{
					final Object ref = bucketsI[i].ref.get();
					if(ref != null && !(ref instanceof Class<?>))
					{
						throw new SwizzleExceptionConsistencyInvalidTypeId(tid);
					}
					synchRemoveEntry(this.slotsPerRef[bucketsI[i].hash & this.modulo], bucketsI[i]);

					bucketsI[i] = null;
					this.size--;
					return (Class<?>)ref;
				}
			}
		}
		return null;
	}

	@Override
	public synchronized boolean removeById(final long id)
	{
		if(id == 0L)
		{
			throw new SwizzleExceptionNullObjectId();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerOid[(int)id & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == id)
				{
					synchRemoveEntry(this.slotsPerRef[bucketsI[i].hash & this.modulo], bucketsI[i]);
					bucketsI[i] = null;
					this.size--;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public synchronized boolean remove(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerRef[identityHashCode(object) & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].ref.get() == object)
				{
					synchRemoveEntry(this.slotsPerOid[(int)bucketsI[i].oid & this.modulo], bucketsI[i]);
					bucketsI[i] = null;
					this.size--;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void iterateTypes(final Consumer<KeyValue<Long, Class<?>>> iterator)
	{
		iterateTypes(this.synchronizedGetSlotsPerOid(), iterator);
	}

	@Override
	public void iterateEntries(final Consumer<KeyValue<Long, Object>> iterator)
	{
		iterateEntries(this.synchronizedGetSlotsPerOid(), iterator);
	}

	@Override
	public void validateTypeMapping(final long typeId, final Class<?> type)
	{
		this.validateExistingMapping(type, typeId);
	}

	@Override
	public synchronized void shrink()
	{
		final int requiredSlotLength;
		if((requiredSlotLength = padCapacity((int)(this.size / this.hashDensity))) >= this.slotsPerOid.length)
		{
			return; // can't shrink, abort
		}
		this.synchRebuild(requiredSlotLength);
	}

	@Override
	public synchronized void cleanUp()
	{
		synchClearOrphanEntries(this.slotsPerOid, null);
		synchClearOrphanEntries(this.slotsPerRef, null);
		this.synchRebuild(this.slotsPerOid.length);
	}

	@Override
	public synchronized HashMapIdId clearOrphanEntries()
	{
		final HashMapIdId orphanage = new HashMapIdId();
		synchClearOrphanEntries(this.slotsPerOid, orphanage);
		synchClearOrphanEntries(this.slotsPerRef, null     );
		return orphanage;
	}



	///////////////////////////////////////////////////////////////////////////
	// member types     //
	/////////////////////

	/* (16.03.2015 TM)TODO: let SwizzleRegistryGrowingRange$Entry extend WeakReference
	 * This saves memory footprint.
	 * Must however on referen update replace Entries with new instances instead of just setting a new WeakReference.
	 */
	private static final class Entry implements KeyValue<Long, Object>
	{
		final long oid, tid;
		int hash; // note: causes 4 byte alignment overhead with and without coops. => 4 byte to spare
		WeakReference<Object> ref;

		Entry(final long oid, final long tid, final Object ref)
		{
			super();
			this.oid  = oid;
			this.tid  = tid;
			this.ref  = new WeakReference<>(ref);
			this.hash = identityHashCode(ref);
		}

		Entry(final long oid, final long tid)
		{
			super();
			this.oid  = oid;
			this.tid  = tid;
			this.ref  = new WeakReference<>(null); // dummy to avoid NPEs / explicit null checks for ref
			this.hash = 0;
		}

		@Override
		public Long key()
		{
			return this.oid;
		}

		@Override
		public Object value()
		{
			return this.ref.get();
		}

		/**
		 * @return {@code true} if this entry has lost its referent.
		 *
		 * @see #isEmpty()
		 */
		boolean isOrphan()
		{
			return this.ref.get() == null && this.hash > 0;
		}

		/**
		 * @return {@code true} if this entry's referent is {@code null} for any reason, {@code false} otherwise.
		 *
		 * @see #isOrphan()
		 */
		boolean isEmpty()
		{
			return this.ref.get() == null;
		}

		int set(final long tid, final Object ref)
		{
			if(this.tid != tid)
			{
				throw new SwizzleExceptionConsistencyTid(this.oid, this.tid, tid, ref);
			}

			// case: hollow oid<->tid entry
			if(this.hash == 0)
			{
				this.ref = new WeakReference<>(ref);
				this.hash = System.identityHashCode(ref);
				return 0;
			}

			// case: already contains this reference
			if(this.ref.get() == ref)
			{
				return -1;
			}

			// case: orphan entry with same oid
			if(this.ref.get() == null)
			{
				this.ref = new WeakReference<>(ref);
				// hardly unlikely that a new instance will have the same hashcode, so ignore check
				final int oldHash = this.hash; // required to rehash entry in the ref slots
				this.hash = identityHashCode(ref);
				return oldHash;
			}
			throw new SwizzleExceptionConsistencyObject(this.oid, this.tid, tid, this.ref.get(), ref);
		}

	}

	@Deprecated
	public void DEBUG_analyze()
	{
		System.out.println("size        = " + this.size);
		System.out.println("hashDensity = " + this.hashDensity);
		System.out.println("hashRange   = " + this.slotsPerOid.length);
		System.out.println("capacity    = " + this.capacity);
		DEBUG_printAnalysis("perRef", DEBUG_analyze(this.slotsPerRef));
		DEBUG_printAnalysis("perOid", DEBUG_analyze(this.slotsPerOid));

	}

	@Deprecated
	private static void DEBUG_printAnalysis(final String label, final int[] analysis)
	{
		System.out.println(label + ":");
		System.out.println("empty\t" + analysis[0]);
		for(int i = 1; i < analysis.length; i++)
		{
			System.out.println(JadothMath.pow(2, i - 1) + "\t" + analysis[i]);
		}
		System.out.println("----");
	}

	// CHECKSTYLE:OFF only for debugging
	@Deprecated
	private static int[] DEBUG_analyze(final Entry[][] slots)
	{
		final int[] bucketsPow2Lenghts = new int[30];
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				bucketsPow2Lenghts[JadothMath.log2pow2(slots[s].length) + 1]++;
			}
			else
			{
				bucketsPow2Lenghts[0]++;
			}
		}
		return bucketsPow2Lenghts;
	}
	// CHECKSTYLE:ON



//	public static void main(final String[] args)
//	{
//		// -XX:-UseCompressedOops
//		System.out.println(
//			2*2*Memory.byteSizeReference()
//			+ Memory.byteSizeInstance(Entry.class)
//			+ Memory.byteSizeInstance(WeakReference.class)
//		);
//	}

}
